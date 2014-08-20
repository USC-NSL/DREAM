package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/8/13
 * Time: 9:07 AM <br/>
 * Implements EWMA with a memory parameter alpha: new = alpha * old +(1-alpha) * new
 * <p>The XML constructor requires the following Property children tags: <ul>
 * <li> name attribute as "Init", the initial value of the aggregator</li>
 * <li> name attribute as "Alpha", the alpha parameter as described above</li>
 * </ul></p>
 */
public class EWMAAccuracyAggregatorImpl extends AccuracyAggregator {
    private final double alpha;
    private double accuracy = 1;
    private boolean init = false;

    public EWMAAccuracyAggregatorImpl(Element element) {
        alpha = Double.parseDouble(Util.getChildrenProperties(element, "Property").get("Alpha").getAttribute(ConfigReader.PROPERTY_VALUE));
        init(Double.parseDouble(Util.getChildrenProperties(element, "Property").get("Init").getAttribute(ConfigReader.PROPERTY_VALUE)));
        init = true;
    }

    public EWMAAccuracyAggregatorImpl(double alpha) {
        this.alpha = alpha;
        init(1);
    }

    public void init(double nextNum) {
        this.accuracy = nextNum;
    }

    @Override
    public double getAccuracy() {
        return this.accuracy;
    }

    @Override
    public double update(double accuracy) {
        if (!init) {
            init = true;
            this.accuracy = accuracy;
            return accuracy;
        } else {
            this.accuracy = alpha * this.accuracy + (1 - alpha) * accuracy;
            return this.accuracy;
        }
    }
}
