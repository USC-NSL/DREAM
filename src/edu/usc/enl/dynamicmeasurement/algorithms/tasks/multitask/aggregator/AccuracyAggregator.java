package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/8/13
 * Time: 9:07 AM <br/>
 * It is an aggregator af accuracy values over a window.
 * It is assumed to return the parameter passed in the constructor if no updates has been performed.
 */
public abstract class AccuracyAggregator {

    /**
     * Should return the number passed in init function
     *
     * @return
     */
    public abstract double getAccuracy();

    public abstract double update(double accuracy);

    public abstract void init(double nextNum);
}
