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
import static org.hamcrest.Matchers.equalTo;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;

import java.io.IOException;
import java.io.InputStream;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class InstanceTestCases {    
    public static ObjectMapper mapper = new ObjectMapper();

      public String schemaPath;
      public String typeName;
      String documentPath;
      Matcher<Object> documentMatcher;
      
      public InstanceTestCases( String schemaPath, String typeName, String documentPath, Matcher<Object> documentMatcher) {
          this.schemaPath = schemaPath;
          this.typeName = typeName;
          this.documentPath = documentPath;
          this.documentMatcher = documentMatcher;
      }

  @Test
  public void jsonWillRoundTrip() throws ClassNotFoundException, JsonParseException, JsonMappingException, IOException {
      Class<?> type = generateAndCompile(schemaPath, "com.example").loadClass(typeName);
      
      try( InputStream in = this.getClass().getResourceAsStream(documentPath) ) {
        JsonNode original = mapper.readValue(in, JsonNode.class);
        Object value = mapper.convertValue(original, type);
        JsonNode roundTrip = mapper.convertValue(value, JsonNode.class);
        
        assertThat(roundTrip, equalTo(original));
      }
  }

  @Test
  public void mappedDocumentMatches() throws ClassNotFoundException, IOException {
      Class<?> type = generateAndCompile(schemaPath, "com.example").loadClass(typeName);
      
      try( InputStream in = this.getClass().getResourceAsStream(documentPath) ) {
        JsonNode original = mapper.readValue(in, JsonNode.class);
        Object value = mapper.convertValue(original, type);
        assertThat(value, documentMatcher);
      }
  }
  }