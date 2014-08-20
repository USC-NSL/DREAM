package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.tcammultitaskmultiswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MultiTaskMultiSwitchResourceNegotiator;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 7/31/13
 * Time: 11:43 AM  <br/>
 * The task handler for the case that we do joint divide & merge and TCAM allocation among tasks
 * where tasks can force others to merge!
 */
public class MultiTaskMultiSwitchPacketUser extends TaskHandler {
    private final List<TCAMMultiSwitchTask> tasks;
    private final MultiTaskMultiSwitchResourceNegotiator resourceControl;
    private final PrintWriter shareWriter;
    private final Element accuracyAggregatorElement;

    public MultiTaskMultiSwitchPacketUser(Element element) throws Exception {
        super();
        this.tasks = new ArrayList<>();
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        Element algorithmElement = properties.get("ResourceControl");
        this.resourceControl = (MultiTaskMultiSwitchResourceNegotiator) Class.forName(algorithmElement.getAttribute(ConfigReader.PROPERTY_VALUE)).
                getConstructor(Element.class, MonitorPoint.class).newInstance(algorithmElement, Util.getNetwork().getFirstMonitorPoints());
        this.shareWriter = new PrintWriter(Util.getRootFolder() + "/share.csv");
        accuracyAggregatorElement = properties.get("AccuracyAggregator");
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        for (TCAMMultiSwitchTask task : tasks) {
            //if the packet matches task filter, add packet to the task associated user
            task.process(p);
        }
    }


    @Override
    protected void step(EpochPacket p) {
        //multiThread.join(); // process2 can do threading if traffic came from steppacketuser
        super.step(p);
        int step = p.getStep();
        for (TCAMMultiSwitchTask task : tasks) {
            task.report();
//            tasks.get(task).update(task.getAccuracy2());
        }

        // update tasks
        for (TCAMMultiSwitchTask task : tasks) {
            task.update(step);
        }

        shareWriter.print(p.getStep());
        //for each task write the resource usage for each monitor point
        int[] monitorPointUsage = new int[resourceControl.getMonitorPoints().size() * 2];
        for (TCAMMultiSwitchTask task : tasks) {
            Arrays.fill(monitorPointUsage, 0);
            for (MonitorPoint monitorPoint : task.getMonitorPoints()) {
                monitorPointUsage[(monitorPoint.getIntId() - 1) * 2] = monitorPoint.getCapacity();
                monitorPointUsage[(monitorPoint.getIntId() - 1) * 2 + 1] = task.getMultiSwitch().getUsedCapacities().get(monitorPoint);
            }
            for (int i : monitorPointUsage) {
                shareWriter.print(String.format(",%d", i));
            }
        }
        shareWriter.println();
    }

    @Override
    public void finish(FinishPacket p) {
        for (TCAMMultiSwitchTask task : tasks) {
            task.finish(p);
        }
        shareWriter.close();
    }

    @Override
    public void writeLog(int step) {

    }

    @Override
    public void addTask(Task2 task, int step) {
        super.addTask(task, step);
        try {
            AccuracyAggregator accuracyAggregator = (AccuracyAggregator) Class.forName(accuracyAggregatorElement.getAttribute(ConfigReader.PROPERTY_VALUE)).
                    getConstructor(Element.class).newInstance(accuracyAggregatorElement);
            ((TCAMMultiSwitchTask) task).setAccuracyAggregator(accuracyAggregator);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<? extends Task2> getTasks() {
        return tasks;
    }

    @Override
    public void removeTask(String taskName, int step) {
        for (TCAMMultiSwitchTask TCAMMultiSwitchTask : tasks) {
            if (TCAMMultiSwitchTask.getName().equals(taskName)) {
                tasks.remove(TCAMMultiSwitchTask);
                return;
            }
        }
        System.err.println("Task " + taskName + " not found to remove");
    }
}
