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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.annot8.api.components.Processor;
import io.annot8.api.settings.NoSettings;
import io.annot8.conventions.PropertyKeys;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class CallSignTest {
  @Test
  public void testCreation() {
    CallSign cs = new CallSign();
    Processor csProc = cs.createComponent(null, NoSettings.getInstance());
    assertNotNull(csProc);
    csProc.close();
  }

  @Test
  public void testCallsigns() {
    RegexTestingUtils.testSubstringToPropertyExtraction(
        "Bob (C\\S ECHO BRAVO) reported a contact at 0900. Alice (C/S FOXTROT) responded.",
        Stream.of("C/S ECHO BRAVO", "C/S FOXTROT"),
        new CallSign(),
        PropertyKeys.PROPERTY_KEY_IDENTIFIER);
  }
}
