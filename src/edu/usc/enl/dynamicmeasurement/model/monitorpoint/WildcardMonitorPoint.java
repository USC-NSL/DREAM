package edu.usc.enl.dynamicmeasurement.model.monitorpoint;

import edu.usc.enl.dynamicmeasurement.algorithms.matcher.HashMatcher;
import edu.usc.enl.dynamicmeasurement.algorithms.matcher.Matcher;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.NeedInitHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.FlowHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/24/13
 * Time: 7:18 PM <br/>
 * The monitor point that knows from which prefixes it has traffic.
 * Thus it uses a matcher to match against IP addresses and prefixes to check if it has
 * traffic from them or not.
 * The challenging method is for finding if this monitor point has traffic from a prefix or not.
 * This means finding if a prefix has overlapping with any of many prefixes
 */
public class WildcardMonitorPoint extends MonitorPoint {
    private Set<WildcardPattern> hasDataFrom;
    private Matcher matcher;
    private int minPatternsWildcardNum;

    public WildcardMonitorPoint(int capacity, Set<WildcardPattern> hasDataFrom) {
        super(capacity);
        this.hasDataFrom = hasDataFrom;
        matcher = new HashMatcher();
        matcher.setMonitors(hasDataFrom);
        minPatternsWildcardNum = WildcardPattern.TOTAL_LENGTH;
        for (WildcardPattern wildcardPattern : hasDataFrom) {
            minPatternsWildcardNum = Math.min(wildcardPattern.getWildcardNum(), minPatternsWildcardNum);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        String outputFile = "network.xml";
        int rangesNum = 1;
        int switchNum = 1;
        int switchCapacity = 1024;
        {
            Options options = new Options();
            options.addOption(new Option("h", false, "Shows this help"));
            options.addOption(OptionBuilder.withArgName("filename").hasArg().isRequired().withDescription("Output file").create('o'));
            options.addOption(OptionBuilder.withArgName("integer").isRequired().withType(Number.class).hasArg().withDescription("Number of prefixes in total").create('r'));
            options.addOption(OptionBuilder.withArgName("integer").isRequired().withType(Number.class).hasArg().withDescription("Number of switches").create('s'));
            options.addOption(OptionBuilder.withArgName("integer").isRequired().withType(Number.class).hasArg().withDescription("Switch capacity").create('c'));

            CommandLineParser parser = new PosixParser();
            try {
                CommandLine cmd = parser.parse(options, args);
                if (cmd.hasOption("h") || cmd.getOptions().length < options.getRequiredOptions().size()) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("java <classname>", options, true);
                    System.exit(0);
                }
                outputFile = cmd.getOptionValue('o');
                rangesNum = Integer.parseInt(cmd.getOptionValue('r'));
                switchNum = Integer.parseInt(cmd.getOptionValue('s'));
                switchCapacity = Integer.parseInt(cmd.getOptionValue('c'));
            } catch (ParseException e) {
                System.err.println("Parsing failed.  Reason: " + e.getMessage());
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java <classname>", options, true);
                System.exit(0);
            }
        }

        WildcardPattern rootFilter = new WildcardPattern("________________________________", 0);
        Random random = new Random(3282423837l);
        final LinkedList<WildcardPattern> ranges = new LinkedList<>();
        FlowHHHAlgorithm.initMonitors(rangesNum, new NeedInitHHHAlgorithm() {
            @Override
            public void addMonitor(WildcardPattern wildcardPattern) {
                ranges.add(wildcardPattern);
            }

            @Override
            public WildcardPattern pollAMonitor() {
                return ranges.pollFirst();
            }
        }, rootFilter);
        List<Set<WildcardPattern>> patterns = new ArrayList<>();
        for (int i = 0; i < switchNum; i++) {
            patterns.add(new HashSet<WildcardPattern>());
        }
        for (WildcardPattern range : ranges) {
            patterns.get(random.nextInt(switchNum)).add(range);
        }
        int i = 1;
        try (PrintWriter pw = new PrintWriter(outputFile)) {
            pw.println("<Network>");
            for (Set<WildcardPattern> pattern : patterns) {
                pw.println("<Switch name=\"" + i++ + "\" capacity=\"" + switchCapacity + "\">");
                for (WildcardPattern wildcardPattern : pattern) {
                    pw.println("<Prefix value=\"" + wildcardPattern.toStringNoWeight() + "\"/>");
                }
                pw.println("</Switch>");
            }
            pw.println("</Network>");
        }
    }

    public Set<WildcardPattern> getHasDataFrom() {
        return hasDataFrom;
    }

    @Override
    public MonitorPoint clone() {
        WildcardMonitorPoint output = new WildcardMonitorPoint(getCapacity(), hasDataFrom);
        output.setIntId(getIntId());
        output.setId(getId());
        return output;
    }

    public boolean hasDataFrom(WildcardPattern wildcardPattern) {
        if (wildcardPattern.getWildcardNum() <= minPatternsWildcardNum) { // an optimiation
            return hasDataFrom(wildcardPattern.getData() << wildcardPattern.getWildcardNum());
        }
        for (WildcardPattern pattern : hasDataFrom) {
            if (wildcardPattern.match(pattern) || pattern.match(wildcardPattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasDataFrom(long item) {
        return matcher.match(item) != null;
    }

//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder(super.toString() + " (");
//        boolean first = true;
//        for (WildcardPattern i : hasDataFrom) {
//            sb.append(first ? "" : ",").append(i);
//            first = false;
//        }
//        return sb.append(")").toString();
//    }
}
