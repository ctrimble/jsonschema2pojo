package org.jsonschema2pojo.integration.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ReflectMatchers {
    
    public static <M extends Member> Matcher<Class<?>> hasDeclaredField(final Matcher<M> memberMatcher ) {
        return new TypeSafeMatcher<Class<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has member ").appendDescriptionOf(memberMatcher);
            }
    
            @Override
            protected boolean matchesSafely(Class<?> item) {
                for( Field field : item.getDeclaredFields() ) {
                  if( memberMatcher.matches(field) ) return true;
                }
                return false;
            }
        };        
    }
    
    public static <M extends Member> Matcher<Class<?>> hasDeclaredField(final String fieldName, final Matcher<M> memberMatcher ) {
        return new TypeSafeMatcher<Class<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has member ").appendDescriptionOf(memberMatcher);
            }
    
            @Override
            protected boolean matchesSafely(Class<?> item) {
                for( Field field : item.getDeclaredFields() ) {
                  try {
                    if( memberMatcher.matches(item.getDeclaredField(fieldName)) ) return true;
                } catch (NoSuchFieldException | SecurityException e) {
                    return false;
                }
                }
                return false;
            }
        };        
    }
    public static <M extends Member> Matcher<M> nameMatches(final Matcher<String> nameMatcher) {
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

    public static <M extends Member> Matcher<M> hasModifiers(final int modifiers) {
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

}
