package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.drop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.PriorityDropPolicy;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.TaskRecord2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 6:55 PM <br/>
 * Implement the local drop policy.
 */
public abstract class DropFlagRaiser {
    protected final ThresholdGuaranteeAlgorithm2 algorithm;
    protected final PriorityDropPolicy dropPolicy;

    public DropFlagRaiser(ThresholdGuaranteeAlgorithm2 algorithm, PriorityDropPolicy dropPolicy) {
        this.algorithm = algorithm;
        this.dropPolicy = dropPolicy;
    }

    public List<DreamTaskRecord> checkDrop(Collection<DreamTaskRecord> poorTaskRecords,
                                           Collection<DreamTaskRecord> richTaskRecords,
                                           DummyTaskRecord dummyTaskRecord) {
        ArrayList<DreamTaskRecord> output = new ArrayList<>();
        boolean raised = raise2(poorTaskRecords, richTaskRecords, dummyTaskRecord);
        TaskRecord2 dropped = dropPolicy.checkForDrop(raised, algorithm.getTasks());
        if (dropped != null) {
            output.add((DreamTaskRecord) dropped);
        }
        return output;
    }

    protected abstract boolean raise2(Collection<DreamTaskRecord> poorTaskRecords,
                                      Collection<DreamTaskRecord> richTaskRecords,
                                      DummyTaskRecord dummyTaskRecord);
}
