package s2s.query_compiler.template.stream;


import s2s.query_compiler.options.CommonCompilerOption;
import s2s.query_compiler.options.SingleFieldTuplesConverter;

import java.util.ArrayList;
import java.util.List;

public class StreamCompilerOptions implements CommonCompilerOption {

    public static final StreamCompilerOptions[] ALL_COMBINATION;
    public static final StreamCompilerOptions[] ALL_SEQUENTIAL;

    public static List<StreamCompilerOptions> allCombinationsMultiThreading(MultiThreading multiThreading) {
        boolean[] bools = new boolean[]{true, false};
        List<StreamCompilerOptions> options = new ArrayList<>();
        for(JoinConverter joinConverter : JoinConverter.values()) {
            for(boolean compressPredicateConjunction : bools) {
                options.add(new StreamCompilerOptions(
                        joinConverter,
                        multiThreading,
                        compressPredicateConjunction,
                        true));
            }
        }
        return options;
    }

    static {
        ALL_SEQUENTIAL = allCombinationsMultiThreading(MultiThreading.SEQUENTIAL)
                .toArray(new StreamCompilerOptions[0]);

        ArrayList<StreamCompilerOptions> all = new ArrayList<>();
        for(MultiThreading multiThreading : MultiThreading.values()) {
            all.addAll(allCombinationsMultiThreading(multiThreading));
        }
        ALL_COMBINATION = all.toArray(new StreamCompilerOptions[0]);
    }

    private final JoinConverter joinConverter;
    private final MultiThreading multiThreading;
    private final boolean fuseFilters;
    private final boolean useStreamToList;

    public StreamCompilerOptions(JoinConverter joinConverter,
                                 MultiThreading multiThreading,
                                 boolean fuseFilters,
                                 boolean useStreamToList) {
        this.joinConverter = joinConverter;
        this.multiThreading = multiThreading;
        this.fuseFilters = fuseFilters;
        this.useStreamToList = useStreamToList;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toCompactString() {
        return "Opt_" + joinConverter +
                "_" + multiThreading +
                "_" + (fuseFilters ? "fuseFilters" : "notFuseFilters")
                ;
    }

    @Override
    public SingleFieldTuplesConverter getSingleFieldTuplesConverter() {
        return SingleFieldTuplesConverter.TO_PRIMITIVE;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public boolean useStreamToList() {
        return useStreamToList;
    }

    public static final class Builder {
        JoinConverter joinConverter = JoinConverter.FLATMAP;
        MultiThreading multiThreading = MultiThreading.SEQUENTIAL;
        boolean compressPredicateConjunction = false;
        boolean useStreamToList = true;

        private Builder() {}

        public Builder(JoinConverter joinConverter,
                       MultiThreading multiThreading,
                       boolean compressPredicateConjunction,
                       boolean useStreamToList) {
            this.joinConverter = joinConverter;
            this.multiThreading = multiThreading;
            this.compressPredicateConjunction = compressPredicateConjunction;
            this.useStreamToList = useStreamToList;
        }

        private Builder copy() {
            return new Builder(
                    joinConverter,
                    multiThreading,
                    compressPredicateConjunction,
                    useStreamToList
            );
        }

        public Builder joinWithFlatMap() {
            return new Builder(
                    JoinConverter.FLATMAP,
                    multiThreading,
                    compressPredicateConjunction,
                    useStreamToList
            );
        }
        public Builder joinWithMapMulti() {
            return new Builder(
                    JoinConverter.MAPMULTI,
                    multiThreading,
                    compressPredicateConjunction,
                    useStreamToList
            );
        }

        public Builder withMultiThreading(MultiThreading multiThreading) {
            return new Builder(
                    joinConverter,
                    multiThreading,
                    compressPredicateConjunction,
                    useStreamToList
            );
        }
        public Builder withSequentialThread() {
            return withMultiThreading(MultiThreading.SEQUENTIAL);
        }

        public Builder withPredicateConjunctionCompression(boolean shouldCompress) {
            return new Builder(
                    joinConverter,
                    multiThreading,
                    shouldCompress,
                    useStreamToList
            );
        }

        public Builder withStreamToList(boolean shouldUseStreamToList) {
            return new Builder(
                    joinConverter,
                    multiThreading,
                    compressPredicateConjunction,
                    shouldUseStreamToList
            );
        }

        public StreamCompilerOptions build() {
            return new StreamCompilerOptions(
                    joinConverter,
                    multiThreading,
                    compressPredicateConjunction,
                    useStreamToList
            );
        }
    }

    public enum JoinConverter {
        FLATMAP, MAPMULTI
    }

    public enum MultiThreading {
        SEQUENTIAL,
        PARALLEL_UNORDERED,
        CONCURRENT, // imply unordered
        CONCURRENT_COLLECTOR; // imply unordered

        public boolean isSequential() {
            return this == SEQUENTIAL;
        }

        public boolean isConcurrent() {
            return this == CONCURRENT || this == CONCURRENT_COLLECTOR;
        }

        public boolean isConcurrentCollector() {
            return this == CONCURRENT_COLLECTOR;
        }

        @Override
        public String toString() {
            if(this == SEQUENTIAL) return "SEQ";
            if(this == PARALLEL_UNORDERED) return "PU";
            if(this == CONCURRENT) return "CG";
            if(this == CONCURRENT_COLLECTOR) return "CGCC";
            throw new RuntimeException("Should not reach this, unexpected name" + this);
        }
    }

    public JoinConverter getJoinConverter() {
        return joinConverter;
    }

    public MultiThreading getMultiThreading() {
        return multiThreading;
    }

    public boolean shouldCompressPredicateConjunction() {
        return fuseFilters;
    }

    public boolean isOrdered() {
        return multiThreading.isSequential();
    }

}
