package edu.usc.enl.dynamicmeasurement.floodlight;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.Packet;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.event.Event;
import edu.usc.enl.dynamicmeasurement.model.event.EventRunner;
import edu.usc.enl.dynamicmeasurement.model.event.TaskEvent;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.WildcardMonitorPoint;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.util.Util;
import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.counter.ICounterStoreService;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.openflow.protocol.OFBarrierReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/8/13
 * Time: 9:59 PM <br/>
 * The main module of DREAM on Floodlight.
 * The experiment starts in the following way:
 * In the beginning the controller waits for all switches to register.
 * Once all switches register, it initiates a periodic configure-fetch algorithm in EpochProcedure class
 */
public class PeriodicReport implements IFloodlightModule, IOFSwitchListener, IOFMessageListener {
    protected static Logger log = LoggerFactory.getLogger(PeriodicReport.class);
    // Module dependencies
    protected IFloodlightProviderService floodlightProvider;
    protected IThreadPoolService threadPool;
    protected ICounterStoreService counterStore;
    //
    protected Map<String, SwitchData> registeredSwitches;
    private Map<String, MonitorPoint> toRegisterSwitches;
    /**
     * The number of seconds of an epoch
     */
    private int epochSize;
    /**
     * The actual periodical algorithm
     */
    private EpochProcedure doEachEpoch;
    /**
     * The folder that contains the experiment configuration.
     */
    private String configFolder;

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
        l.add(ICounterStoreService.class);
        l.add(IThreadPoolService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        WildcardPattern.TOTAL_LENGTH = 32;
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        counterStore = context.getServiceImpl(ICounterStoreService.class);
        threadPool = context.getServiceImpl(IThreadPoolService.class);
        registeredSwitches = new HashMap<>();
        Map<String, String> configParams = context.getConfigParams(this);
        epochSize = Integer.parseInt(configParams.get("epochSize"));
        configFolder = configParams.get("configFolder");
        loadConfigFile(configFolder);
    }

    public String getConfigFolder() {
        return configFolder;
    }

    private void loadConfigFile(String filename) throws FloodlightModuleException {
        try {
            File file = new File(filename);
            if (file.isDirectory()) {
                filename = file.getAbsolutePath() + "/config.xml";
            }
            ConfigReader configReader = new ConfigReader();
            configReader.read(filename);
            log.info("Load config file " + filename);
            //get list of switches
            toRegisterSwitches = new HashMap<>();
            for (MonitorPoint monitorPoint : Util.getNetwork().getMonitorPoints()) {
                toRegisterSwitches.put(monitorPoint.getStringId(), monitorPoint);
            }
            TaskHandler taskHandler = configReader.getTaskHandler();
            for (Event event : configReader.getEvents()) {
                event.setEpoch(event.getEpoch() / epochSize); // the traffic generation is based on per second epochs, but run is different
                if (event instanceof TaskEvent) {
                    ((TaskEvent) event).setHandler(taskHandler);
                }
            }

            // wrap it by event runner
            EventRunner eventRunner = new EventRunner(new PacketUser() {
                @Override
                public void process(Packet p) {

                }

                @Override
                public void finish(FinishPacket p) {

                }
            }, configReader.getEvents());
            RuntimeTaskHandler runtimeTaskHandler = new RuntimeTaskHandler(taskHandler, eventRunner, epochSize);
            doEachEpoch = new EpochProcedure(runtimeTaskHandler, epochSize);
        } catch (Exception e) {
            throw new FloodlightModuleException(e);
        }
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFSwitchListener(this);
        floodlightProvider.addOFMessageListener(OFType.BARRIER_REPLY, this);
    }

    @Override
    public void switchAdded(long switchId) {
        IOFSwitch sw = floodlightProvider.getSwitch(switchId);
        if (sw == null)
            return;
        registeredSwitches.put(sw.getStringId(), new SwitchData(sw, floodlightProvider, (WildcardMonitorPoint) toRegisterSwitches.get(sw.getStringId())));
        if (registeredSwitches.size() >= toRegisterSwitches.size()) {
            log.info("All switches are connected.");
            ScheduledExecutorService ses = threadPool.getScheduledExecutor();
            SingletonTask flowInfoTask = new SingletonTask(ses, doEachEpoch);
            doEachEpoch.init(registeredSwitches, flowInfoTask);
            flowInfoTask.reschedule(0, TimeUnit.SECONDS);
        } else {
            log.info("Still waiting for " + (toRegisterSwitches.size() - registeredSwitches.size()) + " more switches");
        }
    }

    @Override
    public void switchRemoved(long switchId) {
        registeredSwitches.remove("" + switchId);
    }

    @Override
    public void switchActivated(long switchId) {

    }

    @Override
    public void switchPortChanged(long switchId, ImmutablePort port, IOFSwitch.PortChangeType type) {

    }

    @Override
    public void switchChanged(long switchId) {

    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        if (msg.getType().equals(OFType.BARRIER_REPLY)) {
            SwitchData switchData = registeredSwitches.get(sw.getStringId());
            if (switchData != null) {
                switchData.receiveBarrier(((OFBarrierReply) msg));
            }
        } else {
            doEachEpoch.receive(sw, msg);
        }
        return Command.CONTINUE;
    }

    @Override
    public String getName() {
        return "PeriodicReport";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }
}
