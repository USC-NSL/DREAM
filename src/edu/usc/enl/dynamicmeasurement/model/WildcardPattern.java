package edu.usc.enl.dynamicmeasurement.model;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/22/13
 * Time: 11:42 AM <br/>
 * The main class that represents a prefix and its size
 */
public class WildcardPattern implements Cloneable, Comparable<WildcardPattern> {
    public static final WeightComparator WEIGHT_COMPARATOR = new WeightComparator();
    public static final WildcardPatternWildcardNumComparator WILDCARDNUM_COMPARATOR = new WildcardPatternWildcardNumComparator();
    public static final char WILDCARD_CHAR = '_';
    public static final char WILDCARD_FOLDER_CHAR = '=';
    public static int TOTAL_LENGTH = 32;
    /**
     * the prefix
     */
    protected long data;
    /**
     * Number of wildard numbers for the prefix. Thus the actual output is data < < wildcardNum
     */
    protected int wildcardNum;
    /**
     * keeps track of the size this prefix . Merging this into the prefix class may not be the best design but is
     * convenient.
     */
    private double weight;

    /**
     * @param input  must cover all bits of TOTAL_LENGTH bits.
     *               Multiple formats are supported. <ul>
     *               <li>Numeric formation e.g., 10.0.5.0/24</li>
     *               <li>bit pattern e.g., 111111001010101010_____. where the character for don't care (_ here)
     *               can be either WILDCARD_CHAR or WILDCARD_FOLDER_CHAR</li>
     *               </ul>
     * @param weight
     */
    public WildcardPattern(String input, double weight) {
        if (input.matches("((\\d+)\\.){3}\\d+(/\\d+)?")) {
            int dataInt;
            int slashIndex = input.indexOf("/");
            if (slashIndex >= 0) {
                dataInt = toIPv4Address(input.substring(0, slashIndex));
                wildcardNum = TOTAL_LENGTH - Integer.parseInt(input.substring(slashIndex + 1));
                if (wildcardNum == TOTAL_LENGTH) {
                    dataInt = 0;
                } else {
                    dataInt >>>= wildcardNum;
                }
            } else {
                dataInt = toIPv4Address(input);
                wildcardNum = 0;
            }
            data = dataInt & ((1l << 32) - 1);

        } else {
            int notWildcardsNum = input.indexOf(WILDCARD_CHAR);
            if (notWildcardsNum < 0) {
                notWildcardsNum = input.indexOf(WILDCARD_FOLDER_CHAR);
            }
            data = 0;
            if (notWildcardsNum == 0) {
                data = 0;
            } else if (notWildcardsNum > 0) {
                data = Long.parseLong(input.substring(0, notWildcardsNum), 2);
            } else {
                data = Long.parseLong(input, 2);
                notWildcardsNum = TOTAL_LENGTH;
            }
            wildcardNum = TOTAL_LENGTH - notWildcardsNum;
        }
        this.weight = weight;
    }

    public WildcardPattern(long data, int wildcardNum, double weight) {
        this.data = data;
        this.wildcardNum = wildcardNum;
        this.weight = weight;
    }

    public static void main(String[] args) {
        int x = -1;
        long y = x;
        System.out.println(y);
        System.out.println(y >>> 5);
        System.out.println(y & ((1l << 32) - 1));
        y = ((1l << 32) - 1) - 1232;
        System.out.println((int) y);
        TOTAL_LENGTH = 32;
        System.out.println(new WildcardPattern("0.0.0.0", 0).toCIDRString());
        System.out.println(new WildcardPattern("1.0.0.0", 0).toCIDRString());
        System.out.println(new WildcardPattern("0.128.0.0", 0).toCIDRString());
        System.out.println(new WildcardPattern("1.0.0.0/0", 0).toCIDRString());
        System.out.println(new WildcardPattern("1.0.0.0/32", 0).toCIDRString());
        System.out.println(new WildcardPattern("192.168.0.1/24", 0).toCIDRString());
    }

    /**
     * GOT FROM FLOODLIGHT!!!
     * Accepts an IPv4 address of the form xxx.xxx.xxx.xxx, ie 192.168.0.1 and
     * returns the corresponding 32 bit integer.
     *
     * @param ipAddress
     * @return
     */
    public static int toIPv4Address(String ipAddress) {
        if (ipAddress == null)
            throw new IllegalArgumentException("Specified IPv4 address must" +
                    "contain 4 sets of numerical digits separated by periods");
        String[] octets = ipAddress.split("\\.");
        if (octets.length != 4)
            throw new IllegalArgumentException("Specified IPv4 address must" +
                    "contain 4 sets of numerical digits separated by periods");

        int result = 0;
        for (int i = 0; i < 4; ++i) {
            int oct = Integer.valueOf(octets[i]);
            if (oct > 255 || oct < 0)
                throw new IllegalArgumentException("Octet values in specified" +
                        " IPv4 address must be 0 <= value <= 255");
            result |= oct << ((3 - i) * 8);
        }
        return result;
    }

    protected static String ipToString(long ip) {
        return ((ip & 0xff000000) >>> 24)
                + "." + ((ip & 0x00ff0000) >> 16) + "."
                + ((ip & 0x0000ff00) >> 8) + "."
                + (ip & 0x000000ff);
    }

    public static String toStringNoWeight(long data, int wildcardNum, char c) {
        char[] buf = new char[TOTAL_LENGTH];
        int charPos = TOTAL_LENGTH - wildcardNum;
        long d = data;
        while (charPos > 0) {
            buf[--charPos] = (d & 1) == 0 ? '0' : '1';
            d >>>= 1;
        }
        for (int i = 0; i < wildcardNum; i++) {
            buf[TOTAL_LENGTH - i - 1] = c;
        }
        return new String(buf);
    }

    public String toCIDRString() {
        long ip = data << wildcardNum;
        int prefix = TOTAL_LENGTH - wildcardNum;
        String str;
        if (prefix >= 32) {
            str = ipToString(ip);
        } else {
            // use the negation of mask to fake endian magic
            int mask = ~((1 << (32 - prefix)) - 1);
            str = ipToString(ip & mask) + "/" + prefix;
        }

        return str;
    }

    /**
     * Gets the parent prefix (check with canGoUp) method first
     * Note that this will change the hashcode of the prefix. Thus make sure it is removed from any hashset
     */
    public WildcardPattern goUp() {
        data = (data >>> 1);
        wildcardNum++;
        return this;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Make shure the number of wildcards are greater than 0
     *
     * @return
     */
    public boolean canGoDown() {
        return wildcardNum > 0;
    }

    // make sure it is removed from any hashset
    public WildcardPattern goDown(boolean oneZero) throws InvalidWildCardValue {
        if (!canGoDown()) {
            throw new InvalidWildCardValue("Negative wildcardNum(" + wildcardNum + ")");
        }
        data = data << 1;
        if (oneZero) {
            data++;
        }
        wildcardNum--;
        return this;
    }

    public long getData() {
        return data;
    }

    public int getWildcardNum() {
        return wildcardNum;
    }

    /**
     * If this is not the root of the tree, the output will be the other child of the parent
     *
     * @return
     */
    public WildcardPattern getSibling() {
        WildcardPattern clone = clone();
        clone.goUp();
        try {
            clone.goDown((data & 1) == 0);
        } catch (InvalidWildCardValue invalidWildCardValue) {
            invalidWildCardValue.printStackTrace();
        }
        clone.setWeight(0);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WildcardPattern)) return false;

        WildcardPattern that = (WildcardPattern) o;

        if (data != that.data) return false;
        if (wildcardNum != that.wildcardNum) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (data ^ (data >>> 32));
        result = 31 * result + wildcardNum;
        return result;
    }

    @Override
    public WildcardPattern clone() {
        return new WildcardPattern(data, wildcardNum, weight);
    }

    /**
     * @param d
     * @return if this prefix matches d
     */
    public boolean match(long d) {
        return (d >>> wildcardNum) == data;
    }

    /**
     * If prefix "pattern" is descendant of this.
     *
     * @param pattern
     * @return
     */
    public boolean match(WildcardPattern pattern) {
        if (pattern.getWildcardNum() > wildcardNum) {
            return false;
        }
        long d = pattern.getData() >>> (wildcardNum - pattern.getWildcardNum());
        return data == d;
    }

    @Override
    //InOrder comparison
    public int compareTo(WildcardPattern o) {
        if (wildcardNum > o.wildcardNum) {
            long d2 = o.data >>> (wildcardNum - o.wildcardNum - 1);
            if (data == d2 >>> 1) {//parent child
                return (d2 & 1) == 0 ? 1 : -1;
            }
            return Long.compare(data, (d2 >>> 1));
        } else if (wildcardNum < o.wildcardNum) {
            long d1 = data >>> (o.wildcardNum - wildcardNum - 1);
            if (o.data == d1 >>> 1) {//parent child
                return (d1 & 1) == 0 ? -1 : 1;
            }
            return Long.compare((d1 >>> 1), o.data);
        } else {
            return Long.compare(data, o.data);
        }
    }

    public boolean isLeft() {
        return (data & 1) == 0;
    }

    public boolean isSibling(WildcardPattern pattern) {
        return wildcardNum == pattern.wildcardNum && (data >>> 1) == (pattern.data >>> 1);
    }

    /**
     * @return If there is any bit to be wildcarded
     */
    public boolean canGoUp() {
        return wildcardNum < TOTAL_LENGTH;
    }

    @Override
    public String toString() {
        return toStringNoWeight() + ": " + weight;
    }

    /**
     * @param p
     * @return the common pattern (common ancestor in the prefix tree)
     */
    public WildcardPattern getCommonParent(WildcardPattern p) {
        WildcardPattern pMax = wildcardNum > p.wildcardNum ? this : p;
        WildcardPattern pMin = wildcardNum > p.wildcardNum ? p : this;
        //make same level
        long dMin = pMin.data >>> (pMax.wildcardNum - pMin.wildcardNum);
        long dMax = pMax.data;
        int w = pMax.wildcardNum;
        while (w <= TOTAL_LENGTH) {
            if (dMin == dMax) {
                return new WildcardPattern(dMax, w, 0);
            }
            w++;
            dMin >>>= 1;
            dMax >>>= 1;
        }
        return new WildcardPattern(0, TOTAL_LENGTH, 0);
    }

    public WildcardPattern getCommonParent2(WildcardPattern p) { //BUG FOR 0 AND 010
        long shiftedData = data << wildcardNum;
        long uncommonDataMask = shiftedData ^ (p.data << p.wildcardNum);
        if (uncommonDataMask == 0) {
            //one is parent
            WildcardPattern parent = (wildcardNum > p.wildcardNum ? this : p).clone();
            parent.setWeight(0);
            return parent;
        } else {
            long l = (Long.highestOneBit(uncommonDataMask) << 1) - 1;
            int parentWildcardNum = Math.max(Math.max(Long.bitCount(l), wildcardNum), p.wildcardNum);
            long parentData = (shiftedData & (~l)) >>> parentWildcardNum;

            WildcardPattern wildcardPattern = new WildcardPattern(parentData, parentWildcardNum, 0);
//        if (!wildcardPattern.equals(getCommonParent2(p))){
//            System.out.println("Error");
//            System.exit(1);
//        }
            return wildcardPattern;
        }
    }

    public String toStringFolderName() {
        return toStringNoWeight(data, wildcardNum, WILDCARD_FOLDER_CHAR);
    }

    public String toStringNoWeight() {
        return toStringNoWeight(data, wildcardNum, WILDCARD_CHAR);
    }

    public boolean isChild(WildcardPattern child) {
        return child.getWildcardNum() == wildcardNum - 1 && (child.getData() >>> 1) == data;
    }

    public String getLabel() {
        return (isLeft() ? "0" : "1");
    }

    public static class WeightComparator implements Comparator<WildcardPattern> {

        @Override
        public int compare(WildcardPattern o1, WildcardPattern o2) {
            int c = Double.compare(o1.weight, o2.weight);
            if (c == 0) {
//                System.out.println(o1+" vs "+o2+"="+-o1.compareTo(o2));
                return -o1.compareTo(o2);
            }
//            System.out.println(o1+" vs "+o2+"="+-c);
            return -c;
        }
    }

    public static class WildcardPatternWildcardNumComparator implements Comparator<WildcardPattern> {
        @Override
        public int compare(WildcardPattern o1, WildcardPattern o2) { //sort to not process a children after its parent
            int c = (o1.getWildcardNum() - o2.getWildcardNum());
            if (c == 0) {
                return o1.compareTo(o2);
            }
            return c;
        }
    }

    public class InvalidWildCardValue extends Exception {
        public InvalidWildCardValue(String message) {
            super(message);
        }
    }
}
