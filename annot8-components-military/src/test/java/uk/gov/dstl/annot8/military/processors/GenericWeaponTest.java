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
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class GenericWeaponTest {
  @Test
  public void testCreation() {
    GenericWeapon genericWeapon = new GenericWeapon();
    Processor processor = genericWeapon.createComponent(null, NoSettings.getInstance());
    assertNotNull(processor);
    processor.close();
  }

  @Test
  public void testSimple() {
    testSingleMatch(
        "Natalie had a tactical assault rifle hidden in her cupboard.",
        "tactical assault rifle",
        "firearm");
  }

  @Test
  public void testPlural() {
    testSingleMatch("He was found with 47 bullets.", "bullets", "ammunition");
  }

  @Test
  public void testNoDescriptor() {
    testSingleMatch("Sam owned a machete.", "machete", "bladed");
  }

  private void testSingleMatch(String sentence, String match, String subtype) {
    TestItem item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class).withData(sentence).save();

    Matcher m = Pattern.compile("\\w+").matcher(content.getData());
    while (m.find()) {
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(m.start(), m.end()))
          .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
          .save();
    }

    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(0, sentence.length()))
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .save();

    GenericWeapon genericWeapon = new GenericWeapon();
    Processor processor = genericWeapon.createComponent(null, NoSettings.getInstance());
    processor.process(item);

    assertEquals(
        1, content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WEAPON).count());

    Annotation a =
        content
            .getAnnotations()
            .getByType(AnnotationTypes.ANNOTATION_TYPE_WEAPON)
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
