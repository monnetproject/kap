/****************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package eu.monnetproject.gelato;


import eu.monnetproject.lemon.LinguisticOntology;
import eu.monnetproject.lemon.model.Condition;
import eu.monnetproject.lemon.model.Definition;
import eu.monnetproject.lemon.model.Edge;
import eu.monnetproject.lemon.model.FormVariant;
import eu.monnetproject.lemon.model.LemonElementOrPredicate;
import eu.monnetproject.lemon.model.LexicalVariant;
import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.PropertyValue;
import eu.monnetproject.lemon.model.Representation;
import eu.monnetproject.lemon.model.SenseContext;
import eu.monnetproject.lemon.model.SenseRelation;
import eu.monnetproject.lemon.model.SynArg;
import eu.monnetproject.lemon.model.Text;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class DummyLinguisticOntology implements LinguisticOntology{

    public Property getProperty(final String string) {
        return new Property() {

            public URI getURI() {
                return URI.create("file:"+string);
            }
            
        };
    }

    public Collection<Property> getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PropertyValue getPropertyValue(String string) {
        return new PropertyValue() {

            public Map<Property, Collection<PropertyValue>> getPropertys() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Collection<PropertyValue> getProperty(Property prprt) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean addProperty(Property prprt, PropertyValue pv) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean removeProperty(Property prprt, PropertyValue pv) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getID() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Collection<URI> getTypes() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void addType(URI uri) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void removeType(URI uri) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public URI getURI() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Map<URI, Collection<Object>> getAnnotations() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Collection<Object> getAnnotations(URI uri) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean addAnnotation(URI uri, Object o) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean removeAnnotation(URI uri, Object o) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public Collection<PropertyValue> getValues(Property prprt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SynArg getSynArg(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<SynArg> getSynArgs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<SynArg> getSynArgsForFrame(URI uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Condition getCondition(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Condition> getConditions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SenseContext getContext(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<SenseContext> getContexts() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Definition getDefinition(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Definition> getDefinitions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Edge getEdge(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Edge> getEdge() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FormVariant getFormVariant(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<FormVariant> getFormVariant() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LexicalVariant getLexicalVariant(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalVariant> getLexicalVariant() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Representation getRepresentation(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Representation> getRepresentation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SenseRelation getSenseRelation(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<SenseRelation> getSenseRelation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<URI> getFrameClasses() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI getFrameClass(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<Property, Collection<PropertyValue>> getPropertyMap(String... strings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<String> getDefinitions(LemonElementOrPredicate elem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Text> getExamples(URI frameClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
