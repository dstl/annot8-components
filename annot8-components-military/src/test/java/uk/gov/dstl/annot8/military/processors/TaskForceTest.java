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

import io.annot8.api.components.Processor;
import io.annot8.api.settings.NoSettings;
import io.annot8.conventions.PropertyKeys;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TaskForceTest {
  @Test
  public void testCreation(){
    TaskForce tf = new TaskForce();
    Processor tfProc = tf.createComponent(null, NoSettings.getInstance());
    assertNotNull(tfProc);
    tfProc.close();
  }

  @Test
  public void testTaskForce(){
    RegexTestingUtils.testSubstringToPropertyExtraction(
            "Task force 123, TF4-56 and TF 789. But not ATF000 or TF000a.",
            Stream.of("TF123", "TF4-56", "TF789"),
            new TaskForce(),
            PropertyKeys.PROPERTY_KEY_IDENTIFIER);
  }
}
