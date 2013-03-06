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


import eu.monnetproject.lemon.model.FormVariant;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.PropertyValue;
import eu.monnetproject.lemon.model.Representation;
import eu.monnetproject.lemon.model.Text;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author John McCrae
 */ 
public class DummyForm implements LexicalForm {

    public Text getWrittenRep() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWrittenRep(Text text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<Representation, Collection<Text>> getRepresentations() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Text> getRepresentation(Representation r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addRepresentation(Representation r, Text text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeRepresentation(Representation r, Text text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<FormVariant, Collection<LexicalForm>> getFormVariants() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalForm> getFormVariant(FormVariant fv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addFormVariant(FormVariant fv, LexicalForm lf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeFormVariant(FormVariant fv, LexicalForm lf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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
    
}
