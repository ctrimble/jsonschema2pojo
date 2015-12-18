package org.jsonschema2pojo.integration;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.JsonSchema2PojoRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.ReflectMatchers.*;

@RunWith(Parameterized.class)
public class JsonSchemaMetaIT {

    @SuppressWarnings("unchecked")
    @Parameters(name="{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            {
                "com.example.JsonSchema",
                allOf(
                    hasDeclaredField("definitions", hasModifiers(Modifier.PRIVATE)))
            }
        });
    }
    
    @ClassRule
    public static JsonSchema2PojoRule staticSchemaRule = new JsonSchema2PojoRule();
    private static ClassLoader classLoader;
    
    @BeforeClass
    public static void beforeClass() {
        classLoader = staticSchemaRule.generateAndCompile("/schema/metaSchema/json-schema.json", "com.example");
    }

    private String className;
    private Matcher<Class<?>> matcher;
    
    public JsonSchemaMetaIT(String className, Matcher<Class<?>> matcher ) {
        this.className = className;
        this.matcher = matcher;
    }

    @Test
    public void schemaMatches() throws ClassNotFoundException {
        assertThat(classLoader.loadClass(className), matcher);
    }

}
