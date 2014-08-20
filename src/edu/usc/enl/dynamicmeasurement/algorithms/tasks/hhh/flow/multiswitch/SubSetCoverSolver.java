package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/23/13
 * Time: 8:53 AM <br/>
 * solves subset cover problem of item of type E
 */
public class SubSetCoverSolver<E> {
    /**
     * Can mess with sets and toCover
     *
     * @param toCover
     * @param sets
     * @param costs
     * @param solution
     * @param <F>      the type of the key to recognize each set with
     * @return
     */
    public <F> double solve(Set<E> toCover, Map<F, Set<E>> sets, Map<F, Double> costs, List<F> solution) {
        double costSum = 0;
        while (toCover.size() > 0) {
            //intersect sets with to cover
            {
                for (Iterator<Map.Entry<F, Set<E>>> iterator = sets.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<F, Set<E>> entry = iterator.next();
                    Set<E> set = entry.getValue();
                    set.retainAll(toCover);
                    if (set.size() == 0) {
                        iterator.remove();
                    }
                }
            }

            //find min avgweights
            double minWeight = -1;
            F minWeightSetIdentifier = null;
            for (Map.Entry<F, Set<E>> entry : sets.entrySet()) {
                Set<E> set = entry.getValue();
                double avgWeightSet = costs.get(entry.getKey()) / set.size();
                if (avgWeightSet < minWeight || minWeightSetIdentifier == null) {
                    minWeightSetIdentifier = entry.getKey();
                    minWeight = avgWeightSet;
                }
                if (minWeight == 0) { // you cannot find a cost less than 0
                    break;
                }
            }
            if (minWeightSetIdentifier == null) {
                break;
            }
            costSum += costs.get(minWeightSetIdentifier);
            Set<E> set = sets.get(minWeightSetIdentifier);
            if (minWeightSetIdentifier instanceof DetailSolution) {
                ((DetailSolution) minWeightSetIdentifier).setUsefulFor(set, minWeight);
            }
            toCover.removeAll(set);
            solution.add(minWeightSetIdentifier);
            sets.remove(minWeightSetIdentifier);
        }
        if (toCover.size() == 0) {
            return costSum;
        } else {
            //unsuccessful
//            for (Set<E> es : setsTemp.values()) {
//                if (es.contains(toCoverTemp.iterator().next())){
//                    System.out.println("hey I still can contribute");
//                }
//            }

            return -1;
        }
    }

    /**
     * If the identifier of each set implements this, it will be notified when it is used to cover a subset of the target set
     */
    public static interface DetailSolution {
        public void setUsefulFor(Set monitorPoints, double avgCost);
    }


}
