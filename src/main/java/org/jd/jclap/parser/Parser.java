package org.jd.jclap.parser;

import java.util.LinkedList;
import java.util.List;
import org.jd.jclap.options.Option;
import org.jd.jclap.options.OptionSet;
import org.jd.jclap.options.OptionWithValue;

public class Parser {

    /*
     **********************************************
     * The differents type of errors
     **********************************************
     */
    /**
     * The option does not exist in the option set
     */
    protected static final int INVALID_OPTION = 1;
    /**
     * The option does not require an argument
     */
    protected static final int NO_ARG_VALUE = 2;
    /**
     * The option do require an argument
     */
    protected static final int NEEDS_ARG_VALUE = 3;
    /**
     * Unexpected argument (a non-option argument between options)
     */
    protected static final int UNEXPECTED_ARG = 4;
    /*
     **********************************************
     * Attributes
     **********************************************
     */
    /**
     * Error message. Set by the parser when a error is detected.
     */
    protected String errorMsg;
    /**
     * Name of the application. This name is used to generate the error messages
     */
    protected String appName;

    /*
     **********************************************
     * Constructor
     **********************************************
     */
    /**
     * Creates a new instance of Parser
     *
     * @param appName Name of the application
     */
    public Parser(String appName) {
        this.appName = appName;
    }

    /*
     **********************************************
     * Getters
     **********************************************
     */
    /**
     * Returns the error message
     *
     * @return The error message, null if there is no error
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /*
     **********************************************
     * Methods
     **********************************************
     */
    /**
     * Parse the arguments. At the end, the options (param) are changed
     * (attribute : "set" and "value"). Returns the first non-option argument
     * index (in the args array), -1 in an error was detected.
     *
     * @param options The option collection
     * @param args    The arguments
     * @return        The non-optional arguments (ex : files, strings...), null
     *                an error was detected.
     */
    public String[] parse(OptionSet options, String[] args) {
        int i;
        List<String> otherArgs = new LinkedList<>();
        for (i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                if (args[i].length() == 1) {
                    log("-", INVALID_OPTION);
                    return null;
                }
                Option opt;
                if (args[i].charAt(1) != '-') {
                    /*
                     * Short name option(s)
                     */
                    char shortName;
                    for (int j = 1; j < args[i].length() - 1; j++) {
                        // In this loop, the options are in the same "block"
                        // For instance : "-abc", here we analyse "a" and "b"
                        // These 2 options can't have an argument, only the last option can (next argument)
                        shortName = args[i].charAt(j);
                        opt = options.getByShortName(shortName);
                        if (opt == null) {
                            log(String.valueOf(shortName), INVALID_OPTION);
                            return null;
                        }
                        if (opt instanceof OptionWithValue) {
                            // There is only options without argument in this loop
                            log(String.valueOf(shortName), NEEDS_ARG_VALUE);
                            return null;
                        }
                        opt.setIsSet(true);
                    }
                    // Last option (ex : 's' in "-s" or 'c' in "-abc")
                    // This option can have an argument
                    shortName = args[i].charAt(args[i].length() - 1);
                    opt = options.getByShortName(shortName);
                    if (opt == null) {
                        log(String.valueOf(shortName), INVALID_OPTION);
                        return null;
                    }
                    if (opt instanceof OptionWithValue) {
                        if (i >= args.length - 1) {
                            log(String.valueOf(shortName), NEEDS_ARG_VALUE);
                            return null;
                        }
                        ((OptionWithValue) opt).setValue(args[++i]);
                    }
                    opt.setIsSet(true);
                }
                else {
                    /*
                     * Long name option
                     */
                    String longName, value = null;
                    longName = args[i].substring(2);
                    // Split the option name and the value
                    int equalSignIndex = args[i].indexOf('='); // index of '=', -1 if it not is this string
                    if (equalSignIndex > -1) {
                        // The argument is an option with argument
                        longName = args[i].substring(2, equalSignIndex);
                        value = args[i].substring(equalSignIndex + 1);
                    }
                    opt = options.getByLongName(longName);
                    if (opt == null) {
                        log(longName, INVALID_OPTION);
                        return null;
                    }
                    if (opt instanceof OptionWithValue) {
                        if (value == null) {
                            // The option expected an argument, but it was not specified
                            log(longName, NEEDS_ARG_VALUE);
                            return null;
                        }
                        ((OptionWithValue) opt).setValue(value);
                    }
                    else if (value != null) {
                        // The option does not expect an argument, but there is one
                        log(longName, NO_ARG_VALUE);
                        return null;
                    }
                    opt.setIsSet(true);
                }
            }
            else {
                otherArgs.add(args[i]);
            }
        }
        return otherArgs.toArray(new String[0]);
    }

    /**
     * Set the error message.
     *
     * @param opt       The option name which is the source of the error
     * @param errorType The type of the error
     */
    protected void log(String opt, int errorType) {
        errorMsg = appName + ": ";
        switch (errorType) {
            case INVALID_OPTION:
                errorMsg += "invalid option -- '" + opt + "'";
                break;
            case NO_ARG_VALUE:
                errorMsg += "option '" + opt + "' does not require an argument";
                break;
            case NEEDS_ARG_VALUE:
                errorMsg += "option '" + opt + "' requires an argument";
                break;
            case UNEXPECTED_ARG:
                errorMsg += "unexpected option '" + opt + "'";
                break;
        }
        errorMsg += "\nTry '" + appName + " --help' for more information.";
    }

    /**
     * Reset the state of the parser. Set the error message to null
     */
    public void reset() {
        errorMsg = null;
    }
}
