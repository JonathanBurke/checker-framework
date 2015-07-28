package org.checkerframework.framework.test2;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by jburke on 6/24/15.
 */
public interface TestConfiguration {
    List<File> getTestSourceFiles();
    List<String> getProcessors();
    Map<String, String> getOptions();
    List<String> getFlatOptions();
    boolean shouldEmitDebugInfo();
}
