package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.drop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.PriorityDropPolicy;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/26/13
 * Time: 12:38 PM
 */
public class NoDropPolicy extends DropFlagRaiser {
    public NoDropPolicy(ThresholdGuaranteeAlgorithm2 algorithm, PriorityDropPolicy dropPolicy) {
        super(algorithm, dropPolicy);
    }

    @Override
    protected boolean raise2(Collection<DreamTaskRecord> poorTaskRecords, Collection<DreamTaskRecord> richTaskRecords,
                             DummyTaskRecord dummyTaskRecord) {
        return false;
    }
}





