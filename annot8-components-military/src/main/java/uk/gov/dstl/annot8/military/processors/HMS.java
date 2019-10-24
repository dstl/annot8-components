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
import io.annot8.api.components.annotations.SettingsClass;
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

@ComponentName("HMS")
@ComponentDescription("Extracts HMS designations (e.g. HMS Dreadnought) from text")
@SettingsClass(NoSettings.class)
public class HMS extends AbstractProcessorDescriptor<HMS.Processor, NoSettings> {
  @Override
  protected Processor createComponent(Context context, NoSettings noSettings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_VEHICLE, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {
    public Processor() {
      super(
          Pattern.compile(
              "\\bH[.]?M[.]?((A|B|C|N[.]?Z|P[.]?N[.]?G|J|T[.]?S)[.]?)?S[.]?( [A-Z][a-z]*){1,3}\\b"),
          0,
          AnnotationTypes.ANNOTATION_TYPE_VEHICLE);
    }

    @Override
    protected void addProperties(Annotation.Builder builder, Matcher m) {
      builder.withProperty(PropertyKeys.PROPERTY_KEY_NAME, m.group());
      builder.withProperty(PropertyKeys.PROPERTY_KEY_SUBTYPE, "maritime");
    }
  }
}
