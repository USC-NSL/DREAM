package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.TaskEventPublisher;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.globaldrop.GlobalDrop;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.MultiTaskResourceControl;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/20/13
 * Time: 6:13 PM
 */
public class SeparateMultiTaskMultiSwitchTaskHandler extends TaskHandler implements Observer {
    protected final ControlledBufferWriter logWriter;
    private final Element localAccuracyAggregatorElement;
    private final Element globalAccuracyAggregatorElement;
    private final int updateInterval;
    private final ControlledBufferWriter shareWriter;
    protected Collection<MultiSwitchTask> acceptedTasks;
    private Map<MonitorPoint, MonitorPointData> monitorPoints;
    private List<Task2> toAddTasks = new ArrayList<>();
    private Map<String, Task2> forParentTasks = new HashMap<>();
    private GlobalDrop globalDrop;
    private boolean debugResourceAllocation = false;
    private Set<Task2> toDropTasks = Collections.synchronizedSet(new HashSet<Task2>());

    public SeparateMultiTaskMultiSwitchTaskHandler(Element element) throws Exception {
        super();
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        localAccuracyAggregatorElement = properties.get("AccuracyAggregator");
        globalAccuracyAggregatorElement = properties.get("GlobalAccuracyAggregator");
        Element resourceControlElement = properties.get("ResourceControl");
        this.updateInterval = Integer.parseInt(properties.get("UpdateInterval").getAttribute(ConfigReader.PROPERTY_VALUE));
        globalDrop = (GlobalDrop) Class.forName(properties.get("GlobalDrop").
                getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class, SeparateMultiTaskMultiSwitchTaskHandler.class).newInstance(properties.get("GlobalDrop"), this);
        logWriter = Util.getNewWriter(Util.getRootFolder() + "/resource.log", !debugResourceAllocation);

        acceptedTasks = new ArrayList<>();
        monitorPoints = new HashMap<>();
        for (MonitorPoint monitorPoint : Util.getNetwork().getMonitorPoints()) {
            MultiTaskResourceControl resourceControl = (MultiTaskResourceControl) Class.forName(
                    resourceControlElement.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class, MonitorPoint.class).
                    newInstance(resourceControlElement, monitorPoint);
            MonitorPointData value = new MonitorPointData(monitorPoint, resourceControl);
            value.getEventPublisher().subscribe(this);
            monitorPoints.put(monitorPoint, value);
            value.resourceControl.setLogWriter(logWriter);
        }
        this.shareWriter =
                Util.getNewWriter(Util.getRootFolder() + "/share.csv");
        shareWriter.println("time,task,switch,share,accuracy,accuracy_agg");
    }

    @Override
    protected void step(EpochPacket p) {
        super.step(p);
        int step = p.getStep();
        profile("Report");
        // report tasks
        // update accuracy aggregates
        {
            for (MultiSwitchTask task : acceptedTasks) {
                task.setStep(step);
                multiThread.offer(task.getReportTaskMethod());
            }
            multiThread.runJoin();
        }

        for (MultiSwitchTask task : acceptedTasks) {
            task.updateStats();
        }

        profile("Add");
        addByDelay(step);

        profile("Allocate");

        // allocate
        globalDrop.update();
        if ((step + 1) % updateInterval == 0) {
            globalDrop.globalDrop();
            allocate();
        }

        profile("Update_Structure");
        {

            for (MultiSwitchTask task : acceptedTasks) {
                task.setStep(step);
//                    task.getUpdateTaskMethod().run();
                multiThread.offer(task.getUpdateTaskMethod());
            }
            multiThread.runJoin();
        }
        profile(null);
    }

    private void addByDelay(int step) {
        for (Task2 toAddTask : toAddTasks) {
            addTask2(toAddTask, step);
        }
        toAddTasks.clear();
    }

    public void writeLog(int step) {
        for (MultiSwitchTask task : acceptedTasks) {
            for (MonitorPoint monitorPoint : task.getMonitorPoints()) {
                MultiSwitchTask.SingleSwitchTaskView viewFor = task.getViewFor(monitorPoint);
                if (Double.isNaN(viewFor.getAccuracy2())) {
                    System.out.println("Got Nan estimated accuracy from " + viewFor);
                }
                shareWriter.print(String.format("%d,%s,%d,%d,%.3f,%.3f",
                        step, task.getName(), monitorPoint.getIntId(),
                        viewFor.getResourceShare(), viewFor.getAccuracy2(),
                        viewFor.getAggregatedAccuracy()));

                shareWriter.println();
            }
        }
        shareWriter.flush();
        logWriter.flush();
    }

    protected void allocate() {
        toDropTasks.clear();
        if (debugResourceAllocation) {
            for (Map.Entry<MonitorPoint, MonitorPointData> entry : monitorPoints.entrySet()) {
                entry.getValue().getResourceControl().allocate();
            }
        } else {
            for (Map.Entry<MonitorPoint, MonitorPointData> entry : monitorPoints.entrySet()) {
                multiThread.offer(entry.getValue().getAllocateTask());
            }
            multiThread.runJoin();
        }


        for (Task2 toDropTask : toDropTasks) {
            drop((MultiSwitchTask) toDropTask);
        }
    }

    @Override
    public void removeTask(String taskName, int step) {
        MultiSwitchTask task = (MultiSwitchTask) forParentTasks.get(taskName);
        if (task != null) {//otherwise it has been dropped/rejected before
            acceptedTasks.remove(task);
            doRemove(task);
            task.finish(null);
        }
    }

    protected void doRemove(MultiSwitchTask multiSwitchTask) {
        for (MonitorPoint monitorPoint : multiSwitchTask.getMonitorPoints()) {
            monitorPoints.get(monitorPoint).remove(multiSwitchTask);
        }
        forParentTasks.remove(multiSwitchTask.getName());
        globalDrop.doRemove(multiSwitchTask);
    }

    @Override
    protected void process2(final DataPacket p) {
        for (MultiSwitchTask task : acceptedTasks) {
            task.process(p);
        }
    }

    @Override
    public void finish(FinishPacket p) {
        super.finish(p);
        for (MultiSwitchTask task : acceptedTasks) {
            //if the packet matches task filter, add packet to the task associated user
            task.finish(p);
        }
        acceptedTasks.clear();
        forParentTasks.clear();
        shareWriter.close();
        logWriter.close();
    }

    @Override
    public void addTask(Task2 task2, int step) {
        super.addTask(task2, step);
        toAddTasks.add(task2);
        forParentTasks.put(task2.getName(), task2);
    }

    private void addTask2(Task2 task2, int step) {
        logWriter.println("Add " + task2);
        MultiSwitchTask task = (MultiSwitchTask) task2;
        try {
            //add to every resource control it has traffic from
            boolean added = true;
            for (MonitorPoint monitorPoint : task.getMonitorPoints()) {
                boolean result = monitorPoints.get(monitorPoint).resourceControl.addTask(task.getViewFor(monitorPoint));
                if (!result) {
                    added = false;
                    logWriter.println("task" + task + " rejected on " + monitorPoint);
                    break;
                }
            }
            if (added) {
                AccuracyAggregator globalAccuracyAggregator = (AccuracyAggregator) Class.forName(globalAccuracyAggregatorElement.getAttribute(ConfigReader.PROPERTY_VALUE)).
                        getConstructor(Element.class).newInstance(globalAccuracyAggregatorElement);
                task.setAccuracyAggregator(globalAccuracyAggregator);
                for (MonitorPoint monitorPoint : task.getMonitorPoints()) {
                    AccuracyAggregator accuracyAggregator = (AccuracyAggregator) Class.forName(localAccuracyAggregatorElement.getAttribute(ConfigReader.PROPERTY_VALUE)).
                            getConstructor(Element.class).newInstance(localAccuracyAggregatorElement);
                    task.setAccuracyAggregator(monitorPoint, accuracyAggregator);
                }
                acceptedTasks.add(task);
                task.update(step); //TODO, for reading traces, the task was in forparenttasks and the parent read the trace for it
            } else {
                //ok should let the resourceControl free any partial resource it gave to it
                doRemove(task);
                task.drop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<MultiSwitchTask> getAcceptedTasks() {
        return acceptedTasks;
    }

    @Override
    public Collection<? extends Task2> getTasks() {
        return forParentTasks.values();
    }

    @Override
    public void update(Observable o, Object arg) {
        TaskEventPublisher.EventWrapper eventWrapper = (TaskEventPublisher.EventWrapper) arg;
        MultiSwitchTask task = (MultiSwitchTask) eventWrapper.getTask();
//        drop(task);// don't do drop in the middle of allocate just keep the record
        //but that allocator relies on dropping this, so just let it see this removed
        for (MonitorPoint monitorPoint : task.getMonitorPoints()) {
            MonitorPointData monitorPointData = monitorPoints.get(monitorPoint);
            if (monitorPointData.getResourceControl().equals(eventWrapper.getResourceControl())) {
                monitorPointData.remove(task);
                break;
            }
        }
        toDropTasks.add(task);
    }

    public void drop(MultiSwitchTask task) {
        if (acceptedTasks.remove(task)) {
            logWriter.println("Do drop " + task);
            doRemove(task);
            task.drop();
        }
    }

    public ControlledBufferWriter getLogWriter() {
        return logWriter;
    }

    private static class MonitorPointData {
        private TaskEventPublisher eventPublisher = new TaskEventPublisher();
        private MultiTaskResourceControl resourceControl;
        private MonitorPoint monitorPoint;
        private AllocateThreadMethod allocateTask;

        private MonitorPointData(MonitorPoint monitorPoint, MultiTaskResourceControl resourceControl) {
            this.monitorPoint = monitorPoint;
            this.resourceControl = resourceControl;
            resourceControl.setEventPublisher(eventPublisher);
            allocateTask = new AllocateThreadMethod(resourceControl);
        }

        private TaskEventPublisher getEventPublisher() {
            return eventPublisher;
        }

        private MultiTaskResourceControl getResourceControl() {
            return resourceControl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MonitorPointData that = (MonitorPointData) o;

            if (monitorPoint != null ? !monitorPoint.equals(that.monitorPoint) : that.monitorPoint != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return monitorPoint != null ? monitorPoint.hashCode() : 0;
        }

        public void remove(MultiSwitchTask multiSwitchTask) {
            resourceControl.removeTask(multiSwitchTask.getViewFor(monitorPoint));
        }

        public AllocateThreadMethod getAllocateTask() {
            return allocateTask;
        }
    }

    private static class AllocateThreadMethod implements Runnable {
        private final MultiTaskResourceControl resourceControl;

        private AllocateThreadMethod(MultiTaskResourceControl resourceControl) {
            this.resourceControl = resourceControl;
        }

        @Override
        public void run() {
            resourceControl.allocate();
        }
    }

}
