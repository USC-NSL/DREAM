package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import edu.usc.enl.dynamicmeasurement.util.IntegerWrapper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 9:13 PM
 */
public class WeightedResourceDistributor extends AbstractResourceDistributor {
    public WeightedResourceDistributor(ThresholdGuaranteeAlgorithm2 algorithm) {
        super(algorithm);
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
                    //giveToAll(dummyGive, algorithm);
                }

            }
        }
        if (richTasks.size() > 0 && poorTasks.size() > 0 && sumGet.getValue() > 0) {
            distribute(richTasks, poorTasks, sumGive, sumGet);
            if (sumGet.getValue() > 0 && sumGive.getValue() > 0) {
                flush(poorTasks, sumGet, richTasks);
            }
        }
    }

    protected void flush(List<DreamTaskRecord> poorTasks, IntegerWrapper sumGet2, List<DreamTaskRecord> richTasks) {
        //flush dummy resource
        ControlledBufferWriter logWriter = algorithm.getLogWriter();
        Collections.sort(richTasks);
        Collections.sort(poorTasks, Collections.reverseOrder());
        for (DreamTaskRecord richTaskRecord : richTasks) {
            if (richTaskRecord.getTempToGiveGet() > 0) {
                int i = 0;
                int toGive = richTaskRecord.getTempToGiveGet();
                while (toGive > 0 && sumGet2.getValue() > 0) {
                    if (i >= poorTasks.size()) {
                        System.err.println("Check it sumget>0 but no poor in need?");
                    }
                    DreamTaskRecord poorTaskRecord = poorTasks.get(i++);
                    int toGet = poorTaskRecord.getTempToGiveGet();
                    if (toGet > 0) {
                        int transfer = Math.min(toGet, toGive);
                        AllocationTaskView richTask = richTaskRecord.getTask();
                        AllocationTaskView poorTask = poorTaskRecord.getTask();
                        logWriter.println(richTask + "-!>" + transfer + "-!>" + poorTask);
                        poorTask.setResourceShare(poorTask.getResourceShare() + transfer);
                        toGive -= transfer;
                        sumGet2.add(-transfer);
                        poorTaskRecord.setTempToGiveGet(toGet - transfer);
                        richTask.setResourceShare(richTask.getResourceShare() - transfer);
                    }
                }
                richTaskRecord.setTempToGiveGet(toGive);
            }
        }

    }

    void distribute(List<DreamTaskRecord> richTasks, List<DreamTaskRecord> poorTasks, IntegerWrapper sumGive, IntegerWrapper sumGet) {
        int originalSumGet = sumGet.getValue();
        int originalSumGive = sumGive.getValue();
        ControlledBufferWriter logWriter = algorithm.getLogWriter();
        if (sumGet.getValue() > sumGive.getValue()) {
            //distribute among poors based on weights
            //added value is sumGive*get/sumGet
            for (DreamTaskRecord poorTaskRecord : poorTasks) {
                int toGet = (int) (1.0 * poorTaskRecord.getTempToGiveGet() / originalSumGet * originalSumGive);
                AllocationTaskView poorTask = poorTaskRecord.getTask();
                while (true) {
                    if (richTasks.size() == 0) {
                        System.err.println("Rich task not found");
                        break;
                    }
                    DreamTaskRecord richTaskRecord = richTasks.remove(0);
                    AllocationTaskView richTask = richTaskRecord.getTask();
                    int toGive = richTaskRecord.getTempToGiveGet();

                    int transfer = Math.min(toGet, toGive);
                    logWriter.println(richTask + "-p>" + transfer + "->" + poorTask);
                    richTask.setResourceShare(richTask.getResourceShare() - transfer);
                    poorTask.setResourceShare(poorTask.getResourceShare() + transfer);
                    sumGet.add(-transfer);
                    sumGive.add(-transfer);
                    toGive -= transfer;
                    toGet -= transfer;
                    richTaskRecord.setTempToGiveGet(toGive);
                    poorTaskRecord.setTempToGiveGet(poorTaskRecord.getTempToGiveGet() - transfer);//need ot update this for later flush
                    if (toGive > 0) { //rich should stay for next poor
                        richTasks.add(richTaskRecord);
                    }
                    if (toGet == 0) { //this poor is finished
                        break;
                    }
                }
            }
        } else {
            for (DreamTaskRecord richTaskRecord : richTasks) {
                int toGive = (int) (1.0 * richTaskRecord.getTempToGiveGet() / originalSumGive * originalSumGet);
                AllocationTaskView richTask = richTaskRecord.getTask();
                while (true) {
                    if (poorTasks.size() == 0) {
                        System.err.println("Poor task not found");
                        break;
                    }
                    DreamTaskRecord poorTaskRecord = poorTasks.remove(0);
                    AllocationTaskView poorTask = poorTaskRecord.getTask();
                    int toGet = poorTaskRecord.getTempToGiveGet();

                    int transfer = Math.min(toGet, toGive);
                    logWriter.println(richTask + "-r>" + transfer + "->" + poorTask);
                    richTask.setResourceShare(richTask.getResourceShare() - transfer);
                    poorTask.setResourceShare(poorTask.getResourceShare() + transfer);
                    sumGet.add(-transfer);
                    sumGive.add(-transfer);
                    toGet -= transfer;
                    toGive -= transfer;
                    poorTaskRecord.setTempToGiveGet(toGet);
                    richTaskRecord.setTempToGiveGet(richTaskRecord.getTempToGiveGet() - transfer);//need for flush as it may not be used up because of int
                    if (toGet > 0) {//this poor should stay
                        poorTasks.add(poorTaskRecord);
                    }
                    if (toGive == 0) {
                        break; //this rich is finished
                    }
                }
            }
        }
    }
}
