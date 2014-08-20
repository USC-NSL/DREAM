package edu.usc.enl.dynamicmeasurement.floodlight;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.NeedInitHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.FlowHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.WildcardMonitorPoint;
import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.openflow.protocol.OFBarrierReply;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/25/14
 * Time: 8:05 AM <br/>
 * A simple floodlight module to install rules, an delete rules from switches to calculate the delay of that
 * using the profiler class
 */
public class SwitchTest implements IFloodlightModule, IOFSwitchListener, IOFMessageListener {
    protected IFloodlightProviderService floodlightProvider;
    protected IThreadPoolService threadPool;
    private SwitchData switchData;
    private int rulesNum;
    private MyRunnable task;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<>();
        l.add(IFloodlightProviderService.class);
        l.add(IThreadPoolService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        threadPool = context.getServiceImpl(IThreadPoolService.class);
        Map<String, String> configParams = context.getConfigParams(this);
        rulesNum = Integer.parseInt(configParams.get("rulesNum"));
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFSwitchListener(this);
        floodlightProvider.addOFMessageListener(OFType.BARRIER_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        if (msg.getType().equals(OFType.BARRIER_REPLY)) {
            if (this.switchData != null) {
                this.switchData.receiveBarrier(((OFBarrierReply) msg));
            }
        } else {
            System.out.println(sw + "-->" + msg);
        }
        return Command.CONTINUE;
    }

    @Override
    public String getName() {
        return "BarrierTest";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    @Override
    public void switchAdded(long switchId) {

    }

    @Override
    public void switchRemoved(long switchId) {
        if (switchData != null && switchId == switchData.getIntId()) {
            switchData = null;
            System.out.println("switch " + switchId + " removed");
            task.cancel();
        }
    }

    @Override
    public void switchActivated(long switchId) {
        if (switchData != null) { //just one switch
            return;
        }
        System.out.println("switch " + switchId + " wants to connect");
        if (switchId > 100 || switchId == 1) {//just to ignore other switches except the switch with mac 1
            final IOFSwitch sw = floodlightProvider.getSwitch(switchId);
            ScheduledExecutorService ses = threadPool.getScheduledExecutor();
            this.switchData = new SwitchData(sw, floodlightProvider,
                    new WildcardMonitorPoint(rulesNum, new HashSet<>(Arrays.asList(new WildcardPattern(0, WildcardPattern.TOTAL_LENGTH, 0)))));
            task = new MyRunnable(this.switchData, rulesNum);
            SingletonTask flowInfoTask = new SingletonTask(ses, task);
            task.setFlowInfoTask(flowInfoTask);
            flowInfoTask.reschedule(5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void switchPortChanged(long switchId, ImmutablePort port, IOFSwitch.PortChangeType type) {

    }

    @Override
    public void switchChanged(long switchId) {

    }

    private enum State {INIT, SAVE, DELETE}

    private class MyRunnable implements Runnable {
        private final SwitchData sw;
        private final ArrayList<OFFlowStatisticsReply> stats;
        private SingletonTask flowInfoTask;
        private List<OFMessage> rules;
        private List<OFMessage> deleteRules;
        private State state;
        private boolean cancel = false;
        private int epoch = 0;

        public MyRunnable(SwitchData sw, int rulesToInstall) {
            this.sw = sw;
            rules = new ArrayList<>();
            OFFlowMod defaultFlowMod = RuleSaverThreadMethod.getOfFlowMod(RuleSaverThreadMethod.DEFAULT_PATTERN, sw, false);
            rules.add(defaultFlowMod);
            final List<WildcardPattern> patterns = new ArrayList<>();
            try {
                FlowHHHAlgorithm.initMonitors(rulesToInstall, new NeedInitHHHAlgorithm() {
                    @Override
                    public void addMonitor(WildcardPattern wildcardPattern) {
                        patterns.add(wildcardPattern);
                    }

                    @Override
                    public WildcardPattern pollAMonitor() {
                        return patterns.remove(patterns.size() - 1);
                    }
                }, RuleSaverThreadMethod.DEFAULT_PATTERN.clone().goDown(true));
            } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
                invalidWildCardValue.printStackTrace();
            }
            for (WildcardPattern pattern : patterns) {
                rules.add(RuleSaverThreadMethod.getOfFlowMod(pattern, sw, true));
            }

            deleteRules = new ArrayList<>();
            for (WildcardPattern pattern : patterns) {
                deleteRules.add(RuleSaverThreadMethod.getDeleteRule(pattern, sw.getFlow()));
            }

            stats = new ArrayList<>();
            state = State.INIT;
        }

        private void setFlowInfoTask(SingletonTask flowInfoTask) {
            this.flowInfoTask = flowInfoTask;
        }

        @Override
        public void run() {
            if (cancel) {
                return;
            }
            if (state == State.DELETE || state == State.SAVE) {
                stats.clear();
                long time = System.nanoTime();
                RuleFetcherThreadMethod.fetch(sw.getSw(), stats);
                long fetchDelay = System.nanoTime() - time;
                int size = stats.size();
                System.out.println("Fetch, " + epoch + "," + fetchDelay / 1e6);
            }

            try {
                Long time = System.nanoTime();
                if (state == State.SAVE) {
                    sw.write(deleteRules);
                    state = State.DELETE;
                    System.out.print("Delete, ");
                } else if (state == State.INIT || state == State.DELETE) {
                    sw.write(rules);
                    state = State.SAVE;
                    System.out.print("Save, ");
                }
                sw.sendBarrier();
                sw.flush();

                long nanoDelay = -1;
                while (System.nanoTime() - time < 600e9) {
                    nanoDelay = sw.waitOnLastSend(10);//wait 10us
                    if (nanoDelay >= 0) {
                        System.out.println(epoch + "," + (System.nanoTime() - time) / 1e6);
                        break;
                    }
                }
                if (nanoDelay < 0) {
                    System.out.println(epoch + ",-1");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            epoch++;
            flowInfoTask.reschedule(5, TimeUnit.SECONDS);
        }

        public void cancel() {
            cancel = true;

        }
    }
}
