package edu.usc.enl.dynamicmeasurement.data.scenario.traffic;


import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/27/13
 * Time: 9:31 PM
 */
public class TrafficGenerator {
    //output timestep pattern,size,starttime,finishtime pattern,size,starttime,finishtime pattern,size,starttime,finishtime
    public static void main(String[] args) throws FileNotFoundException {
        List<TrafficFlow> flows = new ArrayList<>();
        String output = args[0];
        long step = Long.parseLong(args[1]);
        long minTime = Long.MAX_VALUE;
        long maxTime = -1;
        for (int i = 2; i < args.length; i++) {
            String arg = args[i];
            String[] split = arg.split(",");
            long start = Long.parseLong(split[2]);
            long finish = Long.parseLong(split[3]);
            flows.add(new TrafficFlow(new WildcardPattern(split[0], 0), Integer.parseInt(split[1]), finish, start));
            minTime = Math.min(minTime, start);
            maxTime = Math.max(maxTime, finish);
        }

        TrafficGenerator trafficGenerator = new TrafficGenerator();
        try (PrintWriter pw = new PrintWriter(output)) {
            for (long time = minTime; time < maxTime; time += step) {
                trafficGenerator.writeFlows(time, flows, pw);
            }
        }
    }

    public void writeFlows(long time, List<TrafficFlow> flows, PrintWriter pw) {
        for (Iterator<TrafficFlow> iterator = flows.iterator(); iterator.hasNext(); ) {
            TrafficFlow flow = iterator.next();
            if (flow.getFinish() <= time) {
                iterator.remove();
                continue;
            }
            if (flow.getStart() <= time) {
                flow.print(pw, time);
            }
        }
    }

    private static class TrafficFlow {
        private final WildcardPattern pattern;
        private final int eachIPSize;
        private final long finish;
        private final long start;

        private TrafficFlow(WildcardPattern pattern, int eachIPSize, long finish, long start) {
            this.pattern = pattern;
            this.eachIPSize = eachIPSize;
            this.finish = finish;
            this.start = start;
        }

        private WildcardPattern getPattern() {
            return pattern;
        }

        private int getEachIPSize() {
            return eachIPSize;
        }

        private long getFinish() {
            return finish;
        }

        private long getStart() {
            return start;
        }

        public void print(PrintWriter pw, long time) {
            long num = 1l << pattern.getWildcardNum();
            long start = pattern.getData() << pattern.getWildcardNum();
            for (long i = 0; i < num; i++) {
                pw.println(time + "," + (start + i) + ",0,0,0,0," + eachIPSize);
            }
        }
    }
}
