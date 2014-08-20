package edu.usc.enl.dynamicmeasurement.data;

import edu.usc.enl.dynamicmeasurement.model.Packet;

import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/24/13
 * Time: 9:52 AM  <br/>
 * The main type of packets that represent a network packet read from trace files
 */
public class DataPacket extends Packet {
    private long srcIP;
    private long dstIP;
    private int srcPort;
    private int dstPort;
    private int protocol;
    private double size;

    public DataPacket(long time, long srcIP, long dstIP, int srcPort, int dstPort, int protocol, double size) {
        super(time);
        this.srcIP = srcIP;
        this.dstIP = dstIP;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
        this.size = size;
    }

    public DataPacket(StringTokenizer st) throws PacketParseException {
        super();
        fill(st);
    }

    public static long getSrcIP(StringTokenizer st) {
        st.nextToken();
        return Long.parseLong(st.nextToken());
    }

    public void fill(StringTokenizer st) throws PacketParseException {
        try {
            setTime(Long.parseLong(st.nextToken()));
            srcIP = Long.parseLong(st.nextToken());
            dstIP = Long.parseLong(st.nextToken());
            if (st.hasMoreTokens()) {
                srcPort = Integer.parseInt(st.nextToken());
                dstPort = Integer.parseInt(st.nextToken());
                String s = st.nextToken();
                protocol = s.equals("null") ? 0 : Integer.parseInt(s);
                size = Double.parseDouble(st.nextToken());
            } else {
                //it is a compressed format
                //time *= 1000000;
                size = dstIP;
                dstIP = 0;
                srcPort = 0;
                dstPort = 0;
                protocol = 0;
            }
        } catch (NumberFormatException e) {
            throw new PacketParseException(e);
        }
    }

    public String print() {
        String comma = ",";
        return time + comma + srcIP + comma + dstIP + comma + srcPort + comma + dstPort + comma + protocol + comma + size;
    }

    public long getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(long srcIP) {
        this.srcIP = srcIP;
    }

    public long getDstIP() {
        return dstIP;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public int getProtocol() {
        return protocol;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setDstIP(long dstIP) {
        this.dstIP = dstIP;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public static class PacketParseException extends Exception {
        public PacketParseException(Throwable cause) {
            super(cause);
        }
    }
}
