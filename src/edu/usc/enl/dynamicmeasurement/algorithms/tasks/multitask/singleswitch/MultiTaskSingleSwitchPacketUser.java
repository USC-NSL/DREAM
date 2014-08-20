package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.TaskEventPublisher;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.MultiTaskResourceControl;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
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
 * Date: 7/31/13
 * Time: 11:43 AM <br/>
 *
 */
public class MultiTaskSingleSwitchPacketUser extends TaskHandler implements Observer {
    private final List<SingleSwitchTask> tasks;
    private final MultiTaskResourceControl resourceControl;
    private final ControlledBufferWriter shareWriter;
    private final int updateInterval;
    private final Element accuracyAggregatorElement;
    private final TaskEventPublisher eventPublisher;
    private final ControlledBufferWriter logWriter;

    public MultiTaskSingleSwitchPacketUser(Element element) throws Exception {
        super();

        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        accuracyAggregatorElement = properties.get("AccuracyAggregator");
        Element resourceControlElement = properties.get("ResourceControl");
        this.resourceControl = (MultiTaskResourceControl) Class.forName(resourceControlElement.getAttribute(ConfigReader.PROPERTY_VALUE)).
                getConstructor(Element.class, MonitorPoint.class).newInstance(resourceControlElement, Util.getNetwork().getFirstMonitorPoints());
        eventPublisher = new TaskEventPublisher();
        resourceControl.setEventPublisher(eventPublisher);
        logWriter = Util.getNewWriter(Util.getRootFolder() + "/resource.log");
        resourceControl.setLogWriter(logWriter);
        this.shareWriter = Util.getNewWriter(Util.getRootFolder() + "/share.csv");
        this.updateInterval = Integer.parseInt(properties.get("UpdateInterval").getAttribute(ConfigReader.PROPERTY_VALUE));
        tasks = new LinkedList<>();
        eventPublisher.subscribe(this);
        //initialize tasks resources
//        resourceControl.initialize(tasks.keySet());
        shareWriter.println("time,task,switch,share,accuracy,accuracy_agg");


    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        for (Task2 task : tasks) {
            task.process(p);
        }
    }

    @Override
    protected void step(EpochPacket p) {
//        multiThread.join(); // process2 can do threading if traffic came from steppacketuser
        super.step(p);
        int step = p.getStep();
        // report tasks
        // update accuracy aggregates
        {
            for (SingleSwitchTask singleSwitchTask : tasks) {
                singleSwitchTask.setStep(step);
                multiThread.offer(singleSwitchTask.getReportTaskMethod());
            }
            multiThread.runJoin();
        }

        for (SingleSwitchTask singleSwitchTask : tasks) {
            singleSwitchTask.updateStats();
        }

        // allocate
        if ((step + 1) % updateInterval == 0) {
            //update tasks resource share
            resourceControl.allocate();
        }

        // update tasks
        {
            for (SingleSwitchTask singleSwitchTask : tasks) {
                singleSwitchTask.setStep(step);
                multiThread.offer(singleSwitchTask.getUpdateTaskMethod());
            }
            multiThread.runJoin();
        }

    }

    @Override
    public void finish(FinishPacket p) {
        super.finish(p);
        for (Task2 task : tasks) {
            task.finish(p);
        }
        shareWriter.close();
        logWriter.close();
    }

    @Override
    public void writeLog(int step) {
        for (SingleSwitchTask singleSwitchTask : tasks) {
            shareWriter.print(step + "," + 1 + "," + singleSwitchTask.getName());
            shareWriter.print(String.format(",%d,%.3f,%.3f", singleSwitchTask.getResourceShare(), singleSwitchTask.getAccuracy2(),
                    singleSwitchTask.getAggregatedAccuracy()));
            shareWriter.println();
        }
        logWriter.flush();
    }

    @Override
    public void addTask(Task2 task, int step) {
        super.addTask(task, step);
        System.out.println("Add " + task);
        //instantiate accuracy aggregator
        try {
            boolean result = resourceControl.addTask((SingleSwitchTask) task);
            if (result) {
                AccuracyAggregator accuracyAggregator = (AccuracyAggregator) Class.forName(accuracyAggregatorElement.getAttribute(ConfigReader.PROPERTY_VALUE)).
                        getConstructor(Element.class).newInstance(accuracyAggregatorElement);
                ((SingleSwitchTask) task).setAccuracyAggregator(accuracyAggregator);
                tasks.add((SingleSwitchTask) task);
                task.update(step);
            } else {
                System.out.println("task" + task + " rejected");
                //ok should let the resourceControl free any partial resource it gave to it
                doRemove((SingleSwitchTask) task);
                task.drop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<? extends Task2> getTasks() {
        return tasks;
    }

    @Override
    public void removeTask(String taskName, int step) {
        System.out.println("Remove " + taskName);
        for (SingleSwitchTask task : tasks) {
            if (task.getName().equals(taskName)) {
                doRemove(task);
                task.finish(null);
                return;
            }
        }
        System.err.println("Task " + taskName + " not found to remove");
    }

    private void doRemove(SingleSwitchTask task) {
        tasks.remove(task);
        resourceControl.removeTask(task);
    }

    @Override
    public void update(Observable o, Object arg) {
        TaskEventPublisher.EventWrapper e = (TaskEventPublisher.EventWrapper) arg;
        if (e.getEvent() == TaskEventPublisher.EventType.Add) {
            // don't add out of
        } else if (e.getEvent() == TaskEventPublisher.EventType.Drop) {
            Task2 task = e.getTask();
            doRemove((SingleSwitchTask) task);
            task.drop();
        }
    }

}
