package edu.usc.enl.dynamicmeasurement.metric.monitors;

import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/27/13
 * Time: 9:15 PM
 */
public abstract class MonitorMetric extends Metric{
    public abstract Double compute(List<WildcardPattern> monitors);

    @Override
    public abstract String toString();
}
