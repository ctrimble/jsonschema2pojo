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

package org.jsonschema2pojo.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;

import org.hamcrest.Matcher;
import org.junit.Test;

public abstract class GenerationTestCases {
      
    public String schemaPath;
    public String typeName;
    public Matcher<Class<?>> typeMatcher;

    public GenerationTestCases( String schemaPath, String typeName, Matcher<Class<?>> typeMatcher ) {
          this.schemaPath = schemaPath;
          this.typeName = typeName;
          this.typeMatcher = typeMatcher;
      }
    @Test
    public void generatedCodeCompiles() throws ClassNotFoundException {
      generateAndCompile(schemaPath, "com.example");
    }
  
    @Test
    public void generatedClassMatches() throws ClassNotFoundException {
      Class<?> type = generateAndCompile(schemaPath, "com.example").loadClass(typeName);
      assertThat(type, typeMatcher);
    }
  }