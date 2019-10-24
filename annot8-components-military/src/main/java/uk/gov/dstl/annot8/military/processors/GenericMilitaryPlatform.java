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

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.Processor;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.MultiProcessor;
import io.annot8.components.gazetteers.processors.AhoCorasick;
import io.annot8.components.gazetteers.processors.impl.CollectionGazetteer;
import io.annot8.components.stopwords.resources.NoOpStopwords;
import io.annot8.components.stopwords.resources.Stopwords;
import io.annot8.conventions.AnnotationTypes;
import java.util.Set;

@ComponentName("Generic Military Platform")
@ComponentDescription("Extracts military platforms (with descriptions) from text")
public class GenericMilitaryPlatform
    extends AbstractProcessorDescriptor<MultiProcessor, NoSettings> {

  public static final String MILITARY_PLATFORM_TYPE =
      AnnotationTypes.ENTITY_PREFIX + "militaryPlatform";

  @Override
  protected MultiProcessor createComponent(Context context, NoSettings noSettings) {
    Stopwords sw;
    if (context == null || context.getResource(Stopwords.class).isEmpty()) {
      sw = new NoOpStopwords();
    } else {
      sw = context.getResource(Stopwords.class).get();
    }

    Processor pNaval =
        new AhoCorasick.Processor(
            new CollectionGazetteer(
                Set.of(
                    "aircraft carrier",
                    "assault ship",
                    "frigate",
                    "destroyer",
                    "submarine",
                    "minesweeper",
                    "warship")),
            new GenericMilitaryPlatformSettings(MILITARY_PLATFORM_TYPE, "naval"));

    Processor pGround =
        new AhoCorasick.Processor(
            new CollectionGazetteer(
                Set.of(
                    "tank", "armoured vehicle", "humvee", "military vehicle", "tactical vehicle")),
            new GenericMilitaryPlatformSettings(MILITARY_PLATFORM_TYPE, "ground"));

    Processor pAir =
        new AhoCorasick.Processor(
            new CollectionGazetteer(
                Set.of(
                    "attack aircraft",
                    "attack helicopter",
                    "drone",
                    "fighter jet",
                    "fighter plane",
                    "uav",
                    "warplane")),
            new GenericMilitaryPlatformSettings(MILITARY_PLATFORM_TYPE, "air"));

    return new MultiProcessor(pNaval, pGround, pAir);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(MILITARY_PLATFORM_TYPE, SpanBounds.class)
        .build();
  }

  private static class GenericMilitaryPlatformSettings extends AhoCorasick.Settings {
    public GenericMilitaryPlatformSettings(String type, String subtype) {
      super();

      setType(type);
      setSubType(subtype);

      setAdditionalData(false);
      setCaseSensitive(false);
      setExactWhitespace(false);
      setPlurals(true);
    }
  }
}
