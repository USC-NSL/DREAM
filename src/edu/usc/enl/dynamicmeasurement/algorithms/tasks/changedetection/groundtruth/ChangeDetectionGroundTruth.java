package edu.usc.enl.dynamicmeasurement.algorithms.tasks.changedetection.groundtruth;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.changedetection.ChangeDetectionAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/10/14
 * Time: 2:56 PM <br/>
 * The groundtruth implementation for finding big changes.
 * It keeps track of the changes of the most exact IPs after ignoring WildcardNum bits.
 * <p>
 * <p>An item may have no traffic for many epochs.
 * Their record will be removed after NO_TRAFFIC_REMOVE epochs to save memory.
 * The item that has no record is assumed to have 0 traffic.</p>
 * <p>
 * <p>This class does not respect allocated resources and always estimate its accuracy to be 1.
 * This class is not a TCAM-based algorithm so it cannot be used in real experiments.</p>
 */
public class ChangeDetectionGroundTruth extends ChangeDetectionAlgorithm implements SingleSwitchTask.SingleSwitchTaskImplementation {
    public static final int NO_TRAFFIC_REMOVE = 5;
    private Map<Long, GroundTruthNodeHistory> IPHistory = new HashMap<>();
    private int step = 0;

    public ChangeDetectionGroundTruth(Element element) {
        super(element);
    }

    @Override
    public void match(long item, double diff) {
        item >>>= wildcardNum; //ignore bits
        GroundTruthNodeHistory nodeHistory = IPHistory.get(item);
        if (nodeHistory == null) {
            nodeHistory = new GroundTruthNodeHistory();
            IPHistory.put(item, nodeHistory);
        }
        nodeHistory.size += diff;
    }

    /**
     * Go through all IP histories gathered and report those with large traffic changes
     *
     * @param step
     * @return
     */
    @Override
    public Collection<WildcardPattern> findBigChanges(int step) {
        Collection<WildcardPattern> output = new ArrayList<>();
        for (Iterator<Map.Entry<Long, GroundTruthNodeHistory>> iterator = IPHistory.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Long, GroundTruthNodeHistory> entry = iterator.next();
            GroundTruthNodeHistory history = entry.getValue();
            double size = history.size;
            if (size == 0) {
                history.noTrafficEpochs++;
                if (history.noTrafficEpochs > NO_TRAFFIC_REMOVE) {
                    iterator.remove();
                    continue;
                }
            } else {
                history.noTrafficEpochs = 0;
            }

            try {
                Double mean = history.getMean();
                Long item = entry.getKey();
                if (Math.abs(mean - history.size) >= threshold) {
                    output.add(new WildcardPattern(item, wildcardNum, size - mean));
//                    System.out.println(this.step + "," + new WildcardPattern(item, 0, size).toStringNoWeight() + "," + size + "," + mean);
                }
            } catch (NotEnoughDataException e) {

            }
            history.update();
        }
        this.step++;

        return output;
    }

    @Override
    public void setCapacityShare(int resource) {

    }

    @Override
    public double estimateAccuracy() {
        return 1;
    }

    @Override
    public int getUsedResourceShare() {
        return 0;
    }

    @Override
    public void reset() {
        super.reset();
        for (GroundTruthNodeHistory history : IPHistory.values()) {
            history.size = 0;
        }
    }

    /**
     * keeps track of the history of an item
     */
    private class GroundTruthNodeHistory {
        private int noTrafficEpochs = 0;
        private double ewmaMean;
        private double size;

        public void update() {
            ewmaMean = ewmaMean * ewmaAlpha + size * (1 - ewmaAlpha);
        }

        public Double getMean() throws NotEnoughDataException {
            return ewmaMean;
        }
    }
}
