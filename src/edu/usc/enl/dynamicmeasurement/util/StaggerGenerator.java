package edu.usc.enl.dynamicmeasurement.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 10/8/12
 * Time: 5:44 AM  <br/>
 * A random item selector based on the passed probabilities.
 * The items can be numbers or any object.
 * Use the static randomSelect method for the simplest usage
 */
public class StaggerGenerator<T> {
    /**
     * commulative probabilities
     */
    private double[] cumulative;
    private List<T> values;
    private Random random;

    /**
     * @param random
     * @param probabilities probability of each item
     * @param values        values for each choice. It must be the same size as probabilities
     */
    public StaggerGenerator(Random random, double[] probabilities, List<T> values) {
        this.random = random;
        cumulative = new double[probabilities.length];
        for (int i = 0, probabilitiesLength = probabilities.length; i < probabilitiesLength; i++) {
            double probability = probabilities[i];
            cumulative[i] = probabilities[i] + (i == 0 ? 0 : cumulative[i - 1]);
        }
        this.values = values;
    }

    public static int randomSelect(double[] cumulativeScores, Random random) {
        double randomValue = random.nextDouble() * cumulativeScores[cumulativeScores.length - 1];
        int i = Arrays.binarySearch(cumulativeScores, randomValue);
        if (randomValue == 0 && i >= 0) {//there is a zero in the list and we matched on it!
            //go forward
            for (; i < cumulativeScores.length; i++) {
                if (cumulativeScores[i] != 0) {
                    break;
                }
            }
        } else {
            if (i < 0) {
                i = -i - 1;
            } else {
                //we need the earliest of equal items
                for (; i > 0; i--) {
                    if (cumulativeScores[i] != cumulativeScores[i - 1]) {
                        break;
                    }
                }
            }
        }
        return i;
    }

    /**
     * @return the next random choice based on the initializing probabilities
     */
    public T next() {
        return values.get(randomSelect(cumulative, random));
    }
}
