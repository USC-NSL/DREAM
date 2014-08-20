package edu.usc.enl.dynamicmeasurement.model.monitorpoint;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/24/13
 * Time: 7:18 PM <br/>
 * A simple monitor point that knows it can monitor from exact addresses. This is mosstly for test and comparing
 * the speed to the prefix one
 */
public class IntegerMonitorPoint extends MonitorPoint {
    private Set<Integer> hasDataFrom;

    public IntegerMonitorPoint(int capacity, Set<Integer> hasDataFrom) {
        super(capacity);
        this.hasDataFrom = hasDataFrom;
    }

    @Override
    public MonitorPoint clone() {
        IntegerMonitorPoint output = new IntegerMonitorPoint(getCapacity(), hasDataFrom);
        output.setIntId(getIntId());
        setId(getId());
        return output;

    }

    public boolean hasDataFrom(WildcardPattern wildcardPattern) {
        for (Integer integer : hasDataFrom) {
            if (wildcardPattern.match(integer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasDataFrom(long item) {
        return hasDataFrom.contains((int) item);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString() + " (");
        boolean first = true;
        for (Integer i : hasDataFrom) {
            sb.append(first ? "" : ",").append(i);
            first = false;
        }
        return sb.append(")").toString();
    }
}
