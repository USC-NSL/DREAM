package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.step;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/26/13
 * Time: 4:20 PM
 * ------------------------------------------------------------------------------------------------------------<br/>
 * <pre>
 * Old \ New |Poor                      |Rich                      |Middle
 * Change    |>=0         |< 0          |>=0          |< 0         |>=0                |< 0
 * Poor      |A+=c        |A+=c         |R-=mc/R      |R=max(A,c) -|A=A-mc/A,R=A-mc/A  |A=c,R=max(A,c)
 * Rich      |A=max(R,c)  |A-=mc/A      |R+=c         |R+=c        |A=max(R,c),R=c     |A=R-mc/R,R=R-mc/R
 * Middle    |A=max(R,c)  |A+=c         |R+=c         |R=max(A,c)  |-                  |-
 * c can be summed over delayed ones
 * </pre>
 */
public class MIADStepUpdater extends AIMDStepUpdater {
    @Override
    protected void RichGavePoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getReductionStep() - probAdd * change / taskRecord.getReductionStep());
    }

    @Override
    protected void PoorGotRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(taskRecord.getAdditionStep() - probAdd * change / taskRecord.getAdditionStep());
    }

    @Override
    protected void PoorGotMiddle(DreamTaskRecord taskRecord, int change) {
        int v = taskRecord.getAdditionStep() - probAdd * change / taskRecord.getAdditionStep();
        taskRecord.setAdditionStep(v);
        taskRecord.setReductionStep(v);
    }

    @Override
    protected void RichGaveMiddle(DreamTaskRecord taskRecord, int change) {
        int v = taskRecord.getReductionStep() - probAdd * change / taskRecord.getReductionStep();
        taskRecord.setAdditionStep(v);
        taskRecord.setReductionStep(v);
    }

    protected void PoorGotPoor(DreamTaskRecord taskRecord, int change) {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change);
    }

    protected void RichGaveRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(taskRecord.getReductionStep() + change);
    }
}
