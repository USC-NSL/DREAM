package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch;

import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 11/6/13
 * Time: 10:34 AM <br/>
 * Bundles the data we need to keep per switch
 */
public class MonitorPointData {
    private MonitorPoint monitorPoint;
    /**
     * Number of rules that can be saved at the switch
     */
    private int capacity = 1;
    /**
     * Number of rules already saved at it
     */
    private int usedCapacity = 0;
    private double lastAccuracy = 0;
    private int count = 1;

    MonitorPointData(MonitorPoint monitorPoint) {
        this.monitorPoint = monitorPoint;
    }

    @Override
    public String toString() {
        return "MonitorPointData{" +
                "monitorPoint=" + monitorPoint +
                ", capacity=" + capacity +
                ", usedCapacity=" + usedCapacity +
                ", lastAccuracy=" + lastAccuracy +
                '}';
    }

    protected MonitorPoint getMonitorPoint() {
        return monitorPoint;
    }

    private void setMonitorPoint(MonitorPoint monitorPoint) {
        this.monitorPoint = monitorPoint;
    }

    protected int getCapacity() {
        return capacity;
    }

    protected void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    protected int getUsedCapacity() {
        return usedCapacity;
    }

    protected void setUsedCapacity(int usedCapacity) {
        this.usedCapacity = usedCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonitorPointData that = (MonitorPointData) o;

        if (monitorPoint != null ? !monitorPoint.equals(that.monitorPoint) : that.monitorPoint != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return monitorPoint != null ? monitorPoint.hashCode() : 0;
    }

    public double getLastAccuracy() {
        return lastAccuracy;
    }

    protected void setLastAccuracy(double lastAccuracy) {
        this.lastAccuracy = lastAccuracy;
    }

    public void reset() {
        lastAccuracy = 0;
        count = 0;
    }

    public boolean isFull() {
        return capacity <= usedCapacity;
    }

    /**
     * Add a precision number because this switch is responsible for the item
     *
     * @param precision
     */
    public void addPrecision(double precision) {
        lastAccuracy += precision;
        count++;

    }

    public void addPrecision(double precision, int n) {
        lastAccuracy += n * precision;
        count += n;

    }

    /**
     * Average precision numbers and save in lastAccuracy. The result will be 1 if no precision has been added
     */
    public void computeAccuracy() {
        if (count == 0) {
            lastAccuracy = 1;
        } else {
            lastAccuracy /= count;
        }
    }
}
