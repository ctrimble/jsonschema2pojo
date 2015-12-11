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

package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.*;

import java.util.Collection;

import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.experimental.runners.Enclosed;

import java.util.Arrays;

@RunWith(Enclosed.class)
public class AnyOfIT {

   @RunWith(Parameterized.class)
   public static class Generation extends GenerationTestCases {
        @SuppressWarnings("unchecked")
        @Parameters(name = "{0}")
        public static Collection<Object[]> parameters() {
            return Arrays.asList(new Object[][] {
                    { 
                        "/schema/union/anyOfInlineChildren.json",
                        "com.example.AnyOfInlineChildren",
                        allOf(
                        )
                    }
            });
        }

        public Generation(String schemaPath, String typeName, Matcher<Class<?>> typeMatcher) {
            super(schemaPath, typeName, typeMatcher);
        }
    }
   
   @RunWith(Parameterized.class)
   public static class Instance extends InstanceTestCases {
        @SuppressWarnings("unchecked")
        @Parameters(name = "{2}")
        public static Collection<Object[]> parameters() {
            return Arrays.asList(new Object[][] {
                    { 
                        "/schema/union/anyOfInlineChildren.json",
                        "com.example.AnyOfInlineChildren",
                        "/schema/union/anyOfInlineChildrenExample.json",
                        allOf(
                        )
                    }
            });
        }

        public Instance(String schemaPath, String typeName, String documentPath, Matcher<Object> documentMatcher) {
            super(schemaPath, typeName, documentPath, documentMatcher);
        }
    }
}
