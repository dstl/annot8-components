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
package uk.gov.dstl.annot8.military.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("British Army Units")
@ComponentDescription("Extracts British Army Unit designations from text")
public class BritishArmyUnit extends AbstractProcessorDescriptor<BritishArmyUnit.Processor, NoSettings> {
  @Override
  protected Processor createComponent(Context context, NoSettings noSettings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_ORGANISATION, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {
    public Processor() {
      super(
          Pattern.compile("\\b(?=\\d+ Sect\\b|\\d+ Pl\\b|[A-Z] Coy\\b)" +
                  "(?<section>\\d+ Sect\\b)?+[\\s,]*" +
                  "(?<platoon>\\d+ Pl\\b)?+[\\s,]*" +
                  "(?<company>[A-Z]+ Coy\\b)?+[\\s,]*" +
                  "(?<platoon2>\\d+ Pl\\b)?+[\\s,]*" +
                  "(?<section2>\\d+ Sect\\b)?+[\\s,]*", // Will match abbreviations in any order
              Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_ORGANISATION
      );
    }

    @Override
    protected void addProperties(Annotation.Builder builder, Matcher m){
      if (m.group("platoon2") != null){
        builder.withProperty("platoon", m.group("platoon2"));
      }else{
        builder.withProperty("platoon", m.group("platoon"));
      }

      if (m.group("section2") != null){
        builder.withProperty("section", m.group("section2"));
      }else{
        builder.withProperty("section", m.group("section"));
      }

      builder.withProperty("company", m.group("company"));

      builder.withProperty(PropertyKeys.PROPERTY_KEY_SUBTYPE, "army");
    }
  }
}