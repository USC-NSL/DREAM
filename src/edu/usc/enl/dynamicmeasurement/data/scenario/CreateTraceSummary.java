package edu.usc.enl.dynamicmeasurement.data.scenario;

import edu.usc.enl.dynamicmeasurement.algorithms.transform.EpochPacer;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.process.Simulator;
import edu.usc.enl.dynamicmeasurement.process.scripts.SumReportPrefixes;
import edu.usc.enl.dynamicmeasurement.util.NumberAwareComparator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/25/13
 * Time: 3:52 PM
 */
public class CreateTraceSummary {

    public static void main(String[] args) throws FileNotFoundException {
        List<String> folders = listFolders(args[0]);
        List<WildcardPattern> wildcardPatterns = MultiFileScenarioGenerator.getWildcardPatterns(new WildcardPattern(0, WildcardPattern.TOTAL_LENGTH, 0), 16);
        for (String folder : folders) {
            File folderFile = new File(folder);
            File[] files = folderFile.listFiles();
            if (files == null) {
                throw new RuntimeException("Folder " + folder + " not found");
            }
            Arrays.sort(files, new NumberAwareComparator());
            String[] packetsFile = new String[files.length];
            int i = 0;
            for (File file : files) {
                packetsFile[i++] = file.getAbsolutePath();
            }

//                        SumReportPrefixesWrite user1 = new SumReportPrefixesWrite(wildcardPatterns, "output/traceprofile/"+folders[0].replaceAll(".*[/\\\\]",""));
            SumReportPrefixes user1 = new SumReportPrefixes(wildcardPatterns, false);

            PacketUser user = new EpochPacer(user1, 1000000);
            //run simulator
            new Simulator(0).run(packetsFile, user);
            try (PrintWriter pw = new PrintWriter(folder + "/summary.txt")) {
                for (WildcardPattern wildcardPattern : wildcardPatterns) {
                    pw.println(wildcardPattern.toStringNoWeight() + "," + wildcardPattern.getWeight());
                }
            }
        }
    }

    private static List<String> listFolders(String tracesRootFolder) {
        List<String> output;
        File file = new File(tracesRootFolder);
        File[] files = file.listFiles();
        if (files == null) {
            System.err.println("Trace folder " + tracesRootFolder + " cannot be accessed");
            System.exit(1);
        }
        output = new ArrayList<>(files.length);
        for (File file1 : files) {
            output.add(file1.getPath());
        }
        return output;
    }
}
