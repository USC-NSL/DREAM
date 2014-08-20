package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation;

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
 * Time: 12:50 PM <br/>
 * Fixed fraction of switch capacity and rejects tasks.
 * If the Share parameter is less than or equal 1, it will be a fraction. If it is greater than 1, it is an absolute value
 */
public class FixedAlgorithm extends MultiTaskResourceControl {

    protected final List<TaskRecord2> taskRecords;
    private int share;
    private int capacity;
    private int currentUsedShare;

    public FixedAlgorithm(Element element, MonitorPoint monitorPoint) {
        taskRecords = new LinkedList<>();
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");

        capacity = monitorPoint.getCapacity();
        double share1 = Double.parseDouble(properties.get("Share").getAttribute(ConfigReader.PROPERTY_VALUE));
        if (share1 <= 1) {
            share = (int) (share1 * capacity);
        } else {
            share = (int) share1;
        }
        currentUsedShare = 0;

    }

    @Override
    public void allocate() {
        //instead do global drop


//        List<TaskRecord> toDrop = new LinkedList<>();
//        for (TaskRecord taskRecord : taskRecords) {
//            double accuracy = 0;
//            try {
//                accuracy = taskRecord.getTask().getAggregatedAccuracy();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if (taskRecord.shouldDrop(accuracy < lowThreshold)) {
//                toDrop.add(taskRecord);
//            }
//        }
//        for (TaskRecord taskRecord : toDrop) {
//            drop(taskRecord);
//        }
    }

    @Override
    public boolean addTask(AllocationTaskView task) {
        if (currentUsedShare + share <= capacity) {
            task.setResourceShare(share);
            taskRecords.add(new TaskRecord2(task, -taskRecords.size()));
            currentUsedShare += share;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removeTask(AllocationTaskView task) {
        task.setResourceShare(0);
        for (Iterator<TaskRecord2> iterator = taskRecords.iterator(); iterator.hasNext(); ) {
            TaskRecord2 taskRecord = iterator.next();
            if (taskRecord.getTask().equals(task)) {
                iterator.remove();
                currentUsedShare -= share;
                break;
            }
        }
    }

//    private void drop(TaskRecord2 task) {
//        eventPublisher.publish(task.getTask().getTask(), TaskEventPublisher.EventType.Drop, this);
//    }


}
