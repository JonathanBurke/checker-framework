package org.checkerframework.framework.test2.diagnostics;

import org.checkerframework.framework.util.PluginUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpectedDiagnosticLine {
    //this regex represents how the diagnostics appear in Java source files
    public static final String DIAGNOSTIC_IN_JAVA_REGEX = "\\s*(error|fixable-error|warning|other):\\s*\\((.*)\\)\\s*";
    public static final Pattern DIAGNOSTIC_IN_JAVA_PATTERN = Pattern.compile(DIAGNOSTIC_IN_JAVA_REGEX);

    public static final String DIAGNOSTIC_WARNING_IN_JAVA_REGEX = "\\s*warning:\\s*(\\[.*\\]\\s*.*)\\s*";
    public static final Pattern DIAGNOSTIC_WARNING_IN_JAVA_PATTERN = Pattern.compile(DIAGNOSTIC_WARNING_IN_JAVA_REGEX);

    //this regex represents how the diagnostics appear in diagnostic files (.out) and from the compiler
    public static final String DIAGNOSTIC_REGEX = ":(\\d+):" + DIAGNOSTIC_IN_JAVA_REGEX;
    public static final Pattern DIAGNOSTIC_PATTERN = Pattern.compile(DIAGNOSTIC_REGEX);

    public static final String DIAGNOSTIC_WARNING_REGEX = ":(\\d+):" + DIAGNOSTIC_WARNING_IN_JAVA_REGEX;
    public static final Pattern DIAGNOSTIC_WARNING_PATTERN = Pattern.compile(DIAGNOSTIC_WARNING_REGEX);

    private static final List<ExpectedDiagnostic> EMPTY = Collections.unmodifiableList(new ArrayList<ExpectedDiagnostic>());

    public static String createSourceString(List<ExpectedDiagnostic> diagnostics) {
        List<String> sourceStrings = new ArrayList<>(diagnostics.size());
        for (ExpectedDiagnostic diagnostic : diagnostics) {
            sourceStrings.add(diagnostic.asSourceString());
        }

        return "//:: " + PluginUtil.join(" :: ", sourceStrings);
    }

    private final int lineNumber;
    private final String originalLine;
    private final List<ExpectedDiagnostic> diagnostics;

    public static ExpectedDiagnosticLine fromSourceLine(String originalLine, int lineNumber) {
        final String trimmedLine = originalLine.trim();
        int errorLine = lineNumber + 1;
        if (trimmedLine.startsWith("//::")) {
            String[] diagnosticStrs =
                    trimmedLine
                            .substring(4) // drop the //::
                            .split("::");

            List<ExpectedDiagnostic> diagnostics = new ArrayList<>(diagnosticStrs.length);
            for (String diagnostic : diagnosticStrs) {
                diagnostics.add(new ExpectedDiagnostic(errorLine, diagnostic));
            }

            return new ExpectedDiagnosticLine(errorLine, originalLine, Collections.unmodifiableList(diagnostics));

        } else {
            return new ExpectedDiagnosticLine(errorLine, originalLine, EMPTY);

        }
    }

    public static ExpectedDiagnosticLine fromDiagnosticLine(String diagnosticLine) {
        final String trimmedLine = diagnosticLine.trim();
        if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
            return new ExpectedDiagnosticLine(-1, diagnosticLine, EMPTY);
        }

        ExpectedDiagnostic diagnostic = new ExpectedDiagnostic(diagnosticLine);
        return new ExpectedDiagnosticLine(diagnostic.lineNumber, diagnosticLine, Arrays.asList(diagnostic));
    }

    public ExpectedDiagnosticLine(int lineNumber, String originalLine,
                                  List<ExpectedDiagnostic> diagnostics) {
        this.lineNumber = lineNumber;
        this.originalLine = originalLine;
        this.diagnostics = diagnostics;
    }

    public boolean hasDiagnostics() {
        return !diagnostics.isEmpty();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getOriginalLine() {
        return originalLine;
    }

    public List<ExpectedDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public static class ExpectedDiagnostic {
        public final int lineNumber;
        public final DiagnosticCategory category;
        public final String errorMessage;
        public final boolean noParentheses;

        public ExpectedDiagnostic(String diagnosticStr) {

            Matcher diagnosticMatcher = DIAGNOSTIC_PATTERN.matcher(diagnosticStr);
            if (diagnosticMatcher.matches()) {
                lineNumber = Integer.valueOf(diagnosticMatcher.group(1));
                category   = DiagnosticCategory.fromParseString(diagnosticMatcher.group(2));
                errorMessage = diagnosticMatcher.group(3);
                noParentheses = false;

            } else {
                Matcher warningMatcher = DIAGNOSTIC_WARNING_PATTERN.matcher(diagnosticStr);
                if (warningMatcher.matches()) {
                    lineNumber = Integer.valueOf(diagnosticMatcher.group(1));
                    category   = DiagnosticCategory.Warning;
                    errorMessage = diagnosticMatcher.group(2);
                    noParentheses = true;
                } else {
                    throw new RuntimeException("Malformed start of diagnosticLine: " + diagnosticStr);
                    //TODO: ADDRESS COMMENT?
                    // Either other javac output should be redirected
                    // elsewhere, so as not to confuse assertDiagnostics,
                    // or else assertDiagnostics ought to recognize other
                    // javac output.  And the file format should be defined
                    // somewhere -- what is expected to precede the first
                    // colon?  Should it always be in the first column?
                }
            }
        }

        //used to get the diagnostic as they appear in source
        public ExpectedDiagnostic(int lineNumber, String diagnosticStr) {
            this.lineNumber = lineNumber;

            Matcher diagnosticMatcher = DIAGNOSTIC_IN_JAVA_PATTERN.matcher(diagnosticStr);
            if (diagnosticMatcher.matches()) {
                this.category = DiagnosticCategory.fromParseString(diagnosticMatcher.group(1));
                this.errorMessage = diagnosticMatcher.group(2);
                noParentheses = false;
            } else {
                Matcher warningMatcher = DIAGNOSTIC_WARNING_IN_JAVA_PATTERN.matcher(diagnosticStr);
                if (warningMatcher.matches()) {
                    this.category = DiagnosticCategory.Warning;
                    this.errorMessage =  warningMatcher.group(1);
                    noParentheses = true;
                } else {
                    this.category = DiagnosticCategory.Other;
                    this.errorMessage = diagnosticStr;
                    noParentheses = true;
                }
            }
        }

        public ExpectedDiagnostic(int lineNumber, DiagnosticCategory category, String message, boolean noParentheses) {
            this.lineNumber = lineNumber;
            this.category = category;
            this.errorMessage = message;
            this.noParentheses = false;
        }

        public String asSourceString() {
            if (noParentheses) {
                return category.parseString + " "+ errorMessage;
            }
            return category.parseString + " (" + errorMessage + ")";
        }

        public String toString() {
            if (noParentheses) {
                return ":" + lineNumber + ": " + category.parseString + ": " + errorMessage;
            }
            return ":" + lineNumber + ": " + category.parseString + ": (" + errorMessage + ")";
        }
    }

    public static List<String> diagnosticsToString(List<ExpectedDiagnostic> diagnostics) {
        final List<String> strings = new ArrayList<String>(diagnostics.size());
        for (ExpectedDiagnostic diagnostic : diagnostics) {
            strings.add(diagnostic.toString());
        }
        return strings;
    }
}
