package edu.usc.enl.dynamicmeasurement.data.scenario.traffic;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/27/13
 * Time: 9:41 PM
 */
public class MergeTrafficFiles {
    // output  input1 input2 input3
    public static void main(String[] args) throws IOException {
        List<FileData> allFiles = new ArrayList<>();
        PriorityQueue<FileData> files = new PriorityQueue<>();
        String outputFile = args[0];
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile))) {
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                FileData e = new FileData(arg);
                files.add(e);
                allFiles.add(e);
            }
            while (files.size() > 0) {
                FileData file = files.poll();
                file.write(pw);
                if (file.hasData()) {
                    files.add(file);
                }
            }

            for (FileData file : allFiles) {
                file.finish();
            }
        }
    }

    public static long getTime(String line) {
        return Long.parseLong(line.substring(0, line.indexOf(",")));
    }

    private static class FileData implements Comparable<FileData> {
        private String line;
        private long time;
        private BufferedReader br;

        public FileData(String fileName) throws IOException {
            br = new BufferedReader(new FileReader(fileName));
            line = br.readLine();
            time = getTime(line);
        }

        public void finish() throws IOException {
            br.close();
        }

        public boolean hasData() {
            return line != null;
        }

        @Override
        public int compareTo(FileData o) {
            return Long.compare(time, o.time);
        }

        public void write(PrintWriter pw) throws IOException {
            pw.println(line);
            line = br.readLine();
            if (line != null) {
                time = getTime(line);
            }
        }


    }
}
