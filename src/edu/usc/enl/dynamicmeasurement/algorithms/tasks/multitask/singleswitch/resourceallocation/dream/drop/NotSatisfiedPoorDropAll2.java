package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.drop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.EWMAAccuracyAggregatorImpl;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.PriorityDropPolicy;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.TaskRecord2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DreamTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.DummyTaskRecord;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream.ThresholdGuaranteeAlgorithm2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 7:53 PM
 */
public class NotSatisfiedPoorDropAll2 extends NotSatisfiedPoor {
    private AccuracyAggregator averageToDropAgg;

    public NotSatisfiedPoorDropAll2(ThresholdGuaranteeAlgorithm2 algorithm1, PriorityDropPolicy dropPolicy1) {
        super(algorithm1, dropPolicy1);
        averageToDropAgg = new EWMAAccuracyAggregatorImpl(1.0 / dropPolicy1.getDropEpochs());
    }

    @Override
    public List<DreamTaskRecord> checkDrop(Collection<DreamTaskRecord>
                                                   poorTaskRecords, Collection<DreamTaskRecord> richTaskRecords, DummyTaskRecord dummyTaskRecord) {
        ArrayList<DreamTaskRecord> output = new ArrayList<>();
        int sumGive = dummyTaskRecord.getTask().getResourceShare();
        int sumGet = 0;
        for (DreamTaskRecord richTaskRecord : richTaskRecords) {
            sumGive += richTaskRecord.getReductionStep2();
        }
        for (DreamTaskRecord poorTaskRecord : poorTaskRecords) {
            sumGet += poorTaskRecord.getAdditionStep2();
        }
        Collection<? extends TaskRecord2> tasks = new ArrayList<>(algorithm.getTasks());
        //averageToDropAgg.update(sumGet - sumGive);
        TaskRecord2 dropped = dropPolicy.checkForDrop(sumGet > 2 * sumGive, tasks);
        if (dropped != null) {
            double averageToDrop = (sumGet - 2 * sumGive);
            //averageToDropAgg.getAccuracy();
            while (averageToDrop > 0) {
                dropped = dropPolicy.getDropCandidate(tasks);
                tasks.remove(dropped);
                DreamTaskRecord dropped1 = (DreamTaskRecord) dropped;
                output.add(dropped1);
                if (poorTaskRecords.contains(dropped1)) {
                    averageToDrop -= dropped1.getAdditionStep2();
                    averageToDrop -= dropped1.getTask().getResourceShare();
                } else if (richTaskRecords.contains(dropped1)) {
                    averageToDrop += dropped1.getReductionStep2();
                    averageToDrop -= dropped1.getTask().getResourceShare();
                } else {
                    averageToDrop -= dropped1.getTask().getResourceShare();
                }
            }
        }
        return output;
    }
}
