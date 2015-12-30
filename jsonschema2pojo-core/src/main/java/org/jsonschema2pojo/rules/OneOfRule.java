package org.jsonschema2pojo.rules;

import java.util.ArrayList;
import java.util.List;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

public class OneOfRule implements Rule<JClassContainer, JType> {
    
    private final RuleFactory ruleFactory;

    protected OneOfRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JType apply(String nodeName, JsonNode node, JClassContainer generatableType, Schema currentSchema) {
        if( !currentSchema.getContent().isArray() ) {
            throw new GenerationException("oneOf does not contain an array");
        }
        
        List<JType> childTypes = new ArrayList<JType>();
        
        for( int i = 0; i < currentSchema.getContent().size(); i++ ) {
            JsonNode child = currentSchema.getContent().get(i);
            
            if( !child.isObject() ) {
                throw new GenerationException("child of oneOf is not an object");
            }
            
            childTypes.add(ruleFactory.getSchemaRule().apply(nodeName+"Index"+i, child, generatableType, currentSchema));           
        }
        
        // compute the widest type.
        return generatableType.owner().ref(Object.class);
    }
}
