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
import io.annot8.components.base.processors.DescribedWordToken;
import io.annot8.components.base.processors.MultiProcessor;
import io.annot8.components.stopwords.resources.NoOpStopwords;
import io.annot8.components.stopwords.resources.Stopwords;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.utils.text.PluralUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@ComponentName("Generic Weapon")
@ComponentDescription("Extracts weapons (with descriptions) from text")
public class GenericWeapon extends AbstractProcessorDescriptor<MultiProcessor, NoSettings> {

  @Override
  protected MultiProcessor createComponent(Context context, NoSettings noSettings) {
    Stopwords sw;
    if (context == null || context.getResource(Stopwords.class).isEmpty()) {
      sw = new NoOpStopwords();
    } else {
      sw = context.getResource(Stopwords.class).get();
    }

    Set<String> firearm = Set.of("firearm", "gun", "handgun", "pistol", "revolver", "rifle", "sidearm", "shotgun");
    Set<String> ammunition = Set.of("ammo", "ammunition", "bullet", "cartridge", "magazine", "round", "shell");
    Set<String> explosive = Set.of("airstrike", "artillery", "bomb", "explosive", "grenade", "ied", "missile", "mortar", "ordnance", "rocket", "rpg", "torpedo", "vbied");
    Set<String> bladed = Set.of("axe", "blade", "dagger", "knife", "knive", "machete", "sword");
    Set<String> other = Set.of("armament", "flamethrower", "munition", "weapon", "wmd");

    Set<String> descriptors = Set.of(
        "chemical", "biological", "nuclear", "atomic", "sonic", "laser",
        "rocket", "propelled", "rail", "air", "ground", "surface",
        "dirty", "improvised", "explosive", "unexploded",
        "lethal", "less-lethal", "non-lethal",
        "tactical", "combat", "commando", "recoilless", "silenced",
        "anti", "aircraft", "tank", "ship", "submarine", "antiaircraft", "anti-aircraft", "antitank", "anti-tank", "antiship", "anti-ship", "antisubmarine", "anti-submarine",
        "sniper", "machine", "assault", "submachine", "sub-machine"
    );

    Processor pFirearm = new DescribedWordToken.Processor(sw, AnnotationTypes.ANNOTATION_TYPE_WEAPON,
        PluralUtils.pluraliseSet(firearm),
        descriptors,
        false,
        Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "firearm"));

    Processor pAmmunition = new DescribedWordToken.Processor(sw, AnnotationTypes.ANNOTATION_TYPE_WEAPON,
        PluralUtils.pluraliseSet(ammunition),
        descriptors,
        false,
        Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "ammunition"));

    Processor pExplosive = new DescribedWordToken.Processor(sw, AnnotationTypes.ANNOTATION_TYPE_WEAPON,
        PluralUtils.pluraliseSet(explosive),
        descriptors,
        false,
        Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "explosive"));

    Processor pBladed = new DescribedWordToken.Processor(sw, AnnotationTypes.ANNOTATION_TYPE_WEAPON,
        PluralUtils.pluraliseSet(bladed),
        descriptors,
        false,
        Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "bladed"));

    Processor pOther = new DescribedWordToken.Processor(sw, AnnotationTypes.ANNOTATION_TYPE_WEAPON,
        PluralUtils.pluraliseSet(other),
        descriptors,
        false,
        Collections.emptyMap());

    return new MultiProcessor(pFirearm, pAmmunition, pExplosive, pBladed, pOther);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WEAPON, SpanBounds.class)
        .build();
  }
}
