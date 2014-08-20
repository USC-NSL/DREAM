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
 * Change    |>=0         |< 0          |>=0          |< 0         |>=0              |< 0
 * Poor      |A+=c        |             |             |           -|                 |
 * Rich      |            |             |             |R+=c        |                 |
 * Middle    |            |             |             |            |-                |-
 * c can be summed over delayed ones
 * </pre>
 */
public class MIMDStepUpdater extends AIMDStepUpdater {

    protected void PoorGotPoor(DreamTaskRecord taskRecord, int change) {
//        if (taskRecord.notUsingAll()) {
//            taskRecord.setAdditionStep(Math.max(taskRecord.getAdditionStep(), change));
//        } else {
        taskRecord.setAdditionStep(taskRecord.getAdditionStep() + change);
//        }
    }

    protected void RichGaveRich(DreamTaskRecord taskRecord, int change) {
        taskRecord.setReductionStep(taskRecord.getReductionStep() + change);
    }
}
