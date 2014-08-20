package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.MultiTaskResourceControl;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.TaskRecord2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor.AbstractResourceDistributor;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.step.RichPoorStepUpdater;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.step.StepUpdater;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/30/13
 * Time: 3:23 PM <br/>
 * Take resource from tasks above high threshold to tasks below low threshold.
 * It should also try to dream tasks that cannot be brought up a few epochs
 * <p>The XML constructor requires the following Property children tags: <ul>
 * <li> name attribute as "LowThreshold", low threshold for accuracy bound</li>
 * <li> name attribute as "HighThreshold", high threshold for accuracy bound</li>
 * <li> name attribute as "PotentialDrop", should it use a potential-based drop in the local drop algorithm or not (Optional, not used)</li>
 * <li> name attribute as "Headroom", the percentage of headroom</li>
 * <li> name attribute as "MinResource", minimum resources for tasks and minimum size of step</li>
 * <li> name attribute as "Distributor", Refers to the class for distributing resources among tasks</li>
 * <li> name attribute as "StepUpdater", Refers to the class that implements the updating step policy</li>
 * </ul></p>
 */
public class ThresholdGuaranteeAlgorithm2 extends MultiTaskResourceControl {
    protected static final double probAdd2 = 0.05;
    protected static final int probDiv = 2;
    protected final double lowThreshold;
    protected final double highThreshold;
    protected final int headroom;
    protected final int minResource;
    protected final int maxResource;
    protected final Map<AllocationTaskView, DreamTaskRecord> taskRecords;
    protected final DummyTaskRecord dummyTaskRecord;
    protected final List<AllocationTaskView> joiningTasks = new LinkedList<>();
    protected int step;
    //    private DropFlagRaiser dropFlagRaiser;
    private AbstractResourceDistributor resourceDistributor;
    private StepUpdater stepUpdater;
    private int tasksNumForPriority = 0;
    private ControlledBufferWriter logWriter;

    public ThresholdGuaranteeAlgorithm2(Element element, MonitorPoint monitorPoint) {
        int capacity = monitorPoint.getCapacity();

        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        this.lowThreshold = Double.parseDouble(properties.get("LowThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.highThreshold = Double.parseDouble(properties.get("HighThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        boolean potentialDrop = false;
        if (properties.containsKey("PotentialDrop")) {
            potentialDrop = Integer.parseInt(properties.get("PotentialDrop").getAttribute(ConfigReader.PROPERTY_VALUE)) > 0;
        }
        if (properties.containsKey("Headroom")) {
            headroom = (int) (capacity * Double.parseDouble(properties.get("Headroom").getAttribute(ConfigReader.PROPERTY_VALUE)));
        } else {
            headroom = 0;
        }
        this.minResource = Integer.parseInt(properties.get("MinResource").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.maxResource = capacity;
//        int dropEpochs = Integer.parseInt(properties.get("DropEpochs").getAttribute(ConfigReader.PROPERTY_VALUE));
//        PriorityDropPolicy dropPolicy = new PriorityDropPolicy(potentialDrop, dropEpochs);
        try {
//            dropFlagRaiser = (DropFlagRaiser) Class.forName(properties.get("DropPolicy").getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(
//                    ThresholdGuaranteeAlgorithm2.class, PriorityDropPolicy.class).newInstance(this, dropPolicy);
            resourceDistributor = (AbstractResourceDistributor) Class.forName(properties.get("Distributor").
                    getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(ThresholdGuaranteeAlgorithm2.class).newInstance(this);
            stepUpdater = (StepUpdater) Class.forName(properties.get("StepUpdater").getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor().newInstance();
            stepUpdater.init((int) (capacity * probAdd2), probDiv, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        taskRecords = new HashMap<>();
        {
            dummyTaskRecord = resourceDistributor.getDummy();
            dummyTaskRecord.getTask().setResourceShare(maxResource);
            dummyTaskRecord.setReductionStep(maxResource);
        }
        step = 0;
    }

    public int getHeadroom() {
        return headroom;
    }

    private void addJoining(List<DreamTaskRecord> poorTaskRecords) {
        int newTaskNum = taskRecords.size() + joiningTasks.size();

        for (AllocationTaskView task : joiningTasks) {
            DreamTaskRecord newTaskRecord = new DreamTaskRecord(task, getNewTaskPriority(), this);
//            if (task.getAggregatedAccuracy() < lowThreshold) {
            poorTaskRecords.add(newTaskRecord);
//                newTaskRecord.setAdditionStep((maxResource / newTaskNum) - minResource );//I already got minResource. it sinks all resources in TCAM
            newTaskRecord.setAdditionStep(Math.min(10 * minResource, (maxResource / newTaskNum) - minResource));
            newTaskRecord.setReductionStep(newTaskRecord.getAdditionStep() / 2);
            newTaskRecord.setWasRich(false);
            newTaskRecord.setWasPoor(true);
//            }
//            else {
//                newTaskRecord.setAdditionStep(minResource);
//                newTaskRecord.setReductionStep(minResource);
//                newTaskRecord.setWasRich(task.getAggregatedAccuracy() > highThreshold);
//                newTaskRecord.setWasPoor(false);
//            }
            taskRecords.put(task, newTaskRecord);
            tasksNumForPriority++;
        }
        joiningTasks.clear();
    }

    public double getLowThreshold() {
        return lowThreshold;
    }

    public double getHighThreshold() {
        return highThreshold;
    }

    @Override
    public void allocate() {
//        int sum = 0;
//        for (AllocationTaskView allocationTaskView : taskRecords.keySet()) {
//            sum += allocationTaskView.getResourceShare();
//        }
//        for (AllocationTaskView joiningTask : joiningTasks) {
//            sum += joiningTask.getResourceShare();
//        }
//        if (sum + dummyTaskRecord.getTask().getResourceShare() != (1 << 15)) {
//            System.out.println("Wasted resource");
//            System.exit(1);
//        }

        List<DreamTaskRecord> richTaskRecords = new ArrayList<>();
        List<DreamTaskRecord> poorTaskRecords = new ArrayList<>();

        for (DreamTaskRecord dreamTaskRecord : taskRecords.values()) {
            dreamTaskRecord.updateState(this);
        }
        for (DreamTaskRecord taskRecord : taskRecords.values()) {
            if (taskRecord.getDelayedResourceChange() != null) {
                taskRecord.setLastResourceChange(taskRecord.getLastResourceChange() + taskRecord.getDelayedResourceChange());
            }
            if (taskRecord.amRich()) {
                richTaskRecords.add(taskRecord);
                taskRecord.setDelayedResourceChange(null); //No delay in richness to poorness
            } else if (taskRecord.amPoor()) {
                if (taskRecord.notUsingAll()) {
                    taskRecord.setDelayedResourceChange(taskRecord.getLastResourceChange());
                } else {
                    poorTaskRecords.add(taskRecord);
                    taskRecord.setDelayedResourceChange(null);
                }
            } else {
                taskRecord.setDelayedResourceChange(null); //middle case
            }
        }

        stepUpdater.updateStep(taskRecords.values());
        addJoining(poorTaskRecords);


        logWriter.println(step);
        for (DreamTaskRecord taskRecord : taskRecords.values()) {
            logWriter.println(taskRecord + " agg acc=" + taskRecord.getTask().getAggregatedAccuracy());
        }
        logWriter.println(dummyTaskRecord.toString());


        prepareForDistribute(richTaskRecords, poorTaskRecords);
        for (DreamTaskRecord taskRecord : taskRecords.values()) {
            taskRecord.setWasPoor(taskRecord.amPoor());
            taskRecord.setWasRich(taskRecord.amRich());
            taskRecord.setLastAccuracy(taskRecord.getTask().getAggregatedAccuracy());
        }
        step++;
    }

    private void prepareForDistribute(List<DreamTaskRecord> richTaskRecords, List<DreamTaskRecord> poorTaskRecords) {
        for (DreamTaskRecord taskRecord : taskRecords.values()) {
            taskRecord.setLastResourceChange(0);
        }
        Map<DreamTaskRecord, Integer> taskLastResource = new HashMap<>();
        for (DreamTaskRecord task : taskRecords.values()) {
            taskLastResource.put(task, task.getTask().getResourceShare());
        }
        resourceDistributor.distribute(richTaskRecords, poorTaskRecords);
        for (Map.Entry<DreamTaskRecord, Integer> entry : taskLastResource.entrySet()) {
            DreamTaskRecord taskRecord = entry.getKey();
            taskRecord.setLastResourceChange(taskRecord.getTask().getResourceShare() - entry.getValue());
        }
    }

    @Override
    public boolean addTask(AllocationTaskView task) {
        //check headroom
        double currentHeadroom = calculateHeadroom(false);
        if (headroom > 0 && headroom > currentHeadroom) {  //not enough headroom
            //need some logs
            logWriter.println("not enough headroom (" + currentHeadroom + ") for  task " + task);
//            calculateHeadroom(true);
            return false;
        }

        //just give min resource from the one with max resource
        double max = 0;
        DreamTaskRecord maxTask = null;
        if (dummyTaskRecord.getTask().getResourceShare() >= minResource) {
            maxTask = dummyTaskRecord;
        } else {
            for (Map.Entry<AllocationTaskView, DreamTaskRecord> entry : taskRecords.entrySet()) {
                AllocationTaskView task1 = entry.getKey();
                if (maxTask == null || max < task1.getResourceShare() ||
                        (max == task1.getResourceShare() && maxTask.compareTo(entry.getValue()) > 0)) {//to always take resource from lower priority in case of equal max
                    max = task1.getResourceShare();
                    maxTask = entry.getValue();
                }
            }
        }
        if (maxTask == null || !maxTask.canOfferNewComer()) {
            return false;
        }
        task.setResourceShare(minResource);
        maxTask.getTask().setResourceShare(maxTask.getTask().getResourceShare() - minResource);
        joiningTasks.add(task);

        return true;
    }

    private double calculateHeadroom(boolean log) {
        double currentHeadroom = maxResource;
        int newTaskPriority = getNewTaskPriority();
        for (DreamTaskRecord dreamTaskRecord : taskRecords.values()) {
            if (dreamTaskRecord.getDropPriority() < newTaskPriority) {
                continue;
            }
            AllocationTaskView task1 = dreamTaskRecord.getTask();
            double aggregatedAccuracy = task1.getAggregatedAccuracy();
            if (aggregatedAccuracy < lowThreshold) { //cannot use the was poor as it may be in the middle of two allocation phase
                int oldAdd = dreamTaskRecord.getAdditionStep();
                int oldRed = dreamTaskRecord.getReductionStep();
                ((RichPoorStepUpdater) stepUpdater).runForTask(dreamTaskRecord, dreamTaskRecord.findAmRich(this), dreamTaskRecord.findAmPoor(this));
                currentHeadroom -= task1.getUsedResourceShare() + dreamTaskRecord.getAdditionStep2();
                if (log) {
                    logWriter.println(currentHeadroom + "," + task1);
                }
                dreamTaskRecord.setAdditionStep(oldAdd);
                dreamTaskRecord.setReductionStep(oldRed);
//                }
            } else if (aggregatedAccuracy > highThreshold) { // rich
                int oldAdd = dreamTaskRecord.getAdditionStep();
                int oldRed = dreamTaskRecord.getReductionStep();
                ((RichPoorStepUpdater) stepUpdater).runForTask(dreamTaskRecord, dreamTaskRecord.findAmRich(this), dreamTaskRecord.findAmPoor(this));
                currentHeadroom -= Math.max(minResource, task1.getUsedResourceShare() - dreamTaskRecord.getReductionStep2());
                if (log) {
                    logWriter.println(currentHeadroom + "," + task1);
                }
                dreamTaskRecord.setAdditionStep(oldAdd);
                dreamTaskRecord.setReductionStep(oldRed);
            } else {
                currentHeadroom -= task1.getUsedResourceShare();
                if (log) {
                    logWriter.println(currentHeadroom + "," + task1);
                }
            }
        }
        currentHeadroom -= joiningTasks.size() *
                (taskRecords.size() > 0 ?
                        Math.min(
                                (maxResource - Math.max(0, currentHeadroom)) / taskRecords.size(),
                                10 * minResource) :
                        10 * minResource);
        if (log) {
            logWriter.println(currentHeadroom + "," + joiningTasks.size() + "," + (taskRecords.size() > 0 ?
                    Math.min(
                            (maxResource - Math.max(0, currentHeadroom)) / taskRecords.size(),
                            10 * minResource) :
                    10 * minResource));
        }
        return currentHeadroom;
    }

    @Override
    public void removeTask(AllocationTaskView task) {
        DreamTaskRecord remove = taskRecords.remove(task);
        if (remove == null) {//may be it just joined
            for (Iterator<AllocationTaskView> iterator = joiningTasks.iterator(); iterator.hasNext(); ) {
                AllocationTaskView next = iterator.next();
                if (next.equals(task)) {
                    iterator.remove();
                    break;
                }
            }
        }
        dummyTaskRecord.getTask().setResourceShare(dummyTaskRecord.getTask().getResourceShare() + task.getResourceShare());
        task.setResourceShare(0);
    }

    public ControlledBufferWriter getLogWriter() {
        return logWriter;
    }

    public void setLogWriter(ControlledBufferWriter log) {
        logWriter = log;
    }

    public DummyTaskRecord getDummyTaskRecord() {
        return dummyTaskRecord;
    }

    protected DreamTaskRecord getTask(AllocationTaskView richTask) {
        if (richTask.equals(dummyTaskRecord.getTask())) {
            return dummyTaskRecord;
        }
        return taskRecords.get(richTask);
    }

    public int getMinResource() {
        return minResource;
    }

    public int getMaxResource() {
        return maxResource;
    }

    public Collection<? extends TaskRecord2> getTasks() {
        return taskRecords.values();
    }

    public int getNewTaskPriority() {
        return -tasksNumForPriority;
    }
}
