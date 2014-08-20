package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch;

import java.util.*;

/*
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/22/13
 * Time: 11:07 PM <br/>
 * A set of limited number of objects.
 * This saves memory comparing to a hashset and can find union, intersection faster using bitwise operations
*/

public class MatrixSet<E extends MatrixSet.MatrixObject> implements Set<E>, Cloneable {
    private final MatrixMapping<E> mapping;
    private long[] matrix;
    private int size;

    /*public static void setMapping(MatrixMapping matrixMapping) {
        if (mapping == null) {
            mapping = matrixMapping;
        } else {
            System.out.println("Mapping has been set before");
            mapping = matrixMapping;
            //throw new UnsupportedOperationException("RuleSet has been set before");
        }
    }*/

    public MatrixSet(MatrixMapping<E> mapping) {
        this.mapping = mapping;
        matrix = new long[getLongNums(this.mapping.size())];
        size = 0;
    }

    public MatrixSet(long[] matrix, MatrixMapping<E> mapping) {
        this.mapping = mapping;
        this.matrix = matrix;
        size = getSize(matrix);
    }

    public static int getLongNums(int rulesNum) {
        return (int) Math.ceil(1.0 * rulesNum / 64);
    }

    public static long[] convertToMatrix(long[] partitionRuleMatrix, Collection<MatrixObject> rules) {
        Arrays.fill(partitionRuleMatrix, 0l);
        for (MatrixObject rule : rules) {
            int longIndex = (rule.getId() - 1) / 64;
            int intraIndex = 63 - (rule.getId() - 1) % 64;
            partitionRuleMatrix[longIndex] |= (1l << intraIndex);
        }
        return partitionRuleMatrix;
    }

    public static void setUnSet(long[] matrix, int index, boolean set) {
        int longIndex = index / 64;
        int intraIndex = 63 - index % 64;
        if (set) {
            matrix[longIndex] |= (1l << intraIndex);
        } else {
            matrix[longIndex] &= ~(1l << intraIndex);
        }
    }

    public static int getSize(long[] union) {
        int unionSize = 0;
        for (long u : union) {
            unionSize += Long.bitCount(u);
        }
        return unionSize;
    }

    public static void union(long[] union, long[] longs) {
        for (int j = 0; j < longs.length; j++) {
            union[j] |= longs[j];
        }
    }

    public static void subtract(long[] union, long[] longs) {
        for (int j = 0; j < longs.length; j++) {
            union[j] &= ~longs[j];
        }
    }

    public static boolean hasCommon(long[] a, long[] b) {
        for (int j = 0; j < a.length; j++) {
            if ((a[j] & b[j]) > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasOneAt(long[] longs, int i) {
        int longIndex = i / 64;
        int intraIndex = 63 - i % 64;
        if (longIndex >= longs.length) {
            System.out.println();
        }
        return (longs[longIndex] & (1l << intraIndex)) != 0;
    }

    public void extend(int newSize) {
        long[] newMatrix = new long[getLongNums(newSize)];
        System.arraycopy(matrix, 0, newMatrix, 0, Math.min(matrix.length, newMatrix.length));
        matrix = newMatrix;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof MatrixObject) {
            return hasOneAt(matrix, ((MatrixObject) o).getId() - 1);
        }
        return false;
    }

    @Override
    public MatrixSet<E> clone() {
        MatrixSet<E> output = new MatrixSet<E>(mapping);
        System.arraycopy(matrix, 0, output.matrix, 0, matrix.length);
        output.size = size;
        return output;
    }

    @Override
    public boolean add(E rule) {
        if (rule == null) {
            return false;
        }
        final int index = rule.getId() - 1;
        boolean alreadyThere = hasOneAt(matrix, index);
        if (!alreadyThere) {
            setUnSet(matrix, index, true);
            size++;
        }
        return !alreadyThere;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        final int index = ((E) o).getId() - 1;
        boolean alreadyThere = hasOneAt(matrix, index);
        if (alreadyThere) {
            setUnSet(matrix, index, false);
            size--;
        }
        return alreadyThere;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c instanceof MatrixSet) {
            for (int i = 0; i < matrix.length; i++) {
                long l = matrix[i] | ((MatrixSet) c).matrix[i];
                if (Long.bitCount(l) > Long.bitCount(matrix[i])) {
                    return false;
                }
            }
        } else {
            for (E rule : (Collection<? extends E>) c) {
                if (rule == null) {
                    return false;
                }
                if (!hasOneAt(matrix, rule.getId() - 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
/*
     * Note that it returns always true because of performance
 */
    public boolean addAll(Collection<? extends E> c) {
        boolean output = false;
        if (c instanceof MatrixSet) {
            int oldSize = size;
            union(matrix, ((MatrixSet) c).matrix);
            size = getSize(matrix);
            output = size != oldSize;
        } else {
            for (E rule : c) {
                output = add(rule) || output;
            }
        }
        return output;
    }

    public void copy(MatrixSet src) {
        System.arraycopy(src.matrix, 0, matrix, 0, matrix.length);
        size = src.size;
    }

    public int getSimilarity(MatrixSet other) {
        int sum = 0;
        for (int i = 0; i < matrix.length; i++) {
            sum += Long.bitCount(matrix[i] & other.matrix[i]);
        }
        return sum;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean output = false;
        if (c instanceof MatrixSet) {
            int oldSize = size;
            subtract(matrix, ((MatrixSet) c).matrix);
            size = getSize(matrix);
            output = size != oldSize;
        } else {
            for (E rule : (Collection<? extends E>) c) {
                output = remove(rule) || output;
            }
        }
        return output;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int oldSize = size;
        if (c instanceof MatrixSet) {
            for (int i = 0; i < matrix.length; i++) {
                matrix[i] &= ((MatrixSet) c).matrix[i];
            }
        } else {
            for (int i = 0; i < c.size(); i++) {
                boolean modified = false;
                Iterator<E> it = iterator();
                while (it.hasNext()) {
                    if (!c.contains(it.next())) {
                        it.remove();
                        modified = true;
                    }
                }
                return modified;
            }
        }
        size = getSize(matrix);
        return size != oldSize;
    }

    @Override
    public void clear() {
        Arrays.fill(matrix, 0l);
        size = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatrixSet rules = (MatrixSet) o;
        if (size != rules.size()) {
            return false;
        }

        if (!Arrays.equals(matrix, rules.matrix)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(matrix);
    }

    @Override
    public Iterator<E> iterator() {
//        System.out.println("A rule iterator on Matrix ruleset created!!!");
//        try {
//            throw new RuntimeException();
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//        }
        return new RuleIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] output = new Object[size()];
        final Iterator<E> iterator = iterator();
        int i = 0;
        while (iterator.hasNext()) {
            output[i] = iterator.next();
        }
        return output;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        final int size = size();
        if (a.length < size) {
            return (T[]) toArray();
        }
        final Iterator<E> iterator = iterator();
        int i = 0;
        while (iterator.hasNext()) {
            a[i] = (T) iterator.next();
        }
        a[size] = null;
        return a;
    }

    /**
     * Each matrix item must be distinguishable by a unique ID
     */
    public static class MatrixObject {
        private int id;

        protected int getId() {
            return id;
        }

        protected void setId(int id) {
            this.id = id;
        }
    }

    public static class MatrixMapping<E extends MatrixObject> extends AbstractCollection<E> {
        private final List<E> objects;

        public MatrixMapping() {
            this.objects = new ArrayList<>();
        }

        @Override
        public Iterator<E> iterator() {
            return objects.iterator();
        }

        public int size() {
            return objects.size();
        }

        public boolean add(E o) {
            objects.add(o);
            o.setId(objects.size());
            return true;
        }

        public E get(int currentIndex) {
            return objects.get(currentIndex);
        }
    }

    private class RuleIterator implements Iterator<E> {
        private static final int INITIAL_VALUE = -2;
        private static final int INVALID_VALUE = -1;
        int nextIndex;
        int currentIndex = INITIAL_VALUE;

        private RuleIterator() {
            nextIndex = findNextIndex();
        }

        @Override
        public boolean hasNext() {
            return nextIndex != INVALID_VALUE;
        }

        @Override
        public E next() {
            if (nextIndex == INVALID_VALUE) {
                throw new NoSuchElementException();
            }
            currentIndex = nextIndex;
            nextIndex++;
            nextIndex = findNextIndex();
            return (E) mapping.get(currentIndex);
        }

        @Override
        public void remove() {
            if (currentIndex >= 0) {
                final int index = currentIndex;
                boolean alreadyThere = hasOneAt(matrix, index);
                if (alreadyThere) {
                    setUnSet(matrix, index, false);
                    size--;
                }
                currentIndex = -1;
            }
        }

        private int findNextIndex() {
            for (int i = Math.max(nextIndex, 0); i < matrix.length * 64; i++) {
                if (hasOneAt(matrix, i)) {
                    return i;
                }
            }
            return INVALID_VALUE;
        }
    }
}
