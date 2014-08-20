package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.globaldrop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/27/13
 * Time: 6:44 AM <br/>
 * Represents a global drop policy
 */
public interface GlobalDrop {
    /**
     * forcefully remove this task from its data-structure (may be the task left)
     *
     * @param multiSwitchTask
     */
    public void doRemove(MultiSwitchTask multiSwitchTask);

    /**
     * Runs the global drop algorithm to decide which tasks to drop
     */
    public void globalDrop();

    void update();
}
