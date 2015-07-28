package org.checkerframework.framework.test2;

import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.processing.AbstractProcessor;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.checkerframework.framework.test2.TestConfigurationBuilder.buildDefaultConfiguration;

/**
 * To use this class you must do two things:
 * 1) Create exactly 1 constructor in the subclass with exactly 1 argument which is a java.io.File.  This File
 * will be the Java file that is compiled and whose output is verified.
 * 2) Create a public static method that is annotation with org.junit.runners.Parameterized.Parameters.  This method
 * should look like: {@code,
 *     @Parameters
 *     List<Object[]> getTestFiles();
 * }
 * The return type should be a List of 1 element arrays, where each member of that array is a Java file to be tested.
 * Usually the body of this method should look like: {@code,
 *     return findNestedJavaTestFiles("myCheckerDir1","all-systems","someDir");
 * }
 *
 * findNestedJavaTestFiles will prepend "path to currentWorkingDir/tests/" to the argument.
 *
 * The TestSuite will then instantiate the subclass once for each file returned by getTestFiles and execute
 * the run method.
 */
@RunWith(TestSuite.class)
public abstract class OneByOneCheckerTest {

    protected final File testFile;

    /** The fully-qualified class name of the checker to use for tests. */
    protected final String checkerName;

    /** The path, relative to currentDir/test to the directory containing test inputs. */
    protected final String checkerDir;

    /** Extra options to pass to javac when running the checker. */
    protected final List<String> checkerOptions;

    /**
     * Creates a new checker test.
     *
     * @param checker the class for the checker to use
     * @param checkerDir the path to the directory of test inputs
     * @param checkerOptions options to pass to the compiler when running tests
     */
    public OneByOneCheckerTest(File testFile,
                               Class<? extends AbstractProcessor> checker,
                               String checkerDir, String... checkerOptions) {
        this.testFile = testFile;
        this.checkerName = checker.getName();
        this.checkerDir = "tests" + File.separator + checkerDir;
        this.checkerOptions = Arrays.asList(checkerOptions);
    }

    @Test
    public void run() {
        String emitDebug = System.getProperty("emit.test.debug");
        boolean shouldEmitDebugInfo = emitDebug != null && emitDebug.equalsIgnoreCase("true");
        TestConfiguration config = buildDefaultConfiguration(checkerDir, testFile, checkerName, checkerOptions,
                                                             shouldEmitDebugInfo);
        TypecheckResult testResult = new TypecheckExecutor().runTest(config);
        TestUtilities.assertResultsAreValid(testResult);
    }
}
