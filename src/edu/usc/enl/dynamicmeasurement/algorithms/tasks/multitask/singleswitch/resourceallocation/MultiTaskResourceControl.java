package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.TaskEventPublisher;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 7/30/13
 * Time: 12:12 PM <br/>
 * The interface for resource allocator on a single switch
 */
public abstract class MultiTaskResourceControl {
    protected TaskEventPublisher eventPublisher;

    public abstract void allocate();

    public abstract boolean addTask(AllocationTaskView task);

    public abstract void removeTask(AllocationTaskView task);

    public void setEventPublisher(TaskEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        //eventPublisher.subscribe(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public void setLogWriter(ControlledBufferWriter log) {

    }

}
