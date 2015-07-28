package org.checkerframework.framework.test2.diagnostics;

import java.util.LinkedHashMap;
import java.util.Map;

public enum DiagnosticCategory {
    Warning("warning"),
    Error("error"),
    FixableError("fixable-error"),
    Other("other");

    public final String parseString;

    DiagnosticCategory(String parseString) {
        this.parseString = parseString;
    }

    private static final Map<String, DiagnosticCategory> stringToCategory = new LinkedHashMap<>();
    static {
        for (DiagnosticCategory cat : values()) {
            stringToCategory.put(cat.parseString, cat);
        }
    }

    public static DiagnosticCategory fromParseString(String parseStr) {
        return stringToCategory.get(parseStr);
    }
}
