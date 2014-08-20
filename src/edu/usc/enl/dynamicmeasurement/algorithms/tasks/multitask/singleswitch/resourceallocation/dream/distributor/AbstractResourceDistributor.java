package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyAllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 9:12 PM <br/>
 * Implements different approaches of distributing the resources from rich to poor
 */
public abstract class AbstractResourceDistributor {
    protected final ThresholdGuaranteeAlgorithm2 algorithm;

    protected AbstractResourceDistributor(ThresholdGuaranteeAlgorithm2 algorithm) {
        this.algorithm = algorithm;
    }

    public abstract void distribute(List<DreamTaskRecord> richTasks, List<DreamTaskRecord> poorTasks);

    public DummyTaskRecord getDummy() {
        DummyAllocationTaskView dummy = new DummyAllocationTaskView();
        return new DummyTaskRecord(dummy, algorithm);
    }
}
