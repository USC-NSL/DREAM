package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh;

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
 * User: masoud
 * Date: 8/17/13
 * Time: 7:14 PM <br/>
 * The task implementation for heavy hitter detection.
 * <p>The XML constructor requires a Property tag with name attribute as "Algorithm"
 * and with value attribute pointing to a class that implements HHAlgorithm.</p>
 *
 * @see edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.HHAlgorithm
 */
public class HHPacketUser extends TaskUser {

    protected final HHAlgorithm hhAlgorithm;
    private int step;
    private double sum;
    private ControlledBufferWriter reportPrintWriter;

    public HHPacketUser(Element element) throws Exception {
        super(element);
        Element algorithmElement = Util.getChildrenProperties(element, "Property").get("Algorithm");
        this.hhAlgorithm = (HHAlgorithm) Class.forName(algorithmElement.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class).newInstance(algorithmElement);
        step = 0;
    }

    public HHPacketUser(HHAlgorithm hhAlgorithm, ControlledBufferWriter reportPrintWriter) {
        super();
        this.hhAlgorithm = hhAlgorithm;
        this.reportPrintWriter = reportPrintWriter;
        sum = 0;
        step = 0;
    }

    public void report(int step) {
        this.step = step;
        hhAlgorithm.setStep(step);
        hhAlgorithm.setSum(sum);
        Collection<WildcardPattern> hh = hhAlgorithm.findHH();
        reportHH(hh);
    }

    /**
     * write the report in a sorted manner
     *
     * @param hh
     */
    protected void reportHH(Collection<WildcardPattern> hh) {
//        System.out.println(report + ": " + hh.size() + " hhs");
        List<WildcardPattern> hh_Sorted = new ArrayList<>(hh);
        Collections.sort(hh_Sorted);
        for (WildcardPattern wildcardPattern : hh) {
            reportPrintWriter.println(step + "," + wildcardPattern.toStringNoWeight()
                    + "," + wildcardPattern.getWeight());
        }
        reportPrintWriter.flush();
    }

    public void update(int step) {
        this.step = step;
        hhAlgorithm.setStep(step);
        hhAlgorithm.update(step);
        reset();
    }

    protected void reset() {
        sum = 0;
        hhAlgorithm.reset();
    }

    @Override
    public void process2(DataPacket p) {
        sum += p.getSize();
        long srcIP = p.getSrcIP();
        hhAlgorithm.match(srcIP, p.getSize());
    }

    @Override
    public void finish(FinishPacket p) {
        hhAlgorithm.finish();
        reportPrintWriter.close();
    }

    @Override
    public void setFolder(String folder) {
        hhAlgorithm.setFolder(folder);
        try {
            if (reportPrintWriter != null) {
                reportPrintWriter.close();
            }
            reportPrintWriter = Util.getNewWriter(folder + "/hhh.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Task2.TaskImplementation getImplementation() {
        return hhAlgorithm;
    }
}