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

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.components.stopwords.resources.CollectionStopwords;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TokenFrequencySummarisationTest {

  @Test
  public void test(){
    //Text taken from: https://en.wikipedia.org/wiki/Ginetta_G15
    String text = "The Ginetta G15 was a British sports car made by the Ginetta company in Witham, Essex between 1968 and 1974. " +
        "The car was initially available only in kit form but later some factory built examples were available. " +
        "It was launched at the 1967 London Motor Show. The body was of glass fibre and was mounted on a tubular steel chassis. " +
        "The engine and four speed transmission were taken from the Hillman Imp range and were rear-mounted driving the rear wheels. " +
        "The suspension was by independent coil springs at the front and rear with the front steering/suspension derived from the Triumph Herald. " +
        "The car used 13 in (330 mm) wheels as opposed to the 12 in (305 mm) of the Imp giving higher gearing. " +
        "The front compartment where the engine would have been on a front engined car contained the fuel tank and spare wheel leaving no room for luggage.";

    TestItem testItem = new TestItem();
    TestStringContent content = testItem.createContent(TestStringContent.class)
        .withData(text)
        .save();

    // Split into words and sentences (naively is good enough)
    Matcher mSent = Pattern.compile("(.*?\\.)\\h*").matcher(text);
    while(mSent.find()){
      content.getAnnotations().create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .withBounds(new SpanBounds(mSent.start(1), mSent.end(1)))
          .save();
    }

    Matcher mWord = Pattern.compile("([0-9a-z]+|[().,/])", Pattern.CASE_INSENSITIVE).matcher(text);
    while(mWord.find()){
      content.getAnnotations().create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
          .withBounds(new SpanBounds(mWord.start(), mWord.end()))
          .save();
    }

    TokenFrequencySummarisation.Processor p = new TokenFrequencySummarisation.Processor(3, new CollectionStopwords("en", Set.of("the", "of", "and", "a", "in", "was", "by", "on", "an", "at", "it")));
    p.process(content);

    List<String> summaries = content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_SUMMARY)
        .map(a -> a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE, String.class).get())
        .collect(Collectors.toList());
    assertEquals(1, summaries.size());

    String summary = summaries.get(0);
    assertFalse(summary.isEmpty());
    assertEquals(3, summary.chars().filter(ch -> ch == '.').count()); //Check there are 3 sentences

    p.close();
  }

}
