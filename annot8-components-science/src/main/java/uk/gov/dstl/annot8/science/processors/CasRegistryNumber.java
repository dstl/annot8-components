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
package uk.gov.dstl.annot8.science.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("CAS Registry Number")
@ComponentDescription("Identify chemicals by looking for CAS numbers and checking the check digit")
@ComponentTags({"science", "chemistry", "chemical", "cas"})
public class CasRegistryNumber
    extends AbstractProcessorDescriptor<CasRegistryNumber.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_CHEMICAL, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    public static final Pattern CAS_REGEX =
        Pattern.compile("\\b(\\d{2,7})-(\\d{2})-(\\d)\\b", Pattern.CASE_INSENSITIVE);

    @Override
    protected void process(Text content) {

      AnnotationStore annotationStore = content.getAnnotations();

      Matcher m = CAS_REGEX.matcher(content.getData());
      while (m.find()) {

        // Check checksum
        int checkDigit = Integer.parseInt(m.group(3));

        String part1 = m.group(1);
        String part2 = m.group(2);

        part1 = new StringBuilder(part1).reverse().toString();

        int sum =
            Integer.parseInt(part2.substring(1, 2)) + (2 * Integer.parseInt(part2.substring(0, 1)));
        int pos = 0;
        while (pos < part1.length()) {
          int x = Integer.parseInt(part1.substring(pos, pos + 1));
          sum += (pos + 3) * x;

          pos++;
        }

        if (sum % 10 == checkDigit) {

          annotationStore
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_CHEMICAL)
              .withBounds(new SpanBounds(m.start(), m.end()))
              .save();
        }
      }
    }
  }
}
