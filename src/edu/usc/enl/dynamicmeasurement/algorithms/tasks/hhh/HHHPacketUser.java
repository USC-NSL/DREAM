package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.TaskUser;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/24/13
 * Time: 10:31 AM <br/>
 * The task implementation for hierarchical heavy hitter detection.
 * <p>The XML constructor requires a Property tag with name attribute as "Algorithm"
 * and with value attribute pointing to a class that implements HHHAlgorithm.</p>
 *
 * @see edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.HHHAlgorithm
 */
public class HHHPacketUser extends TaskUser {
    protected final HHHAlgorithm hhhAlgorithm;
    private double sum;
    private ControlledBufferWriter reportPrintWriter;

    public HHHPacketUser(Element element) throws Exception {
        super(element);
        Element algorithmElement = Util.getChildrenProperties(element, "Property").get("Algorithm");
        this.hhhAlgorithm = (HHHAlgorithm) Class.forName(algorithmElement.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class).newInstance(algorithmElement);
        sum = 0;
    }

    public void report(int step) {
        hhhAlgorithm.setStep(step);
        Collection<WildcardPattern> hhh = hhhAlgorithm.findHHH();
        reportHHH(hhh, step);
    }

    /**
     * Write a sorted set of HHHs in the report file
     *
     * @param hhh
     * @param step
     */
    protected void reportHHH(Collection<WildcardPattern> hhh, int step) {
//        System.out.println(report + ": " + hhh.size() + " hhhs");
        List<WildcardPattern> hhh_Sorted = new ArrayList<>(hhh);
        Collections.sort(hhh_Sorted);
        for (WildcardPattern wildcardPattern : hhh) {
            reportPrintWriter.println(step + "," + wildcardPattern.toStringNoWeight() + "," + wildcardPattern.getWeight());
        }
        reportPrintWriter.flush();
    }

    public void update(int step) {
        hhhAlgorithm.setStep(step);
        hhhAlgorithm.setSum(sum);
        hhhAlgorithm.doUpdate();
        reset();
    }

    protected void reset() {
        sum = 0;
        hhhAlgorithm.reset();
    }

    @Override
    public void process2(DataPacket p) {
        sum += p.getSize();
        long srcIP = p.getSrcIP();
        hhhAlgorithm.match(srcIP, p.getSize());
    }

    @Override
    public void finish(FinishPacket p) {
        hhhAlgorithm.finish();
        reportPrintWriter.close();
    }

    @Override
    public void setFolder(String folder) {
        try {
            if (reportPrintWriter != null) {
                reportPrintWriter.close();
            }
            reportPrintWriter = Util.getNewWriter(folder + "/hhh.csv");
            //new PrintWriter(folder + "/hhh.csv");
            hhhAlgorithm.setFolder(folder);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Task2.TaskImplementation getImplementation() {
        return hhhAlgorithm;
    }
}
