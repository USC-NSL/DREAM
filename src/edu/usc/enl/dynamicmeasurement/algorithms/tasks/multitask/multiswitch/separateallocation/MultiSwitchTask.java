package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/9/13
 * Time: 9:58 AM <br/>
 * Represents a task that can have traffic from multiple switches
 */
public class MultiSwitchTask extends Task2 {
    private Map<MonitorPoint, Double> currentAccuracyEstimation; //its easier to contact with tasks
    private Map<MonitorPoint, Integer> resourceShare; //its easier to contact with tasks
    private Map<MonitorPoint, Integer> usedResourceShare; //its easier to contact with tasks
    private Map<MonitorPoint, SingleSwitchTaskView> taskViews;
    private AccuracyAggregator accuracyAggregator;

    public MultiSwitchTask(Element e) throws Exception {
        super(e);
        resourceShare = new HashMap<>();
        usedResourceShare = new HashMap<>();
        currentAccuracyEstimation = new HashMap<>();
        taskViews = new HashMap<>();
        Set<MonitorPoint> monitorPoints = Util.getNetwork().getMonitorPoints();
        for (MonitorPoint monitorPoint : monitorPoints) {
            if (monitorPoint.hasDataFrom(filter)) {
                taskViews.put(monitorPoint, new SingleSwitchTaskView(this, monitorPoint));
                resourceShare.put(monitorPoint, 0);
                usedResourceShare.put(monitorPoint, 0);
                currentAccuracyEstimation.put(monitorPoint, 0d);
            }
        }
    }

    public double getGlobalAccuracy() {
        return accuracyAggregator.getAccuracy();
    }

    public Set<MonitorPoint> getMonitorPoints() {
        return taskViews.keySet();
    }

    /**
     * @param monitorPoint
     * @return the allocation view for that monitorPont (switch)
     */
    public SingleSwitchTaskView getViewFor(MonitorPoint monitorPoint) {
        return taskViews.get(monitorPoint);
    }

    @Override
    public void update(int step) {
        ((MultiSwitchTaskImplementation) user.getImplementation()).setCapacityShare(resourceShare);
        super.update(step);
    }

    @Override
    public void updateStats() {
        MultiSwitchTaskImplementation implementation = (MultiSwitchTaskImplementation) user.getImplementation();
        accuracyAggregator.update(implementation.getGlobalAccuracy());
        implementation.estimateAccuracy(currentAccuracyEstimation);
        for (Map.Entry<MonitorPoint, Double> entry : currentAccuracyEstimation.entrySet()) {
            taskViews.get(entry.getKey()).updateAccuracy(entry.getValue());
        }

        ((MultiSwitchTaskImplementation) user.getImplementation()).getUsedResources(usedResourceShare);
    }

    public void setAccuracyAggregator(AccuracyAggregator accuracyAggregator) {
        this.accuracyAggregator = accuracyAggregator;
    }

    public void setAccuracyAggregator(MonitorPoint monitorPoint, AccuracyAggregator accuracyAggregator) {
        taskViews.get(monitorPoint).setAccuracyAggregator(accuracyAggregator);
    }

    /**
     * The actual implementation of an algorithm on multiple switches must also implement this interface
     */
    public static interface MultiSwitchTaskImplementation extends TaskImplementation {
        /**
         * Sets the share of this task on each of the switches
         *
         * @param resource
         */
        void setCapacityShare(Map<MonitorPoint, Integer> resource);

        /**
         * Estimate the local accuracy on each of these switches
         *
         * @param accuracy
         */
        void estimateAccuracy(Map<MonitorPoint, Double> accuracy);

        double getGlobalAccuracy();

        /**
         * How many of resources at each switch have been used
         *
         * @param resource
         */
        void getUsedResources(Map<MonitorPoint, Integer> resource);
    }

    /**
     * The vew of the task on a single switch.
     */
    public class SingleSwitchTaskView implements AllocationTaskView {
        private MonitorPoint monitorPoint;
        private MultiSwitchTask task;
        private AccuracyAggregator accuracyAggregator;

        public SingleSwitchTaskView(MultiSwitchTask task, MonitorPoint monitorPoint) {
            this.task = task;
            this.monitorPoint = monitorPoint;
        }

        public void setAccuracyAggregator(AccuracyAggregator accuracyAggregator) {
            this.accuracyAggregator = accuracyAggregator;
        }

        @Override
        public int getResourceShare() {
            return resourceShare.get(monitorPoint);
        }

        @Override
        public void setResourceShare(int c) {
            if (c < 0) {
                throw new RuntimeException("Negative resource share " + c);
            }
            resourceShare.put(monitorPoint, c);
        }

        @Override
        public double getAggregatedAccuracy() {
            return accuracyAggregator.getAccuracy();
        }

        @Override
        public Task2 getTask() {
            return task;
        }

        @Override
        public double getAccuracy2() {
            return currentAccuracyEstimation.get(monitorPoint);
        }

        @Override
        public int getUsedResourceShare() {
            return usedResourceShare.get(monitorPoint);
        }

        public String toString() {
            return task.toString() + ", ResourceShare: " + getUsedResourceShare() + " out of " + resourceShare.get(monitorPoint) + " on " + monitorPoint.getIntId();
        }

        public void updateAccuracy(Double accuracy) {
            accuracyAggregator.update(accuracy);
        }
    }
}
