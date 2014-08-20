package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.TaskRecord2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import edu.usc.enl.dynamicmeasurement.util.IntegerWrapper;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 9:13 PM
 */
public class WeightedResourceDistributorNoDummy extends WeightedResourceDistributor {
    public WeightedResourceDistributorNoDummy(ThresholdGuaranteeAlgorithm2 algorithm) {
        super(algorithm);
    }

    public static void giveToAll(IntegerWrapper dummyGive, ThresholdGuaranteeAlgorithm2 algorithm) {
        Collection<? extends TaskRecord2> tasks = algorithm.getTasks();
        int originalDummyGive = dummyGive.getValue();
        //flush among all equally
        List<DreamTaskRecord> wantEqualShare = new ArrayList<>();
        for (TaskRecord2 task1 : tasks) {
            DreamTaskRecord task = (DreamTaskRecord) task1;
            wantEqualShare.add(task);
        }
        Collections.sort(wantEqualShare, Collections.reverseOrder());
        if (wantEqualShare.size() > 0) {
            ControlledBufferWriter logWriter = algorithm.getLogWriter();
            int toGive = 1;
            while (toGive > 0) {
                toGive = (dummyGive.getValue() / wantEqualShare.size());
                for (TaskRecord2 taskRecord : wantEqualShare) {
                    taskRecord.getTask().setResourceShare(taskRecord.getTask().getResourceShare() + toGive);
                    dummyGive.add(-toGive);
                    logWriter.println(dummyGive + "-d>" + toGive + "-d>" + taskRecord);
                }
            }
            if (toGive == 0 && dummyGive.getValue() > 0) {
                for (DreamTaskRecord taskRecord : wantEqualShare) {
                    taskRecord.getTask().setResourceShare(taskRecord.getTask().getResourceShare() + 1);
                    dummyGive.add(-1);
                    logWriter.println(dummyGive + "-d>" + 1 + "-d>" + taskRecord);
                    if (dummyGive.getValue() <= 0) {
                        break;
                    }
                }
            }
            algorithm.getDummyTaskRecord().getTask().setResourceShare(algorithm.getDummyTaskRecord().getTask().getResourceShare()
                    - originalDummyGive);
        }
    }

    public void distribute(List<DreamTaskRecord> richTasks, List<DreamTaskRecord> poorTasks) {
        //I want to take resources from those that can give more
        IntegerWrapper sumGive = new IntegerWrapper(0);
        for (DreamTaskRecord richTask : richTasks) {
            sumGive.add(richTask.getReductionStep2());
            richTask.setTempToGiveGet(richTask.getReductionStep2());
        }

        IntegerWrapper sumGet = new IntegerWrapper(0);
        for (DreamTaskRecord poorTask : poorTasks) {
            sumGet.add(poorTask.getAdditionStep2());
            poorTask.setTempToGiveGet(poorTask.getAdditionStep2());
        }
        DummyTaskRecord dummyTaskRecord = algorithm.getDummyTaskRecord();

        if (dummyTaskRecord.isRich()) {
            LinkedList<DreamTaskRecord> richTasks1 = new LinkedList<>();
            richTasks1.add(dummyTaskRecord);
            IntegerWrapper dummyGive = new IntegerWrapper(dummyTaskRecord.getTempToGiveGet());
            if (poorTasks.size() > 0) {
                distribute(richTasks1, poorTasks, dummyGive, sumGet);
            }
            if (dummyGive.getValue() > 0) {
                if (sumGet.getValue() > 0) {
                    richTasks1.clear();
                    richTasks1.add(dummyTaskRecord);
                    flush(poorTasks, sumGet, richTasks1);
                } else {
                    giveToAll(dummyGive, algorithm);
                }
            }
        }
        if (richTasks.size() > 0 && poorTasks.size() > 0 && sumGet.getValue() > 0) {
            distribute(richTasks, poorTasks, sumGive, sumGet);
            if (sumGet.getValue() > 0 && sumGive.getValue() > 0) {
                flush(poorTasks, sumGet, richTasks);
            }
        }
//        if (dummyTaskRecord.getTask().getResourceShare() > 0 && algorithm.getTasks().size() > 0) {
//            System.out.println();
//        }
    }
}
