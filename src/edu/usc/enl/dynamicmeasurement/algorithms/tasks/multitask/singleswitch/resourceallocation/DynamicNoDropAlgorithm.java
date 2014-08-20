package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation;

import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/1/13
 * Time: 12:50 PM <br/>
 * Equal allocation without drop
 */
public class DynamicNoDropAlgorithm extends MultiTaskResourceControl {
    private final int maxResource;
    private List<TaskRecord2> taskRecords;

    public DynamicNoDropAlgorithm(Element element, MonitorPoint monitorPoint) {
        maxResource = monitorPoint.getCapacity();
        taskRecords = new LinkedList<>();
    }

    @Override
    public void allocate() {
        if (taskRecords.size() > 0) {
            int share = maxResource / taskRecords.size();
            for (TaskRecord2 taskRecord : taskRecords) {
                taskRecord.getTask().setResourceShare(share);
            }
        }
    }

    @Override
    public boolean addTask(AllocationTaskView task) {
        taskRecords.add(new TaskRecord2(task, -taskRecords.size()));
        int resourceShare = maxResource / taskRecords.size();
        for (TaskRecord2 task1 : taskRecords) {
            task1.getTask().setResourceShare(resourceShare);
        }
        return true;
    }

    @Override
    public void removeTask(AllocationTaskView task) {
        for (Iterator<TaskRecord2> iterator = taskRecords.iterator(); iterator.hasNext(); ) {
            TaskRecord2 next = iterator.next();
            if (next.getTask().equals(task)) {
                iterator.remove();
                break;
            }
        }
        for (TaskRecord2 taskRecord : taskRecords) {
            taskRecord.getTask().setResourceShare(maxResource / taskRecords.size());
        }
        task.setResourceShare(0);
    }

}
