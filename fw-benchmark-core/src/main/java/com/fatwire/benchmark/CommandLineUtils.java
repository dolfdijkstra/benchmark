package com.fatwire.benchmark;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import com.fatwire.benchmark.script.AbstractScriptFactory;
import com.fatwire.benchmark.script.RandomScriptFactory;
import com.fatwire.benchmark.script.Script;
import com.fatwire.benchmark.script.SimpleScript;
import com.fatwire.benchmark.script.SimpleScriptFactory;
import com.fatwire.benchmark.session.Page;

public class CommandLineUtils {

    static Options getOptions() {
        Options options = new Options();
        /*
        options.addOption(new OptionBuilder().withLongOpt("concurrency").withDescription("The number of user-agents to start").hasArg(true).withArgName("number").create('c'));
        options.addOption("r", "rampup",true,
                "rampup time, time to wait before starting a new use-agent");
        options
                .addOption("n", true,
                        "total number of requests, or period to run, for instance 1h30m15s");
        options
                .addOption(new OptionBuilder()
                        .withArgName("milliseconds")
                        .hasArg()
                        .isRequired(false)
                        .withDescription(
                                "delay in milliseconds between requests by same user-agent. Defaults to zero.")
                        .create('d'));
        */

        OptionGroup scriptGroup = new OptionGroup();

        scriptGroup.addOption(new Option("url", true,
                "absolute url of page to test. Exclusive with -script option."));
        
        
        scriptGroup.addOption(new OptionBuilder().withArgName("file or url").hasArg()
                .isRequired(false).withDescription(
                        "file or url with the script").create("script"));
        options
                .addOption(new OptionBuilder()
                        .withArgName("string")
                        .hasArg(true)
                        .withDescription(
                                "how to iterate of the list of urls in the script. Possible values are 'simple' or 'random'. If omitted 'simple' is assumed.")
                        .create("type"));

        options
                .addOption(new OptionBuilder()
                        .withArgName("uri")
                        .hasArg(true)
                        .withDescription(
                                "uri to resolve relative uri against, for instance http://localhost:8080. If ommitted the urls in the script file must be absolute (containing scheme, host and path).")
                        .create("host"));
        options.addOptionGroup(scriptGroup);
        
        
        options
                .addOption(new OptionBuilder()
                        .withArgName("directory")
                        .hasArg(true)
                        .withDescription(
                                "directory where the reports will be written. If ommitted defaults to ./reports/current_date_time)")
                        .create("reportdir"));
        options
                .addOption(new OptionBuilder()
                        .withArgName("boolean")
                        .hasArg(true)
                        .withDescription(
                                "to use user-agent cache for contidional gets, true/false. Defaults to true")
                        .create("usercache"));

        options
                .addOption(new OptionBuilder().withArgName("string").withLongOpt(
                        "schedule").hasArg(true).withDescription(
                        "The schedule to use for the test. A schedule is build of a number of legs. Each leg has a rampup period to get to a number of concurrent user-agents and then a period to hold that level. "
                                + "The short notation for a leg is 'steps:rampup_time:user_level:hold_period', for instance '5:2m:200:2m'. "
                                + "This means that in 5 steps in 2 minutes a level of 200 (40 users per step, waits between steps is 24 seconds) will be reached and that level is held for 2 minutes. "
                                + "Each leg is seperated from the other by a semi-colon.").create("s"));

        return options;
    }

    static void parseCommandLine(CommandLine cmd, FatWireBenchmark benchMark) {

        
        long delay = 0;

        if (cmd.hasOption('d')) {
            String s = cmd.getOptionValue('d');
            try {
                delay = Long.parseLong(s);
            } catch (NumberFormatException ex) {
                printError("Invalid delay level: " + s);
            }
        }

        if (cmd.hasOption("script")) {

            String filename = cmd.getOptionValue("script");
            String type = "simple";
            if (cmd.hasOption("type")) {
                type = cmd.getOptionValue("type");
            }
            AbstractScriptFactory sf = null;
            if ("simple".equals(type)) {
                sf = new SimpleScriptFactory(filename, delay);
            } else if ("random".equals(type)) {
                sf = new RandomScriptFactory(filename, delay);
            } else {
                printError("unknown script type: " + type);
            }
            if (cmd.hasOption("host")) {
                String s = cmd.getOptionValue("host");
                try {
                    sf.setHost(s);
                } catch (Exception ex) {
                    printError("Invalid host: " + s);
                }
            }

            try {
                Script script = sf.getScript();
                benchMark.setScript(script);
            } catch (Exception e) {
                printError("Could not read script: " + filename);
            }
        } else if (cmd.hasOption("url")) {
            String url = cmd.getOptionValue("url");
            Page p = new Page(URI.create(url));
            p.setReadTime(delay);
            benchMark.setScript(new SimpleScript(Arrays
                    .asList(new Page[] { p }), delay));
        } else {
            printError("Neither script nor url specified.");
        }

        if (cmd.hasOption("usercache")) {
            String s = cmd.getOptionValue("usercache");
            try {
                benchMark.setUserCache(Boolean.parseBoolean(s));
            } catch (Exception ex) {
                printError("Invalid usercache: " + s);
            }
        }

        WorkerManager manager = new WorkerManager();
        benchMark.setWorkManager(manager);
        if (cmd.hasOption('s')) {
            String s = cmd.getOptionValue('s');
            Schedule schedule = Schedule.parse(s);
            manager.setSchedule(schedule);
            benchMark.setSchedule(schedule);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("1:"); // one step
            if (cmd.hasOption("r")) {
                String s = cmd.getOptionValue("r");
                try {
                    sb.append(Long.parseLong(s));
                    sb.append('s');
                    sb.append(':');
                } catch (Exception ex) {
                    printError("Invalid rampup: " + s);
                }
            } else {
                sb.append('1');
                sb.append('s');
                sb.append(':');
            }

            if (cmd.hasOption('c')) {
                String s = cmd.getOptionValue('c');
                try {
                    sb.append(Integer.parseInt(s));
                    sb.append(':');
                } catch (NumberFormatException ex) {
                    printError("Invalid number of workers level: " + s);
                }
            } else {
                sb.append(1);
                sb.append(':');

            }
            if (cmd.hasOption('n')) {
                String s = cmd.getOptionValue('n');
                sb.append(s);
            }
            Schedule schedule = Schedule.parse(sb.toString());
            manager.setSchedule(schedule);
            benchMark.setSchedule(schedule);

        }

        File reportDirectory = null;
        if (cmd.hasOption("reportdir")) {
            String s = cmd.getOptionValue("reportdir");
            try {
                reportDirectory = new File(s);
                reportDirectory.mkdirs();
            } catch (Exception ex) {
                printError("Invalid reportdir: " + s);
            }
        } else {
            File d = new File("reports");
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
            File repdir = new File(d, df.format(new Date()));

            repdir.mkdirs();
            reportDirectory = repdir;
        }

        benchMark.setStat(new BenchmarkStatistics(reportDirectory));

        benchMark.setReportDirectory(reportDirectory);

    }

    static void showUsage(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("FatWireBenchmark ", options,true);
    }

    static void printError(String msg) {
        System.err.println(msg);
        showUsage(getOptions());
        System.exit(-1);
    }
}

/**
 * OptionBuilder allows the user to create Options using descriptive methods.
*
* <p>Details on the Builder pattern can be found at
* <a href="http://c2.com/cgi-bin/wiki?BuilderPattern">
* http://c2.com/cgi-bin/wiki?BuilderPattern</a>.</p>
*
* @author John Keyes (john at integralsource.com)
* @version $Revision$, $Date$
* @since 1.0
*/
final class OptionBuilder {
    /** long option */
    private String longopt;

    /** option description */
    private String description;

    /** argument name */
    private String argName;

    /** is required? */
    private boolean required;

    /** the number of arguments */
    private int numberOfArgs = Option.UNINITIALIZED;

    /** option type */
    private Object type;

    /** option can have an optional argument value */
    private boolean optionalArg;

    /** value separator for argument value */
    private char valuesep;

    OptionBuilder() {
        // reset the OptionBuilder properties
        this.reset();
    }

    /**
     * Resets the member variables to their default values.
     */
    private void reset() {
        description = null;
        argName = "arg";
        longopt = null;
        type = null;
        required = false;
        numberOfArgs = Option.UNINITIALIZED;

        // PMM 9/6/02 - these were missing
        optionalArg = false;
        valuesep = (char) 0;
    }

    /**
     * The next Option created will have the following long option value.
     *
     * @param newLongopt the long option value
     * @return the OptionBuilder instance
     */
    public OptionBuilder withLongOpt(String newLongopt) {
        this.longopt = newLongopt;

        return this;
    }

    /**
     * The next Option created will require an argument value.
     *
     * @return the OptionBuilder instance
     */
    public OptionBuilder hasArg() {
        this.numberOfArgs = 1;

        return this;
    }

    /**
     * The next Option created will require an argument value if
     * <code>hasArg</code> is true.
     *
     * @param hasArg if true then the Option has an argument value
     * @return the OptionBuilder instance
     */
    public OptionBuilder hasArg(boolean hasArg) {
        this.numberOfArgs = hasArg ? 1 : Option.UNINITIALIZED;

        return this;
    }

    /**
     * The next Option created will have the specified argument value name.
     *
     * @param name the name for the argument value
     * @return the OptionBuilder instance
     */
    public OptionBuilder withArgName(String name) {
        this.argName = name;

        return this;
    }

    /**
     * The next Option created will be required.
     *
     * @return the OptionBuilder instance
     */
    public OptionBuilder isRequired() {
        this.required = true;

        return this;
    }

    /**
     * The next Option created uses <code>sep</code> as a means to
     * separate argument values.
     *
     * <b>Example:</b>
     * <pre>
     * Option opt = OptionBuilder.withValueSeparator(':')
     *                           .create('D');
     *
     * CommandLine line = parser.parse(args);
     * String propertyName = opt.getValue(0);
     * String propertyValue = opt.getValue(1);
     * </pre>
     *
     * @param sep The value separator to be used for the argument values.
     *
     * @return the OptionBuilder instance
     */
    public OptionBuilder withValueSeparator(char sep) {
        this.valuesep = sep;

        return this;
    }

    /**
     * The next Option created uses '<code>=</code>' as a means to
     * separate argument values.
     *
     * <b>Example:</b>
     * <pre>
     * Option opt = OptionBuilder.withValueSeparator()
     *                           .create('D');
     *
     * CommandLine line = parser.parse(args);
     * String propertyName = opt.getValue(0);
     * String propertyValue = opt.getValue(1);
     * </pre>
     *
     * @return the OptionBuilder instance
     */
    public OptionBuilder withValueSeparator() {
        this.valuesep = '=';

        return this;
    }

    /**
     * The next Option created will be required if <code>required</code>
     * is true.
     *
     * @param newRequired if true then the Option is required
     * @return the OptionBuilder instance
     */
    public OptionBuilder isRequired(boolean newRequired) {
        this.required = newRequired;

        return this;
    }

    /**
     * The next Option created can have unlimited argument values.
     *
     * @return the OptionBuilder instance
     */
    public OptionBuilder hasArgs() {
        this.numberOfArgs = Option.UNLIMITED_VALUES;

        return this;
    }

    /**
     * The next Option created can have <code>num</code> argument values.
     *
     * @param num the number of args that the option can have
     * @return the OptionBuilder instance
     */
    public OptionBuilder hasArgs(int num) {
        this.numberOfArgs = num;

        return this;
    }

    /**
     * The next Option can have an optional argument.
     *
     * @return the OptionBuilder instance
     */
    public OptionBuilder hasOptionalArg() {
        this.numberOfArgs = 1;
        this.optionalArg = true;

        return this;
    }

    /**
     * The next Option can have an unlimited number of optional arguments.
     *
     * @return the OptionBuilder instance
     */
    public OptionBuilder hasOptionalArgs() {
        this.numberOfArgs = Option.UNLIMITED_VALUES;
        this.optionalArg = true;

        return this;
    }

    /**
     * The next Option can have the specified number of optional arguments.
     *
     * @param numArgs - the maximum number of optional arguments
     * the next Option created can have.
     * @return the OptionBuilder instance
     */
    public OptionBuilder hasOptionalArgs(int numArgs) {
        this.numberOfArgs = numArgs;
        this.optionalArg = true;

        return this;
    }

    /**
     * The next Option created will have a value that will be an instance
     * of <code>type</code>.
     *
     * @param newType the type of the Options argument value
     * @return the OptionBuilder instance
     */
    public OptionBuilder withType(Object newType) {
        this.type = newType;

        return this;
    }

    /**
     * The next Option created will have the specified description
     *
     * @param newDescription a description of the Option's purpose
     * @return the OptionBuilder instance
     */
    public OptionBuilder withDescription(String newDescription) {
        this.description = newDescription;

        return this;
    }

    /**
     * Create an Option using the current settings and with
     * the specified Option <code>char</code>.
     *
     * @param opt the character representation of the Option
     * @return the Option instance
     * @throws IllegalArgumentException if <code>opt</code> is not
     * a valid character.  See Option.
     */
    public Option create(char opt) throws IllegalArgumentException {
        return create(String.valueOf(opt));
    }

    /**
     * Create an Option using the current settings
     *
     * @return the Option instance
     * @throws IllegalArgumentException if <code>longOpt</code> has not been set.
     */
    public Option create() throws IllegalArgumentException {
        if (longopt == null) {
            throw new IllegalArgumentException("must specify longopt");
        }

        return create(null);
    }

    /**
     * Create an Option using the current settings and with
     * the specified Option <code>char</code>.
     *
     * @param opt the <code>java.lang.String</code> representation
     * of the Option
     * @return the Option instance
     * @throws IllegalArgumentException if <code>opt</code> is not
     * a valid character.  See Option.
     */
    public Option create(String opt) throws IllegalArgumentException {
        // create the option
        Option option = new Option(opt, description);

        // set the option properties
        option.setLongOpt(longopt);
        option.setRequired(required);
        option.setOptionalArg(optionalArg);
        option.setArgs(numberOfArgs);
        option.setType(type);
        option.setValueSeparator(valuesep);
        option.setArgName(argName);

        // return the Option instance
        return option;
    }
}
