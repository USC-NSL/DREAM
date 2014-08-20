package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.step;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/6/13
 * Time: 7:31 PM
 */
public class LinearStepUpdater extends RichPoorStepUpdater {
    private int minResourceChange;

    @Override
    public void init(int probAdd, int probDiv, ThresholdGuaranteeAlgorithm2 algorithm) {
        super.init(probAdd, probDiv, algorithm);
        minResourceChange = 2 * algorithm.getMinResource();
    }

    @Override
    protected void finishForTask(DreamTaskRecord taskRecord) {

    }

    @Override
    protected void MiddleGavePoor(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setAdditionStep(computeLinearChange(taskRecord, -change));
        } catch (IndecisiveResultException e) {
            taskRecord.setAdditionStep(minResourceChange);
        }
    }

    @Override
    protected void MiddleGotMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(0);
        taskRecord.setAdditionStep(0);
    }

    @Override
    protected void MiddleGotRich(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setReductionStep(computeLinearChange(taskRecord, change));
        } catch (IndecisiveResultException e) {
            taskRecord.setReductionStep(minResourceChange);
        }
    }

    @Override
    protected void MiddleGaveRich(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setReductionStep(computeLinearChange(taskRecord, -change));
        } catch (IndecisiveResultException e) {
            taskRecord.setReductionStep(minResourceChange);
        }
    }

    @Override
    protected void MiddleGaveMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(0);
        taskRecord.setAdditionStep(0);
    }

    @Override
    protected void MiddleGotPoor(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setAdditionStep(computeLinearChange(taskRecord, change));
        } catch (IndecisiveResultException e) {
            taskRecord.setAdditionStep(minResourceChange);
        }
    }

    @Override
    protected void RichGotMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(0);
        taskRecord.setAdditionStep(0);
    }

    @Override
    protected void PoorGaveMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(0);
        taskRecord.setAdditionStep(0);
    }

    @Override
    protected void PrepareForTask(AllocationTaskView task) {

    }

    @Override
    protected void prepare() {

    }

    @Override
    protected void finishing() {

    }

    @Override
    protected void RichGaveMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(0);
        taskRecord.setAdditionStep(0);
    }

    @Override
    protected void PoorGotMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(0);
        taskRecord.setAdditionStep(0);
    }

    @Override
    protected void RichGavePoor(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setReductionStep(computeLinearChange(taskRecord, -change));
        } catch (IndecisiveResultException e) {
            taskRecord.setAdditionStep(minResourceChange);
        }
    }

    @Override
    protected void RichGotPoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(minResourceChange);
    }

    @Override
    protected void PoorGavePoor(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setReductionStep(computeLinearChange(taskRecord, -change));
        } catch (IndecisiveResultException e) {
            taskRecord.setAdditionStep(minResourceChange);
        }
    }

    @Override
    protected void PoorGotPoor(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setAdditionStep(computeLinearChange(taskRecord, change));
        } catch (IndecisiveResultException e) {
            taskRecord.setAdditionStep(minResourceChange);
        }
    }

    private int computeLinearChange(DreamTaskRecord taskRecord, int change) throws IndecisiveResultException {
        double x2 = taskRecord.getTask().getResourceShare();
        double y2 = taskRecord.getTask().getAggregatedAccuracy();
        double x1 = x2 - change;
        double y1 = taskRecord.getLastAccuracy();
        if (change == 0 || y1 == y2) {
            throw new IndecisiveResultException();
        }
        double targetY = algorithm.getLowThreshold();
        return (int) ((targetY - y2) * (x1 - x2) / (y1 - y2));
    }

    @Override
    protected void PoorGaveRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(minResourceChange);
    }

    @Override
    protected void PoorGotRich(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setReductionStep(computeLinearChange(taskRecord, change));
        } catch (IndecisiveResultException e) {
            taskRecord.setReductionStep(minResourceChange);
        }
    }

    @Override
    protected void RichGotRich(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setReductionStep(computeLinearChange(taskRecord, change));
        } catch (IndecisiveResultException e) {
            taskRecord.setReductionStep(minResourceChange);
        }
    }

    @Override
    protected void RichGaveRich(DreamTaskRecord taskRecord, int change) {
        try {
            taskRecord.setReductionStep(computeLinearChange(taskRecord, -change));
        } catch (IndecisiveResultException e) {
            taskRecord.setReductionStep(minResourceChange);
        }
    }

    private class IndecisiveResultException extends Throwable {
    }
}
