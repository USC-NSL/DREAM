package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 9:17 PM  <br/>
 * The dummy task that works as resource pool
 */
public class DummyTaskRecord extends DreamTaskRecord {

    public DummyTaskRecord(AllocationTaskView task, ThresholdGuaranteeAlgorithm2 algorithm) {
        super(task, Integer.MIN_VALUE, algorithm);
    }

    @Override
    public int getReductionStep2() {
        return task.getResourceShare();
    }

    public boolean canOfferNewComer() {
        return task.getResourceShare() >= algorithm.getMinResource();
    }

    public boolean isRich() {
        return task.getResourceShare() > 0;
    }

    @Override
    public int getTempToGiveGet() {
        return getReductionStep2();
    }
}
