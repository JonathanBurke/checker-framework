package org.checkerframework.framework.test2;

import org.checkerframework.framework.test2.diagnostics.DiagnosticCategory;
import org.checkerframework.framework.test2.diagnostics.ExpectedDiagnosticLine;
import org.checkerframework.framework.test2.diagnostics.ExpectedDiagnosticLine.ExpectedDiagnostic;
import org.checkerframework.framework.test2.diagnostics.MixedDiagnosticReader;
import org.checkerframework.framework.util.PluginUtil;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import org.junit.Assert;

public class TestUtilities {


    public static final boolean isJSR308Compiler;
    public static final boolean isAtLeast8Jvm;

    static {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        OutputStream err = new ByteArrayOutputStream();
        compiler.run(null, null, err, "-version");
        isJSR308Compiler = err.toString().contains("jsr308");
        isAtLeast8Jvm = org.checkerframework.framework.util.PluginUtil.getJreVersion() >= 1.8d;
    }

    public static List<String> readExpectedDiagnosticsFromJavaFile(File file) {
        try {
            InputStream fileInputStream = new FileInputStream(file);
            List<String> diagnostics = readExpectedDiagnosticsFromJavaFile(fileInputStream);
            fileInputStream.close();

            return diagnostics;

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Exception while trying to read diagnostics for file: " + file.getName(), e);

        } catch (IOException e) {
            throw new RuntimeException("Exception while trying to read diagnostics for file: " + file.getName(), e);
        }
    }

    public static List<String> readExpectedDiagnosticsFromJavaFile(JavaFileObject file) {
        try {
            InputStream inputStream = file.openInputStream();
            List<String> diagnostics = readExpectedDiagnosticsFromJavaFile(inputStream);
            inputStream.close();

            return diagnostics;

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Exception while trying to read diagnostics for file: " + file.getName(), e);

        } catch (IOException e) {
            throw new RuntimeException("Exception while trying to read diagnostics for file: " + file.getName(), e);
        }
    }

    public static String lineToDiagnostic(String line, int lineNumber) {
        line = line.trim();
        if (line.startsWith("//::")) {
            int errorLine = lineNumber + 1;
            // drop the //::
            line = line.substring(4);
            String[] msgs = line.split("::");
            for (String msg : msgs) {
                // The trim removes spaces before and after the message.
                // This allows us to write "//:: A :: B
                // But it prevents us to check on leading spaces in messages.
                // I think that's OK, as we're always testing against "(codes)".
                msg = ":" + errorLine + ": " + msg.trim();
                return msg;
            }
        } else if (line.startsWith("//warning:")) {
            // com.sun.tools.javac.util.AbstractDiagnosticFormatter.formatKind(JCDiagnostic, Locale)
            // These are warnings from javax.tools.Diagnostic.Kind.WARNING
            String msg = line.substring(2);
            return msg;
        }

        return null;
    }

    public static List<String> readExpectedDiagnosticsFromJavaFile(InputStream javaFileStream) {
        List<String> expected = new ArrayList<String>();

        try {
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(javaFileStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String diagnosticStr = lineToDiagnostic(line, reader.getLineNumber());
                if (diagnosticStr != null) {
                    expected.add(diagnosticStr);
                }
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return expected;
    }

    public static List<String> readExpectedDiagnosticsFromJavaFile(Iterable<? extends JavaFileObject> fileObjects) {
        final List<String> expected = new ArrayList<String>();

        for (JavaFileObject file : fileObjects) {
            expected.addAll(readExpectedDiagnosticsFromJavaFile(file));
        }

        return expected;
    }


    public static List<String> readExpectedDiagnosticsFromJavaFile(File[] files) {
        final List<String> expected = new ArrayList<String>();

        for (File file : files) {
            expected.addAll(readExpectedDiagnosticsFromJavaFile(file));
        }

        return expected;
    }

    public static List<String> readExpectedDiagnosticsFromDiagnosticsFile(File expectedDiagnosticFile) {
        ArrayList<String> expectedDiagnostics = new ArrayList<String>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(expectedDiagnosticFile));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#"))
                    continue;
                int colonIndex = line.indexOf(':');
                if (colonIndex != -1) {
                    expectedDiagnostics.add(line.substring(colonIndex).trim());
                } else {
                    // Either other javac output should be redirected
                    // elsewhere, so as not to confuse assertDiagnostics,
                    // or else assertDiagnostics ought to recognize other
                    // javac output.  And the file format should be defined
                    // somewhere -- what is expected to precede the first
                    // colon?  Should it always be in the first column?
                }
            }
            reader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return expectedDiagnostics;
    }

    public static List<Object[]> findNestedJavaTestFiles(String... dirNames) {
        return findRelativeNestedJavaFiles(new File("tests"), dirNames);
    }

    public static List<Object[]> findRelativeNestedJavaFiles(String parent, String... dirNames) {
        return findRelativeNestedJavaFiles(new File(parent), dirNames);
    }

    public static List<Object[]> findRelativeNestedJavaFiles(File parent, String... dirNames) {
        File [] dirs = new File[dirNames.length];

        int i = 0;
        for(String dirName : dirNames) {
            dirs[i] = new File(parent, dirName);
            i += 1;
        }

        return getJavaFilesAsArgumentList(dirs);
    }

    //We should do this while discovering source file but that would require a
    //refactoring of the test utilities
    public static void filterOutJdk8Sources(List<Object[]> sources) {
        for (int i = 0; i < sources.size(); ) {
            for (Object sourceObj : sources.get(i)) {
                File sourceFile = (File) sourceObj;
                if (sourceFile.getAbsolutePath().contains("java8")) {
                    sources.remove(i);
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Traverses the directories listed looking for java test files
     * @param dirs
     * @return
     */
    public static List<Object[]> getJavaFilesAsArgumentList(File... dirs) {
        List<Object[]> arguments = new ArrayList<Object[]>();
        for (File dir : dirs) {
            List<File> javaFiles = deeplyEnclosedJavaTestFiles(dir);

            for (File javaFile : javaFiles) {
                arguments.add(new Object[] { javaFile });
            }
        }
        return arguments;
    }

    /**
     * Returns all the java files that are descendants of the given directory
     */
    public static List<File> deeplyEnclosedJavaTestFiles(File directory) {
        if (!directory.exists())
            throw new IllegalArgumentException("directory does not exist: " + directory);
        if (!directory.isDirectory())
            throw new IllegalArgumentException("found file instead of directory: " + directory);

        List<File> javaFiles = new ArrayList<File>();

        File[] in = directory.listFiles();
        Arrays.sort(in, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (File file : in) {
            if (file.isDirectory())
                javaFiles.addAll(deeplyEnclosedJavaTestFiles(file));
            else if (isJavaTestFile(file))
                javaFiles.add(file);
        }

        return javaFiles;
    }

    public static boolean isJavaFile(File file) {
        return file.isFile() && file.getName().endsWith(".java");
    }

    public static boolean isJavaTestFile(File file) {
        if (!isJavaFile(file))
            return false;
        Scanner in = null;
        try {
            in = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (in.hasNext()) {
            String nextLine = in.nextLine();
            if (nextLine.contains("@skip-test") ||
                    (!isJSR308Compiler && nextLine.contains("@non-308-skip-test")) ||
                    (!isAtLeast8Jvm && nextLine.contains("@below-java8-jdk-skip-test"))) {
                in.close();
                return false;
            }
        }
        in.close();
        return true;
    }

    public static String diagnosticToString(final Diagnostic<? extends JavaFileObject> diagnostic, boolean usingAnomsgtxt) {

        String result = diagnostic.toString().trim();

        // suppress Xlint warnings
        if ( result.contains("uses unchecked or unsafe operations.")
          || result.contains("Recompile with -Xlint:unchecked for details.")
          || result.endsWith(" declares unsafe vararg methods.")
          || result.contains("Recompile with -Xlint:varargs for details.")) {
            return null;
        }

        if (usingAnomsgtxt) {
            // Lines with "unexpected Throwable" are stack traces
            // and should be printed in full.
            if (!result.contains("unexpected Throwable")) {
                String firstLine;
                if (result.contains("\n")) {
                    firstLine = result.substring(0, result.indexOf('\n'));
                } else {
                    firstLine = result;
                }
                if (firstLine.contains(".java:")) {
                    firstLine = firstLine.substring(firstLine.indexOf(".java:") + 5).trim();
                }
                result = firstLine;
            }
        }

        return result;
    }

    public static Set<String> diagnosticsToStrings(final Iterable<Diagnostic<? extends JavaFileObject>> actualDiagnostics, boolean usingAnomsgtxt) {
        Set<String> actualDiagnosticsStr = new LinkedHashSet<String>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : actualDiagnostics) {
            String diagnosticStr = TestUtilities.diagnosticToString(diagnostic, usingAnomsgtxt);
            if (diagnosticStr != null) {
                actualDiagnosticsStr.add(diagnosticStr);
            }
        }

        return actualDiagnosticsStr;
    }

    public static List<ExpectedDiagnostic> convertExpectedDiagnostics(List<String> stringDiagnostics) {
        List<ExpectedDiagnostic> diagnostics = new ArrayList<>(stringDiagnostics.size());

        for (String stringDiagnostic : stringDiagnostics) {
            diagnostics.add(new ExpectedDiagnostic(stringDiagnostic));
        }

        return diagnostics;
    }

    public static String summarizeSourceFiles(List<File> javaFiles) {
        StringBuilder listStrBuilder = new StringBuilder();

        boolean first = true;
        for (File file : javaFiles) {
            if (first) {
                first = false;
            } else {
                listStrBuilder.append(" ,");
            }
            listStrBuilder.append(file.getAbsolutePath());
        }

        return listStrBuilder.toString();
    }

    public static File getTestFile(String fileRelativeToTestsDir) {
        return new File("tests", fileRelativeToTestsDir);
    }

    public static File findComparisonFile(File testFile) {
        final File comparisonFile = new File(testFile.getParent(), testFile.getName().replace(".java", ".out"));
        return comparisonFile;
    }

    public static List<String> optionMapToList(Map<String, String> options) {
        List<String> optionList = new ArrayList<>(options.size() * 2);

        for (Entry<String, String> opt : options.entrySet()) {
            optionList.add(opt.getKey());

            if (opt.getValue() != null) {
                optionList.add(opt.getValue());
            }
        }

        return optionList;
    }

    public static void writeLines(File file, Iterable<?> lines) {
        try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            Iterator<?> iter = lines.iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (next == null) {
                    bw.write("<null>");
                } else {
                    bw.write(next.toString());
                }
            }
            bw.flush();
            bw.close();

        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public static void writeDiagnostics(File file, File testFile, List<String> expected, List<String> actual, List<String> unexpected, List<String> missing, boolean usingNoMsgText, boolean testFailed) {
        try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write("File: " + testFile.getAbsolutePath() + "\n");
            bw.write("TestFailed: " + testFailed + "\n");
            bw.write("Using nomsgtxt: " + usingNoMsgText + "\n");
            bw.write("#Missing: " + missing.size() + "      #Unexpected: " + unexpected.size() + "\n");

            bw.write("Expected:\n");
            bw.write(PluginUtil.join("\n", expected));
            bw.newLine();

            bw.write("Actual:\n");
            bw.write(PluginUtil.join("\n", actual));
            bw.newLine();

            bw.write("Missing:\n");
            bw.write(PluginUtil.join("\n", missing));
            bw.newLine();

            bw.write("Unexpected:\n");
            bw.write(PluginUtil.join("\n", unexpected));
            bw.newLine();

            bw.newLine();
            bw.newLine();
            bw.flush();
            bw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeTestConfiguration(File file, TestConfiguration config) {
        try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(config.toString());
            bw.newLine();
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJavacArguments(File file, Iterable<? extends JavaFileObject> files, Iterable<String> options, Iterable<String> processors) {
        try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write("Files:\n");
            for(JavaFileObject f : files) {
                bw.write("    " + f.getName());
                bw.newLine();
            }
            bw.newLine();

            bw.write("Options:\n");
            for(String o : options) {
                bw.write("    " + o);
                bw.newLine();
            }
            bw.newLine();


            bw.write("Processors:\n");
            for(String p : processors) {
                bw.write("    " + p);
                bw.newLine();
            }
            bw.newLine();
            bw.newLine();

            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO: REDO COMMENT
     * Compares the result of the compiler against an array of Strings.
     *
     * In a checker, we treat a more specific error message as subsumed by a general one.
     * For example, "new.array.type.invalid" is subsumed by "type.invalid".
     * This is not the case in the test framework; the exact error key is expected.
     */
    public static void assertResultsAreValid(TypecheckResult testResult) {
        if (testResult.didTestFail()) {
            Assert.fail(testResult.summarize());
        }
    }

    public static void ensureDirectoryExists(File path) {
        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new RuntimeException("Could not make directory: " + path.getAbsolutePath());
            }
        }
    }

    public static boolean testBooleanProperty(String propName) {
        return testBooleanProperty(propName, false);
    }

    public static boolean testBooleanProperty(String propName, boolean defaultValue) {
        return System.getProperty(propName, String.valueOf(defaultValue)).equalsIgnoreCase("true");
    }
}
