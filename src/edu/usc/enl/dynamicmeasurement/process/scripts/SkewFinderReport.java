package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 6/25/13
 * Time: 6:56 PM
 * <br/> Just a packet user class to find the skew of the traffic from source IPs.
 * The output will be step, srcip IP, size ordered based on step and size.
 * Later use the output to find the skew as the slope of the curve of
 * frequency vs ordered seen size.
 */
public class SkewFinderReport extends StepPacketUser {
    private Map<Long, Double> srcIPSize = new HashMap<>();
    private List<Long> buffer = new ArrayList<>();
    private PrintWriter pw;
    private final int wildcard;

    public SkewFinderReport(boolean resetOnStep, int wildcard) {
        super();
        this.wildcard = wildcard;
    }

    public void setStatsOutputWriter(PrintWriter pw) {
        this.pw = pw;
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        long srcIP = p.getSrcIP() >>> wildcard;
        Double sum = srcIPSize.get(srcIP);
        if (sum == null) {
            sum = 0d;
        }
        Double value = sum + p.getSize();
        srcIPSize.put(srcIP, value);
    }

    @Override
    protected void step(EpochPacket p) {
        buffer.addAll(srcIPSize.keySet());
        Collections.sort(buffer, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return -Double.compare(srcIPSize.get(o1), srcIPSize.get(o2));
            }
        });
        for (Long srcIP : buffer) {
            Double sum = srcIPSize.get(srcIP);
            pw.println(p.getStep() + "," + srcIP + "," + sum);
        }
        pw.flush();
        buffer.clear();
        srcIPSize.clear();
    }

    @Override
    public void finish(FinishPacket p) {
        pw.close();
    }
}
