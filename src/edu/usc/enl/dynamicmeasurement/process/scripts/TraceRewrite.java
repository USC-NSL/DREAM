package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/20/13
 * Time: 4:12 PM
 */
public class TraceRewrite extends StepPacketUser {
    private PrintWriter pw;
    private int chop;
    private int currentStepInChop;
    private int fileNameIndex;
    private String prefix;

    public TraceRewrite(String prefix, int chop) throws IOException {
        fileNameIndex = 0;
        currentStepInChop = 1;
        this.chop = chop;
        this.prefix = prefix;
        getWriter(prefix);
    }

    private void getWriter(String s) {
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(s + "_" + fileNameIndex)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        pw.println(p.print());
    }

    @Override
    protected void step(EpochPacket p) {
        if (currentStepInChop < chop) {
            pw.flush();
            currentStepInChop++;
        } else {
            currentStepInChop = 1;
            pw.close();
            fileNameIndex++;
            getWriter(prefix);
        }
    }

    @Override
    public void finish(FinishPacket p) {
        pw.close();
    }
}
