/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface ProblemReporter {
    public static final ProblemReporter DISCARDING = new ProblemReporter(){

        @Override
        public ProblemReporter forChild(PathElement pathElement) {
            return this;
        }

        @Override
        public void report(Problem problem) {
        }
    };

    public ProblemReporter forChild(PathElement var1);

    public void report(Problem var1);

    public static class ScopedCollector
    extends Collector
    implements AutoCloseable {
        private final Logger logger;

        public ScopedCollector(Logger logger) {
            this.logger = logger;
        }

        public ScopedCollector(PathElement pathElement, Logger logger) {
            super(pathElement);
            this.logger = logger;
        }

        @Override
        public void close() {
            if (!this.isEmpty()) {
                this.logger.warn("[{}] Serialization errors:\n{}", (Object)this.logger.getName(), (Object)this.getTreeReport());
            }
        }
    }

    public static class Collector
    implements ProblemReporter {
        public static final PathElement EMPTY_ROOT = () -> "";
        @Nullable
        private final Collector parent;
        private final PathElement element;
        private final Set<Entry> problems;

        public Collector() {
            this(EMPTY_ROOT);
        }

        public Collector(PathElement pathElement) {
            this.parent = null;
            this.problems = new LinkedHashSet<Entry>();
            this.element = pathElement;
        }

        private Collector(Collector collector, PathElement pathElement) {
            this.problems = collector.problems;
            this.parent = collector;
            this.element = pathElement;
        }

        @Override
        public ProblemReporter forChild(PathElement pathElement) {
            return new Collector(this, pathElement);
        }

        @Override
        public void report(Problem problem) {
            this.problems.add(new Entry(this, problem));
        }

        public boolean isEmpty() {
            return this.problems.isEmpty();
        }

        public void forEach(BiConsumer<String, Problem> biConsumer) {
            ArrayList<PathElement> arrayList = new ArrayList<PathElement>();
            StringBuilder stringBuilder = new StringBuilder();
            for (Entry entry : this.problems) {
                Collector collector = entry.source;
                while (collector != null) {
                    arrayList.add(collector.element);
                    collector = collector.parent;
                }
                for (int i = arrayList.size() - 1; i >= 0; --i) {
                    stringBuilder.append(((PathElement)arrayList.get(i)).get());
                }
                biConsumer.accept(stringBuilder.toString(), entry.problem());
                stringBuilder.setLength(0);
                arrayList.clear();
            }
        }

        public String getReport() {
            HashMultimap hashMultimap = HashMultimap.create();
            this.forEach((arg_0, arg_1) -> ((Multimap)hashMultimap).put(arg_0, arg_1));
            return hashMultimap.asMap().entrySet().stream().map(entry -> " at " + (String)entry.getKey() + ": " + ((Collection)entry.getValue()).stream().map(Problem::description).collect(Collectors.joining("; "))).collect(Collectors.joining("\n"));
        }

        public String getTreeReport() {
            ArrayList<PathElement> arrayList = new ArrayList<PathElement>();
            ProblemTreeNode problemTreeNode = new ProblemTreeNode(this.element);
            for (Entry entry : this.problems) {
                Collector collector = entry.source;
                while (collector != this) {
                    arrayList.add(collector.element);
                    collector = collector.parent;
                }
                ProblemTreeNode problemTreeNode2 = problemTreeNode;
                for (int i = arrayList.size() - 1; i >= 0; --i) {
                    problemTreeNode2 = problemTreeNode2.child((PathElement)arrayList.get(i));
                }
                arrayList.clear();
                problemTreeNode2.problems.add(entry.problem);
            }
            return String.join((CharSequence)"\n", problemTreeNode.getLines());
        }

        static final class Entry
        extends Record {
            final Collector source;
            final Problem problem;

            Entry(Collector collector, Problem problem) {
                this.source = collector;
                this.problem = problem;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this, object);
            }

            public Collector source() {
                return this.source;
            }

            public Problem problem() {
                return this.problem;
            }
        }

        static final class ProblemTreeNode
        extends Record {
            private final PathElement element;
            final List<Problem> problems;
            private final Map<PathElement, ProblemTreeNode> children;

            public ProblemTreeNode(PathElement pathElement) {
                this(pathElement, new ArrayList<Problem>(), new LinkedHashMap<PathElement, ProblemTreeNode>());
            }

            private ProblemTreeNode(PathElement pathElement, List<Problem> list, Map<PathElement, ProblemTreeNode> map) {
                this.element = pathElement;
                this.problems = list;
                this.children = map;
            }

            public ProblemTreeNode child(PathElement pathElement) {
                return this.children.computeIfAbsent(pathElement, ProblemTreeNode::new);
            }

            public List<String> getLines() {
                int n = this.problems.size();
                int n2 = this.children.size();
                if (n == 0 && n2 == 0) {
                    return List.of();
                }
                if (n == 0 && n2 == 1) {
                    ArrayList<String> arrayList = new ArrayList<String>();
                    this.children.forEach((pathElement, problemTreeNode) -> arrayList.addAll(problemTreeNode.getLines()));
                    arrayList.set(0, this.element.get() + (String)arrayList.get(0));
                    return arrayList;
                }
                if (n == 1 && n2 == 0) {
                    return List.of(this.element.get() + ": " + this.problems.getFirst().description());
                }
                ArrayList<String> arrayList = new ArrayList<String>();
                this.children.forEach((pathElement, problemTreeNode) -> arrayList.addAll(problemTreeNode.getLines()));
                arrayList.replaceAll(string -> "  " + string);
                for (Problem problem : this.problems) {
                    arrayList.add("  " + problem.description());
                }
                arrayList.addFirst(this.element.get() + ":");
                return arrayList;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this, object);
            }

            public PathElement element() {
                return this.element;
            }

            public List<Problem> problems() {
                return this.problems;
            }

            public Map<PathElement, ProblemTreeNode> children() {
                return this.children;
            }
        }
    }

    public record ElementReferencePathElement(ResourceKey<?> id) implements PathElement
    {
        @Override
        public String get() {
            return "->{" + String.valueOf(this.id.location()) + "@" + String.valueOf(this.id.registry()) + "}";
        }
    }

    public record IndexedPathElement(int index) implements PathElement
    {
        @Override
        public String get() {
            return "[" + this.index + "]";
        }
    }

    public record IndexedFieldPathElement(String name, int index) implements PathElement
    {
        @Override
        public String get() {
            return "." + this.name + "[" + this.index + "]";
        }
    }

    public record FieldPathElement(String name) implements PathElement
    {
        @Override
        public String get() {
            return "." + this.name;
        }
    }

    public record RootElementPathElement(ResourceKey<?> id) implements PathElement
    {
        @Override
        public String get() {
            return "{" + String.valueOf(this.id.location()) + "@" + String.valueOf(this.id.registry()) + "}";
        }
    }

    public record RootFieldPathElement(String name) implements PathElement
    {
        @Override
        public String get() {
            return this.name;
        }
    }

    @FunctionalInterface
    public static interface PathElement {
        public String get();
    }

    public static interface Problem {
        public String description();
    }
}

