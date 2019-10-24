/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.dstl.annot8.triage.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.components.stopwords.resources.Stopwords;
import io.annot8.components.stopwords.resources.StopwordsIso;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ComponentName("Token Frequency Summarisation")
@ComponentDescription("Create a text summary of a document based on token frequency")
@SettingsClass(TokenFrequencySummarisation.Settings.class)
public class TokenFrequencySummarisation extends AbstractProcessorDescriptor<TokenFrequencySummarisation.Processor, TokenFrequencySummarisation.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    Stopwords sw;
    if(context == null || context.getResource(Stopwords.class).isEmpty()){
      sw = new StopwordsIso();
    }else{
      sw = context.getResource(Stopwords.class).get();
    }
    return new Processor(settings.getNumSentences(), sw);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SUMMARY, ContentBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private final int numSentences;
    private final Stopwords stopwords;

    private static final List<String> END_OF_SENTENCE = Arrays.asList(".", "!", "?");

    public Processor(int numSentences, Stopwords stopwords){
      this.numSentences = numSentences;
      this.stopwords = stopwords;
    }

    @Override
    protected void process(Text content) {
      Map<String, Integer> tokenFrequency = new HashMap<>();
      Map<Annotation, Integer> sentenceScores = new HashMap<>();

      //Score all tokens
      content.getAnnotations().getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
          .map(a -> getLemma(content, a))
          .filter(w -> !stopwords.isStopword(w))
          .filter(w -> w.matches(("[a-z][-a-z0-9]*"))) // Ignore punctuation, just numbers, etc.
          .forEach(w ->  tokenFrequency.merge(w, 1, Integer::sum));

      //Score each sentence, ignoring stop words
      content.getAnnotations().getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .forEach(s -> {
            SpanBounds sb = s.getBounds(SpanBounds.class).get();

            int score = content.getBetween(sb.getBegin(), sb.getEnd())
                .filter(a -> AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN.equals(a.getType()))
                .map(a -> getLemma(content, a))
                .filter(w -> !stopwords.isStopword(w))
                .filter(w -> w.matches(("[a-z][-a-z0-9]*")))
                .mapToInt(tokenFrequency::get)
                .sum();

            sentenceScores.put(s, score);
          });

      //Read top X sentences
      List<Annotation> topSentences =
          sentenceScores.entrySet().stream()
              .sorted((e1, e2) -> -Double.compare(e1.getValue(), e2.getValue()))
              .limit(numSentences)
              .map(Map.Entry::getKey)
              .sorted(SortUtils.SORT_BY_SPANBOUNDS)
              .collect(Collectors.toList());

      //Create summary string
      String summary = topSentences.stream()
              .map(s -> content.getText(s).orElse(""))
              .filter(s -> !s.isEmpty())
              .map(
                  s -> {
                    String lastChar = s.substring(s.length() - 1);
                    if (!END_OF_SENTENCE.contains(lastChar)) {
                      return s + ".";
                    }else {
                      return s;
                    }
                  })
              .collect(Collectors.joining(" "));

      //Create annotation
      content.getAnnotations().create()
          .withBounds(ContentBounds.getInstance())
          .withType(AnnotationTypes.ANNOTATION_TYPE_SUMMARY)
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, summary)
      .save();
    }

    private static String getLemma(Text content, Annotation a) {
      if(a.getProperties().has(PropertyKeys.PROPERTY_KEY_LEMMA, String.class)){
        return a.getProperties().get(PropertyKeys.PROPERTY_KEY_LEMMA, String.class).get().toLowerCase();
      }else{
        return content.getText(a).orElse("").toLowerCase();
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private final int numSentences;

    @JsonbCreator
    public Settings(@JsonbProperty("numSentences") int numSentences){
      this.numSentences = numSentences;
    }

    public int getNumSentences() {
      return numSentences;
    }

    @Override
    public boolean validate() {
      return numSentences > 0;
    }
  }
}
