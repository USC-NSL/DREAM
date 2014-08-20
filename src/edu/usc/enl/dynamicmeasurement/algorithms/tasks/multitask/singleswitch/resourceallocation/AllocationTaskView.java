package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/20/13
 * Time: 7:20 PM <br/>
 * This is the view that the allocation algorithm needs from a task.
 * Each view represents the task on a specific switch
 */
public interface AllocationTaskView {
    /**
     * @return how much resourses the task has
     */
    public int getResourceShare();

    /**
     * Sets how much resources the task must use
     *
     * @param c
     */
    public void setResourceShare(int c);

    public double getAggregatedAccuracy();

    public Task2 getTask();

    /**
     * @return the instantaneous accuracy of the task (use it cautiously, instead try to use the aggregated accuracy)
     */
    public double getAccuracy2();

    /**
     * @return how much of the allocated resources were actually used
     */
    public int getUsedResourceShare();
}
