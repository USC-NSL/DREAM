package edu.usc.enl.dynamicmeasurement.metric;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/27/13
 * Time: 9:26 PM
 */
public abstract class Metric {
    public void reset() {

    }

    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public static class DummyMetric extends Metric {
        private final String name;

        public DummyMetric(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
