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

import io.annot8.api.settings.NoSettings;
import io.annot8.conventions.PropertyKeys;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HMSTest {
  @Test
  public void testCreation(){
    HMS hms = new HMS();
    HMS.Processor proc = hms.createComponent(null, NoSettings.getInstance());
    assertNotNull(proc);
  }

  @Test
  public void testHMS(){
    RegexTestingUtils.testSubstringToPropertyExtraction(
        "HMS Troutbridge sailed into New York last Friday. H.M.S. Hidden Dragon provided an escort.",
        Stream.of("HMS Troutbridge", "H.M.S. Hidden Dragon"),
        new HMS(),
        PropertyKeys.PROPERTY_KEY_NAME);
  }

  @Test
  public void testHMSVariants(){
    RegexTestingUtils.testSubstringToPropertyExtraction(
        "HMJS Troutbridge sailed into New York last Friday. H.M.P.N.G.S. Hidden Dragon provided an escort.",
        Stream.of("HMJS Troutbridge", "H.M.P.N.G.S. Hidden Dragon"),
        new HMS(),
        PropertyKeys.PROPERTY_KEY_NAME);
  }
}
