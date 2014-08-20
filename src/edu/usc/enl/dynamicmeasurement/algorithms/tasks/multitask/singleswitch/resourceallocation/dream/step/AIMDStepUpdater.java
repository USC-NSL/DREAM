package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.step;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/26/13
 * Time: 4:20 PM
 * ------------------------------------------------------------------------------------------------------------<br/>
 * <pre>
 * Old \ New |Poor                      |Rich                      |Middle
 * Change    |>=0         |< 0          |>=0          |< 0         |>=0              |< 0
 * Poor      |A+=mc/A     |A+=c         |R=c/2        |R=max(A,c) -|A=c/2,R=c/2      |A=R=max(A,c)
 * Rich      |A=max(R,c)  |A=c/2        |R+=c         |R+=mc/A     |A=max(R,c)=R     |A=c/2,R=c/2
 * Middle    |A=max(R,c)  |A+=c         |R+=c         |R=max(A,c)  |-                |-
 * c can be summed over delayed ones
 * </pre>
 */
public class AIMDStepUpdater extends RichPoorStepUpdater {

    @Override
    protected void finishForTask(DreamTaskRecord taskRecord) {
    }

    @Override
    protected void MiddleGavePoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change);
    }

    @Override
    protected void MiddleGotMiddle(DreamTaskRecord taskRecord, int change) {
//        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change / 2);
//        taskRecord.setReductionStep(taskRecord.getReductionStep() + change);
    }

    @Override
    protected void MiddleGotRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(taskRecord.getReductionStep() + change);
    }

    @Override
    protected void MiddleGaveRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(Math.max(taskRecord.getAdditionStep(), change));
    }

    @Override
    protected void MiddleGaveMiddle(DreamTaskRecord taskRecord, int change) {
//        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change);
//        taskRecord.setReductionStep(taskRecord.getReductionStep() + change / 2);
    }

    @Override
    protected void MiddleGotPoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(Math.max(taskRecord.getReductionStep(), change));
    }

    @Override
    protected void RichGotMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(Math.max(taskRecord.getReductionStep(), change));
        taskRecord.setReductionStep(Math.max(taskRecord.getReductionStep(), change));

//        taskRecord.setReductionStep(change);
    }

    @Override
    protected void PoorGaveMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(Math.max(taskRecord.getAdditionStep(), change));
        taskRecord.setAdditionStep(Math.max(taskRecord.getAdditionStep(), change));

//        taskRecord.setAdditionStep(change);
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

    private int getInitReductionStep(DreamTaskRecord taskRecord) {
        int init = 10 * algorithm.getMinResource();
        int maxResource = algorithm.getMaxResource();
        int num = algorithm.getTasks().size();
        return Math.min(init, Math.max(0, taskRecord.getTask().getResourceShare() - maxResource / num));
    }

    private int getInitAdditionStep(DreamTaskRecord taskRecord) {
        int init = 10 * algorithm.getMinResource();
        int maxResource = algorithm.getMaxResource();
        int num = algorithm.getTasks().size();
        return Math.min(init, Math.max(0, maxResource / num - taskRecord.getTask().getResourceShare()));
    }

    @Override
    protected void RichGaveMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(change / 2);
        taskRecord.setReductionStep(change / 2);
    }

    @Override
    protected void PoorGotMiddle(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(change / 2);
        taskRecord.setReductionStep(change / 2);
    }

    @Override
    protected void RichGavePoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(change / 2);
    }

    @Override
    protected void RichGotPoor(DreamTaskRecord taskRecord, int change) {
        //this is for new tasks : With 0 outcome they usually have precision 100 then they find out it is 0
        if (taskRecord.getTask().getResourceShare() <= algorithm.getMinResource()) {
            taskRecord.setAdditionStep(getInitAdditionStep(taskRecord));
        } else {
            taskRecord.setAdditionStep(Math.max(taskRecord.getReductionStep(), change));//conservative
        }
    }

    @Override
    protected void PoorGavePoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change);
    }

    @Override
    protected void PoorGotPoor(DreamTaskRecord taskRecord, int change) {
//        if (taskRecord.notUsingAll()) {
//            taskRecord.setAdditionStep(Math.max(taskRecord.getAdditionStep(), change));
//        } else {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + probAdd * change / taskRecord.getAdditionStep());
//        }
    }

    @Override
    protected void PoorGaveRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(Math.max(taskRecord.getAdditionStep(), change));
//        taskRecord.setReductionStep(getInitReductionStep(taskRecord));
    }

    @Override
    protected void PoorGotRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(change / 2);
    }

    @Override
    protected void RichGotRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(taskRecord.getReductionStep() + change);
    }

    @Override
    protected void RichGaveRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(taskRecord.getReductionStep() + probAdd * change / taskRecord.getReductionStep());
    }
}
