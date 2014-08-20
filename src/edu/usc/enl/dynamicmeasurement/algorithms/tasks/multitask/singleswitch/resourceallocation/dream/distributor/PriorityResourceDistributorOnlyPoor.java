package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.TaskRecord2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/26/14
 * Time: 10:05 AM
 */
public class PriorityResourceDistributorOnlyPoor extends AbstractResourceDistributor {
    protected PriorityResourceDistributorOnlyPoor(ThresholdGuaranteeAlgorithm2 algorithm) {
        super(algorithm);
    }

    @Override
    public void distribute(List<DreamTaskRecord> richTasks, List<DreamTaskRecord> poorTasks) {
        //sort poor and riches
        Collections.sort(richTasks);
        Collections.sort(poorTasks, Collections.reverseOrder());
        DummyTaskRecord dummyTaskRecord = algorithm.getDummyTaskRecord();
        if (dummyTaskRecord.isRich()) {
            richTasks.add(0, dummyTaskRecord);
        }
        Iterator<DreamTaskRecord> richIterator = richTasks.iterator();

        ArrayList<DreamTaskRecord> taskRecord2s = new ArrayList<DreamTaskRecord>();
        for (TaskRecord2 taskRecord2 : algorithm.getTasks()) {
            taskRecord2s.add((DreamTaskRecord) taskRecord2);
        }
        taskRecord2s.add(dummyTaskRecord);
        Collections.sort(taskRecord2s, Collections.reverseOrder());
        ListIterator<DreamTaskRecord> forcedGivingTasksIterator = taskRecord2s.listIterator(taskRecord2s.size());

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
            while (forcedGivingTasksIterator.hasPrevious() && toGet > 0) {//now go through poors in reverse order!!
                DreamTaskRecord weakTask = forcedGivingTasksIterator.previous();
                if (weakTask.equals(poorTask)) {
                    break; //ok I'm the weakest poor!
                }
                int resourceShare = weakTask.getTask().getResourceShare();
                int minResource = algorithm.getMinResource();
                if (weakTask instanceof DummyTaskRecord) {
                    minResource = 0;
                }
                if (resourceShare > minResource) {
                    sufferedWeakPoor.add(weakTask);
                    int transfer = Math.min(toGet, resourceShare - minResource);
                    toGet -= transfer;
                    transfer(poorTask, weakTask, transfer);
                }
                if (weakTask.getTask().getResourceShare() > minResource) {
                    forcedGivingTasksIterator.next();//this weak poor still should suffer!
                }

            }
            poorTask.setTempToGiveGet(toGet);
            if (toGet > 0) {
                break;
            }
        }
//        if (dummyTaskRecord.isRich()) {
//            WeightedResourceDistributorNoDummy.giveToAll(new IntegerWrapper(dummyTaskRecord.getReductionStep2()), algorithm);
//        }
    }

    private void transfer(DreamTaskRecord poorTask, DreamTaskRecord weakPoor, int transfer) {

        algorithm.getLogWriter().println(weakPoor.getTask() + "-f>" + transfer + "-f>" + poorTask.getTask());
        weakPoor.getTask().setResourceShare(weakPoor.getTask().getResourceShare() - transfer);
        poorTask.getTask().setResourceShare(poorTask.getTask().getResourceShare() + transfer);
        weakPoor.setTempToGiveGet(weakPoor.getTempToGiveGet() - transfer);
    }
}
