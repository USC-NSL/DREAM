package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/23/13
 * Time: 11:28 AM <br/>
 * represents a task on a single switch
 */
public class SingleSwitchTask extends Task2 implements AllocationTaskView {
    protected int resourceShare;
    private double accuracy;
    private AccuracyAggregator accuracyAggregator;

    public SingleSwitchTask(Element element) throws Exception {
        super(element);
        resourceShare = 0;
    }

    @Override
    public void updateStats() {
        accuracyAggregator.update(getAccuracy2());
    }

    public int getResourceShare() {
        return resourceShare;
    }

    public void setResourceShare(int resourceShare) {
        this.resourceShare = resourceShare;
    }

    @Override
    public void update(int step) {
        ((SingleSwitchTaskImplementation) user.getImplementation()).setCapacityShare(resourceShare);
        super.update(step);
    }

    @Override
    public String toString() {
        return super.toString() + ", ResourceShare: " + resourceShare;
    }

    public void setAccuracyAggregator(AccuracyAggregator accuracyAggregator) {
        this.accuracyAggregator = accuracyAggregator;
    }

    public double getAggregatedAccuracy() {
        return accuracyAggregator.getAccuracy();
    }

    @Override
    public Task2 getTask() {
        return this;
    }

    public double getAccuracy2() {
        return accuracy;
    }

    @Override
    public int getUsedResourceShare() {
        return ((SingleSwitchTaskImplementation) user.getImplementation()).getUsedResourceShare();
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public void report(int step) {
        super.report(step);
        setAccuracy(((SingleSwitchTaskImplementation) user.getImplementation()).estimateAccuracy());
    }

    /**
     * The actual algorithm must also implement this interface
     */
    public static interface SingleSwitchTaskImplementation extends TaskImplementation {
        /**
         * set resources for the task on this switch
         *
         * @param resource
         */
        void setCapacityShare(int resource);

        /**
         * estimate the instantaneous accuracy
         *
         * @return
         */
        double estimateAccuracy();

        /**
         * @return the number of used resources out of the allocated onces
         */
        int getUsedResourceShare();
    }
}
