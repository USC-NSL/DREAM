package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.tcammultitaskmultiswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MatrixSet;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MultiSwitch2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/23/13
 * Time: 9:54 AM
 */
public class TCAMMultiSwitchTask extends Task2 {
    private final MatrixSet<MonitorPoint> monitorPointUsage;
    private double accuracy;
    private AccuracyAggregator accuracyAggregator;

    @Override
    public void updateStats() {

    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy2(double accuracy) {
        this.accuracy = accuracy;
    }

    public AccuracyAggregator getAccuracyAggregator() {
        return accuracyAggregator;
    }

    public void setAccuracyAggregator(AccuracyAggregator accuracyAggregator) {
        this.accuracyAggregator = accuracyAggregator;
    }

    public TCAMMultiSwitchTask(Element element) throws Exception {
        super(element);
        MatrixSet<MonitorPoint> myMonitorPoints;
        {
            Set<MonitorPoint> monitorPoints = Util.getNetwork().getMonitorPoints();
            MatrixSet.MatrixMapping<MonitorPoint> mapping = new MatrixSet.MatrixMapping<>();
            for (MonitorPoint monitorPoint : monitorPoints) {
                mapping.add(monitorPoint.clone());
            }
            myMonitorPoints = new MatrixSet<MonitorPoint>(mapping);
            myMonitorPoints.clear();
            for (MonitorPoint monitorPoint : monitorPoints) {
                if (monitorPoint.hasDataFrom(this.filter)) {
                    myMonitorPoints.add(monitorPoint);
                }
            }
        }
        this.monitorPointUsage = myMonitorPoints;
    }

    public MultiSwitch2 getMultiSwitch() {
        return (MultiSwitch2) user.getImplementation();
    }

    public double getThreshold() {
        return getMultiSwitch().getThreshold();
    }


    public boolean usedResourceOn(MatrixSet<MonitorPoint> other) {
        return monitorPointUsage.getSimilarity(other) > 0;
    }

    public Set<MonitorPoint> getMonitorPoints() {
        return monitorPointUsage;
    }
}
