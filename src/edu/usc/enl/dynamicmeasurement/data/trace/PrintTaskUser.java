package edu.usc.enl.dynamicmeasurement.data.trace;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.TaskUser;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 11/2/13
 * Time: 9:19 AM  <br/>
 * This is a special task that appends whatever packets it sees into a new trace per switch.
 * Now we will have traces per switch.
 * This is used for preparing the trace for experiments on the prototype
 */
public class PrintTaskUser extends TaskUser {
    private static Map<MonitorPoint, PrintWriter> monitorPointWriters = new HashMap<>();
    private static int tasksNum = 0;
    private final DummyImplementation taskImplementation;
    private Map<Long, Integer> ipSize = new HashMap<>();
    private int taskNumber = 0;

    public PrintTaskUser(Element element) {
        super(element);
        taskImplementation = new DummyImplementation();
        tasksNum++;
        taskNumber = tasksNum;
    }

    /**
     * set the folder of traces to be written into
     *
     * @param folder
     */
    @Override
    public void setFolder(String folder) {
        if (monitorPointWriters.size() == 0) {
            String traceFolder = new File(folder).getAbsoluteFile().getParentFile().getAbsolutePath() + "/trace/";
            new File(traceFolder).mkdirs();
            try {
                for (MonitorPoint monitorPoint : Util.getNetwork().getMonitorPoints()) {
                    monitorPointWriters.put(monitorPoint, new PrintWriter(new BufferedWriter(new FileWriter(traceFolder + monitorPoint.getIntId() + ".txt", true))));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Task2.TaskImplementation getImplementation() {
        return taskImplementation;
    }

    @Override
    public void report(int step) {
//        System.out.println("Print: " + taskNumber + "," + step + "," + ipSize.size());
        for (Map.Entry<Long, Integer> ipSizeEntry : ipSize.entrySet()) {
            for (Map.Entry<MonitorPoint, PrintWriter> monitorPointWriterEntry : monitorPointWriters.entrySet()) {
                Long ip = ipSizeEntry.getKey();
                if (monitorPointWriterEntry.getKey().hasDataFrom(ip)) {
                    PrintWriter writer = monitorPointWriterEntry.getValue();
                    synchronized (writer) {
                        writer.println(step + "," + ip + "," + ipSizeEntry.getValue());
                    }
                    break;
                }
            }
        }
        ipSize.clear();
    }

    @Override
    public void update(int step) {
    }

    @Override
    public void process2(DataPacket p) {
        Integer size = ipSize.get(p.getSrcIP());
        if (size == null) {
            size = 0;
        }
        ipSize.put(p.getSrcIP(), (int) (size + p.getSize()));
    }

    @Override
    public void finish(FinishPacket p) {
        tasksNum--;
        if (tasksNum == 0) {
            for (PrintWriter printWriter : monitorPointWriters.values()) {
                printWriter.close();
            }
            monitorPointWriters.clear();
        } else {
            for (PrintWriter printWriter : monitorPointWriters.values()) {
                printWriter.flush();
            }
        }
    }

    private static class DummyImplementation implements MultiSwitchTask.MultiSwitchTaskImplementation, SingleSwitchTask.SingleSwitchTaskImplementation {
        @Override
        public void setCapacityShare(Map<MonitorPoint, Integer> resource) {

        }

        @Override
        public void estimateAccuracy(Map<MonitorPoint, Double> accuracy) {
            for (Map.Entry<MonitorPoint, Double> entry : accuracy.entrySet()) {
                entry.setValue(1d);
            }
        }

        @Override
        public double getGlobalAccuracy() {
            return 1;
        }

        @Override
        public void getUsedResources(Map<MonitorPoint, Integer> resource) {
            for (Map.Entry<MonitorPoint, Integer> entry : resource.entrySet()) {
                entry.setValue(0);
            }
        }

        @Override
        public void setCapacityShare(int resource) {

        }

        @Override
        public double estimateAccuracy() {
            return 1;
        }

        @Override
        public int getUsedResourceShare() {
            return 0;
        }
    }
}
