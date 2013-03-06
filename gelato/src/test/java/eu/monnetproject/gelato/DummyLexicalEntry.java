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


import eu.monnetproject.lemon.model.Component;
import eu.monnetproject.lemon.model.Frame;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.LexicalTopic;
import eu.monnetproject.lemon.model.LexicalVariant;
import eu.monnetproject.lemon.model.MorphPattern;
import eu.monnetproject.lemon.model.Node;
import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.PropertyValue;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class DummyLexicalEntry implements LexicalEntry {

    public LexicalForm getCanonicalForm() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCanonicalForm(LexicalForm lf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalForm> getOtherForms() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addOtherForm(LexicalForm lf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeOtherForm(LexicalForm lf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalForm> getAbstractForms() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addAbstractForm(LexicalForm lf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeAbstractForm(LexicalForm lf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalTopic> getTopics() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addTopic(LexicalTopic lt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeTopic(LexicalTopic lt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<LexicalVariant, Collection<LexicalEntry>> getLexicalVariants() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalEntry> getLexicalVariant(LexicalVariant lv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addLexicalVariant(LexicalVariant lv, LexicalEntry le) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeLexicalVariant(LexicalVariant lv, LexicalEntry le) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Frame> getSynBehaviors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addSynBehavior(Frame frame) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeSynBehavior(Frame frame) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<List<Component>> getDecompositions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addDecomposition(List<Component> list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeDecomposition(List<Component> list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalSense> getSenses() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addSense(LexicalSense ls) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeSense(LexicalSense ls) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Node> getPhraseRoots() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addPhraseRoot(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removePhraseRoot(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalForm> getForms() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addForm(LexicalForm lf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeForm(LexicalForm lf) {
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

    public Collection<MorphPattern> getPatterns() {
        return Collections.EMPTY_LIST;
    }

    public boolean addPattern(MorphPattern mp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removePattern(MorphPattern mp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Component getHead() {
        return null;
    }

    public void setHead(Component cmpnt) {
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
