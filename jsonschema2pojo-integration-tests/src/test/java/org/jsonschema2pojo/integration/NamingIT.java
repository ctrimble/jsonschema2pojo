package org.jsonschema2pojo.integration;

import static java.util.Arrays.asList;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NamingIT {
    
    @Parameters
    public static Collection<Object[]> parameters() {
        return asList(new Object[][] {
                { "com.example.TopLevel" }, 
                { "com.example.dir1.SingleRef" }, 
                { "com.example.dir2.MultipleRef" }, 
                { "com.example.common.CommonType" }
        });
    }
    
    static ClassLoader resultsClassLoader;
    
    @BeforeClass
    public static void generateClasses() {
        try {
          resultsClassLoader = generateAndCompile(new File("src/test/resources/schema/naming"), "com.example");
        }
        catch( Exception e ) {
            throw new RuntimeException("could not generate and compile naming test", e);
        }
    }

    private String className;
    
    public NamingIT( String className ) {
        this.className = className;
    }

    @Test
    public void shouldGenerateClass() throws ClassNotFoundException {
        resultsClassLoader.loadClass(className);
    }

}
