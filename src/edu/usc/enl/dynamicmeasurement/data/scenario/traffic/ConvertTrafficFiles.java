package edu.usc.enl.dynamicmeasurement.data.scenario.traffic;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.IntegerWrapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/27/13
 * Time: 9:55 PM
 */
public class ConvertTrafficFiles {
    //input output time=start,end,newstart range=range1,range2 size=start,end,range,newsize
    public static void main(String[] args) throws IOException {
        String input = args[0];
        String output = args[1];
        ConvertTrafficFiles converter = new ConvertTrafficFiles();
        boolean needSort = false;
        List<Processor> processorList = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            String arg = args[i];
            String[] split = arg.split("=");
            String command = split[0];
            String commandArgs = split[1];
            switch (command) {
                case "time":
                    needSort = true;
                    processorList.add(converter.changeTime(commandArgs.split(",")));
                    break;
                case "range":
                    processorList.add(converter.changeIP(commandArgs.split(",")));
                    break;
                case "size":
                    processorList.add(converter.changeSize(commandArgs.split(",")));
                    break;
                default:
                    throw new IllegalArgumentException("Command " + command + " not found");
            }
        }
        converter.parallelProcess(input, output, processorList);

        if (needSort) {
            //resort the file
            converter.sort(output, 0);
        }
    }

    public void parallelProcess(String input, String output, List<Processor> processorList) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(input));
             PrintWriter pw = new PrintWriter(output)) {
            while (br.ready()) {
                String line = br.readLine();
                for (int i = 0; i < processorList.size() && line != null; i++) {
                    Processor p = processorList.get(i);
                    line = p.process(line);
                }
                if (line != null) {
                    pw.println(line);
                }
            }
        }
    }

    public void process(String input, String output, Processor p) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(input));
             PrintWriter pw = new PrintWriter(output)) {
            while (br.ready()) {
                String line = br.readLine();
                String process = p.process(line);
                if (process != null) {
                    pw.println(process);
                }
            }
        }
    }

    public void sort(String file, int size) throws IOException {
        List<TimeLine> lines = new ArrayList<>(size > 0 ? size : 10);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.ready()) {
                String line = br.readLine();
                long time = MergeTrafficFiles.getTime(line);
                lines.add(new TimeLine(time, line));
            }
        }
        Collections.sort(lines);
        try (PrintWriter pw = new PrintWriter(file)) {
            for (TimeLine line : lines) {
                pw.println(line.line);
            }
        }
    }

    public Processor changeTime(String[] args) throws IOException {
        final long range1Start = Long.parseLong(args[0]);
        final long range1End = Long.parseLong(args[1]);
        final long changeTime = Long.parseLong(args[2]) - range1Start;
        final IntegerWrapper linesNum = new IntegerWrapper(0);
        return new Processor() {
            @Override
            public String process(String line) {
                linesNum.setValue(linesNum.getValue() + 1);
                long time = MergeTrafficFiles.getTime(line);
                if (time < range1End && time >= range1Start) {
                    return time + changeTime + line.substring(line.indexOf(","));
                } else {
                    return line;
                }
            }
        };
    }

    public Processor changeIP(String[] args) throws IOException {
        final WildcardPattern range1 = new WildcardPattern(args[0], 0);
        WildcardPattern range2 = new WildcardPattern(args[1], 0);
        if (range1.getWildcardNum() != range2.getWildcardNum()) {
            throw new IllegalArgumentException("Ranges mismatch");
        }
        final long target = range2.getData() << range2.getWildcardNum();
        final long begin = range1.getData() << range1.getWildcardNum();
        return new Processor() {
            @Override
            public String process(String input) throws IOException {
                try {
                    DataPacket p = new DataPacket(new StringTokenizer(input, ","));
                    if (range1.match(p.getSrcIP())) {
                        p.setSrcIP(p.getSrcIP() - begin + target);
                        return p.print();
                    }
                    return p.print();
                } catch (DataPacket.PacketParseException e) {
                    throw new IOException(e);
                }
            }
        };
    }

    public Processor changeSize(String[] args) throws IOException {
        final long rangeStart = Long.parseLong(args[0]);
        final long rangeEnd = Long.parseLong(args[1]);
        final WildcardPattern wildcardPattern = new WildcardPattern(args[2], 0);
        final int newSize = Integer.parseInt(args[3]);

        return new Processor() {
            @Override
            public String process(String line) throws IOException {
                try {
                    DataPacket p = new DataPacket(new StringTokenizer(line, ","));
                    if (p.getTime() < rangeEnd && p.getTime() >= rangeStart && wildcardPattern.match(p.getSrcIP())) {
                        if (newSize == 0) {
                            return null;
                        }
                        p.setSize(newSize);
                        return p.print();
                    } else {
                        return line;
                    }
                } catch (DataPacket.PacketParseException e) {
                    throw new IOException(e);
                }
            }
        };
    }

    private interface Processor {
        public String process(String input) throws IOException;
    }

    private class TimeLine implements Comparable<TimeLine> {
        long time;
        String line;

        private TimeLine(long time, String line) {
            this.time = time;
            this.line = line;
        }

        @Override
        public int compareTo(TimeLine o) {
            return Long.compare(time, o.time);
        }
    }
}
