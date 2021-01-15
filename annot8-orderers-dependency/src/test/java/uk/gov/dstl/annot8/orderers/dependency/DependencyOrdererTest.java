/*
 * Crown Copyright (C) 2021 Dstl
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
package uk.gov.dstl.annot8.orderers.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.AnnotationCapability;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.capabilities.Capability;
import io.annot8.api.capabilities.ContentCapability;
import io.annot8.api.components.ProcessorDescriptor;
import io.annot8.api.data.Content;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DependencyOrdererTest {
  private int processorName = 1;

  private static final Bounds mockBounds = mock(Bounds.class);

  @BeforeEach
  private void before() {
    processorName = 1;
  }

  @Test
  public void testSimpleTwoProcessors() {
    Content<?> c = mock(Content.class);

    ProcessorDescriptor pd1 =
        contentProcessor(List.of(c), Collections.emptyList(), Collections.emptyList());
    ProcessorDescriptor pd2 =
        contentProcessor(Collections.emptyList(), List.of(c), Collections.emptyList());

    DependencyOrderer orderer = new DependencyOrderer();
    assertEquals(List.of(pd1, pd2), orderer.orderProcessors(List.of(pd2, pd1)));
  }

  @Test
  public void testSimpleThreeProcessors() {
    Content<?> c = mock(Content.class);

    ProcessorDescriptor pd1 =
        contentProcessor(List.of(c), Collections.emptyList(), Collections.emptyList());
    ProcessorDescriptor pd2 =
        contentProcessor(Collections.emptyList(), List.of(c), Collections.emptyList());
    ProcessorDescriptor pd3 =
        contentProcessor(Collections.emptyList(), Collections.emptyList(), List.of(c));

    DependencyOrderer orderer = new DependencyOrderer();
    assertEquals(List.of(pd1, pd2, pd3), orderer.orderProcessors(List.of(pd2, pd3, pd1)));
  }

  @Test
  public void testSimpleFourProcessors() {
    Content<?> c = mock(Content.class);

    ProcessorDescriptor pd1 =
        contentProcessor(List.of(c), Collections.emptyList(), Collections.emptyList());
    ProcessorDescriptor pd2 =
        contentProcessor(Collections.emptyList(), List.of(c), Collections.emptyList());
    ProcessorDescriptor pd3 = contentProcessor(Collections.emptyList(), List.of(c), List.of(c));
    ProcessorDescriptor pd4 =
        contentProcessor(Collections.emptyList(), Collections.emptyList(), List.of(c));

    DependencyOrderer orderer = new DependencyOrderer();
    assertEquals(List.of(pd1, pd2, pd3, pd4), orderer.orderProcessors(List.of(pd2, pd3, pd1, pd4)));
  }

  @Test
  public void testSimpleCycleTwoProcessors() {
    Content<?> c1 = mock(Content.class);

    ProcessorDescriptor pd1 = contentProcessor(List.of(c1), List.of(c1), Collections.emptyList());
    ProcessorDescriptor pd2 = contentProcessor(List.of(c1), List.of(c1), Collections.emptyList());

    DependencyOrderer orderer = new DependencyOrderer();

    // Order isn't defined, either could come first so just check that both are returned
    assertEquals(2, orderer.orderProcessors(List.of(pd1, pd2)).size());
  }

  @Test
  public void testTwoCyclesTwoContent() {
    StringContent c1 = mock(StringContent.class);
    IntegerContent c2 = mock(IntegerContent.class);

    ProcessorDescriptor pd1 = contentProcessor(List.of(c1, c2), List.of(), List.of());
    ProcessorDescriptor pd2 = contentProcessor(List.of(c2), List.of(c1), List.of());
    ProcessorDescriptor pd3 = contentProcessor(List.of(c1), List.of(c2), List.of());
    ProcessorDescriptor pd4 = contentProcessor(List.of(), List.of(c2), List.of());
    ProcessorDescriptor pd5 = contentProcessor(List.of(c1), List.of(), List.of(c2));

    DependencyOrderer orderer = new DependencyOrderer();

    // Order isn't defined, either could come first so just check that both are returned
    assertEquals(5, orderer.orderProcessors(List.of(pd1, pd2, pd3, pd4, pd5)).size());
  }

  @Test
  public void testTwoContent() {
    StringContent c1 = mock(StringContent.class);
    IntegerContent c2 = mock(IntegerContent.class);

    ProcessorDescriptor pd1 =
        contentProcessor(List.of(c1), List.of(c2), Collections.emptyList()); // Depedent on 2
    ProcessorDescriptor pd2 =
        contentProcessor(List.of(c2), List.of(c2), Collections.emptyList()); // Not dependent
    ProcessorDescriptor pd3 =
        contentProcessor(List.of(c1), List.of(c1), Collections.emptyList()); // Dependent on 1

    DependencyOrderer orderer = new DependencyOrderer();

    assertEquals(List.of(pd2, pd1, pd3), orderer.orderProcessors(List.of(pd1, pd2, pd3)));
  }

  @Test
  public void testTwoContentTwoDependencies() {
    StringContent c1 = mock(StringContent.class);
    IntegerContent c2 = mock(IntegerContent.class);

    ProcessorDescriptor pd1 =
        contentProcessor(List.of(c1, c2), List.of(c1, c2), Collections.emptyList());
    ProcessorDescriptor pd2 =
        contentProcessor(List.of(c1, c2), List.of(c1, c2), Collections.emptyList());

    DependencyOrderer orderer = new DependencyOrderer();

    // Order isn't defined, either could come first so just check that both are returned
    assertEquals(2, orderer.orderProcessors(List.of(pd1, pd2)).size());
  }

  @Test
  public void testSimpleTwoProcessorsWildcard() {
    ProcessorDescriptor pd1 =
        annotationProcessor(
            List.of("Foo"),
            Collections.emptyList(),
            Collections.emptyList()); // Creates "Foo" annotations
    ProcessorDescriptor pd2 =
        annotationProcessor(
            Collections.emptyList(), List.of("*"), Collections.emptyList()); // Processes everything

    DependencyOrderer orderer = new DependencyOrderer();
    assertEquals(List.of(pd1, pd2), orderer.orderProcessors(List.of(pd2, pd1)));
  }

  @Test
  public void testWildcardsAndNamed() {
    ProcessorDescriptor pd1 =
        annotationProcessor(
            List.of("Foo", "Bar"), Collections.emptyList(), Collections.emptyList());
    ProcessorDescriptor pd2 =
        annotationProcessor(List.of("Foo"), List.of("Bar"), Collections.emptyList());
    ProcessorDescriptor pd3 =
        annotationProcessor(List.of("Baz"), List.of("*"), Collections.emptyList());
    ProcessorDescriptor pd4 =
        annotationProcessor(Collections.emptyList(), Collections.emptyList(), List.of("Baz"));

    DependencyOrderer orderer = new DependencyOrderer();
    assertEquals(List.of(pd1, pd2, pd3, pd4), orderer.orderProcessors(List.of(pd2, pd1, pd4, pd3)));
  }

  private interface StringContent extends Content<String> {}

  private interface IntegerContent extends Content<Integer> {}

  private ProcessorDescriptor contentProcessor(
      Collection<Content<?>> creates,
      Collection<Content<?>> processes,
      Collection<Content<?>> deletes) {
    List<Capability> lc = new ArrayList<>();
    for (Content<?> c : creates) {
      ContentCapability cc = mock(ContentCapability.class);
      when(cc.getType()).thenAnswer(i -> c.getClass());

      lc.add(cc);
    }

    List<Capability> lp = new ArrayList<>();
    for (Content<?> c : processes) {
      ContentCapability cc = mock(ContentCapability.class);
      when(cc.getType()).thenAnswer(i -> c.getClass());

      lp.add(cc);
    }

    List<Capability> ld = new ArrayList<>();
    for (Content<?> c : deletes) {
      ContentCapability cc = mock(ContentCapability.class);
      when(cc.getType()).thenAnswer(i -> c.getClass());

      ld.add(cc);
    }

    Capabilities c = mock(Capabilities.class);
    when(c.creates()).thenAnswer(i -> lc.stream());
    when(c.processes()).thenAnswer(i -> lp.stream());
    when(c.deletes()).thenAnswer(i -> ld.stream());

    String name = "Processor " + processorName++;

    ProcessorDescriptor pd = mock(ProcessorDescriptor.class);
    when(pd.capabilities()).thenReturn(c);
    when(pd.getName()).thenReturn(name);
    when(pd.toString()).thenReturn(name);

    return pd;
  }

  private ProcessorDescriptor annotationProcessor(
      Collection<String> creates, Collection<String> processes, Collection<String> deletes) {
    List<Capability> lc = new ArrayList<>();
    for (String s : creates) {
      AnnotationCapability ac = mock(AnnotationCapability.class);
      when(ac.getType()).thenAnswer(i -> s);
      when(ac.getBounds()).thenAnswer(i -> mockBounds.getClass());

      lc.add(ac);
    }

    List<Capability> lp = new ArrayList<>();
    for (String s : processes) {
      AnnotationCapability ac = mock(AnnotationCapability.class);
      when(ac.getType()).thenAnswer(i -> s);
      when(ac.getBounds()).thenAnswer(i -> mockBounds.getClass());

      lp.add(ac);
    }

    List<Capability> ld = new ArrayList<>();
    for (String s : deletes) {
      AnnotationCapability ac = mock(AnnotationCapability.class);
      when(ac.getType()).thenAnswer(i -> s);
      when(ac.getBounds()).thenAnswer(i -> mockBounds.getClass());

      ld.add(ac);
    }

    Capabilities c = mock(Capabilities.class);
    when(c.creates()).thenAnswer(i -> lc.stream());
    when(c.processes()).thenAnswer(i -> lp.stream());
    when(c.deletes()).thenAnswer(i -> ld.stream());

    String name = "Processor " + processorName++;

    ProcessorDescriptor pd = mock(ProcessorDescriptor.class);
    when(pd.capabilities()).thenReturn(c);
    when(pd.getName()).thenReturn(name);
    when(pd.toString()).thenReturn(name);

    return pd;
  }
}
