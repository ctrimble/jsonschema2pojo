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

package org.jsonschema2pojo;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jsonschema2pojo.exception.GenerationException;
import org.jsonschema2pojo.rules.RuleFactory;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Annotates generated Java types using the Jackson 2.x mapping annotations.
 * 
 * @see <a
 *      href="https://github.com/FasterXML/jackson-annotations">https://github.com/FasterXML/jackson-annotations</a>
 */
public class Jackson2Annotator extends AbstractAnnotator {

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        JAnnotationArrayMember annotationValue = clazz.annotate(JsonPropertyOrder.class).paramArray("value");

        for (Iterator<String> properties = propertiesNode.fieldNames(); properties.hasNext();) {
            annotationValue.param(properties.next());
        }
    }

    @Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
        clazz.annotate(JsonInclude.class).param("value", JsonInclude.Include.NON_NULL);
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(JsonProperty.class).param("value", propertyName);
        
        if( propertyNode.has("oneOf") ) {
            JsonNode oneOfNode = propertyNode.get("oneOf");
            if( !oneOfNode.isArray() ) {
                throw new GenerationException("malformed oneOf node");
            }
            JType deserializer = defineDeserializer(field, clazz, propertyName, propertyNode );
            field.annotate(JsonDeserialize.class).param("using", deserializer);
        } else if (field.type().erasure().equals(field.type().owner().ref(Set.class))) {
            field.annotate(JsonDeserialize.class).param("as", LinkedHashSet.class);
        }

        if (propertyNode.has("javaJsonView")) {
            field.annotate(JsonView.class).param(
                "value", field.type().owner().ref(propertyNode.get("javaJsonView").asText()));
        }
    }

    @Override
    public void propertyGetter(JMethod getter, String propertyName) {
        getter.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void propertySetter(JMethod setter, String propertyName) {
        setter.annotate(JsonProperty.class).param("value", propertyName);
    }

    @Override
    public void anyGetter(JMethod getter) {
        getter.annotate(JsonAnyGetter.class);
    }

    @Override
    public void anySetter(JMethod setter) {
        setter.annotate(JsonAnySetter.class);
    }

    @Override
    public void enumCreatorMethod(JMethod creatorMethod) {
        creatorMethod.annotate(JsonCreator.class);
    }

    @Override
    public void enumValueMethod(JMethod valueMethod) {
        valueMethod.annotate(JsonValue.class);
    }

    @Override
    public void enumConstant(JEnumConstant constant, String value) {
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return true;
    }

    @Override
    public void additionalPropertiesField(JFieldVar field, JDefinedClass clazz, String propertyName) {
        field.annotate(JsonIgnore.class);
    }

    @Override
    public void ignoreMethod(JMethod method) {
        method.annotate(JsonIgnore.class);
    }

    JType defineDeserializer(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) throws JClassAlreadyExistsException {
        String deserializerName =
                Character.toUpperCase(field.name().charAt(0)) +
                field.name().substring(1) +
                "$JsonDeserializer";
        // add create 
        JDefinedClass deserializer = clazz._class(JMod.STATIC | JMod.PUBLIC, deserializerName);
        
        deserializer._extends(clazz.owner().ref(JsonDeserializer.class).narrow(field.type()));
        
        JMethod deserialize = deserializer.method(JMod.PUBLIC, field.type(), "deserialize");
        JVar parser = deserialize.param(JsonParser.class, "jp");
        JVar context = deserialize.param(DeserializationContext.class, "context");
        
        JBlock body = deserialize.body();
        
        JVar codec = body.decl(clazz.owner().ref(ObjectCodec.class), "codec", parser.invoke("getCodec"));
        JVar tree = body.decl(clazz.owner().ref(TreeNode.class), "tree", codec.invoke("readValueAsTree"));
        
        
        return deserializer;
    }
}
