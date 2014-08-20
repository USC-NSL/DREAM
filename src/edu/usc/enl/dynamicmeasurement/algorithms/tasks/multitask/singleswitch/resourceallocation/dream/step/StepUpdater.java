package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.step;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/26/13
 * Time: 3:21 PM  <br/>
 * Updates the addition and reduction steps
 */
public abstract class StepUpdater {
    protected int probAdd;
    protected int probDiv;
    protected ThresholdGuaranteeAlgorithm2 algorithm;

    public void init(int probAdd, int probDiv, ThresholdGuaranteeAlgorithm2 algorithm) {
        this.probAdd = probAdd;
        this.probDiv = probDiv;
        this.algorithm = algorithm;
    }

    /**
     * Update the steps
     *
     * @param tasks
     */
    public abstract void updateStep(Collection<DreamTaskRecord> tasks);

}
