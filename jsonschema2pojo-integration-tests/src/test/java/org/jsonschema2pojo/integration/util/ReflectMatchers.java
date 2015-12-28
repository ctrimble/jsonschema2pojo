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

import static org.hamcrest.Matchers.allOf;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import static org.hamcrest.Matchers.*;

public class ReflectMatchers {

    public static <M extends Member> Matcher<M> memberName(final Matcher<String> nameMatcher) {
        return new TypeSafeMatcher<M>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("name ").appendDescriptionOf(nameMatcher);
            }
    
            @Override
            protected boolean matchesSafely(M item) {
                return nameMatcher.matches(item.getName());
            }
        };
    }
    
    public static Matcher<Method> parameterTypes( final Matcher<Class<?>[]> expected ) {
        return new TypeSafeMatcher<Method>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(" parameter types ").appendDescriptionOf(expected);
            }

            @Override
            protected boolean matchesSafely(Method item) {
                return expected.matches(item.getParameterTypes());
            }
        };
    }
    
    public static Matcher<Method> returnType( final Matcher<? extends Class<?>> matcher ) {
        return new TypeSafeMatcher<Method>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(" return type ").appendDescriptionOf(matcher);
            }

            @Override
            protected boolean matchesSafely(Method item) {
                return matcher.matches(item.getReturnType());
            }
        };
    }

    public static <M extends Member> Matcher<M> modifiers(final int modifiers) {
        return new TypeSafeMatcher<M>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has modifier ").appendValue(Modifier.toString(modifiers));
            }
    
            @Override
            protected boolean matchesSafely(M item) {
                int masked = item.getModifiers() & modifiers;
                return masked == modifiers;
            }
        };
    }

    public static Matcher<Method> signature( final int modifiers, final Class<?> returnType, final String name, final Class<?>... parameters ) {
          return allOf(
                  modifiers(modifiers),
                  returnType(equalTo(returnType)),
                  memberName(equalTo(name)),
                  parameterTypes(arrayContainingIncludingEmpty(parameters)));
      }
    
    public static <T> Matcher<T[]> arrayContainingIncludingEmpty( @SuppressWarnings("unchecked") T... expected ) {
        return expected.length==0?Matchers.<T>emptyArray():arrayContaining(expected);
    }

    public static Matcher<Class<?>> hasDeclaredMethod( final Matcher<Method> expected ) {
          return new TypeSafeMatcher<Class<?>>() {
    
            @Override
            public void describeTo(Description description) {
                description.appendText(" has declared method ").appendDescriptionOf(expected);
            }
    
            @Override
            protected boolean matchesSafely(Class<?> item) {
                for( Method method : item.getDeclaredMethods() ) {
                    if( expected.matches(method) ) return true;
                }
                return false;
            }
    
          };
      }

}
