package edu.usc.enl.dynamicmeasurement.algorithms.taskhandler;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread.LoadTrafficTaskMethod;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.TrafficTransformer;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.TransformHandlerInterface;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;
import edu.usc.enl.dynamicmeasurement.util.Util;
import edu.usc.enl.dynamicmeasurement.util.multithread.MultiThread;
import edu.usc.enl.dynamicmeasurement.util.profile.LatencyProfiler;
import edu.usc.enl.dynamicmeasurement.util.profile.Profilable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 10:20 AM <br/>
 * The class that handles the dynamics of tasks and runs the main loop of DREAM.
 * It also keeps track of transforms if they come before their task and has profiling data structure
 */
public abstract class TaskHandler extends StepPacketUser implements TransformHandlerInterface, Profilable {
    protected final MultiThread multiThread;
    private final Map<WildcardPattern, TrafficTransformer> currentTransforms = new HashMap<>();
    private LatencyProfiler profiler;

    public TaskHandler() {
        multiThread = new MultiThread(Util.getSimulationConfiguration().getThreads());
    }

    @Override
    public void writeProfiles() {
        if (profiler != null) {
            profiler.write();
        }
    }

    @Override
    public void createProfiler() {
        profiler = new LatencyProfiler(getClass());
    }

    @Override
    public void finishProfiler() {
        if (profiler != null) {
            profiler.finish();
        }
    }

    /**
     * Adds the task, the rejection may happen later at the right time, if the task is dropped before the first measurement
     *
     * @param task
     * @param step
     */
    public void addTask(Task2 task, int step) {
        //find if I have a transform for that task
        for (Map.Entry<WildcardPattern, TrafficTransformer> entry : currentTransforms.entrySet()) {
            if (task.getFilter().match(entry.getKey())) {
                task.addTransform(entry.getValue());
            }
        }
    }

    /**
     * @return all added tasks even if they are not yet doing any measurement and we are not sure if we accept them.
     * Why? because we need to read their traces in the simulation. Note that this will result in ignoring the first
     * second of traces for each task (can we fix this?)
     */
    public abstract Collection<? extends Task2> getTasks();

    /**
     * removes the task from all data structures as if the task finished by user
     *
     * @param taskName
     * @param step
     */
    public abstract void removeTask(String taskName, int step);

    /**
     * reun the main control loop
     *
     * @param p
     */
    @Override
    protected void step(EpochPacket p) {
        profile("ReadTrace");
        boolean hasTraceTask = false;
        for (Task2 task : getTasks()) {
            if (task.getTraceReader() != null) {
                LoadTrafficTaskMethod processTaskMethod = task.getProcessTaskMethod();
                processTaskMethod.setEpoch(p);
                multiThread.offer(processTaskMethod);
                hasTraceTask = true;
            }
        }
        if (hasTraceTask) {
            multiThread.runJoin();
        }
    }

    protected void profile(String s) {
        //just to not write this if every time I want to profile
        if (profiler != null) {
            profiler.sequentialRecord(s);
        }
    }

    @Override
    public void finish(FinishPacket p) {
        multiThread.finishThreads();
    }

    @Override
    public void removeTransform(String transformName) {
        for (Task2 task2 : getTasks()) {
            task2.removeTransform(transformName);
        }
    }

    /**
     * The transform can be applied to the tasks that may instantiate later.
     *
     * @param t
     */
    @Override
    public void addTransform(TrafficTransformer t) {
        currentTransforms.put(t.getFilter(), t);
        for (Task2 task2 : getTasks()) {
            if (task2.getFilter().match(t.getFilter())) {
                task2.addTransform(t);
                return;
            }
        }
//        System.err.println("Cannot find task for transform " + t + " with filter " + t.getFilter());
    }

    /**
     * Call this method to force the handler to write any log it has at this time.
     * Note that writing logs in any place other than this method can cause unpredictable disk delays in the controll loop.
     *
     * @param step
     */
    public abstract void writeLog(int step);
}
