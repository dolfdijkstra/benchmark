package com.fatwire.benchmark.plexus.application;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.fatwire.benchmark.plexus.BenchmarkRunner;

public class App {

    /**
     * @param args
     * @throws PlexusContainerException 
     * @throws ComponentLookupException 
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "-h".equals(args[0])) {
            printUsage();
            return;
        }
        final PlexusContainer container = new DefaultPlexusContainer();

        final BenchmarkRunner runner = (BenchmarkRunner) container.lookup(
                BenchmarkRunner.ROLE, "default");

        for (int i = 0; i < args.length - 1; i++) {
            if ("-c".equals(args[i])) {
                runner.setNumberOfWorkers(Integer.parseInt(args[++i]));
            } else if ("-n".equals(args[i])) {
                runner.setMax(Integer.parseInt(args[++i]));
            } else if ("-d".equals(args[i])) {
                runner.setDelay(Integer.parseInt(args[++i]));
            } else if ("-url".equals(args[i])) {
                runner.setUrl(args[++i]);
            } else if ("-script".equals(args[i])) {
                runner.setScript(args[++i]);
            } else if ("-type".equals(args[i])) {
                runner.setType(args[++i]);

            }
        }
        final Thread hook = new Thread(new Runnable() {

            public void run() {
                System.out.println("shutting down");
                runner.shutdown();
                container.dispose();

            }

        });
        Runtime.getRuntime().addShutdownHook(hook);
        try {
            runner.init();
            runner.go();
            runner.shutdown();
        } finally {
            Runtime.getRuntime().removeShutdownHook(hook);
            container.dispose();
        }

    }

    static void printUsage() {
        System.out.println("command line options are");
        System.out.println("-c <number-of-useragents.");
        System.out.println("-n <total number of requests>");
        System.out
                .println("-d <delay in milliseconds between requests by same user-agent>. Defaults to zero.");
        System.out
                .println("-url <full url of page to test>. Exclusive with -script option.");

        System.out.println("-script <file with the list of urls>");
        System.out
                .println("-type <how to iterate of the list of urls in the script>. Possible values are 'simple' or 'random'. If omitted 'simple' is assumed.");
        System.out
        .println("for example");
        System.out
        .println(App.class.getName() +" -c 1 -n 1 -url http://www.google.com");
        System.out
        .println(App.class.getName() +" -c 20 -n 2000 -script mysite-urls.script -d 1000 -type random");

    }

}
