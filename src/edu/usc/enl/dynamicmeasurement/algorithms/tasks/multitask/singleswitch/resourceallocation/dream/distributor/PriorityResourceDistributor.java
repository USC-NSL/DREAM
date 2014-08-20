package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;
import edu.usc.enl.dynamicmeasurement.util.IntegerWrapper;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 9:25 PM
 */
public class PriorityResourceDistributor extends AbstractResourceDistributor {


    public PriorityResourceDistributor(ThresholdGuaranteeAlgorithm2 algorithm) {
        super(algorithm);
    }

    @Override
    public void distribute(List<DreamTaskRecord> richTasks, List<DreamTaskRecord> poorTasks) {
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

        Collections.sort(richTasks);
        Collections.sort(poorTasks, Collections.reverseOrder());

        DummyTaskRecord dummyTaskRecord = algorithm.getDummyTaskRecord();
        if (dummyTaskRecord.isRich()) {
            richTasks.add(0, dummyTaskRecord);
        }
        Iterator<DreamTaskRecord> richIterator = richTasks.iterator();
        for (DreamTaskRecord poorTask : poorTasks) {
            int toGet = poorTask.getTempToGiveGet();
            DreamTaskRecord rich = null;
            while (toGet > 0 && (richIterator.hasNext() || rich != null)) {
                if (rich == null) {
                    rich = richIterator.next();
                }
                int toGive = rich.getTempToGiveGet();
                int transfer = Math.min(toGet, toGive);
                toGive -= transfer;
                toGet -= transfer;
                rich.getTask().setResourceShare(rich.getTask().getResourceShare() - transfer);
                rich.setTempToGiveGet(rich.getTempToGiveGet() - transfer);
                poorTask.getTask().setResourceShare(poorTask.getTask().getResourceShare() + transfer);
                System.out.println(rich.getTask() + "->" + transfer + "->" + poorTask.getTask());
                if (toGive == 0) {
                    rich = null;
                }
            }
            poorTask.setTempToGiveGet(toGet);
        }
    }

}
