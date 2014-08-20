package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.drop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.PriorityDropPolicy;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 7:07 PM
 */
public class NoRichDrop extends DropFlagRaiser {
    public NoRichDrop(ThresholdGuaranteeAlgorithm2 algorithm1, PriorityDropPolicy dropPolicy1) {
        super(algorithm1, dropPolicy1);
    }

    @Override
    public boolean raise2(Collection<DreamTaskRecord> poorTaskRecords, Collection<DreamTaskRecord> richTaskRecords,
                          DummyTaskRecord dummyTaskRecord) {
        return (richTaskRecords.size() == 0 && !dummyTaskRecord.isRich() && poorTaskRecords.size() > 0);
    }
}
