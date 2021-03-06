package jclapfilereader;

import org.jd.jclap.options.Option;
import org.jd.jclap.options.OptionCreationException;
import org.jd.jclap.options.OptionSet;
import org.jd.jclap.options.OptionWithValue;
import org.jd.jclap.parser.Parser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Simple (and ugly) filereader program using JCLAP
 */
public class JCLAP_FileReader {

    public static void main(String[] args) {
        /*
         * Option creation
         */
        Parser parser;
        OptionSet options = new OptionSet();
        OptionWithValue optLines;
        Option optNumber, optHelp, optVersion;
        String[] files;
        try {
            optLines = new OptionWithValue('l', "lines", "output only NBLINES lines");
            optLines.setValueName("NBLINES");
            optNumber = new Option('n', "number", "number all output lines");
            optHelp = new Option((char) 0, "help", "display this help and exit");
            optVersion = new Option((char) 0, "version", "output version information and exit");
        } catch (OptionCreationException e) {
            System.err.println(e.getMessage());
            return;
        }
        options.add(optLines);
        options.add(optNumber);
        options.add(optHelp);
        options.add(optVersion);
        /*
         * Args parsing
         */
        parser = new Parser("CLAPTest_FileReader");
        files = parser.parse(options, args);
        if (files == null) {
            // Parsing error
            System.err.println(parser.getErrorMsg());
            return;
        }
        if (args.length == 0) {
            // No argument, display the help
            optHelp.setIsSet(true);
        }
        // Version
        if (optVersion.isSet()) {
            System.out.println("CLAPTest_FileReader 1.0\n....");
            return;
        }
        // Help
        if (optHelp.isSet()) {
            System.out.println("CLAPTest_FileReader [OPTION] FILE ...");
            System.out.println(options.getHelpMessage());
            return;
        }
        int lines = -1; // Display all lines
        if (optLines.isSet()) {
            try {
                lines = Integer.parseInt(optLines.getValue());
            } catch (NumberFormatException e) {
                lines = -1;
            }
            if (lines < 1) {
                System.err.println("Invalid number of lines (has to be greater than 0)");
                return;
            }
        }
        try {
            if (files.length == 0) {
                System.err.println("Missing file(s)");
                return;
            }
            write(files, optNumber.isSet(), lines);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void write(String[] files, boolean number, int lines) throws FileNotFoundException, IOException {
        BufferedReader reader;
        String line;
        int num = 1;
        for (String file : files) {
            reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null && (lines == -1 || num < lines)) {
                System.out.println(((number) ? num + " " : "") + line);
                num++;
            }
        }
    }
}
