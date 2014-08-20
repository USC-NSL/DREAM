package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.drop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.PriorityDropPolicy;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/25/13
 * Time: 7:50 AM
 */
public class NotReceivingTasksDrop extends DropFlagRaiser {
    private LinkedList<Set<DreamTaskRecord>> recordsOfPoor;

    public NotReceivingTasksDrop(ThresholdGuaranteeAlgorithm2 algorithm, PriorityDropPolicy dropPolicy) {
        super(algorithm, dropPolicy);
        recordsOfPoor = new LinkedList<>();
        for (int i = 0; i < dropPolicy.getDropEpochs(); i++) {
            recordsOfPoor.add(new HashSet<DreamTaskRecord>());
        }
    }

    @Override
    public List<DreamTaskRecord> checkDrop(Collection<DreamTaskRecord> poorTaskRecords, Collection<DreamTaskRecord> richTaskRecords, DummyTaskRecord dummyTaskRecord) {
        //check tasks that was poor and are poor and not received anything
        Set<DreamTaskRecord> taskRecords1 = recordsOfPoor.pollLast();
        recordsOfPoor.push(taskRecords1);
        taskRecords1.clear();
        for (DreamTaskRecord poorTaskRecord : poorTaskRecords) {
            if (poorTaskRecord.wasPoor() && poorTaskRecord.getLastResourceChange() < algorithm.getMinResource()) {
                System.out.println("Not received " + poorTaskRecord);
                taskRecords1.add(poorTaskRecord);
            }
        }

        //from the poor tasks
        List<DreamTaskRecord> output = new ArrayList<>();
        for (DreamTaskRecord poorTaskRecord : poorTaskRecords) {
            boolean survived = false;
            for (Set<DreamTaskRecord> taskRecords : recordsOfPoor) {
                if (!taskRecords.contains(poorTaskRecord)) {
                    survived = true;
                }
            }
            //drop it if it is there for 3 epochs
            if (!survived) {
                output.add(poorTaskRecord);
            }
        }

        return output;
    }

    @Override
    protected boolean raise2(Collection<DreamTaskRecord> poorTaskRecords, Collection<DreamTaskRecord> richTaskRecords, DummyTaskRecord dummyTaskRecord) {
        return false;
    }
}
