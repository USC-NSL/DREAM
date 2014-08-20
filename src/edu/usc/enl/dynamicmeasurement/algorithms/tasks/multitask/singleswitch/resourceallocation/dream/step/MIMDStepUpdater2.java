package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.step;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/7/13
 * Time: 10:37 PM
 * * ------------------------------------------------------------------------------------------------------------<br/>
 * <pre>
 * Old \ New |Poor                         |Rich                          |Middle
 * Change    |>=0          |< 0            |>=0            |< 0           |>=0          |< 0
 * Poor      |A+=c,R+=c/2  |A=A,R-=c       |A-=c/2,R+=c/2  |A+=c/2,R=R    |A=A,R=R      |A=A,R=R
 * Rich      |A=A,R+=c/2   |A+=c/2,R-=c/2  |A-=c,R=R       |A+=c/2,R+=c   |A=A,R=R      |A=A,R=R
 * Middle    |A=A,R+=c/2   |A+=c/2,R-=c/2  |A-=c/2,R+=c/2  |A+=c/2,R=R    |A=A,R=R      |A=A,R=R
 * c can be summed over delayed ones
 * </pre>
 */
public class MIMDStepUpdater2 extends RichPoorStepUpdater {
    @Override
    protected void finishForTask(DreamTaskRecord taskRecord) {

    }

    @Override
    protected void MiddleGavePoor(DreamTaskRecord taskRecord, int change) {
        RichGavePoor(taskRecord, change);
    }

    @Override
    protected void MiddleGotMiddle(DreamTaskRecord taskRecord, int change) {

    }

    @Override
    protected void MiddleGotRich(DreamTaskRecord taskRecord, int change) {
        PoorGotRich(taskRecord, change);
    }

    @Override
    protected void MiddleGaveRich(DreamTaskRecord taskRecord, int change) {
        PoorGaveRich(taskRecord, change);
    }

    @Override
    protected void MiddleGaveMiddle(DreamTaskRecord taskRecord, int change) {

    }

    @Override
    protected void MiddleGotPoor(DreamTaskRecord taskRecord, int change) {
        RichGotPoor(taskRecord, change);
    }

    @Override
    protected void RichGotMiddle(DreamTaskRecord taskRecord, int change) {

    }

    @Override
    protected void PoorGaveMiddle(DreamTaskRecord taskRecord, int change) {

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

    }

    @Override
    protected void PoorGotMiddle(DreamTaskRecord taskRecord, int change) {

    }

    @Override
    protected void RichGavePoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change / 2);
        taskRecord.setReductionStep(taskRecord.getReductionStep() - change / 2);
    }

    @Override
    protected void RichGotPoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep());
        taskRecord.setReductionStep(taskRecord.getReductionStep() + change / 2);
    }

    @Override
    protected void PoorGavePoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep());
        taskRecord.setReductionStep(taskRecord.getReductionStep() - change);
    }

    @Override
    protected void PoorGotPoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change);
        taskRecord.setReductionStep(taskRecord.getReductionStep() + change / 2);
    }

    @Override
    protected void PoorGaveRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change / 2);
        taskRecord.setReductionStep(taskRecord.getReductionStep());
    }

    @Override
    protected void PoorGotRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() - change / 2);
        taskRecord.setReductionStep(taskRecord.getReductionStep() + change / 2);
    }

    @Override
    protected void RichGotRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() - change);
        taskRecord.setReductionStep(taskRecord.getReductionStep());
    }

    @Override
    protected void RichGaveRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change / 2);
        taskRecord.setReductionStep(taskRecord.getReductionStep() + change);
    }
}
