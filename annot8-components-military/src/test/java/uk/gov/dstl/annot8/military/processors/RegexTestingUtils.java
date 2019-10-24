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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.Processor;
import io.annot8.api.components.ProcessorDescriptor;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.Iterator;
import java.util.stream.Stream;

public class RegexTestingUtils {

  private RegexTestingUtils() {
    // Private constructor for utility class
  }

  public static void testSubstringToPropertyExtraction(
      String inputString,
      Stream<String> expectedStrings,
      ProcessorDescriptor descriptor,
      String propertyKey) {
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem.createContent(TestStringContent.class).withData(inputString).save();

    Processor proc = (Processor) descriptor.create(new SimpleContext());
    proc.process(testItem);

    Iterator<String> expected = expectedStrings.sorted().iterator();

    Iterator<String> actual =
        content
            .getAnnotations()
            .getAll()
            .map(annotation -> annotation.getProperties().getOrDefault(propertyKey, ""))
            .sorted()
            .iterator();

    while (actual.hasNext() && expected.hasNext()) {
      assertEquals(expected.next(), actual.next());
    }

    assertTrue(
        !actual.hasNext() && !expected.hasNext(),
        "Number of expected strings does not match actual string count");
  }
}
