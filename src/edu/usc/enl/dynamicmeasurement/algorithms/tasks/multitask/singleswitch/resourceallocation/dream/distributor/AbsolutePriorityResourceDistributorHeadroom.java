package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyAllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/26/14
 * Time: 8:35 AM
 */
public class AbsolutePriorityResourceDistributorHeadroom extends AbstractResourceDistributor {
    private WeightedResourceDistributor weightedResourceDistributor;
    private PriorityResourceDistributorOnlyPoor priorityPolicy;

    public AbsolutePriorityResourceDistributorHeadroom(ThresholdGuaranteeAlgorithm2 algorithm) {
        super(algorithm);
        weightedResourceDistributor = new WeightedResourceDistributorNoDummy(algorithm);
        priorityPolicy = new PriorityResourceDistributorOnlyPoor(algorithm);
    }

    @Override
    public void distribute(List<DreamTaskRecord> richTasks, List<DreamTaskRecord> poorTasks) {
        DummyHeadroomTaskRecord dummyTaskRecord = (DummyHeadroomTaskRecord) algorithm.getDummyTaskRecord();
        dummyTaskRecord.setFirstTry(true);
        dummyTaskRecord.setTempToGiveGet(dummyTaskRecord.getReductionStep2());
        weightedResourceDistributor.distribute(richTasks, poorTasks);
        dummyTaskRecord.setFirstTry(false);
        dummyTaskRecord.setAmPoor(!dummyTaskRecord.isRich());
        dummyTaskRecord.setAmRich(dummyTaskRecord.isRich());
        int additionStep = algorithm.getHeadroom() - dummyTaskRecord.getTask().getResourceShare();
        dummyTaskRecord.setAdditionStep(additionStep);
        if (!dummyTaskRecord.isRich()) {
            dummyTaskRecord.setTempToGiveGet(additionStep); //it will be suffering poor or get what it want
            poorTasks.add(dummyTaskRecord);
        } else {
            dummyTaskRecord.setTempToGiveGet(dummyTaskRecord.getReductionStep2());
        }
        priorityPolicy.distribute(richTasks, poorTasks);

        // if dummy>headroom
        //      first try to satisfy poor with dummy using their step until headroom value


        // distribute resource from rich to poor
        // if any poor is unsatisfied let it use others resources starting from dummy
    }

    @Override
    public DummyTaskRecord getDummy() {

        return new DummyHeadroomTaskRecord(new DummyAllocationTaskView(), algorithm);
    }

    public static class DummyHeadroomTaskRecord extends DummyTaskRecord {
        private boolean firstTry = false;

        public DummyHeadroomTaskRecord(AllocationTaskView task, ThresholdGuaranteeAlgorithm2 algorithm) {
            super(task, algorithm);
        }

        public boolean isFirstTry() {
            return firstTry;
        }

        public void setFirstTry(boolean firstTry) {
            this.firstTry = firstTry;
        }

        @Override
        public int getTempToGiveGet() {
            return tempToGive;
        }

        @Override
        public int getReductionStep2() {
            if (firstTry) {
                return Math.max(0, task.getResourceShare() - algorithm.getHeadroom());
            } else {
                return super.getReductionStep2();
            }
        }

        @Override
        public boolean isRich() {
            return algorithm.getHeadroom() < task.getResourceShare();
        }
    }
}
