package edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.single;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 4:39 PM  <br/>
 * A very simple task handler that can handle only one task
 */
public class SingleTaskHandler extends TaskHandler {
    private List<Task2> tasks; //kept this as a list to just not create it each time for getTasks method!

    public SingleTaskHandler(Element element) {
        super();
        this.tasks = new ArrayList<>();
    }

    @Override
    public void addTask(Task2 task, int step) {
        super.addTask(task, step);
        if (tasks.size() > 0) {
            tasks.get(0).finish(new FinishPacket(step * Util.getSimulationConfiguration().getEpoch()));
            tasks.clear();
        }
        tasks.add(task);
        ((SingleSwitchTask) task).setResourceShare(Util.getNetwork().getFirstMonitorPoints().getCapacity());
    }

    @Override
    public Collection<? extends Task2> getTasks() {
        return tasks;
    }

    @Override
    public void removeTask(String taskName, int step) {
        System.err.println("Does not support that");
    }

    @Override
    protected void process2(DataPacket p) {
        if (tasks.size() > 0) {
            tasks.get(0).process(p);
        }
    }

    @Override
    protected void step(EpochPacket p) {
        super.step(p);
        if (tasks.size() > 0) {
            int step = p.getStep();
            // report tasks
            tasks.get(0).report(step);
            // update accuracy estimates
            tasks.get(0).update(step);
        }
    }

    @Override
    public void finish(FinishPacket p) {
        if (tasks.size() > 0) {
            tasks.get(0).finish(p);
        }
    }

    @Override
    public void writeLog(int step) {

    }
}
