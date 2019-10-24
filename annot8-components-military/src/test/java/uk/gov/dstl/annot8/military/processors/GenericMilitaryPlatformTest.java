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

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.settings.NoSettings;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

public class GenericMilitaryPlatformTest {
  @Test
  public void testCreation() {
    GenericMilitaryPlatform genericMilitaryPlatform = new GenericMilitaryPlatform();
    Processor processor = genericMilitaryPlatform.createComponent(null, NoSettings.getInstance());
    assertNotNull(processor);
    processor.close();
  }

  @Test
  public void testPhraseDetection() {
    testSingleMatch("The armoured vehicle was hidden in the forest.", "armoured vehicle", "ground");
  }

  @Test
  public void testPluralDetection() {
    testSingleMatch("The UAVs were hidden in the forest.", "UAVs", "air");
  }

  @Test
  public void testGenitiveDetection() {
    testSingleMatch(
        "The aircraft carrier's keys were hidden in the forest.", "aircraft carrier", "naval");
  }

  private void testSingleMatch(String sentence, String match, String subtype) {
    TestItem item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class).withData(sentence).save();

    GenericMilitaryPlatform desc = new GenericMilitaryPlatform();
    Processor processor = desc.createComponent(null, NoSettings.getInstance());
    processor.process(item);

    assertEquals(
        1,
        content.getAnnotations().getByType(GenericMilitaryPlatform.MILITARY_PLATFORM_TYPE).count());

    Annotation a =
        content
            .getAnnotations()
            .getByType(GenericMilitaryPlatform.MILITARY_PLATFORM_TYPE)
            .findFirst()
            .get();
    assertNotNull(a);

    assertEquals(match, content.getText(a).get());

    if (subtype != null) {
      assertTrue(a.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
      assertEquals(
          subtype, a.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
    } else {
      assertFalse(a.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    }
  }
}
