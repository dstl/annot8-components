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
package uk.gov.dstl.annot8.science.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CasRegistryNumberTest {

    @Test
    public void testCasRegistryNumber() throws Annot8Exception {
        try (Processor p = new CasRegistryNumber.Processor()) {
            Item item = new TestItem();

            Text content =
                    item.createContent(TestStringContent.class)
                            .withData(
                                    "The CAS Number for water is 7732-18-5, but carbon could be either CASRN:7440-44-0 or CAS Registry Number 7782-42-5. CAS Number 7440-44-5 is not valid.")
                            .save();

            p.process(item);

            AnnotationStore store = content.getAnnotations();

            List<Annotation> annotations = store.getAll().collect(Collectors.toList());
            Assertions.assertEquals(3, annotations.size());

            List<String> casNumbers =
                    new ArrayList<>(
                            Arrays.asList(
                                    "7732-18-5",
                                    "7440-44-0",
                                    "7782-42-5"));

            for (Annotation a : annotations) {
                Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_CHEMICAL, a.getType());
                Assertions.assertEquals(content.getId(), a.getContentId());
                Assertions.assertEquals(0, a.getProperties().getAll().size());

                Assertions.assertTrue(casNumbers.remove(a.getBounds().getData(content).get()));
            }

            Assertions.assertEquals(0, casNumbers.size());
        }
    }
}