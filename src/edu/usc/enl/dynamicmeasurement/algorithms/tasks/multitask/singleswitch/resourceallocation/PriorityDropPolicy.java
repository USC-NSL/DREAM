package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/13/13
 * Time: 10:49 PM
 */
public class PriorityDropPolicy {
    protected final int dropEpochs;
    protected int notHelpingEpochs = 0;
    protected boolean potentialDrop = false;

    public PriorityDropPolicy(boolean potentialDrop, int dropEpochs) {
        this.potentialDrop = potentialDrop;
        this.dropEpochs = dropEpochs;
    }

    public PriorityDropPolicy(int dropEpochs) {
        this.dropEpochs = dropEpochs;
    }

    public int getDropEpochs() {
        return dropEpochs;
    }

    @Override
    public String toString() {
        return "PriorityDropPolicy{" +
                "notHelpingEpochs=" + notHelpingEpochs +
                '}';
    }

    public TaskRecord2 checkForDrop(boolean notHelpedPoor, Collection<? extends TaskRecord2> tasks) {
        if (notHelpedPoor) {
            notHelpingEpochs++;
        } else {
            if (notHelpingEpochs > 0) {
                if (potentialDrop) {
                    notHelpingEpochs--;
                } else {
                    notHelpingEpochs = 0;
                }
            }
        }
        if (notHelpingEpochs >= dropEpochs) {
            if (notHelpingEpochs > 0) {
                if (potentialDrop) {
                    notHelpingEpochs--;
                } else {
                    notHelpingEpochs = 0;
                }
            }
            //dream
            return getDropCandidate(tasks);
        }
        return null;
    }

    public TaskRecord2 getDropCandidate(Collection<? extends TaskRecord2> tasks) {
        TaskRecord2 minPriorityTask = null;
        for (TaskRecord2 taskRecord : tasks) {
            if (minPriorityTask == null || minPriorityTask.getDropPriority() > taskRecord.getDropPriority()) {
                minPriorityTask = taskRecord;
            }
        }
        return minPriorityTask;
    }
}
