package edu.usc.enl.dynamicmeasurement.floodlight;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import net.floodlightcontroller.core.IOFSwitch;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/18/13
 * Time: 3:57 PM <br/>
 * This class is responsible to fetch counters from a single switch
 */
class RuleFetcherThreadMethod implements Runnable {
    private static final long INT_MASK = ((1l << 32) - 1);
    private final IOFSwitch iofSwitch;
    /**
     * Raw OpenFlow Stats Reply messages
     */
    private List<OFFlowStatisticsReply> stats;
    /**
     * Fetched rules will be saved in this data structure.
     * The data structure is passed in the constructor and is usually kept in the SwitchData objects
     */
    private Map<WildcardPattern, WildcardPattern> fetchedRules;
    /**
     * Keeps track of last fetched rules to decrease their weight from the new ones if anything is common
     */
    private Map<WildcardPattern, WildcardPattern> lastFetchedRules;
    private int epoch;

    public RuleFetcherThreadMethod(IOFSwitch iofSwitch, int capacity, Map<WildcardPattern, WildcardPattern> fetchedRules,
                                   Map<WildcardPattern, WildcardPattern> lastFetchedRules) {
        this.iofSwitch = iofSwitch;
        this.stats = new ArrayList<>(capacity);
        this.fetchedRules = fetchedRules;
        this.lastFetchedRules = lastFetchedRules;
    }

    public static void fetch(IOFSwitch sw, List<OFFlowStatisticsReply> stats) {
        List<OFStatistics> values = null;
        Future<List<OFStatistics>> future;

        // Statistics request object for getting flows
        OFStatisticsRequest req = new OFStatisticsRequest();
        req.setStatisticType(OFStatisticsType.FLOW);
        int requestLength = req.getLengthU();
        OFFlowStatisticsRequest specificReq = new OFFlowStatisticsRequest();
        specificReq.setMatch(new OFMatch().setWildcards(0xffffffff));
        specificReq.setOutPort(OFPort.OFPP_NONE.getValue());
        specificReq.setTableId((byte) 0xff);
        req.setStatistics(Collections.singletonList((OFStatistics) specificReq));
        requestLength += specificReq.getLength();
        req.setLengthU(requestLength);

        try {
            future = sw.queryStatistics(req);
            values = future.get(2, TimeUnit.SECONDS); //FIXME: is 2 the right number?
            if (values != null) {
//                System.out.println("got in");
                for (OFStatistics stat : values) {
                    stats.add((OFFlowStatisticsReply) stat);
                }
            } else {
                PeriodicReport.log.warn("Got null flow list from " + sw);
            }
        } catch (Exception e) {
            PeriodicReport.log.error("Failure retrieving statistics from switch " + sw, e);
        }
    }

    @Override
    public void run() {
        fetchStats(iofSwitch);
    }

    public Collection<OFFlowStatisticsReply> getStats() {
        return stats;
    }

    /**
     * @param sw the switch object that we wish to get flows from
     *           a list of OFFlowStatisticsReply objects or essentially flows
     */
    public void fetchStats(IOFSwitch sw) {
        stats.clear();

        fetch(sw, stats);
//        Map<WildcardPattern, WildcardPattern> testOldSaved = new HashMap<>(fetchedRules);
        fetchedRules.clear();
        for (OFFlowStatisticsReply statisticsReply : stats) {
            int wildcardNum = WildcardPattern.TOTAL_LENGTH - statisticsReply.getMatch().getNetworkSourceMaskLen();
            long l = statisticsReply.getMatch().getNetworkSource();
            WildcardPattern pattern = new WildcardPattern((l & INT_MASK) >>> wildcardNum, wildcardNum, statisticsReply.getByteCount());
            fetchedRules.put(pattern, pattern);
        }

//        testOldSaved.keySet().removeAll(fetchedRules.keySet());
//        for (WildcardPattern wildcardPattern : testOldSaved.keySet()) {
//            System.out.println("saved "+wildcardPattern+ " but not found it! ");
//        }

        for (WildcardPattern newRule : fetchedRules.values()) {
            WildcardPattern oldRule = lastFetchedRules.get(newRule);
            if (oldRule != null) {
                double weight = newRule.getWeight() - oldRule.getWeight();
                oldRule.setWeight(newRule.getWeight());
                newRule.setWeight(weight);
            } else {
                lastFetchedRules.put(newRule, newRule);
            }
        }
        lastFetchedRules.keySet().retainAll(fetchedRules.keySet());
        if (PeriodicReport.log.isInfoEnabled()) {
            printReport();
        }
    }

    /**
     * Print all stats received (just for debugging)
     */
    private void printReport() {

        PeriodicReport.log.info("report");
        Collections.sort(stats, new Comparator<OFFlowStatisticsReply>() {
            @Override
            public int compare(OFFlowStatisticsReply o1, OFFlowStatisticsReply o2) {
                return Integer.compare(o1.getMatch().getNetworkSource(), o2.getMatch().getNetworkSource());
            }
        });
        PeriodicReport.log.info(iofSwitch + "->");
        int i = 0;
        for (OFFlowStatisticsReply reply : stats) {
            PeriodicReport.log.info("\t " + i + ": " + reply.getMatch() + "," + reply.getByteCount());
            i++;
        }
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }
}
