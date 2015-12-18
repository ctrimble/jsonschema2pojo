/**
 * Copyright © 2010-2014 Nokia
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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;
import static org.fest.util.Lists.newArrayList;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jsonschema2pojo.integration.util.JsonSchema2PojoRule;
import org.jsonschema2pojo.integration.util.ReflectMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;

/**
 * Checks general properties of includeAccessors and different configurations.
 * 
 * @author Christian Trimble
 *
 */
@SuppressWarnings({ "rawtypes" })
@RunWith(Parameterized.class)
public class IncludeAccessorsPropertiesIT {
    public static final String PACKAGE = "com.example";
    public static final String PRIMITIVE_JSON = "/schema/properties/primitiveProperties.json";
    public static final String PRIMITIVE_TYPE = "com.example.PrimitiveProperties";

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            { PRIMITIVE_JSON, PRIMITIVE_TYPE, config() },
            { PRIMITIVE_JSON, PRIMITIVE_TYPE, config("useJodaDates", true) },
            { PRIMITIVE_JSON, PRIMITIVE_TYPE, config("includeAdditionalProperties", false) }
        });
    }

    public @Rule JsonSchema2PojoRule schemaRule = new JsonSchema2PojoRule();

    private String path;
    private String typeName;
    private Map<String, Object> includeAccessorsFalse;
    private Map<String, Object> includeAccessorsTrue;

    public IncludeAccessorsPropertiesIT(String path, String typeName, Map<String, Object> config) {
        this.path = path;
        this.typeName = typeName;
        this.includeAccessorsFalse = configWithIncludeAccessors(config, false);
        this.includeAccessorsTrue = configWithIncludeAccessors(config, true);
    }

    @Test
    public void noGettersOrSettersWhenFalse() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(path, PACKAGE, includeAccessorsFalse);
        Class generatedType = resultsClassLoader.loadClass(typeName);

        assertThat("getters and setters should not exist", generatedType.getDeclaredMethods(), everyItemInArray(anyOf(methodWhitelist(), not(fieldGetterOrSetter()))));
    }

    @Test
    public void hasGettersOrSettersWhenTrue() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(path, PACKAGE, includeAccessorsTrue);
        Class generatedType = resultsClassLoader.loadClass(typeName);

        assertThat("a getter or setter should be found.", generatedType.getDeclaredMethods(), hasItemInArray(allOf(not(methodWhitelist()), fieldGetterOrSetter())));
    }

    @Test
    public void onlyHasPublicInstanceFieldsWhenFalse() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(path, PACKAGE, includeAccessorsFalse);
        Class generatedType = resultsClassLoader.loadClass(typeName);

        assertThat("only public instance fields exist", generatedType.getDeclaredFields(), everyItemInArray(anyOf(ReflectMatchers.hasModifiers(Modifier.STATIC), fieldWhitelist(), ReflectMatchers.hasModifiers(Modifier.PUBLIC))));
    }

    @Test
    public void noPublicInstanceFieldsWhenTrue() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(path, PACKAGE, includeAccessorsTrue);
        Class generatedType = resultsClassLoader.loadClass(typeName);

        assertThat("only public instance fields exist", generatedType.getDeclaredFields(), everyItemInArray(anyOf(not(ReflectMatchers.hasModifiers(Modifier.PUBLIC)), fieldWhitelist())));
    }

    private static Map<String, Object> configWithIncludeAccessors(Map<String, Object> template, boolean includeAccessors) {
        Map<String, Object> config = new HashMap<String, Object>(template);
        config.put("includeAccessors", includeAccessors);
        return config;
    }

    private static <T> Matcher<T[]> everyItemInArray(final Matcher<T> itemMatcher) {
        return new TypeSafeDiagnosingMatcher<T[]>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("every item in array is ").appendDescriptionOf(itemMatcher);
            }

            @Override
            protected boolean matchesSafely(T[] items, Description mismatchDescription) {
                for (T item : items) {
                    if (!itemMatcher.matches(item)) {
                        mismatchDescription.appendText("an item ");
                        itemMatcher.describeMismatch(item, mismatchDescription);
                        return false;
                    }
                }
                return true;
            }

        };
    }

    private static <M extends Member> Matcher<M> methodWhitelist() {
        return ReflectMatchers.nameMatches(isIn(newArrayList("setAdditionalProperty", "getAdditionalProperties")));
    }

    private static <M extends Member> Matcher<M> fieldWhitelist() {
        return ReflectMatchers.nameMatches(isIn(newArrayList("additionalProperties")));
    }

    private static <M extends Member> Matcher<M> fieldGetterOrSetter() {
        return ReflectMatchers.nameMatches(anyOf(startsWith("get"), startsWith("set")));
    }
}
