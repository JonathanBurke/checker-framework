package org.checkerframework.framework.test2;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * CompilationResult represents the output of the compiler after it is run.
 */
public class CompilationResult {
    private final boolean compiledWithoutError;
    private final String javacOutput;
    private final Iterable<? extends JavaFileObject> javaFileObjects;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    CompilationResult(boolean compiledWithoutError, String javacOutput,
                      Iterable<? extends JavaFileObject> javaFileObjects,
                      List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        this.compiledWithoutError = compiledWithoutError;
        this.javacOutput = javacOutput;
        this.javaFileObjects = javaFileObjects;
        this.diagnostics = Collections.unmodifiableList(diagnostics);
    }

    /**
     * @return Whether or not compilation succeeded without errors or exceptions
     */
    public boolean compiledWithoutError() {
        return compiledWithoutError;
    }

    /**
     * @return All of the output from the compiler
     */
    public String getJavacOutput() {
        return javacOutput;
    }

    /**
     * @return The list of Java files passed to the compiler
     */
    public Iterable<? extends JavaFileObject> getJavaFileObjects() {
        return javaFileObjects;
    }

    /**
     * @return The diagnostics reported by the compiler
     */
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return diagnostics;
    }

}
