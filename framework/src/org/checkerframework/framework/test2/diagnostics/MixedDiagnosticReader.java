package org.checkerframework.framework.test2.diagnostics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class MixedDiagnosticReader {

    public static List<ExpectedDiagnosticLine> readExpectedDiagnostics(File toRead, boolean omitEmptyDiagnostics) {
        List<ExpectedDiagnosticLine> lines = new ArrayList<>();
        MixedDiagnosticReader reader = new MixedDiagnosticReader(toRead);
        while(reader.hasNext()) {
            ExpectedDiagnosticLine line = reader.next();
            if (!omitEmptyDiagnostics || line.hasDiagnostics()) {
                lines.add(line);
            }
        }
        reader.close();

        return lines;
    }



    public static List<ExpectedDiagnosticLine> readExpectedDiagnostics(List<File> toRead, boolean omitEmptyDiagnostics) {
        List<ExpectedDiagnosticLine> lines = new ArrayList<>();
        for (File file : toRead) {
            lines.addAll(readExpectedDiagnostics(file, omitEmptyDiagnostics));
        }
        return lines;
    }

    public final File toRead;

    private boolean initialized = false;
    private boolean closed = false;

    private LineNumberReader reader = null;

    public String nextLine = null;
    public int nextLineNumber = -1;

    public MixedDiagnosticReader(File toRead) {
        this.toRead = toRead;
    }

    private void init() throws IOException {
        if (!initialized && !closed) {
            initialized = true;
            reader = new LineNumberReader(new FileReader(toRead));
            advance();
        }
    }

    public boolean hasNext() {
        if (closed) {
            return false;
        }

        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return nextLine != null;
    }

    public ExpectedDiagnosticLine next() {
        try {
            init();

            if (nextLine == null) {
                throw new NoSuchElementException();
            } else if (closed) {
                throw new RuntimeException("Reader has been closed: " + toRead.getAbsolutePath());
            }

            String current = nextLine;
            int currentLineNumber = nextLineNumber;

            advance();

            if (nextLine == null) {
                close();
            }

            return ExpectedDiagnosticLine.fromSourceLine(current, currentLineNumber);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void advance() throws IOException {
        nextLine = reader.readLine();
        nextLineNumber = reader.getLineNumber();
    }

    public void close() {
        try {
            if (initialized) {
                reader.close();
            }

            closed = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
