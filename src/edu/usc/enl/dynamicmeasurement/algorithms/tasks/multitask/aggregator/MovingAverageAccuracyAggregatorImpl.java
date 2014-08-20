package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/8/13
 * Time: 9:09 AM <br/>
 * Implements Moving Average over a window of size = "Width"
 * <p>The XML constructor requires the following Property children tags: <ul>
 * <li> name attribute as "Init", the initial value of the aggregator</li>
 * <li> name attribute as "Width", the width parameter as described above</li>
 * </ul></p>
 */
public class MovingAverageAccuracyAggregatorImpl extends AccuracyAggregator {
    private double[] accuracyWindow;
    private int index = 0;

    public MovingAverageAccuracyAggregatorImpl(Element element) {
        accuracyWindow = new double[(int) Double.parseDouble(Util.getChildrenProperties(element, "Property").get("Width").getAttribute(ConfigReader.PROPERTY_VALUE))];
        init(Double.parseDouble(Util.getChildrenProperties(element, "Property").get("Init").getAttribute(ConfigReader.PROPERTY_VALUE)));
    }

    public MovingAverageAccuracyAggregatorImpl(int windowSize) {
        accuracyWindow = new double[windowSize];
        init(1);
    }

    @Override
    public double getAccuracy() {
        double sum = 0;
        for (double anAccuracyWindow : accuracyWindow) {
            sum += anAccuracyWindow;
        }
        return sum / accuracyWindow.length;
    }

    @Override
    public double update(double accuracy) {
        accuracyWindow[index % accuracyWindow.length] = accuracy;
        index++;
        return getAccuracy();
    }

    @Override
    public void init(double nextNum) {
        Arrays.fill(accuracyWindow, nextNum);
    }
}
