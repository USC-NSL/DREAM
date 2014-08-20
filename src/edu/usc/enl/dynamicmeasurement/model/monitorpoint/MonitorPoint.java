package edu.usc.enl.dynamicmeasurement.model.monitorpoint;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MatrixSet;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/23/13
 * Time: 5:34 PM <br/>
 * Represents a device that can monitor packets.
 */
public abstract class MonitorPoint extends MatrixSet.MatrixObject implements Cloneable {
    /**
     * The number of prefixes that it can monitor
     */
    private int capacity;
    /**
     * This is usually used for MAC address
     */
    private String stringId;
    /**
     * The convenient short ID
     */
    private int intId;

    public MonitorPoint(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStringId() {
        return stringId;
    }

    public void setStringId(String stringId) {
        this.stringId = stringId;
    }

    public int getIntId() {
        return intId;
    }

    public void setIntId(int intId) {
        this.intId = intId;
    }

    @Override
    public abstract MonitorPoint clone();

    /**
     * if this monitorpoint can monitor data from this wildcardpattern
     *
     * @param wildcardPattern
     * @return
     */
    public abstract boolean hasDataFrom(WildcardPattern wildcardPattern);

    public abstract boolean hasDataFrom(long item);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonitorPoint that = (MonitorPoint) o;

        if (intId != that.intId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return intId;
    }

    @Override
    public String toString() {
        return intId + ": " + capacity + (stringId == null ? "" : "(" + stringId + ")");
    }

    public void decCapacity() {
        capacity--;
    }

    public void incCapacity() {
        capacity++;
    }
}
