package org.checkerframework.framework.test2;

import org.checkerframework.framework.util.PluginUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestConfigurationBuilder {


    private List<File> testSourceFiles;
    private Set<String> processors;
    private SimpleOptionMap options;

    private boolean shouldEmitDebugInfo;

    public TestConfigurationBuilder() {
        testSourceFiles = new ArrayList<File>();
        processors = new LinkedHashSet<>();
        options = new SimpleOptionMap();
        shouldEmitDebugInfo = false;
    }

    public <T extends Collection<E>, E>  T addAll(T coll, Iterable<? extends E> objs) {
        for(E obj : objs) {
            coll.add(obj);
        }

        return coll;
    }


    public TestConfigurationBuilder(TestConfiguration initialConfig) {
        this.testSourceFiles = new ArrayList<>(initialConfig.getTestSourceFiles());
        this.processors = new LinkedHashSet<>(initialConfig.getProcessors());
        this.options = new SimpleOptionMap();
        this.addOptions(initialConfig.getOptions());

        this.shouldEmitDebugInfo = initialConfig.shouldEmitDebugInfo();
    }

    public List<String> validate(boolean requireProcessors) {
        List<String> errors = new ArrayList<>();
        if (testSourceFiles == null || !testSourceFiles.iterator().hasNext()) {
            errors.add("No source files specified!");
        }

        if (requireProcessors && !processors.iterator().hasNext()) {
            errors.add("No processors were specified!");
        }

        final Map<String, String> optionMap = options.getOptions();
        if (!optionMap.containsKey("-d") || optionMap.get("-d") == null) {
            errors.add("No output directory was specified.");
        }

        if (optionMap.containsKey("-processor")) {
            errors.add("Processors should not be added to the options list");
        }

        return errors;
    }

    public TestConfigurationBuilder adddToPathOption(String key, String toAppend) {
        options.addToPathOption(key, toAppend);
        return this;
    }

    public TestConfigurationBuilder addSourceFiles(Iterable<File> sourceFiles) {
        final List<File> newSourceFiles = new ArrayList<File>();

        for (File sourceFile : testSourceFiles) {
            newSourceFiles.add(sourceFile);
        }

        for(File sourceFile : sourceFiles){
            newSourceFiles.add(sourceFile);
        }
        testSourceFiles = newSourceFiles;
        return this;
    }

    public TestConfigurationBuilder setSourceFiles(List<File> sourceFiles) {
        this.testSourceFiles = new ArrayList<>(sourceFiles);
        return this;
    }

    public TestConfigurationBuilder setOptions(Map<String, String> options) {
        this.options.setOptions(options);
        return this;
    }

    public TestConfigurationBuilder addOption(String option) {
        this.options.addOption(option);
        return this;
    }

    public TestConfigurationBuilder addOption(String option, String value) {
        this.options.addOption(option, value);
        return this;
    }


    public TestConfigurationBuilder addOptionIfValueNonEmpty(String option, String value) {
        if (value != null && !value.isEmpty()) {
            return addOption(option, value);
        }

        return this;
    }

    public TestConfigurationBuilder addOptions(Map<String, String> options) {
        this.options.addOptions(options);
        return this;
    }

    public TestConfigurationBuilder addOptions(Iterable<String> newOptions) {
        this.options.addOptions(newOptions);
        return this;
    }

    public TestConfigurationBuilder setProcessors(Iterable<String> processors) {
        this.processors.clear();
        for(String proc : processors) {
            this.processors.add(proc);
        }
        return this;
    }

    public TestConfigurationBuilder addProcessor(String processor) {
        this.processors.add(processor);
        return this;
    }

    public TestConfigurationBuilder addProcessors(Iterable<String> processors) {
        for(String processor : processors) {
            this.processors.add(processor);
        }

        return this;
    }

    public TestConfigurationBuilder emitDebugInfo() {
        this.shouldEmitDebugInfo = true;
        return this;
    }

    public TestConfigurationBuilder dontEmitDebugInfo() {
        this.shouldEmitDebugInfo = false;
        return this;
    }

    public TestConfigurationBuilder setShouldEmitDebugInfo(boolean shouldEmitDebugInfo) {
        this.shouldEmitDebugInfo = shouldEmitDebugInfo;
        return this;
    }

    public TestConfiguration build() {
        return new ImmutableTestConfiguration(testSourceFiles, new ArrayList<>(processors), options.getOptions(),
                                              shouldEmitDebugInfo);
    }

    public TestConfiguration validateThenBuild(boolean requireProcessors) {
        List<String> errors = validate(requireProcessors) ;
        if (errors.isEmpty()) {
            return build();
        }

        throw new RuntimeException("Attempted to build invalid test configuration:\n"
                                 + "Errors:\n"
                                 + PluginUtil.join("\n", errors) + "\n"
                                 + this.toString() + "\n");
    }

    public List<String> flatOptions() {
        return options.getOptionsAsList();
    }

    public String toString() {
        return "TestConfigurationBuilder:\n"
             + "testSourceFiles="  + ( testSourceFiles == null ? "null" : PluginUtil.join(" ", testSourceFiles) ) + "\n"
             + "processors="       + ( processors == null      ? "null" : PluginUtil.join(", ", processors)     ) + "\n"
             + "options="          + ( options == null         ? "null" : PluginUtil.join(", ", options.getOptionsAsList())  ) + "\n"
             + "shouldEmitDebugInfo=" + shouldEmitDebugInfo;
    }

    public static final String TESTS_OUTPUTDIR = "tests.outputDir";
    public static File getOutputDirFromProperty() {
        return new File(System.getProperty("tests.outputDir",
                        "tests" + File.separator + "build" + File.separator + "testclasses"));
    }

    public static String getDefaultClassPath() {
        String classpath = System.getProperty("tests.classpath",
                "tests" + File.separator + "build");
        String globalclasspath = System.getProperty("java.class.path", "");
        return "build" + File.pathSeparator +
               "junit.jar" + File.pathSeparator +
               classpath + File.pathSeparator +
               globalclasspath;
    }

    public static String getJdkJarPathFromProperty() {
        return System.getProperty("JDK_JAR");
    }

    /**
     * This is the default configuration used by Checker Framework JUnit tests.
     * @param testSourcePath The path to the Checker test file sources, usually this is the directory of Checker's tests
     * @param outputClassDirectory The directory to place classes compiled for testing
     * @param classPath The classpath to use for compilation
     * @param testSourceFiles The Java files that compose the test
     * @param processors The checkers or other annotation processors to run over the testSourceFiles
     * @param options The options to the compiler/processors
     * @param shouldEmitDebugInfo Whether or not debug information should be emitted
     * @return A TestConfiguration with input parameters added plus the normal default options, compiler,
     *         and file manager used by Checker Framework tests
     */
    public static TestConfiguration buildDefaultConfiguration(String testSourcePath,
                                                              File outputClassDirectory,
                                                              String classPath,
                                                              Iterable<File> testSourceFiles,
                                                              Iterable<String> processors,
                                                              List<String> options,
                                                              boolean shouldEmitDebugInfo) {

        TestConfigurationBuilder configBuilder = new TestConfigurationBuilder()
                .setShouldEmitDebugInfo(shouldEmitDebugInfo)
                .addProcessors(processors)
                .addOption("-Xmaxerrs", "9999")
                .addOption("-g")
                .addOption("-Xlint:unchecked")
                .addOption("-XDrawDiagnostics")  //use short javac diagnostics
                .addOption("-AprintErrorStack")
                .addSourceFiles(testSourceFiles);

        if (outputClassDirectory != null) {
            configBuilder.addOption("-d", outputClassDirectory.getAbsolutePath());
        }

        // Use the annotated jdk for the compile bootclasspath
        // This is set by build.xml
        String jdkJarPath = getJdkJarPathFromProperty();
        if (notNullOrEmpty(jdkJarPath)) {
            configBuilder.addOption("-Xbootclasspath/p:" + jdkJarPath);
        }

        configBuilder
                .addOptionIfValueNonEmpty("-sourcepath", testSourcePath)
                .addOption("-implicit:class")
                .addOption("-classpath", classPath);

        configBuilder.addOptions(options);
        return configBuilder.validateThenBuild(true);
    }

    private static boolean notNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * This is the default configuration used by Checker Framework JUnit tests.
     * @param testSourcePath The path to the Checker test file sources, usually this is the directory of Checker's tests
     * @param testFile a single test java file to compile
     * @param checkerName a single Checker to include in the processors field
     * @param options The options to the compiler/processors
     * @param shouldEmitDebugInfo Whether or not debug information should be emitted
     * @return A TestConfiguration with input parameters added plus the normal default options, compiler,
     *         and file manager used by Checker Framework tests
     */
    public static TestConfiguration buildDefaultConfiguration(String testSourcePath,
                                                              File testFile,
                                                              String checkerName,
                                                              List<String> options,
                                                              boolean shouldEmitDebugInfo) {

        String classpath = getDefaultClassPath();
        File outputDir = getOutputDirFromProperty();
        List<File> javaFiles = Arrays.asList(testFile);
        List<String> processors = Arrays.asList(checkerName);
        return buildDefaultConfiguration(testSourcePath, outputDir, classpath, javaFiles, processors,
                                         options, shouldEmitDebugInfo);
    }

}
