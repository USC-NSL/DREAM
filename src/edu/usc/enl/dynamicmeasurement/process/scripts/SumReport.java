package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 2/4/13
 * Time: 9:29 AM
 */
public class SumReport extends StepPacketUser {
    boolean resetOnStep;
    private double sum = 0;
    private boolean printReport = false;
    private List<Double> output;

    public SumReport(boolean resetOnStep, boolean printReport) {
        super();
        output = new LinkedList<>();
        this.printReport = printReport;
        this.resetOnStep = resetOnStep;
    }

    public SumReport(boolean resetOnStep) {
        this(resetOnStep, false);
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        //System.out.println(p.getSrcIP() + "," + p.getSize());
        sum += p.getSize();
    }

    @Override
    protected void step(EpochPacket p) {
        if (printReport) {
            System.out.println(p.getStep() + "," + sum + "," + p.getTime());
        }
        output.add(sum);
    }

    @Override
    protected void reset() {
        if (resetOnStep) {
            sum = 0;
        }
    }

    @Override
    public void finish(FinishPacket p) {
    }

    public List<Double> getOutput() {
        return output;
    }
}
