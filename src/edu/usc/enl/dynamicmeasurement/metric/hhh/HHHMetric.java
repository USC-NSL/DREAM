package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 2/1/13
 * Time: 6:36 PM
 */
public abstract class HHHMetric extends Metric {
    /**
     * do not mess with hhh or reportedHHH
     *
     * @param hhh
     * @param reportedHHH
     * @param step
     * @param folder
     * @return
     */
    public abstract Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder);

}
