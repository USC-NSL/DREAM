package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/13/13
 * Time: 10:07 AM
 */
public class Utilization extends HHHMetric {
    private final static double ACCURACY_BOUND = 0.8;

    private HHHMetric accuracyMetric;
    private Map<String, Map<Integer, Double>> resources;

    public Utilization(File parentFolder) {
        accuracyMetric = new Precision();
        resources = loadShareFile(parentFolder.getAbsoluteFile() + "/share.csv");
    }

    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        Map<Integer, Double> currentFolderResources = resources.get(folder);
        if (currentFolderResources == null) {
            return 0d;
        }
        //Double accuracy = accuracyMetric.compute(hhh, reportedHHH, step, folder);
        //load share
        Double resource = currentFolderResources.get(step);
        if (resource == null) {
            return 0d;
        }
        return resource; //* (accuracy >= ACCURACY_BOUND ? 1 : 0);
    }

    private Map<String, Map<Integer, Double>> loadShareFile(String shareFile) {
        //time,task,switch,share,accuracy,accuracy_agg
        Map<String, Map<Integer, Double>> output = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(shareFile))) {
            br.readLine();//skip header
            while (br.ready()) {
                String line = br.readLine();
//                StringTokenizer st = new StringTokenizer(line, ",");
//                int time = Integer.parseInt(st.nextToken());
//                String name = st.nextToken();
//                st.nextToken();
//                String s = st.nextToken();
                String[] split = line.split(",");
                String name = null;
                name = split[1];
                int time = Integer.parseInt(split[0]);
                String s = split[3];
                Map<Integer, Double> share = output.get(name);
                if (share == null) {
                    share = new HashMap<>();
                    output.put(name, share);
                }
                Double timeShare = share.get(time);
                if (timeShare == null) {
                    timeShare = 0d;
                }
                timeShare += Double.parseDouble(s);
                share.put(time, timeShare);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public String toString() {
        return "Utilization";
    }
}
