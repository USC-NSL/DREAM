package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.TaskEventPublisher;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/1/13
 * Time: 12:50 PM
 */
public class FixedDropAlgorithm2 extends MultiTaskResourceControl {

    protected final double lowThreshold;
    protected final List<TaskRecord2> taskRecords;
    private final PriorityDropPolicy dropPolicy;
    private int resourceStep;
    private int freeCapacity;

    public FixedDropAlgorithm2(Element element, MonitorPoint monitorPoint) {
        taskRecords = new LinkedList<>();
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        this.lowThreshold = Double.parseDouble(properties.get("LowThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        int capacity = monitorPoint.getCapacity();
        resourceStep = (int) (capacity * Double.parseDouble(properties.get("ResourceStep").getAttribute(ConfigReader.PROPERTY_VALUE)));
        freeCapacity = capacity;
        int dropEpochs = Integer.parseInt(properties.get("DropEpochs").getAttribute(ConfigReader.PROPERTY_VALUE));
        dropPolicy = new PriorityDropPolicy(dropEpochs);
    }

    @Override
    public void allocate() {
        boolean notHelping = true;
        for (TaskRecord2 taskRecord : taskRecords) {
            AllocationTaskView task = taskRecord.getTask();
            double accuracy = task.getAggregatedAccuracy();
            if (accuracy < lowThreshold && ((TaskRecord) taskRecord).getFixedShare() < 0) {
                if (freeCapacity > 0) {
                    giveShare(task);
                    notHelping = false;
                } else {
                    break;
                }
            } else {
                ((TaskRecord) taskRecord).setFixedShare(taskRecord.getTask().getResourceShare());
            }
        }
        TaskRecord2 dropped = dropPolicy.checkForDrop(notHelping, taskRecords);
        if (dropped != null) {
            drop(dropped);
        }
    }

    private void giveShare(AllocationTaskView task) {
        int toGive = Math.min(freeCapacity, resourceStep);
        task.setResourceShare(task.getResourceShare() + toGive);
        freeCapacity -= toGive;
    }

    @Override
    public boolean addTask(AllocationTaskView task) {
        if (freeCapacity > 0) {
            giveShare(task);
            taskRecords.add(new TaskRecord(task, -taskRecords.size()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removeTask(AllocationTaskView task) {
        freeCapacity += task.getResourceShare();
        task.setResourceShare(0);
        for (Iterator<TaskRecord2> iterator = taskRecords.iterator(); iterator.hasNext(); ) {
            TaskRecord2 taskRecord = iterator.next();
            if (taskRecord.getTask().equals(task)) {
                iterator.remove();
                break;
            }
        }
    }

    private void drop(TaskRecord2 task) {
        eventPublisher.publish(task.getTask().getTask(), TaskEventPublisher.EventType.Drop, this);
    }

    private class TaskRecord extends TaskRecord2 {
        private int fixedShare = -1;

        private TaskRecord(AllocationTaskView task, int dropPriority) {
            super(task, dropPriority);
        }

        private int getFixedShare() {
            return fixedShare;
        }

        private void setFixedShare(int fixedShare) {
            this.fixedShare = fixedShare;
        }
    }
}
