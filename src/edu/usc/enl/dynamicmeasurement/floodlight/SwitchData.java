package edu.usc.enl.dynamicmeasurement.floodlight;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.WildcardMonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import edu.usc.enl.dynamicmeasurement.util.Util;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import org.openflow.protocol.OFBarrierReply;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/18/13
 * Time: 3:57 PM
 * <br/>
 * A class that bundles the data and functionalities of a switch in dREAM
 */
class SwitchData {
    public static final List<WildcardPattern> NON_DEFAULT_DELETE_PATTERNS = Arrays.asList(new WildcardPattern(0, 31, 0), new WildcardPattern(1, 31, 0));
    /**
     * the corresponding OpenFlow switch
     */
    private final IOFSwitch sw;
    private final RuleFetcherThreadMethod ruleFetcherThreadMethod;
    private final RuleSaverThreadMethod ruleSaverThreadMethod;
    private final Map<Integer, CountDownLatch> observerMap;
    /**
     * The temporary datastructure to keep track of current rules
     */
    private final Map<WildcardPattern, WildcardPattern> workingRules;
    private final IFloodlightProviderService floodlightProvider;
    private final int id;
    /**
     * The log is used to track the number of rules installed, deleted or kept untouched in this switch.
     */
    private ControlledBufferWriter logWriter;
    //to find the delay of the barrier
    private CountDownLatch latch;
    private long timeToReleaseLatch;

    SwitchData(IOFSwitch aSwitch, IFloodlightProviderService floodlightProvider, WildcardMonitorPoint monitorPoint) {
        this.sw = aSwitch;
        this.floodlightProvider = floodlightProvider;
        int capacity = monitorPoint.getCapacity();
        workingRules = new ConcurrentHashMap<>(capacity);
        Map<WildcardPattern, WildcardPattern> lastFetchedRules = new HashMap<>(capacity);
        ruleFetcherThreadMethod = new RuleFetcherThreadMethod(aSwitch, capacity, workingRules, lastFetchedRules);
        ruleSaverThreadMethod = new RuleSaverThreadMethod(this, capacity, workingRules, lastFetchedRules);
        observerMap = new HashMap<>();
        this.id = monitorPoint.getIntId();
        try {
            logWriter = Util.getNewWriter(Util.getRootFolder() + "/switch" + id + ".txt", false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public IOFSwitch getSw() {
        return sw;
    }

    public Map<WildcardPattern, WildcardPattern> getWorkingRules() {
        return workingRules;
    }

    @Override
    public String toString() {
        return "SwitchData{" +
                "sw=" + sw +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SwitchData that = (SwitchData) o;

        if (sw != null ? !sw.equals(that.sw) : that.sw != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sw != null ? sw.hashCode() : 0;
    }

    protected Runnable getStats(int epoch) {
        ruleFetcherThreadMethod.setEpoch(epoch);
        return ruleFetcherThreadMethod;
    }

    protected RuleSaverThreadMethod saveRules(int epoch) {
        ruleSaverThreadMethod.setEpoch(epoch);
        return ruleSaverThreadMethod;
    }

    public String getStringId() {
        return sw.getStringId();
    }

    public int getIntId() {
        return id;
    }

    public OFFlowMod getFlow() {
        return (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
    }

    public OFMessage getBarrier() throws IOException {
        OFMessage barrierMsg = floodlightProvider.getOFMessageFactory().getMessage(OFType.BARRIER_REQUEST);
        barrierMsg.setXid(sw.getNextTransactionId());
        return barrierMsg;
    }

    /**
     * Send the barrrier and setup the timer and latch
     *
     * @return
     * @throws IOException
     */
    public CountDownLatch sendBarrier() throws IOException {
        OFMessage barrierMsg = getBarrier();
        latch = new CountDownLatch(1);
        observerMap.put(barrierMsg.getXid(), latch);
        timeToReleaseLatch = System.nanoTime();
        sw.write(barrierMsg, null);
        return latch;
    }

    public void write(OFFlowMod flowMod) throws IOException {
        sw.write(flowMod, null);
    }

    public void write(List<OFMessage> flowMod) throws IOException {
        sw.write(flowMod, null);
    }

    public void flush() {
        sw.flush();
    }

    /**
     * The barrier is received. Stop the counter and release the latch so that thread can go through
     *
     * @param msg
     */
    public void receiveBarrier(OFBarrierReply msg) {
//        System.out.println("receive barrier msg " + msg);
        CountDownLatch l = observerMap.get(msg.getXid());
        if (l != null && l == latch) {
            latch.countDown();
        } else {
//            PeriodicReport.log.warn("Unknown barrier reply received " + msg);
        }
    }

    public long waitOnLastSend(int timeout) throws InterruptedException {
        boolean await = latch.await(timeout, TimeUnit.MICROSECONDS);
        if (await) {
            return System.nanoTime() - timeToReleaseLatch;
        } else {
            return -1;
        }
    }

    public Collection<OFFlowStatisticsReply> getLastFetchedFlows() {
        return ruleFetcherThreadMethod.getStats();
    }

    public boolean isTrafficStarted(double threshold) {
        for (WildcardPattern wildcardPattern : workingRules.keySet()) {
            if (wildcardPattern.getWeight() > threshold) {
                return true;
            }
        }
        return false;
    }

    ControlledBufferWriter getLogWriter() {
        return logWriter;
    }

    public void finish() {
        logWriter.close();
    }
}
