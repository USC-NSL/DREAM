package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;
import edu.usc.enl.dynamicmeasurement.util.IntegerWrapper;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 9:25 PM
 */
public class AbsolutePriorityResourceDistributor extends AbstractResourceDistributor {


    public AbsolutePriorityResourceDistributor(ThresholdGuaranteeAlgorithm2 algorithm) {
        super(algorithm);
    }

    @Override
    public void distribute(List<DreamTaskRecord> richTasks, List<DreamTaskRecord> poorTasks) {
        for (DreamTaskRecord richTask : richTasks) {
            richTask.setTempToGiveGet(richTask.getReductionStep2());
        }

        for (DreamTaskRecord poorTask : poorTasks) {
            poorTask.setTempToGiveGet(poorTask.getAdditionStep2());
        }

        //sort poor and riches
        Collections.sort(richTasks);
        Collections.sort(poorTasks, Collections.reverseOrder());
        DummyTaskRecord dummyTaskRecord = algorithm.getDummyTaskRecord();
        if (dummyTaskRecord.isRich()) {
            richTasks.add(0, dummyTaskRecord);
        }
        Iterator<DreamTaskRecord> richIterator = richTasks.iterator();
        ListIterator<DreamTaskRecord> poorIterator = poorTasks.listIterator(poorTasks.size());
        Set<DreamTaskRecord> sufferedWeakPoor = new HashSet<>();
        for (DreamTaskRecord poorTask : poorTasks) {
            if (sufferedWeakPoor.contains(poorTask)) {
                break;
            }
//            if (poorTask.getAdditionStep() == 4046 && poorTask.getReductionStep() == 16384 && poorTask.getTask().getTask().getName().equals("3")
//                    && poorTask.getTask().getResourceShare() == 100) {
//                System.out.println();
//            }
            //take resource from riches
            int toGet = poorTask.getTempToGiveGet();
            {
                DreamTaskRecord rich = null;
                while (toGet > 0 && (richIterator.hasNext() || rich != null)) {
                    if (rich == null) {
                        rich = richIterator.next();
                    }
                    int toGive = rich.getTempToGiveGet();
                    int transfer = Math.min(toGet, toGive);
                    toGive -= transfer;
                    toGet -= transfer;
                    transfer(poorTask, rich, transfer);
                    if (toGive == 0) {
                        rich = null;
                    }
                }
            }
            while (poorIterator.hasPrevious() && toGet > 0) {//now go through poors in reverse order!!
                DreamTaskRecord weakPoor = poorIterator.previous();
                if (weakPoor.equals(poorTask)) {
                    break; //ok I'm the weakest poor!
                }
                int resourceShare = weakPoor.getTask().getResourceShare();
                int minResource = algorithm.getMinResource();
                if (resourceShare > minResource) {
                    sufferedWeakPoor.add(weakPoor);
                    int transfer = Math.min(toGet, resourceShare - minResource);
                    toGet -= transfer;
                    transfer(poorTask, weakPoor, transfer);
                }
                if (weakPoor.getTask().getResourceShare() > minResource) {
                    poorIterator.next();//this weak poor still should suffer!
                }

            }
            poorTask.setTempToGiveGet(toGet);
            if (toGet > 0) {
                break;
            }
        }
        if (dummyTaskRecord.isRich()) {
            WeightedResourceDistributorNoDummy.giveToAll(new IntegerWrapper(dummyTaskRecord.getReductionStep2()), algorithm);
        }
    }

    private void transfer(DreamTaskRecord poorTask, DreamTaskRecord weakPoor, int transfer) {

        algorithm.getLogWriter().println(weakPoor.getTask() + "-f>" + transfer + "-f>" + poorTask.getTask());
        weakPoor.getTask().setResourceShare(weakPoor.getTask().getResourceShare() - transfer);
        poorTask.getTask().setResourceShare(poorTask.getTask().getResourceShare() + transfer);
        weakPoor.setTempToGiveGet(weakPoor.getTempToGiveGet() - transfer);
    }


}
