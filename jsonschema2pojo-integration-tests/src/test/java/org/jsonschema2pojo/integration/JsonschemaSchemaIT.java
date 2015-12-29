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

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.jsonschema2pojo.integration.util.ReflectMatchers.*;

import java.lang.reflect.Modifier;

/**
 * Demonstrates Jsonschema2Pojo's self descriptiveness, by generating classes for JSON Schema itself.  The
 * schema being used is not the compele schema for JSON Schema, but does cover as much of the tool as
 * possible.  Ignored tests are provided, to show what needs to be improved.
 * 
 * @author Christian Trimble
 *
 */
public class JsonschemaSchemaIT {
  @ClassRule public static Jsonschema2PojoRule staticSchemaRule = new Jsonschema2PojoRule();

  public static ClassLoader loader;
  
  @BeforeClass
  public static void beforeClass() throws ClassNotFoundException {
      staticSchemaRule.generateAndCompile("/schema/jsonschema/jsonSchema.json", "com.example");
      loader = staticSchemaRule.getClassLoader();
  }

  @Test
  public void additionalPropertiesDefined() throws ClassNotFoundException {
      assertThat(jsonSchemaType(), hasDeclaredMethod(signature(Modifier.PUBLIC, additionalPropertiesType(), "getAdditionalProperties")));
      assertThat(jsonSchemaType(), hasDeclaredMethod(signature(Modifier.PUBLIC, Void.TYPE, "setAdditionalProperties", additionalPropertiesType())));
  }

  @Test
  public void propertiesDefined() throws ClassNotFoundException {
      assertThat(jsonSchemaType(), hasDeclaredMethod(signature(Modifier.PUBLIC, propertiesType(), "getProperties")));
      assertThat(jsonSchemaType(), hasDeclaredMethod(signature(Modifier.PUBLIC, Void.TYPE, "setProperties", propertiesType())));
  }
  
  @Test
  public void titleDefined() throws ClassNotFoundException {
      assertThat(jsonSchemaType(), hasDeclaredMethod(signature(Modifier.PUBLIC, String.class, "getTitle")));
      assertThat(jsonSchemaType(), hasDeclaredMethod(signature(Modifier.PUBLIC, Void.TYPE, "setTitle", String.class)));
  }
  
  @Test
  public void descriptionDefined() throws ClassNotFoundException {
      assertThat(jsonSchemaType(), hasDeclaredMethod(signature(Modifier.PUBLIC, String.class, "getDescription")));
      assertThat(jsonSchemaType(), hasDeclaredMethod(signature(Modifier.PUBLIC, Void.TYPE, "setDescription", String.class)));
  }

  public static Class<?> jsonSchemaType() throws ClassNotFoundException {
      return loader.loadClass("com.example.JsonSchema");
  }

  public static Class<?> propertiesType() throws ClassNotFoundException {
      return loader.loadClass("com.example.Properties");
  }

  public static Class<?> additionalPropertiesType() throws ClassNotFoundException {
      return loader.loadClass("com.example.AdditionalProperties");
  }
}
