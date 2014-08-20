package edu.usc.enl.dynamicmeasurement.algorithms.transform;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/23/13
 * Time: 10:27 PM <br/>
 * Any class implementing this interface, must work as a set of traffic transformers
 */
public interface TransformHandlerInterface {
    /**
     * Add transformers t to this set of transformers
     *
     * @param t
     */
    public void addTransform(TrafficTransformer t);

    /**
     * Remove the transformer with the passed name
     *
     * @param transformName
     */
    public void removeTransform(String transformName);
}
