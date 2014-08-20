package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.distributor;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 9:25 PM
 */
public class AbsolutePriorityResourceDistributorOnlyPoor extends AbstractResourceDistributor {
    private WeightedResourceDistributorNoDummy weightPolicy;
    private PriorityResourceDistributorOnlyPoor priorityPolicy;

    public AbsolutePriorityResourceDistributorOnlyPoor(ThresholdGuaranteeAlgorithm2 algorithm) {
        super(algorithm);
        weightPolicy = new WeightedResourceDistributorNoDummy(algorithm);
        priorityPolicy = new PriorityResourceDistributorOnlyPoor(algorithm);
    }

    @Override
    public void distribute(List<DreamTaskRecord> richTasks, List<DreamTaskRecord> poorTasks) {

        weightPolicy.distribute(new LinkedList<>(richTasks), new LinkedList<>(poorTasks));
        priorityPolicy.distribute(richTasks, poorTasks);

    }

}
