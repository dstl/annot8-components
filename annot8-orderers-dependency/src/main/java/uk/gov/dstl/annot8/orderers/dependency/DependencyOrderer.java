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

import io.annot8.api.capabilities.AnnotationCapability;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.capabilities.Capability;
import io.annot8.api.capabilities.ContentCapability;
import io.annot8.api.capabilities.GroupCapability;
import io.annot8.api.components.Annot8ComponentDescriptor;
import io.annot8.api.components.ProcessorDescriptor;
import io.annot8.api.components.SourceDescriptor;
import io.annot8.api.pipelines.PipelineOrderer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orderer that builds a dependency map of Annot8 processors and returns them in an order such that
 * the dependencies are satisfied. Sources are returned in the order they are specified.
 */
public class DependencyOrderer implements PipelineOrderer {
  private static final Logger LOGGER = LoggerFactory.getLogger(DependencyOrderer.class);

  @Override
  public Collection<ProcessorDescriptor> orderProcessors(
      Collection<ProcessorDescriptor> processors) {
    Graph<ProcessorDescriptor, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

    // Build a dependency graph

    // Add all processors to the graph
    processors.forEach(g::addVertex);

    // Compare all pairs of processors, and identify dependencies
    for (ProcessorDescriptor p1 : processors) {
      Capabilities c1 = p1.capabilities();

      for (ProcessorDescriptor p2 : processors) {
        if (p1 == p2) continue;

        Capabilities c2 = p2.capabilities();

        // P2 is dependent on P1 if:
        //  - P2 processes content/annotations/groups that P1 creates, or
        //  - P2 deletes content/annotations/groups that P1 creates, or
        //  - P2 deletes content/annotations/groups that P1 processes

        if (c2.processes().anyMatch(cp -> c1.creates().anyMatch(cc -> capabilityEqual(cp, cc)))
            || c2.deletes().anyMatch(cd -> c1.creates().anyMatch(cc -> capabilityEqual(cd, cc)))
            || c2.deletes().anyMatch(cd -> c1.processes().anyMatch(cc -> capabilityEqual(cd, cc))))
          g.addEdge(
              p1,
              p2); // Direction of the edge is towards the dependee (i.e. the one using the output)
      }
    }

    // Check for circular dependencies
    DirectedSimpleCycles<ProcessorDescriptor, DefaultEdge> dsc = new HawickJamesSimpleCycles<>(g);
    List<List<ProcessorDescriptor>> cycles = dsc.findSimpleCycles();

    // Remove edges to break cycles, even thought this may give a sub-optimal (or even bad) ordering

    if (!cycles.isEmpty())
      LOGGER.warn(
          "Cycle(s) detected in dependency graph - ordering of processors may not be optimum");

    while (!cycles.isEmpty()) {
      List<ProcessorDescriptor> cycle = cycles.remove(0);
      LOGGER.info(
          "The following {} processors form a cycle: {}",
          cycle.size(),
          cycle.stream().map(Annot8ComponentDescriptor::getName).collect(Collectors.joining(", ")));

      Iterator<ProcessorDescriptor> iter = cycle.iterator();
      ProcessorDescriptor p1 = cycle.iterator().next();

      while (iter.hasNext()) {
        ProcessorDescriptor p2 = iter.next();

        DefaultEdge edge = g.removeEdge(p1, p2);
        if (edge != null) {
          LOGGER.info(
              "Dependency between {} and {} removed to break cycle", p1.getName(), p2.getName());
          break;
        }
      }

      // Recheck every time, as removing one edge may also affect other cycles and we don't want to
      // remove edges unnecessarily
      cycles = dsc.findSimpleCycles();
    }

    // Return graph in topological order
    TopologicalOrderIterator<ProcessorDescriptor, DefaultEdge> orderIterator =
        new TopologicalOrderIterator<>(g);
    List<ProcessorDescriptor> orderedProcessors = new ArrayList<>(processors.size());

    orderIterator.forEachRemaining(orderedProcessors::add);

    return orderedProcessors;
  }

  @Override
  public Collection<SourceDescriptor> orderSources(Collection<SourceDescriptor> sources) {
    return sources;
  }

  private boolean capabilityEqual(Capability c1, Capability c2) {
    if (c1 == c2) return true;

    if (c1 instanceof ContentCapability) {
      if (!(c2 instanceof ContentCapability)) return false;

      return ((ContentCapability) c1).getType().equals(((ContentCapability) c2).getType());
    } else if (c1 instanceof AnnotationCapability) {
      if (!(c2 instanceof AnnotationCapability)) return false;

      AnnotationCapability a1 = (AnnotationCapability) c1;
      AnnotationCapability a2 = (AnnotationCapability) c2;

      // TODO: Should bounds also match on subtypes?
      return a1.getBounds().equals(a2.getBounds())
          && (a1.getType().equals(a2.getType())
              || a1.getType().equals("*")
              || a2.getType().equals("*"));
    } else if (c1 instanceof GroupCapability) {
      if (!(c2 instanceof GroupCapability)) return false;

      return ((GroupCapability) c1).getType().equals(((GroupCapability) c2).getType())
          || ((GroupCapability) c1).getType().equals("*")
          || ((GroupCapability) c2).getType().equals("*");
    }

    return false;
  }
}
