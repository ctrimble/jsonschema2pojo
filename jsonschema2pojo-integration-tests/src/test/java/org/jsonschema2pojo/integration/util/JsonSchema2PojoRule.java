/**
 * Copyright ¬© 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.integration.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * A JUnit rule that executes JsonSchema2Pojo.
 *
 * @author Christian Trimble
 *
 */
public class JsonSchema2PojoRule implements TestRule {

    private File sourcesDir;
    private File classesDir;
    private ClassLoader classLoader;

    public File getSourcesDir() {
        return sourcesDir;
    }

    public File getClassesDir() {
        return classesDir;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    sourcesDir = methodNameDir(classNameDir(sourceBaseDir(), description.getClassName()),
                            description.getMethodName());
                    classesDir = methodNameDir(classNameDir(classBaseDir(), description.getClassName()),
                            description.getMethodName());

                    base.evaluate();
                } finally {
                    sourcesDir = null;
                    classesDir = null;
                    classLoader = null;
                }
            }
        };
    }

    public File generate(String schema, String targetPackage) {
        Map<String, Object> configValues = Collections.emptyMap();
        return generate(schema, targetPackage, configValues);
    }

    public File generate(URL schema, String targetPackage) {
        Map<String, Object> configValues = Collections.emptyMap();
        return generate(schema, targetPackage, configValues);
    }

    public File generate(String schema, String targetPackage, Map<String, Object> configValues) {
        URL schemaUrl = CodeGenerationHelper.class.getResource(schema);
        assertThat("Unable to read schema resource from the classpath: " + schema, schemaUrl, is(notNullValue()));

        return generate(schemaUrl, targetPackage, configValues);
    }

    public File generate(final URL schema, final String targetPackage, final Map<String, Object> configValues) {
        if (sourcesDir == null) {
            throw new RuntimeException("cannot generate outside of rule");
        }
        ensureCleanDirectory(sourcesDir);
        CodeGenerationHelper.generate(schema, targetPackage, configValues, sourcesDir);
        return sourcesDir;
    }

    public ClassLoader compile() {
        return compile(new ArrayList<File>(), new HashMap<String, Object>());
    }

    public ClassLoader compile(List<File> classpath, Map<String, Object> config) {
        if (classesDir == null) {
            throw new RuntimeException("cannot compile outside of rule");
        }
        ensureCleanDirectory(classesDir);
        if (classLoader != null)
            throw new RuntimeException("cannot recompile sources");
        classLoader = CodeGenerationHelper.compile(sourcesDir, classesDir, classpath, config);
        return classLoader;
    }

    public ClassLoader generateAndCompile(String schema, String targetPackage, Map<String, Object> configValues) {
        generate(schema, targetPackage, configValues);
        return compile(new ArrayList<File>(), configValues);
    }

    public ClassLoader generateAndCompile(String schema, String targetPackage) {
        generate(schema, targetPackage);
        return compile();
    }

    public ClassLoader generateAndCompile(URL schema, String targetPackage) {
        generate(schema, targetPackage);
        Map<String, Object> configValues = Collections.emptyMap();
        return compile(new ArrayList<File>(), configValues);
    }

    public ClassLoader generateAndCompile(URL schema, String targetPackage, Map<String, Object> configValues) {
        generate(schema, targetPackage, configValues);
        return compile(new ArrayList<File>(), configValues);
    }

    public File source(String relativeSourcePath) {
        return new File(sourcesDir, relativeSourcePath);
    }

    public static File sourceBaseDir() throws IOException {
        return new File("target" + File.separator + "jsonschema2pojo" + File.separator + "sources");
    }

    public static File classBaseDir() throws IOException {
        return new File("target" + File.separator + "jsonschema2pojo" + File.separator + "classes");
    }

    public static File classNameDir(File baseDir, String className) throws IOException {
        return new File(baseDir, classNameToPath(className));
    }

    public static final Pattern methodNamePattern = compilePattern("\\A([^\\[]+)(?:\\[(.*)\\])?\\Z");

    /**
     * Returns the compiled pattern, or null if the pattern could not compile.
     */
    public static Pattern compilePattern(String pattern) {
        try {
            return Pattern.compile(pattern);
        } catch (Exception e) {
            System.err.println("Could not compile pattern " + pattern);
            e.printStackTrace(System.err);
            return null;
        }
    }

    public static File methodNameDir(File baseDir, String methodName) throws IOException {
        if( methodName == null ) methodName = "class";
        Matcher matcher = methodNamePattern.matcher(methodName);

        if (matcher.matches()) {
            if (matcher.group(2) != null) {
                baseDir = new File(baseDir, safeDirName(matcher.group(2)));
            }
            return new File(baseDir, safeDirName(matcher.group(1)));
        } else {
            throw new IOException("cannot transform methodName (" + methodName + ") into path");
        }
    }

    public static File ensureCleanDirectory(File dir) {
        try {
          forceMkdir(dir);
          cleanDirectory(dir);
          return dir;
        } catch( IOException ioe ) {
            throw new RuntimeException("could not clean directory", ioe);
        }
    }

    public static String safeDirName(String label) {
        return label.replaceAll("[^a-zA-Z1-9]+", "_");
    }

    public static String classNameToPath(String className) {
        return className
                .replaceAll("\\A(?:.*\\.)?([^\\.]*)\\Z", "$1")
                .replaceAll("\\$", File.separator);
    }

}
