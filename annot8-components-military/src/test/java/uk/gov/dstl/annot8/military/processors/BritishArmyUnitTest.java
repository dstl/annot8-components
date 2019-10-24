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
import io.annot8.api.properties.ImmutableProperties;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class BritishArmyUnitTest {

  @Test
  public void testCreation() {
    BritishArmyUnit ba = new BritishArmyUnit();
    BritishArmyUnit.Processor baProc = ba.createComponent(null, NoSettings.getInstance());
    assertNotNull(baProc);
    baProc.close();
  }

  @Test
  public void testDetection() {
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem
            .createContent(TestStringContent.class)
            .withData(
                "1 Pl, A Coy have reported suspicious activity whilst patrolling near CP A."
                    + " 1 Pl did not investigate further.")
            .save();

    BritishArmyUnit ba = new BritishArmyUnit();
    BritishArmyUnit.Processor baProc = ba.createComponent(null, NoSettings.getInstance());
    baProc.process(testItem);

    AnnotationStore store = content.getAnnotations();

    Map<String, Annotation> annotations =
        store.getAll().collect(Collectors.toMap(a -> a.getBounds().toString(), a -> a));
    assertEquals(2, annotations.size());
    assertEquals(
        "1 Pl",
        annotations
            .get("io.annot8.common.data.bounds.SpanBounds [begin=0, end=12]")
            .getProperties()
            .getOrDefault("platoon", ""));
    assertEquals(
        "A Coy",
        annotations
            .get("io.annot8.common.data.bounds.SpanBounds [begin=0, end=12]")
            .getProperties()
            .getOrDefault("company", ""));
    assertEquals(
        "1 Pl",
        annotations
            .get("io.annot8.common.data.bounds.SpanBounds [begin=75, end=80]")
            .getProperties()
            .getOrDefault("platoon", ""));
  }

  @Test
  public void testAlternateOrder() {
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem
            .createContent(TestStringContent.class)
            .withData(
                "B Coy, 2 Pl 2 Sect have reported suspicious activity whilst patrolling near CP A.")
            .save();

    BritishArmyUnit ba = new BritishArmyUnit();
    BritishArmyUnit.Processor baProc = ba.createComponent(null, NoSettings.getInstance());
    baProc.process(testItem);

    AnnotationStore store = content.getAnnotations();

    Map<String, Annotation> annotations =
        store.getAll().collect(Collectors.toMap(a -> a.getBounds().toString(), a -> a));
    assertEquals(1, annotations.size());
    ImmutableProperties match1Props =
        annotations
            .get("io.annot8.common.data.bounds.SpanBounds [begin=0, end=19]")
            .getProperties();
    assertEquals("B Coy", match1Props.getOrDefault("company", ""));
    assertEquals("2 Pl", match1Props.getOrDefault("platoon", ""));
    assertEquals("2 Sect", match1Props.getOrDefault("section", ""));
  }
}
