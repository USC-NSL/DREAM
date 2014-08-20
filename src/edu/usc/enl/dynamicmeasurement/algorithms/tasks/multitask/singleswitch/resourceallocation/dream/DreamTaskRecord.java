package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.TaskRecord2;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 9:17 PM  <br/>
 * Keeps track of the information that DREAM resource allocator needs for each task.
 */
public class DreamTaskRecord extends TaskRecord2 {
    protected int reductionStep = 1;
    protected int additionStep = 1;
    protected ThresholdGuaranteeAlgorithm2 algorithm;
    private boolean wasPoor = true;
    private boolean wasRich = false;
    private boolean amPoor = true;
    private boolean amRich = false;
    private int lastResourceChange = 0;
    protected int tempToGive = 0;
    private int poorDrop = 0;
    private Integer delayedResourceChange = null;
    private double lastAccuracy;

    public DreamTaskRecord(AllocationTaskView task, int dropPriority, ThresholdGuaranteeAlgorithm2 algorithm) {
        super(task, dropPriority);
        this.algorithm = algorithm;
    }

    public double getLastAccuracy() {
        return lastAccuracy;
    }

    public void setLastAccuracy(double lastAccuracy) {
        this.lastAccuracy = lastAccuracy;
    }

    public Integer getDelayedResourceChange() {
        return delayedResourceChange;
    }

    public void setDelayedResourceChange(Integer delayedResourceChange) {
        this.delayedResourceChange = delayedResourceChange;
    }

    public int getTempToGiveGet() {
        return tempToGive;
    }

    public void setTempToGiveGet(int tempToGive) {
        this.tempToGive = tempToGive;
    }

    public int getLastResourceChange() {
        return lastResourceChange;
    }

    public void setLastResourceChange(int lastResourceChange) {
        this.lastResourceChange = lastResourceChange;
    }

    public int getReductionStep() {
        return reductionStep;
    }

    public void setReductionStep(int reductionStep) {
        this.reductionStep = Math.min(task.getResourceShare(), Math.max(algorithm.getMinResource(), reductionStep));
    }

    public int getAdditionStep() {
        return additionStep;
    }

    public void setAdditionStep(int additionStep) {
        this.additionStep = Math.min(algorithm.getMaxResource(), Math.max(algorithm.getMinResource(), additionStep));
    }

    @Override
    public String toString() {
        return "TaskRecord{" +
                "task=" + task +
                "(" + task.getAccuracy2() + ")" +
                ", reductionStep=" + reductionStep +
                ", additionStep=" + additionStep +
                ", Reschange=" + lastResourceChange +
                "(" + delayedResourceChange + ")" +
                ", wRaR=" + wasRich + "," + amRich +
                ", wPaP=" + wasPoor + "," + amPoor + '}';
    }

    public boolean wasPoor() {
        return wasPoor;
    }

    public void setWasPoor(boolean wasPoor) {
        this.wasPoor = wasPoor;
    }

    public boolean wasRich() {
        return wasRich;
    }

    public void setWasRich(boolean wasRich) {
        this.wasRich = wasRich;
    }

    public int getAdditionStep2() {
        return Math.min(additionStep, algorithm.getMaxResource() - task.getResourceShare());
    }

    public int getReductionStep2() {
        return Math.min(reductionStep, task.getResourceShare() - algorithm.getMinResource());
    }

    public boolean canOfferNewComer() {
        return task.getResourceShare() >= 2 * algorithm.getMinResource();
    }

    public void incPoorDropped() {
        poorDrop++;
    }

    public void resetPoorDropped() {
        poorDrop = 0;

    }

    public int getPoorDropped() {
        return poorDrop;
    }

    public boolean amPoor() {
        return amPoor;
    }

    public boolean amRich() {
        return amRich;
    }

    public void updateState(ThresholdGuaranteeAlgorithm2 allocator) {
        amRich = findAmRich(allocator);
        amPoor = findAmPoor(allocator); //&& task.getResourceShare() < maxResource;
        //amPoor = amPoor && task.getUsedResourceShare() >= resourceShare;
    }

    public void setAmPoor(boolean amPoor) {
        this.amPoor = amPoor;
    }

    public void setAmRich(boolean amRich) {
        this.amRich = amRich;
    }

    boolean findAmPoor(ThresholdGuaranteeAlgorithm2 allocator) {
        double accuracy = task.getAggregatedAccuracy();
        return accuracy < allocator.getLowThreshold();
    }

    boolean findAmRich(ThresholdGuaranteeAlgorithm2 allocator) {
        double accuracy = task.getAggregatedAccuracy();
        int resourceShare = task.getResourceShare();
        return accuracy > allocator.getHighThreshold() && resourceShare > allocator.getMinResource();
    }

//    boolean findAmPoor(ThresholdGuaranteeAlgorithm2 allocator) {
//        double accuracy = task.getAggregatedAccuracy();
//        if (wasPoor) return accuracy < allocator.getHighThreshold();
//        else {
//            return accuracy < allocator.getLowThreshold();
//        }
//    }
//
//    boolean findAmRich(ThresholdGuaranteeAlgorithm2 allocator) {
//        return !findAmPoor(allocator);
//    }

    public boolean notUsingAll() {
        return task.getUsedResourceShare() < task.getResourceShare();
    }

    //    public static class TaskPriorityComparator implements Comparator<DreamTaskRecord> {
//        @Override
//        public int compare(DreamTaskRecord o1, DreamTaskRecord o2) {
//            return o1.getDropPriority() - o2.getDropPriority();
//        }
//    }
}
