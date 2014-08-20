package edu.usc.enl.dynamicmeasurement.data.trace;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.data.MultiFileTraceReader;
import edu.usc.enl.dynamicmeasurement.data.PacketLoader;
import edu.usc.enl.dynamicmeasurement.model.Packet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 11/2/13
 * Time: 8:31 AM
 * </br>
 * only keep time, srcip, size in trace
 */
public class CompressTrace {
    public static void main(String[] args) throws FileNotFoundException {
        String outputFolder = args[0];
        new File(outputFolder).mkdirs();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            File f = new File(arg).getAbsoluteFile();
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (File file : files) {
                    convertFile(outputFolder, file);
                }
            }
        }
    }

    private static void convertFile(String outputFolder, File f) throws FileNotFoundException {
        String outputFolder2 = outputFolder + "/" + f.getParentFile().getName();
        new File(outputFolder2).mkdirs();
        System.out.println(f.getAbsoluteFile());
        try (PrintWriter pw = new PrintWriter(outputFolder2 + "/" + f.getName())) {
            ArrayBlockingQueue<Packet> q = new ArrayBlockingQueue<>(1000);
            PacketLoader loader = new PacketLoader(q, new MultiFileTraceReader(new String[]{f.getAbsolutePath()}));
            loader.start();
            while (true) {
                Packet p = q.poll();
                if (p instanceof DataPacket) {
                    pw.println((int) (p.getTime() / 1E6) + "," + ((DataPacket) p).getSrcIP() + "," + (int) (((DataPacket) p).getSize()));
                } else if (p instanceof FinishPacket) {
                    break;
                }
            }
        }
    }
}
