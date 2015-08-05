package org.checkerframework.framework.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic utilities for dealing with IO.
 */
public class IoUtil {
    private static final String PlatformLineSeparator = System.getProperty("line.separator");

    /**
     * For each line in lines, write the line followed by a delimiter to writer
     */
    private static void writeLines(final Writer writer, String delimiter, String ... lines) throws IOException {
        for (String line : lines) {
            writer.write(line);
            writer.append(delimiter);
        }
        writer.flush();
    }

    /**
     * For each line in lines, write the line followed by a delimiter to writer
     */
    private static void writeLines(final Writer writer, String delimiter, Iterable<String> lines) throws IOException {
        for (String line : lines) {
            writer.write(line);
            writer.append(delimiter);
        }
        writer.flush();
    }

    /**
     * Writes each line to file followed by a newline (defined by the platform).
     * Note, this method wraps all exceptions in RuntimeException
     * @param file The file to write to, it will be created if it does not exist
     * @param append If true, lines will be appended to file if it already exists.  Otherwise the file will be
     *               overwritten.
     * @param lines The lines to be written
     */
    private static void writeLines(final File file, boolean append, String ... lines) {
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
            writeLines(writer, PlatformLineSeparator, lines);
            writer.close();

        } catch(IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Writes each line to file followed by a newline (defined by the platform).
     * Note, this method wraps all exceptions in RuntimeException
     * @param file The file to write to, it will be created if it does not exist
     * @param append If true, lines will be appended to file if it already exists.  Otherwise the file will be
     *               overwritten.
     * @param lines The lines to be written
     */
    private static void writeLines(final File file, boolean append, List<String> lines) {
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
            writeLines(writer, PlatformLineSeparator, lines);
            writer.close();

        } catch(IOException exc) {
            throw new RuntimeException(exc);
        }

    }

    /**
     * Writes each line to file, overwriting file if it already exists.  Note, this method
     * wraps any exceptions in a RuntimeException
     */
    public static void writeLines(final File file, String ... lines) {
        writeLines(file, false, lines);
    }

    /**
     * Writes each line to file, overwriting file if it already exists.  Note, this method
     * wraps any exceptions in a RuntimeException
     */
    public static void writeLines(final File file, List<String> lines) {
        writeLines(file, false, lines);
    }

    /**
     * Writes each line to file, appending to the file if it already exists.  Note, this method
     * wraps any exceptions in a RuntimeException
     */
    public static void appendLines(final File file, List<String> lines) {
        writeLines(file, true, lines);
    }

    /**
     * Writes each line to file, appending to the file if it already exists.  Note, this method
     * wraps any exceptions in a RuntimeException
     */
    public static void appendLines(final File file, String ... lines) {
        writeLines(file, true, lines);
    }

    /**
     * Reads all lines in a file and returns them as a list.  Lines are delimited by
     * linefeed ('\n') carriage returns ('\r') or a crlf ("\r\n")
     * Note, this method wraps all exceptions in a RuntimeException
     */
    public static List<String> getLines(File javaFile) {
        try {
            final List<String> lines = new ArrayList<>();
            final BufferedReader lineReader = new BufferedReader(new FileReader(javaFile));
            String line;
            while (true) {
                line = lineReader.readLine();
                if (line != null) {
                    lines.add(line);
                } else {
                    break;
                }

            }
            lineReader.close();
            return lines;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
