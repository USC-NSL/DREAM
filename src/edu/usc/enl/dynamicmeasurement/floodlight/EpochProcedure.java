package edu.usc.enl.dynamicmeasurement.floodlight;

import edu.usc.enl.dynamicmeasurement.util.Util;
import edu.usc.enl.dynamicmeasurement.util.multithread.MultiThread;
import edu.usc.enl.dynamicmeasurement.util.profile.LatencyProfiler;
import edu.usc.enl.dynamicmeasurement.util.profile.Profilable;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.util.SingletonTask;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/18/13
 * Time: 4:16 PM <br/>
 * This is the responsible class for handling the configure-fetch loop (or say, fetch run save).
 * In the beginning the controller installs just one rule at all switches.
 * Once the controller sees a traffic on any of the switches it assumes that the traffic generators started.
 * Thus even if there can be no tasks in the beginning, for the experiment to get started
 * we need to send a kind of small traffic at time 0 of the experiment.
 */
class EpochProcedure implements Runnable, Profilable {
    private final RuntimeTaskHandler runtimeTaskHandler;
    private final int epochSize;
    private MultiThread multiThread;
    private int epoch;
    private Map<String, SwitchData> switches;
    private SingletonTask flowInfoTask;
    //    private long time0;
//    private long time1 = 0;
    private boolean seenTraffic = false;
    private LatencyProfiler profiler;

    EpochProcedure(RuntimeTaskHandler runtimeTaskHandler, int epochSize) {
        this.runtimeTaskHandler = runtimeTaskHandler;
        this.epochSize = epochSize;
        multiThread = new MultiThread(Util.getSimulationConfiguration().getThreads());
        epoch = 0;

        createProfiler();
    }

    @Override
    public void run() {
//        long time = System.currentTimeMillis();
//        System.out.println("Start=" + time);
        reschedule();
        try {
            if (seenTraffic) {
                long t = System.nanoTime();
                profile("Fetch");
                report();
                profile("Run");
                runtimeTaskHandler.step(switches.values(), epoch);
                profile("Save");
                //save rules

                saveRules();


                //wait for barrier of all switches
                long currentTime = System.currentTimeMillis();
                int passedBarrierSwitchesNum = 0;
                double sendCheckDelayBudget = epochSize * 1000 / 2;
//                System.out.println("Barrier wait numbers");
                while (passedBarrierSwitchesNum < switches.size() && (System.currentTimeMillis() - currentTime) < sendCheckDelayBudget) {
                    for (SwitchData switchData : switches.values()) {
                        long nanoDelay = switchData.waitOnLastSend(10); //wait 1us
                        if (nanoDelay >= 0) {
//                            System.out.println(nanoDelay / 1000000.0 + "ms");
                            passedBarrierSwitchesNum++;
                        } else {
//                        System.out.println("not received for " + switchData.getStringId());
                        }
                    }
                }
                if (passedBarrierSwitchesNum < switches.size()) {
                    PeriodicReport.log.warn("Could not verify barrier before " + sendCheckDelayBudget + " only got for " + passedBarrierSwitchesNum);
                }
                profile(null);
                System.out.println(epoch + "," + (System.nanoTime() - t) / 1e6);

                //now write logs and do cleaning stuffs
                runtimeTaskHandler.writeLogs(epoch);
                writeProfiles();

                runtimeTaskHandler.runEvents(epoch); //the eventrunner itself will +1 this
                //get stats
//                computeRuleLifeTimes();

                epoch++;
                if (epoch % 10 == 0) {
                    System.gc();
                }
            } else {
                report();
                for (SwitchData switchData : switches.values()) {
                    if (switchData.isTrafficStarted(10000 * epochSize)) {
                        seenTraffic = true;
                        System.out.println("Seen starting traffic from " + switchData);
                        break;
                    }
                }
                saveRules();
            }
            Util.flushAllControlledWriters();

        } catch (Exception e) {
            PeriodicReport.log.warn("Exception in report(): {}", e);
        }
    }

    /**
     * This is another way of computing the control loop delay.
     * The control loop delay will be the measurement epoch - average rule lifetime at the switches
     */
    private void computeRuleLifeTimes() {
        double sum = 0;
        int num = 0;
        for (SwitchData switchData : switches.values()) {
            for (OFFlowStatisticsReply statisticsReply : switchData.getLastFetchedFlows()) {
                if (statisticsReply.getMatch().getNetworkSourceMaskLen() > 0) {
                    sum += statisticsReply.getDurationSeconds() + statisticsReply.getDurationNanoseconds() / 1E9;
                    num++;
                }
            }
        }
        if (num > 0) {
            System.out.println(String.format(epoch + ": avgruleduration,%f,%d", sum / num, num));
        }
    }

    private void reschedule() {
        if (!runtimeTaskHandler.noEvent()) {
            flowInfoTask.reschedule(epochSize, TimeUnit.SECONDS);
        } else {
            finish();
        }
    }

    public boolean finished() {
        return runtimeTaskHandler.noEvent();
    }

    private void saveRules() {
        PeriodicReport.log.info("save ");
        for (SwitchData switchData : switches.values()) {
            multiThread.offer(switchData.saveRules(epoch));
        }
        multiThread.runJoin();
    }

    public void init(Map<String, SwitchData> switches, SingletonTask flowInfoTask) {
        this.switches = switches;
        this.flowInfoTask = flowInfoTask;
    }

    public void receive(IOFSwitch sw, OFMessage msg) {
//        PeriodicReport.log.warn("Unhandled OF Message: {} from {}", msg, sw);
//        pktIns++;
    }

    @Override
    public void writeProfiles() {
        if (profiler != null) {
            profiler.write();
            runtimeTaskHandler.writeProfiles();
        }
    }

    @Override
    public void createProfiler() {
        this.profiler = new LatencyProfiler(getClass());
        runtimeTaskHandler.createProfiler();
    }

    @Override
    public void finishProfiler() {
        if (profiler != null) {
            profiler.finish();
            runtimeTaskHandler.finishProfiler();
        }
    }

    public void finish() {
        runtimeTaskHandler.finish(epoch);
        multiThread.finishThreads();
        for (SwitchData switchData : switches.values()) {
            switchData.finish();
        }

        finishProfiler();
    }

    protected void profile(String s) {
        if (profiler != null) {
            profiler.sequentialRecord(s);
        }
    }

    private void report() {
        for (final SwitchData switchData : switches.values()) {
            multiThread.offer(switchData.getStats(epoch));
        }
        multiThread.runJoin();
    }
}
