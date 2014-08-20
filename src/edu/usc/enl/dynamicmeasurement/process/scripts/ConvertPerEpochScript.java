package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.algorithms.transform.EpochPacer;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.SourceDestinationPairTransformer;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.TransformHandler;
import edu.usc.enl.dynamicmeasurement.data.FileListParser;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.process.Simulator;
import org.apache.commons.cli.*;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/20/13
 * Time: 4:06 PM
 * <p>
 * This is for converting caida trace to csv file. the input format is output of an awk script
 */
public class ConvertPerEpochScript {
    public static void main(String[] args) throws Exception {

        String inputFolder = "";
        String inputPattern = "";
        String outputPrefix = "";
        int chopSize = 60;

        Options options = new Options();
        options.addOption(new Option("h", false, "Shows this help"));
        options.addOption(OptionBuilder.withArgName("Folder").hasArg().isRequired().withDescription("Input folder").create('i'));
        options.addOption(OptionBuilder.withArgName("File pattern").hasArg().isRequired().withDescription("<File prefix>+<start>-<end>").create("p"));
        options.addOption(OptionBuilder.withArgName("Output").hasArg().isRequired().withDescription("<Output folder>/<prefix>").create('o'));
        options.addOption(OptionBuilder.withArgName("seconds (Default=60)").hasArg().withType(Number.class).withDescription("File chop size").create('s'));

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h") || cmd.getOptions().length < options.getRequiredOptions().size()) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java <classname>", options, true);
                System.exit(0);
            }
            inputFolder = cmd.getOptionValue('i');
            inputPattern = cmd.getOptionValue('p');
            outputPrefix = cmd.getOptionValue('o');
            if (cmd.hasOption('s')) {
                chopSize = Integer.parseInt(cmd.getOptionValue('s'));
            }
        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java <classname>", options, true);
            System.exit(0);
        }

        new File(outputPrefix).getParentFile().mkdirs();
        FileListParser fileListParser = new FileListParser(inputFolder);
        fileListParser.parse(inputPattern);
        String[] packetsFile = fileListParser.getPacketsFile();
//        String[] packetsFile = new String[]{args[0]};
        int updateStepDuration = 1000000;
        int maxTime = 0;
        {
            PacketUser user = new TraceRewrite(outputPrefix, chopSize);
            user = new TransformHandler(user);
//            ((TransformHandler) user).addTransform(new SkewPacketUser(new WildcardPattern(0, 32, 0), 1.0));
            ((TransformHandler) user).addTransform(new SourceDestinationPairTransformer(new WildcardPattern(0, 32, 0)));
            user = new EpochPacer(user, updateStepDuration);
            new Simulator(maxTime).run(packetsFile, user);
        }
    }
}
