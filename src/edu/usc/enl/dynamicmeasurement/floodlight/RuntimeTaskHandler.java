package edu.usc.enl.dynamicmeasurement.floodlight;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.event.EventRunner;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
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
 * Date: 10/18/13
 * Time: 4:00 PM <br/>
 * This is the wrapper around the task handler to adapt it for FloodLight.
 * The main functionality of this class is make sure the profilers are finished.
 * Finding which OpenFlow rules go on which switch based on the mapping of wildcard patterns and switches.
 * Lastly, when the controller receives the counts, it updates the wildcard pattern weights
 */
class RuntimeTaskHandler implements Profilable {

    private final TaskHandler taskHandler;
    private final EventRunner eventRunner;
    private final int epochSize;
    private LatencyProfiler profiler;
    private MultiThread multiThread;
    private Map<String, Map<WildcardPattern, WildcardPattern>> tempRuleMap = new HashMap<>();

    RuntimeTaskHandler(TaskHandler taskHandler, EventRunner eventRunner, int epochSize) {
        this.taskHandler = taskHandler;
        this.eventRunner = eventRunner;
        this.epochSize = epochSize;
        multiThread = new MultiThread(Util.getSimulationConfiguration().getThreads());
    }

    protected void step(Collection<SwitchData> switches, int epoch) {
        if (tempRuleMap.size() == 0) {
            for (SwitchData aSwitch : switches) {
                tempRuleMap.put(aSwitch.getStringId(), aSwitch.getWorkingRules());
            }
        }
        profile("UpdateMonitors");
        updateMonitors();
        profile("Step");
        taskHandler.forceStep(new EpochPacket(0, epoch));
        //get rules
        profile("GetRules");
        getRules();
        profile(null);
    }

    @Override
    public void writeProfiles() {
        if (profiler != null) {
            profiler.write();
            taskHandler.writeProfiles();
            for (Task2 task2 : taskHandler.getTasks()) {
                Task2.TaskImplementation implementation = task2.getUser().getImplementation();
                if (implementation instanceof Profilable) {
                    ((Profilable) implementation).writeProfiles();
                }
            }
        }
    }

    @Override
    public void createProfiler() {
        this.profiler = new LatencyProfiler(getClass());
        taskHandler.createProfiler();
    }

    @Override
    public void finishProfiler() {
        if (profiler != null) {
            this.profiler.finish();
            taskHandler.finishProfiler();
            for (Task2 task2 : taskHandler.getTasks()) {
                Task2.TaskImplementation implementation = task2.getUser().getImplementation();
                if (implementation instanceof Profilable) {
                    ((Profilable) implementation).finishProfiler();
                }
            }
        }
    }

    protected Collection<? extends Task2> getTasks() {
        return taskHandler.getTasks();
    }

    public boolean noEvent() {
        return eventRunner.isEmpty();
    }

    protected void profile(String s) {
        if (profiler != null) {
            profiler.sequentialRecord(s);
        }
    }

    protected void runEvents(int epoch) {
//        System.out.println("event runner empty " + eventRunner.isEmpty() + " for epoch " + epoch);
        if (!eventRunner.isEmpty()) {
            eventRunner.forceStep(new EpochPacket(0, epoch));
        }
    }

    /**
     * Find the OpenFlow rules that must be saved at each switch based on the to be monitored prefixes
     */
    private void getRules() {
        for (Map<WildcardPattern, WildcardPattern> map : tempRuleMap.values()) {
            map.clear();
        }
        for (final Task2 task2 : getTasks()) {
            multiThread.offer(new Runnable() {
                @Override
                public void run() {
                    MultiSwitchTask task = (MultiSwitchTask) task2;
                    TCAMAlgorithm hhhAlgorithm = (TCAMAlgorithm) task.getUser().getImplementation();
                    Collection<WildcardPattern> monitors = hhhAlgorithm.getMonitors();

                    for (WildcardPattern monitor : monitors) {
                        for (MonitorPoint monitorPoint : hhhAlgorithm.getWhichSwitch(monitor)) {
                            tempRuleMap.get(monitorPoint.getStringId()).put(monitor, monitor);
                        }
                    }
                }
            });
        }
        multiThread.runJoin();
    }

    /**
     * update wildcard patterns using the counters they saved on the switches based on the mapping of the switches and
     * prefixes. It warns if it does not find the corresponding rule at the switch where it should be.
     * In the first epoch of the task lifetime this warning should be normal.
     */
    private void updateMonitors() {
        //now for each task get its monitors and try to update its monitors using the counters
        for (final Task2 task2 : getTasks()) {
            multiThread.offer(new Runnable() {
                @Override
                public void run() {
                    MultiSwitchTask task = (MultiSwitchTask) task2;
                    TCAMAlgorithm hhhAlgorithm = (TCAMAlgorithm) task.getUser().getImplementation();
                    Collection<WildcardPattern> monitors = hhhAlgorithm.getMonitors();


                    for (WildcardPattern monitor : monitors) {
                        for (MonitorPoint monitorPoint : hhhAlgorithm.getWhichSwitch(monitor)) {
                            Map<WildcardPattern, WildcardPattern> switchStats = tempRuleMap.get(monitorPoint.getStringId() + "");
                            WildcardPattern patternInSwitch = switchStats.get(monitor);
                            if (patternInSwitch != null) {
                                monitor.setWeight(monitor.getWeight() + patternInSwitch.getWeight());
                            } else {
                                PeriodicReport.log.warn("Pattern {} not found in switch {}", monitor.toCIDRString(), monitorPoint.getStringId());
                            }
                        }
                    }
                }
            });
        }
        multiThread.runJoin();
    }

    public void finish(int epoch) {
        taskHandler.finish(new FinishPacket((epoch + 1) * epochSize + 0));
        multiThread.finishThreads();
    }

    public void writeLogs(int epoch) {
        taskHandler.writeLog(epoch);
    }
}
