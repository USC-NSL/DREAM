package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.step;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/26/13
 * Time: 3:21 PM <br/>
 * This has the skeleton of the algorithm. Implement methods for a specific policy
 */
public abstract class RichPoorStepUpdater extends StepUpdater {
    public void updateStep(Collection<DreamTaskRecord> tasks) {
        prepare();
        for (DreamTaskRecord taskRecord : tasks) {
            if (taskRecord.getDelayedResourceChange() != null) {
                continue;
            }
            boolean amRich = taskRecord.amRich();
            boolean amPoor = taskRecord.amPoor();
            runForTask(taskRecord, amRich, amPoor);
        }
        finishing();
    }

    public void runForTask(DreamTaskRecord taskRecord, boolean amRich, boolean amPoor) {
        AllocationTaskView task = taskRecord.getTask();
        boolean wasRich = taskRecord.wasRich();
        boolean wasPoor = taskRecord.wasPoor();
        int change = taskRecord.getLastResourceChange();
        PrepareForTask(task);
        boolean wasMiddle = !wasPoor && !wasRich;
        boolean isMiddle = !amPoor && !amRich;
        if (wasPoor) {
            if (amPoor) {
                if (change >= 0) {
                    PoorGotPoor(taskRecord, change);
                } else {
                    PoorGavePoor(taskRecord, -change);
                }
            } else if (amRich) {
                if (change >= 0) {
                    PoorGotRich(taskRecord, change);
                } else {
                    PoorGaveRich(taskRecord, -change);
                }
            } else {//if (isMiddle){
                if (change >= 0) {
                    PoorGotMiddle(taskRecord, change);
                } else {
                    PoorGaveMiddle(taskRecord, -change);
                }
            }
        }
        if (wasRich) {
            if (amPoor) {
                if (change >= 0) {
                    RichGotPoor(taskRecord, change);
                } else {
                    RichGavePoor(taskRecord, -change);
                }
            } else if (amRich) {
                if (change >= 0) {
                    RichGotRich(taskRecord, change);
                } else {
                    RichGaveRich(taskRecord, -change);
                }
            } else {// if (isMiddle){
                if (change >= 0) {
                    RichGotMiddle(taskRecord, change);
                } else {
                    RichGaveMiddle(taskRecord, -change);
                }
            }
        }
        if (wasMiddle) {
            if (amPoor) {
                if (change >= 0) {
                    MiddleGotPoor(taskRecord, change);
                } else {
                    MiddleGavePoor(taskRecord, -change);
                }
            } else if (amRich) {
                if (change >= 0) {
                    MiddleGotRich(taskRecord, change);
                } else {
                    MiddleGaveRich(taskRecord, -change);
                }
            } else {//if (isMiddle){
                if (change >= 0) {
                    MiddleGotMiddle(taskRecord, change);
                } else {
                    MiddleGaveMiddle(taskRecord, -change);
                }
            }
        }
        finishForTask(taskRecord);
    }

    protected abstract void finishForTask(DreamTaskRecord taskRecord);

    protected abstract void MiddleGavePoor(DreamTaskRecord taskRecord, int change);

    protected abstract void MiddleGotMiddle(DreamTaskRecord taskRecord, int change);

    protected abstract void MiddleGotRich(DreamTaskRecord taskRecord, int change);

    protected abstract void MiddleGaveRich(DreamTaskRecord taskRecord, int change);

    protected abstract void MiddleGaveMiddle(DreamTaskRecord taskRecord, int change);

    protected abstract void MiddleGotPoor(DreamTaskRecord taskRecord, int change);



   /* @Override
    public void updateStep(Collection<DreamTaskRecord> tasks) {
        prepare();
        for (DreamTaskRecord taskRecord : tasks) {
            if (taskRecord.isDropped()) {
                continue;
            }
            AllocationTaskView task = taskRecord.getTask();
            boolean amRich = taskRecord.amRich();
            boolean wasRich = taskRecord.wasRich();
            int change = taskRecord.getLastResourceChange();
            PrepareForTask(task);
            if (amRich) {
                if (wasRich) {
                    if (change < 0) {
                        //was rich gave and now rich again
                        RichGaveRich(taskRecord);
                    } else {
                        //was rich didn't give or even got (dummy or drop/leave) and now rich
                        //keep old offer
                        RichGotRich(taskRecord, change);
                    }
                } else {
                    if (change > 0) {
                        //was poor, got and now rich
                        PoorGotRich(taskRecord, change);
                    } else {
                        //was poor, didn't get and now rich
                        //offer the same as request
                        PoorGaveRich(taskRecord);
                    }
                }
            }

            boolean amPoor = taskRecord.amPoor();
            boolean wasPoor = taskRecord.wasPoor();
            if (amPoor) {
                if (wasPoor) {
                    if (change > 0) {
                        //was poor got and poor again
                        PoorGotPoor(taskRecord);
                    } else {
                        //was poor did not get and poor
                        PoorGavePoor(taskRecord, change);
                    }
                } else {
                    if (change >= 0) {
                        //was rich, didn't give and poor now
                        //so request whatever I wanted to give
                        RichGotPoor(taskRecord, task);

                    } else {
                        //was rich, gave but now poor
                        RichGavePoor(taskRecord, change);
                    }
                }
            }

            //just in case if you want to update something
            if (!amRich && !amPoor) {
                if (wasPoor) {
                    if (change > 0) {
                        PoorGotMiddle(taskRecord, change);
                    } else {
                        PoorGaveMiddle(taskRecord, change);
                    }
                }
                if (wasRich) {
                    if (change < 0) {
                        RichGaveMiddle(taskRecord, change);
                    } else {
                        RichGotMiddle(taskRecord, change);
                    }
                }
            }
            taskRecord.setWasPoor(amPoor);
            taskRecord.setWasRich(amRich);
        }

        finishing();
    }*/

    protected abstract void RichGotMiddle(DreamTaskRecord taskRecord, int change);

    protected abstract void PoorGaveMiddle(DreamTaskRecord taskRecord, int change);

    protected abstract void PrepareForTask(AllocationTaskView task);

    protected abstract void prepare();

    protected abstract void finishing();

    protected abstract void RichGaveMiddle(DreamTaskRecord taskRecord, int change);

    protected abstract void PoorGotMiddle(DreamTaskRecord taskRecord, int change);

    protected abstract void RichGavePoor(DreamTaskRecord taskRecord, int change);

    protected abstract void RichGotPoor(DreamTaskRecord taskRecord, int change);

    protected abstract void PoorGavePoor(DreamTaskRecord taskRecord, int change);

    protected abstract void PoorGotPoor(DreamTaskRecord taskRecord, int change);

    protected abstract void PoorGaveRich(DreamTaskRecord taskRecord, int change);

    protected abstract void PoorGotRich(DreamTaskRecord taskRecord, int change);

    protected abstract void RichGotRich(DreamTaskRecord taskRecord, int change);

    protected abstract void RichGaveRich(DreamTaskRecord taskRecord, int change);
}
