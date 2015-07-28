package org.checkerframework.framework.test2;

import org.checkerframework.framework.test2.diagnostics.ExpectedDiagnosticLine;
import org.checkerframework.framework.test2.diagnostics.ExpectedDiagnosticLine.ExpectedDiagnostic;
import org.checkerframework.framework.util.PluginUtil;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TypecheckResult {
    private final TestConfiguration configuration;
    private final CompilationResult compilationResult;
    private final List<String> expectedDiagnostics;

    private final boolean testFailed;

    private final List<String> missingDiagnostics;
    private final List<String> unexpectedDiagnostics;

    protected TypecheckResult(TestConfiguration configuration, CompilationResult compilationResult,
                              List<String> expectedDiagnostics, boolean testFailed, List<String> missingDiagnostics,
                              List<String> unexpectedDiagnostics) {
        this.configuration = configuration;
        this.compilationResult = compilationResult;
        this.expectedDiagnostics = expectedDiagnostics;
        this.testFailed = testFailed;
        this.missingDiagnostics = missingDiagnostics;
        this.unexpectedDiagnostics = unexpectedDiagnostics;
    }

    public TestConfiguration getConfiguration() {
        return configuration;
    }

    public CompilationResult getCompilationResult() {
        return compilationResult;
    }

    public List<Diagnostic<? extends JavaFileObject>> getActualDiagnostics() {
        return compilationResult.getDiagnostics();
    }

    public List<String> getExpectedDiagnostics() {
        return expectedDiagnostics;
    }

    public boolean didTestFail() {
        return testFailed;
    }

    public List<String> getMissingDiagnostics() {
        return missingDiagnostics;
    }

    public List<String> getUnexpectedDiagnostics() {
        return unexpectedDiagnostics;
    }

    public List<String> getErrorHeaders() {
        List<String> errorHeaders = new ArrayList<>();

        //none of these should be true if the test didn't fail
        if (testFailed) {
            if (compilationResult.compiledWithoutError() && !expectedDiagnostics.isEmpty()) {
                errorHeaders.add("The test run was expected to issue errors/warnings, but it did not.");

            } else if (!compilationResult.compiledWithoutError() && expectedDiagnostics.isEmpty()) {
                errorHeaders.add("The test run was not expected to issue errors/warnings, but it did.");
            }

            List<Diagnostic<? extends JavaFileObject>> actualDiagnostics = getActualDiagnostics();
            if (!unexpectedDiagnostics.isEmpty() || !missingDiagnostics.isEmpty()) {
                errorHeaders.add(
                    actualDiagnostics.size() + " out of " + expectedDiagnostics.size() + " expected diagnostics "
                  + (actualDiagnostics.size() == 1 ? "was" : "were") + " found."
                );
            }


        }

        return errorHeaders;
    }

    public String summarize() {
        if (testFailed) {
            StringBuilder summaryBuilder = new StringBuilder();
            summaryBuilder.append(PluginUtil.join("\n", getErrorHeaders()));
            summaryBuilder.append("\n");

            if (!missingDiagnostics.isEmpty()) {
                summaryBuilder.append(
                     missingDiagnostics.size() == 1
                           ? "1 expected diagnostic was not found:\n"
                           : missingDiagnostics.size() + " expected diagnostics were not found:\n"
                );

                for (String missing : missingDiagnostics) {
                    summaryBuilder.append(missing);
                    summaryBuilder.append("\n");
                }
            }

            if (!unexpectedDiagnostics.isEmpty()) {
                summaryBuilder.append(
                    unexpectedDiagnostics.size() == 1
                            ? "1 unexpected diagnostic was found:\n"
                            : missingDiagnostics.size() + " unexpected diagnostics were found:\n"
                );

                for (String unexpected : unexpectedDiagnostics) {
                    summaryBuilder.append(unexpected);
                    summaryBuilder.append("\n");
                }
            }

            summaryBuilder.append("While type-checking " + TestUtilities.summarizeSourceFiles(configuration.getTestSourceFiles()));
            return summaryBuilder.toString();
        }

        return "";
    }

    public static TypecheckResult fromCompilationResults(TestConfiguration configuration, CompilationResult result,
                                                         List<String> expectedDiagnostics) {

        boolean usingAnomsgtxt = configuration.getOptions().containsKey("-Anomsgtext");
        final Set<String> actualDiagnostics = TestUtilities.diagnosticsToStrings(result.getDiagnostics(), usingAnomsgtxt);


        final Set<String> unexpectedDiagnostics = new LinkedHashSet<>();
        unexpectedDiagnostics.addAll(actualDiagnostics);
        unexpectedDiagnostics.removeAll(expectedDiagnostics);

        final List<String> missingDiagnostics = new LinkedList<String>(expectedDiagnostics);
        missingDiagnostics.removeAll(actualDiagnostics);

        boolean testFailed = !unexpectedDiagnostics.isEmpty() || !missingDiagnostics.isEmpty();

        return new TypecheckResult(configuration, result, expectedDiagnostics, testFailed,
                missingDiagnostics, new ArrayList<>(unexpectedDiagnostics));
    }


    public static TypecheckResult fromCompilationResultsExpectedDiagnostics(
            TestConfiguration configuration, CompilationResult result, List<ExpectedDiagnostic> expectedDiagnostics) {

        boolean usingAnomsgtxt = configuration.getOptions().containsKey("-Anomsgtext");
        final Set<String> actualDiagnostics = TestUtilities.diagnosticsToStrings(result.getDiagnostics(), usingAnomsgtxt);
        final List<String> expectedDiagnosticStrings = ExpectedDiagnosticLine.diagnosticsToString(expectedDiagnostics);


        final Set<String> unexpectedDiagnostics = new LinkedHashSet<>();
        unexpectedDiagnostics.addAll(actualDiagnostics);
        unexpectedDiagnostics.removeAll(expectedDiagnosticStrings);

        final List<String> missingDiagnostics = new LinkedList<String>(expectedDiagnosticStrings);
        missingDiagnostics.removeAll(actualDiagnostics);

        boolean testFailed = !unexpectedDiagnostics.isEmpty() || !missingDiagnostics.isEmpty();

        return new TypecheckResult(configuration, result, expectedDiagnosticStrings, testFailed,
                missingDiagnostics, new ArrayList<>(unexpectedDiagnostics));
    }
}
