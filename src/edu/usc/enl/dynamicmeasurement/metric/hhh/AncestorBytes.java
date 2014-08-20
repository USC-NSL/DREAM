package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/28/13
 * Time: 7:37 AM
 */
public class AncestorBytes extends HHHMetric {
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        if (hhh.size() == 0) {
            return 1d;
        }
        Map<WildcardPattern, WildcardPattern> reported = new HashMap<>();
        for (WildcardPattern wildcardPattern : reportedHHH) {
            reported.put(wildcardPattern, wildcardPattern);
        }
        double score = 0;
        for (WildcardPattern wildcardPattern : hhh) {
            wildcardPattern = wildcardPattern.clone();
            int i = 1;
            do {
                if (reportedHHH.contains(wildcardPattern)) {
                    score += wildcardPattern.getWeight() / reported.get(wildcardPattern).getWeight();
                    break;
                }
                if (wildcardPattern.canGoUp()) {
                    wildcardPattern = wildcardPattern.goUp();
                }
            } while (wildcardPattern.canGoUp());
        }
        double v = 1.0 * score / hhh.size();
//        if (v > 1) {
//            List<WildcardPattern> h1 = new ArrayList<>(hhh);
//            Collections.sort(h1);
//            for (WildcardPattern wildcardPattern : h1) {
//                System.out.println(wildcardPattern);
//            }
//            System.out.println();
//            h1 = new ArrayList<>(reportedHHH);
//            Collections.sort(h1);
//            for (WildcardPattern wildcardPattern : h1) {
//                System.out.println(wildcardPattern);
//            }
//            System.out.println();
////            System.exit(1);
//        }
        return v;
    }

    @Override
    public String toString() {
        return "AncestorBytes";
    }
}
