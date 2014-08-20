package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/13/13
 * Time: 10:49 PM
 */
public class TaskRecord2 implements Comparable<TaskRecord2> {
    protected final AllocationTaskView task;
    private final int dropPriority;

    public TaskRecord2(AllocationTaskView task, int dropPriority) {
        this.task = task;
        this.dropPriority = dropPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskRecord2 that = (TaskRecord2) o;

        if (task != null ? !task.equals(that.task) : that.task != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return task != null ? task.hashCode() : 0;
    }

    public int getDropPriority() {
        return dropPriority;
    }

    public AllocationTaskView getTask() {
        return task;
    }

    @Override
    public int compareTo(TaskRecord2 o) {
        int c = Integer.compare(dropPriority, o.dropPriority);
        if (c == 0) {
            return Integer.compare(task.hashCode(), o.task.hashCode());
        }
        return c;
    }
}
