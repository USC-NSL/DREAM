package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.dream;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.AllocationTaskView;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 6:58 PM
 */
public class DummyAllocationTaskView implements AllocationTaskView {
    private int resourceShare = 0;

    @Override
    public String toString() {
        return "Dummy, ResourceShare: " + resourceShare;
    }

    @Override
    public int getResourceShare() {
        return resourceShare;
    }

    @Override
    public void setResourceShare(int c) {
        if (c < 0) {
            throw new RuntimeException("negative resource at dummy ");
//            System.exit(1);
        }
        resourceShare = c;
    }

    @Override
    public double getAggregatedAccuracy() {
        return 1;
    }

    @Override
    public Task2 getTask() {
        return null;
    }

    @Override
    public double getAccuracy2() {
        return 1;
    }

    @Override
    public int getUsedResourceShare() {
        return 0;
    }
}
