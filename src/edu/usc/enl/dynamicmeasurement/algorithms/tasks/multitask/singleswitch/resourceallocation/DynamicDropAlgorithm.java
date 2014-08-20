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
public class DynamicDropAlgorithm extends MultiTaskResourceControl {

    private final PriorityDropPolicy dropPolicy;
    protected final int maxResource;
    protected final double lowThreshold;
    protected final List<TaskRecord2> taskRecords;


    public DynamicDropAlgorithm(Element element, MonitorPoint monitorPoint) {
        taskRecords = new LinkedList<>();
        this.maxResource = monitorPoint.getCapacity();
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        this.lowThreshold = Double.parseDouble(properties.get("LowThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        int dropEpochs = Integer.parseInt(properties.get("DropEpochs").getAttribute(ConfigReader.PROPERTY_VALUE));
        dropPolicy = new PriorityDropPolicy(dropEpochs);
    }

    @Override
    public void allocate() {
        if (taskRecords.size() > 0) {
            int poorNum = 0;
            for (TaskRecord2 task : taskRecords) {
                if (task.getTask().getAggregatedAccuracy() < lowThreshold) {
                    poorNum++;
                }
            }
            TaskRecord2 dropped = dropPolicy.checkForDrop(poorNum == taskRecords.size(), taskRecords);
            if (dropped != null) {
                eventPublisher.publish(dropped.getTask().getTask(), TaskEventPublisher.EventType.Drop, this);
            }

            int share = maxResource / taskRecords.size();
            for (TaskRecord2 task : taskRecords) {
                task.getTask().setResourceShare(share);
            }
        }
    }

    @Override
    public boolean addTask(AllocationTaskView task) {
        int resourceShare = maxResource / (taskRecords.size() + 1);
        for (TaskRecord2 task1 : taskRecords) {
            task1.getTask().setResourceShare(resourceShare);
        }
        task.setResourceShare(resourceShare);
        taskRecords.add(new TaskRecord2(task, -taskRecords.size()));
        return true;
    }

    @Override
    public void removeTask(AllocationTaskView task) {
        task.setResourceShare(0);
        for (Iterator<TaskRecord2> iterator = taskRecords.iterator(); iterator.hasNext(); ) {
            TaskRecord2 taskRecord = iterator.next();
            if (taskRecord.getTask().equals(task)) {
                iterator.remove();
                break;
            }
        }
        for (TaskRecord2 task1 : taskRecords) {
            task1.getTask().setResourceShare(maxResource / taskRecords.size());
        }
    }

}
