package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow;

import edu.usc.enl.dynamicmeasurement.algorithms.matcher.HashMatcher;
import edu.usc.enl.dynamicmeasurement.algorithms.matcher.Matcher;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.HHHPacketUser;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import org.w3c.dom.Element;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 3/3/13
 * Time: 7:49 AM <br/>
 * The parent class for all TCAM-based HHH detection algorithms.
 */
public class FlowHHHPacketUser extends HHHPacketUser {
    private final Matcher matcher;
    private final int updatePerReport;
    private int updates = 0;
    private ControlledBufferWriter monitorWriter = null;


    public FlowHHHPacketUser(Element element) throws Exception {
        super(element);
        this.updatePerReport = 1;
        //this cannot be moved to the parent of HHH algorithm
        matcher = new HashMatcher();
        matcher.setMonitors(((FlowHHHAlgorithm) hhhAlgorithm).getMonitors());
    }

    @Override
    public void setFolder(String folder) {
        super.setFolder(folder);
//        try {
//            this.monitorWriter = Util.getNewWriter(folder + "/monitors.csv", true);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void update(int step) {
        super.update(step);
        matcher.setMonitors(((FlowHHHAlgorithm) hhhAlgorithm).getMonitors());
    }

    @Override
    public void process2(DataPacket p) {
        super.process2(p);
        //update monitors
        WildcardPattern match = matcher.match(p);
        if (match != null) {
            match.setWeight(match.getWeight() + p.getSize());
        } else {
            throw new RuntimeException("No matcher found");
        }
    }

    @Override
    public void report(int step) {
        super.report(step);
        //write monitors in a file
//        if (monitorWriter != null) {
//            Collection<WildcardPattern> monitors = hhhAlgorithm.getMonitors();
//            for (WildcardPattern wildcardPattern : monitors) {
//                monitorWriter.println(step + "," + wildcardPattern.toStringNoWeight() + "," + wildcardPattern.getWeight());
//            }
//            monitorWriter.flush();
//        }
    }

    @Override
    protected void reset() {
        if (updates % updatePerReport == 0) {
            super.reset();
        }
    }

    @Override
    protected void reportHHH(Collection<WildcardPattern> hhh, int step) {
        super.reportHHH(hhh, step);
    }

    @Override
    public void finish(FinishPacket p) {
        super.finish(p);
//        monitorWriter.close();
    }
}
