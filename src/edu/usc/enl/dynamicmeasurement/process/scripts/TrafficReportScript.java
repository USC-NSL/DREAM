package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.algorithms.transform.EpochPacer;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.process.Simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/31/13
 * Time: 7:45 PM
 */
public class TrafficReportScript {
    public static void main(String[] args) throws Exception {
//        FileListParser fileListParser = new FileListParser();
//        fileListParser.parse(args[0]);
//        String[] packetsFile = fileListParser.getPacketsFile();
//        String outputFile = "output/skew/2/skew1_w";
//        WildcardPattern.TOTAL_LENGTH = 32;
//        int updateStepDuration = 1000000;
//        boolean resetOnStep = true;
//        int maxTime = 0;
//        new File(outputFile).getParentFile().mkdirs();

//        {
//            Set<WildcardPattern> wildcards;
//            // create wildcard report user
//            WildcardReport user = new WildcardReport();
//            //run simulator
//            new Simulator(maxTime).run(packetsFile, user);
//            wildcards = user.getWildcards();
//            PacketUser user2 = new TrimmedTrafficReport2(updateStepDuration, resetOnStep, wildcards, outputFile, 16);
//            new Simulator(maxTime).run(packetsFile, user2);
//        }

//        {
//            //create traffic report user
//            List<WildcardPattern> monitors = new ArrayList<>();
//            int items = 8;
//            for (int i = 0; i < items; i++) {
//                monitors.add(new WildcardPattern(i, WildcardPattern.TOTAL_LENGTH - 3, 0));
//            }
//            PacketUser user = new TestPrefixReport(updateStepDuration, resetOnStep, monitors);
//            //run simulator
//            new Simulator(maxTime).run(packetsFile, user);
//        }

//        {
//            //create traffic report user
//            String folder = "E:\\enl\\measurement\\DynamicMonitor\\..\\caida2\\trace\\1";
//            File folderFile = new File(folder);
//            File[] files = folderFile.listFiles();
//            if (files == null) {
//                throw new Exception("Folder " + folder + " not found");
//            }
//            Arrays.sort(files, new NumberAwareComparator());
//            packetsFile = new String[files.length];
//            int i = 0;
//            for (File file : files) {
//                packetsFile[i++] = file.getAbsolutePath();
//            }
//
//            PacketUser user = new SumReport(true, true);
//            user = new TransformHandler(user);
//            ((TransformHandler) user).addTransform(new SkewPacketUser(new WildcardPattern(0, 32, 0), 0.6));
////            user = new PacketFilter(user, new WildcardPattern(4, 32 - 4, 0));
//            user = new EpochPacer(user, updateStepDuration);
////            user = new SkewPacketUser(updateStepDuration, true, user, 1.2);
////            user = new RandomAdditionPacketUser(updateStepDuration, true, user, 0.1, new Random(12392342));
//            //run simulator
//            new Simulator(maxTime).run(packetsFile, user);
//        }

//        {
//            //create traffic report user
//            int wildcard = 0;
//            PacketUser user = new SkewFinderReport(updateStepDuration, resetOnStep, wildcard);
//            ((SkewFinderReport) user).setStatsOutputWriter(new PrintWriter(outputFile + "_" + wildcard + ".txt"));
//            user = new SkewPacketUser(updateStepDuration, true, user, 2);
//            //run simulator
//            new Simulator(maxTime).run(packetsFile, user);
//        }

        {
            //create traffic report user
//            PacketUser user = new SumReport(true, true);
            List<WildcardPattern> monitors = new ArrayList<>();
            int items = 16;
            for (int i = 0; i < items; i++) {
                monitors.add(new WildcardPattern(i, WildcardPattern.TOTAL_LENGTH - 4, 0));
            }
//            PacketUser user = new SumReportPrefixes(monitors, true);
            PacketUser user = new SumReport(true, true);
            user = new EpochPacer(user, 1000000);

            //run simulator
            new Simulator(0).run(new String[]{"F:\\trace\\pkt2all\\1\\20120920_0",
                    "F:\\trace\\pkt2all\\1\\20120920_1",
                    "F:\\trace\\pkt2all\\1\\20120920_2",
                    "F:\\trace\\pkt2all\\1\\20120920_3",
                    "F:\\trace\\pkt2all\\1\\20120920_4"}, user);
//            new Simulator(maxTime).run(new String[]{"E:\\enl\\measurement\\DynamicMonitor\\output\\test\\1\\trace\\1.txt"}, user);
        }


    }

    public Set<WildcardPattern> createTrimmedTree(int maxWildcards) {
        Set<WildcardPattern> snapshot = new HashSet<>(1 << (maxWildcards + 1), 1);
        for (int i = 0; i < (1 << maxWildcards); i++) {
            WildcardPattern w = new WildcardPattern(i, maxWildcards, 0);
            snapshot.add(w);
            while (w.canGoUp()) {
                if (w.isLeft()) {
                    w = w.clone().goUp();
                    snapshot.add(w);
                } else {
                    break;
                }
            }
        }
        return snapshot;
    }
}
