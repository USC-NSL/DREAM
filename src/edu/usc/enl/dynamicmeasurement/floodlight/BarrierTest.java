package edu.usc.enl.dynamicmeasurement.floodlight;

import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 11/7/13
 * Time: 2:14 PM <br/>
 * Just a simple class to make sure the barrier algorithm is implemented right
 */
public class BarrierTest implements IFloodlightModule, IOFSwitchListener, IOFMessageListener {
    protected IFloodlightProviderService floodlightProvider;
    protected IThreadPoolService threadPool;

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
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFSwitchListener(this);
        floodlightProvider.addOFMessageListener(OFType.BARRIER_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        System.out.println(sw + "-->" + msg);
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
        Map<OFType, List<IOFMessageListener>> listeners = floodlightProvider.getListeners();
        for (Map.Entry<OFType, List<IOFMessageListener>> entry : listeners.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        final IOFSwitch sw = floodlightProvider.getSwitch(switchId);
        ScheduledExecutorService ses = threadPool.getScheduledExecutor();
        MyRunnable task = new MyRunnable(sw);
        SingletonTask flowInfoTask = new SingletonTask(ses, task);
        task.setFlowInfoTask(flowInfoTask);

        flowInfoTask.reschedule(2, TimeUnit.SECONDS);
    }

    @Override
    public void switchRemoved(long switchId) {

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

    private class MyRunnable implements Runnable {
        private final IOFSwitch sw;
        private SingletonTask flowInfoTask;

        public MyRunnable(IOFSwitch sw) {
            this.sw = sw;
        }

        private void setFlowInfoTask(SingletonTask flowInfoTask) {
            this.flowInfoTask = flowInfoTask;
        }

        @Override
        public void run() {
            OFMessage barrierMsg = floodlightProvider.getOFMessageFactory().getMessage(OFType.BARRIER_REQUEST);
            barrierMsg.setXid(sw.getNextTransactionId());
            try {
                sw.write(barrierMsg, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sw.flush();
            flowInfoTask.reschedule(2, TimeUnit.SECONDS);
        }
    }
}
