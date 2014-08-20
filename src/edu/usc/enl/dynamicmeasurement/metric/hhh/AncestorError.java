package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/28/13
 * Time: 7:37 AM
 */
public class AncestorError extends HHHMetric implements NeedGroundTruthFolder {
    private List<Double> sums;
    private final boolean relative;
    private int step = 0;

    public AncestorError(boolean relative) {
        this.relative = relative;
    }

    public void reset() {
        step = 0;
    }

    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        if (hhh.size() == 0) {
            return 1d;
        }
        Map<WildcardPattern, WildcardPattern> reported = new HashMap<>();
        for (WildcardPattern wildcardPattern : reportedHHH) {
            reported.put(wildcardPattern, wildcardPattern);
        }
        double score = 0;
        double hhhSum = -1;
        double notHHHSum = 0;
        for (WildcardPattern wildcardPattern : hhh) {
            wildcardPattern = wildcardPattern.clone();
            do {
                if (reportedHHH.contains(wildcardPattern)) {
                    score += Math.abs(wildcardPattern.getWeight() - reported.get(wildcardPattern).getWeight());
                    if (relative) {
                        score /= sums.get(step);
                        //wildcardPattern.getWeight();
                    }
                    break;
                }
                if (wildcardPattern.canGoUp()) {
                    wildcardPattern = wildcardPattern.goUp();
                } else {
                    //ancestor not found
                    if (hhhSum < 0) {
                        //first compute hhh sum
                        hhhSum = computeHHHSum(reportedHHH);
                        notHHHSum = Math.max(0d, sums.get(this.step) - hhhSum);
                    }
                    score += Math.abs(wildcardPattern.getWeight() - notHHHSum);
                    if (relative) {
                        score /= score /= sums.get(step);
                        //wildcardPattern.getWeight();
                    }
                }
            } while (wildcardPattern.canGoUp());
        }
        this.step++;
        return 1.0 * score / hhh.size();
    }

    private double computeHHHSum(List<WildcardPattern> reportedHHH) {
        double sum = 0;
        for (WildcardPattern wildcardPattern : reportedHHH) {
            sum += wildcardPattern.getWeight();
        }
        return sum;
    }

    @Override
    public String toString() {
        return "AncestorError";
    }

    @Override
    public void setGroundTruthFolder(String sumsFile) {
        sums = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(sumsFile + "/sums.csv"))) {
            while (br.ready()) {
                sums.add(Double.parseDouble(br.readLine().split(",")[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
