package edu.usc.enl.dynamicmeasurement.data.trace;

import edu.usc.enl.dynamicmeasurement.data.CachedMultiFileTraceReader;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.MultiFileTraceReader;
import edu.usc.enl.dynamicmeasurement.data.PacketLoader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.Util;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/23/13
 * Time: 2:20 PM  <br/>
 * This is the utility class that reads the trace associated with prefix of a task from a trace that covers that prefix
 */
public class TaskTraceReader implements TaskTraceReaderInterface {
    private final long epochSize;
    /**
     * The start time of the task (This will be added to the data packets timestamps)
     */
    private final long taskStart;
    /**
     * The prefix filter for the task
     */
    private final WildcardPattern filter;
    /**
     * The prefix filter for the trace
     */
    private final WildcardPattern tracePattern;
    /**
     * The data that must be added to the key of data packets to make sure that they match the trace pattern
     */
    private final long tracePatternShift;
    /**
     * The start of simulation time (this should be useless. just 0 should work)
     */
    private final long simulationStartTime;
    private PacketLoader.TraceReader br;
    /**
     * keeps track of a data packets came at the end of last epoch that should have been processed in the next epoch
     */
    private DataPacket keepDataForNext = null;
    private int traceCurrentTime = -1;
    private long lastPacketTime = -1;

    public TaskTraceReader(long taskStart, WildcardPattern filter, String[] files, WildcardPattern tracePattern, boolean cached) throws IOException {
        this.taskStart = taskStart;
        this.filter = filter;
        this.tracePattern = tracePattern;
        if (cached) {
            br = new CachedMultiFileTraceReader(new MultiFileTraceReader(files));
        } else {
            br = new MultiFileTraceReader(files);
        }
        epochSize = Util.getSimulationConfiguration().getEpoch();
        tracePatternShift = tracePattern.getData() << tracePattern.getWildcardNum();
        simulationStartTime = Util.getSimulationConfiguration().getStartTime();
    }


    /**
     * reset a trace file to read it from the beginning!
     */
    public void reset() {
        if (br instanceof CachedMultiFileTraceReader) {
            ((CachedMultiFileTraceReader) br).reset();
        } else {
            try {
                br.close();
                br = new MultiFileTraceReader(((MultiFileTraceReader) br).getFiles());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        keepDataForNext = null;
        traceCurrentTime = -1;
        lastPacketTime = -1;
    }

    public DataPacket getNextPacket(DataPacket p) throws DataPacket.PacketParseException, IOException {
        while (true) {
            if (keepDataForNext != null) {
                DataPacket temp = keepDataForNext;
                keepDataForNext = null;
                return temp;
            }
            String line = br.readLine();
            if (line == null) {
                return null;
            }
            StringTokenizer st = new StringTokenizer(line, ",");
            long time = Long.parseLong(st.nextToken());
            if (lastPacketTime != time) {
                if (time > lastPacketTime && time - lastPacketTime < 300) { //check if it is in this set of traces
                    traceCurrentTime += time - lastPacketTime;
                } else {
                    traceCurrentTime++;
                }
                lastPacketTime = time;
            }
            long srcIP = Long.parseLong(st.nextToken());
            srcIP = (srcIP >> (WildcardPattern.TOTAL_LENGTH - tracePattern.getWildcardNum())) + tracePatternShift;
            if (filter.match(srcIP)) {
                if (p == null) {
                    p = new DataPacket(new StringTokenizer(line, ","));
                } else {
                    p.fill(new StringTokenizer(line, ","));
                }
                p.setSrcIP(srcIP);
//                if (filter.toStringNoWeight().equals("01001100010_____________________")) {
//                    System.out.println(traceCurrentTime + " " + p.getSize());
//                }
                p.setTime(taskStart * epochSize + traceCurrentTime + simulationStartTime);
                return p;
            }
        }
    }

    public void finish() {
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void keepForNext(DataPacket p) {
        keepDataForNext = p;
    }
}
