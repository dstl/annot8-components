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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.settings.NoSettings;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

public class BritishRanksTest {

  @Test
  public void testCreation() {
    BritishRanks br = new BritishRanks();
    BritishRanks.Processor brProc = br.createComponent(null, NoSettings.getInstance());
    assertNotNull(brProc);
    brProc.close();
  }

  @Test
  public void testDetection() {
    test("Last week, Pte SMITH joined the Army", "Pte", "SMITH");
    test("Last week, Private SMITH joined the Army", "Private", "SMITH");
    test("Sqn Sgt Maj J Davies", "Sqn Sgt Maj", "J Davies");
    test("Squadron Sergeant Major J Davies", "Squadron Sergeant Major", "J Davies");
    test("Gp Capt HELEN JONES", "Gp Capt", "HELEN JONES");
    test("Group Captain HELEN JONES", "Group Captain", "HELEN JONES");
    test("On Thursday it was announced that 2Lt Baker would get a promotion.", "2Lt", "Baker");
    test(
        "On Thursday it was announced that Second Lieutenant Baker would get a promotion.",
        "Second Lieutenant",
        "Baker");
    test("Off Cdt O'Neill", "Off Cdt", "O'Neill");
    test("Officer Cadet O'Neill", "Officer Cadet", "O'Neill");
    test("Col Sam Jones-Smith", "Col", "Sam Jones-Smith");
    test("Colonel Sam Jones-Smith", "Colonel", "Sam Jones-Smith");
  }

  @Test
  public void testMultipleDetection() {
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem
            .createContent(TestStringContent.class)
            .withData("Capt Jones and CSgt Doe were last seen on Monday")
            .save();

    BritishRanks br = new BritishRanks();
    BritishRanks.Processor brProc = br.createComponent(null, NoSettings.getInstance());
    brProc.process(testItem);

    assertEquals(2L, content.getAnnotations().getAll().count());
    assertEquals(
        2L, content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON).count());
  }

  private void test(String text, String rank, String name) {
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem.createContent(TestStringContent.class).withData(text).save();

    BritishRanks br = new BritishRanks();
    BritishRanks.Processor brProc = br.createComponent(null, NoSettings.getInstance());
    brProc.process(testItem);

    assertEquals(1L, content.getAnnotations().getAll().count());
    assertEquals(
        1L, content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON).count());

    Annotation a =
        content
            .getAnnotations()
            .getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .findFirst()
            .get();

    assertEquals(rank + " " + name, a.getBounds().getData(content).orElse("** BAD **"));
    assertEquals(rank, a.getProperties().get("rank").orElse("** BAD **"));
    assertEquals(name, a.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).orElse("** BAD **"));
  }
}
