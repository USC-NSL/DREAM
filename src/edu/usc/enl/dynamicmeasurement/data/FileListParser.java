package edu.usc.enl.dynamicmeasurement.data;

import edu.usc.enl.dynamicmeasurement.process.Simulator;
import edu.usc.enl.dynamicmeasurement.process.scripts.SumReport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 7/25/13
 * Time: 12:53 PM  <br/>
 * Just can parse a list of trace files and it has a special input parameter format pattern+start index-last index.
 * start and last index refer to the list of items in the specified folder that match the pattern .csv
 * It is ok for the pattern to have + or - as the last + and - will be used for the purpose of finding the indeces.
 */
public class FileListParser {
    public String traceFolder = "../caida";
    private String[] packetsFile;

    public FileListParser() {
    }

    public FileListParser(String traceFolder) {
        this.traceFolder = traceFolder;
    }

    public static File[] getFiles(final String pattern, String folder) {
        File folderFile = new File(folder);
        File[] files = folderFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(pattern);
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return files;
    }

    public String[] getPacketsFile() {
        return packetsFile;
    }

    public double parseAndThreshold(String inputTrace, long epochLength) throws Exception {
        String pattern = inputTrace.substring(0, inputTrace.indexOf('+'));
        int start = Integer.parseInt(inputTrace.substring(inputTrace.lastIndexOf('+') + 1, inputTrace.lastIndexOf('-')));
        int end = Integer.parseInt(inputTrace.substring(inputTrace.lastIndexOf('-') + 1));
//        String pattern = "";
        packetsFile = new String[end - start];
        pattern = ".*" + pattern + ".*\\.csv$";
//        switch (Character.toLowerCase(traceChar)) {
//            case 'a':
//                pattern = ".*dirA.*\\.csv$";
//                break;
//            case 'b':
//                pattern = ".*dirB.*\\.csv$";
//                break;
//            default:
//                throw new Exception("Trace type not found");
//        }
        return InitTrace(pattern, packetsFile, start, end, epochLength);
    }

    public void parse(String inputTrace) throws Exception {
        parseAndThreshold(inputTrace, 0);
    }

    private double InitTrace(String s, String[] packetsFile, int start, int end, long epochSize) {
        File[] files = getFiles(s, traceFolder);
        if (files == null) {
            System.err.println("No trace file found in " + traceFolder);
            System.exit(1);
        }
        int end2 = Math.min(files.length, end);
        int k = 0;
        for (int i = start; i < end2; i++) {
            packetsFile[k++] = files[i].getAbsolutePath();
        }
        for (String s1 : packetsFile) {
            System.out.println(s1);
        }
        //find max traffic
        double max = -1;
        if (epochSize > 0) {
            try {
                SumReport user = new SumReport(true, true);
                //run simulator
                new Simulator(0).run(packetsFile, user);
                List<Double> sumReport = user.getOutput();
                for (Double sum : sumReport) {
                    max = Math.max(sum, max);
                }
            } catch (FileNotFoundException e) {
                System.err.println("Exception in finding maximum size of epoch");
                e.printStackTrace();
                System.exit(1);
            }
        }
        return max;
    }
}
