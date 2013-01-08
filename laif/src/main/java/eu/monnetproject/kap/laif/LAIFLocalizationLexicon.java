/**
 * ********************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************
 */
package eu.monnetproject.kap.laif;

import eu.monnetproject.kap.laif.rules.LAIFLiteral;
import eu.monnetproject.kap.laif.rules.LAIFUserCall;
import eu.monnetproject.kap.laif.rules.LAIFValue;
import eu.monnetproject.kap.laif.rules.LAIFVariable;
import eu.monnetproject.l10n.LocalizationLexicon;
import eu.monnetproject.lang.Language;
import eu.monnetproject.lemon.LemonModels;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.PropertyValue;
import eu.monnetproject.lemon.model.SenseDefinition;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static eu.monnetproject.util.CollectionFunctions.*;
import java.util.Collection;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class LAIFLocalizationLexicon implements LocalizationLexicon {
    protected final String appKey;
    protected final Map<Language,LAIFRuleSet> rules = new HashMap<Language, LAIFRuleSet>();
    protected final Map<Language,Lexicon> lexica = new HashMap<Language,Lexicon>();

    public LAIFLocalizationLexicon(String appKey) {
        this.appKey = appKey;
    }
    
    protected LAIFRuleSet rules(Language lang) {
        return rules.get(lang);
    }

    public void addRule(Language lang, LAIFRule rule) {
        if(!rules.containsKey(lang)) {
            rules.put(lang, new LAIFRuleSet(null, new ArrayList<LAIFRule>(),lang,appKey));
        }
        rules.get(lang).add(rule);
    }
    
    public void addLexicon(Lexicon lexicon) {
        final Language lang = Language.get(lexicon.getLanguage());
        lexica.put(lang, lexicon);
    }

    protected Lexicon lexica(Language lang) {
        return lexica.get(lang);
    }

    public LAIFLexicon sparqlLexicon(Language lang) {
        final Lexicon lexicon = lexica(lang);
        if (lexicon != null) {
            return new LemonLexicon(lexicon);
        } else {
            return new TestLexicon();
        }
    }

    @Override
    public List<String> form(URI sense, Language language) {
        return Arrays.asList(sparqlLexicon(language).form(sense));
    }
    
    @Override
    public String get(String tagString, Language language) {
        try {
            final LAIFRuleSet theRules = rules(language);
            if(theRules != null) {
                final LAIFRule rule = theRules.keyMap().get(tagString);
                if(rule != null) {
                    return rule.apply(sparqlLexicon(language), new LinkedList<LAIFValue>(), theRules.ruleMap());
                } else {
                    addNewRule(tagString,language);
                    return tagString;
                }
            } else {
                return "#ERR#";
            }
        } catch(LAIFApplyException x) {
            x.printStackTrace();
            return "ERROR";
        }
    }
    
    private static final Random random = new Random();
    
    private void addNewRule(String tagString, Language language) {
        final String s = tagString.replaceAll("[^\\w]", "");
        final String r = s.substring(0, Math.min(s.length(), 30));
        final String res = r.length() > 0 ? ("local:"+ r) : ("local:n" + Math.abs(random.nextLong())) ;
        final LAIFRule rule = new LAIFRule(URI.create(res), tagString, new ArrayList<LAIFVariable>(0), Arrays.asList((LAIFValue)new LAIFLiteral(tagString)));
        addRule(language, rule);
    }

    @Override
    public String get(String key, List<Object> arguments, Language language) {
        final LAIFRuleSet theRules = rules(language);
        if(theRules != null) {
            final LAIFRule rule = theRules.keyMap().get(key);
            if(rule != null) {
                return rule.apply(sparqlLexicon(language), map(arguments, new Converter<Object, LAIFValue>() {

                    @Override
                    public LAIFValue f(Object e) {
                        if(e instanceof String) {
                            return new LAIFLiteral(e.toString());
                        } else if(e instanceof URI) {
                            return new LAIFUserCall((URI)e, new ArrayList<LAIFValue>(0));
                        } else {
                            return new LAIFLiteral(e.toString());
                        }
                    }
                }), theRules.ruleMap());
            } else {
                return key;
            }
        } else {
            return "#ERROR#";
        }
        
    }

    @Override
    public List<String> description(URI sense, Language language) {
        final String description = sparqlLexicon(language).description(sense);
        return description == null ? new ArrayList<String>(0) : Arrays.asList(description);
    }
    
    
    @Override
    public String getAppKey() {
        if(appKey != null) {
            return appKey;
        } else {
            throw new RuntimeException();
        }
    }
    
    public LAIFRuleSet getRules(Language language) {
        return rules(language);
    }
    

    private class TestLexicon implements LAIFLexicon {

        @Override
        public String form(URI sense) {
            return sense.getFragment();
        }

        @Override
        public String form(URI sense, Map<URI, URI> props) {
            return sense.getFragment();
        }

        @Override
        public String description(URI sense) {
            return "desc:" + sense.getFragment();
        }

        @Override
        public Map<URI, URI> entryProps(URI sense) {
            return new HashMap<URI, URI>();
        }
    }

    private class LemonLexicon implements LAIFLexicon {

        private final Lexicon lexicon;

        public LemonLexicon(Lexicon lexicon) {
            this.lexicon = lexicon;
        }
        
        private List<LexicalEntry> entries(URI sense) {
            return LemonModels.getEntryByReference(lexicon, sense);
        }

        @Override
        public String form(URI sense) {
            final List<LexicalEntry> entries = entries(sense);
            for(LexicalEntry entry : entries) {
                if(entry.getCanonicalForm() != null && entry.getCanonicalForm().getWrittenRep() != null) {
                    return entry.getCanonicalForm().getWrittenRep().value;
                }
                for(LexicalForm form : entry.getOtherForms()) {
                    if(form.getWrittenRep() != null) {
                        return form.getWrittenRep().value;
                    }
                }
                for(LexicalForm form : entry.getAbstractForms()) {
                    if(form.getWrittenRep() != null) {
                        return form.getWrittenRep().value;
                    }
                }
            }
            return null;
        }

        public boolean checkProps(LexicalForm form, Map<URI,URI> props) {
            if(props == null)
                return true;
            int match = 0;
            PROP: for (Map.Entry<Property, Collection<PropertyValue>> prop : form.getPropertys().entrySet()) {
                if(props.containsKey(prop.getKey().getURI())) {
                    for (PropertyValue propVal : prop.getValue()) {
                        if(propVal.getURI().equals(props.get(prop.getKey().getURI()))) {
                            match++;
                            continue PROP;
                        }
                    }
  
                }
            }
            return match == props.size();
        }
        
        @Override
        public String form(URI sense, Map<URI, URI> props) {
            final List<LexicalEntry> entries = entries(sense);
            for(LexicalEntry entry : entries) {
                if(entry.getCanonicalForm() != null && entry.getCanonicalForm().getWrittenRep() != null && checkProps(entry.getCanonicalForm(), props)) {
                    return entry.getCanonicalForm().getWrittenRep().value;
                }
                for(LexicalForm form : entry.getOtherForms()) {
                    if(form.getWrittenRep() != null && checkProps(form, props)) {
                        return form.getWrittenRep().value;
                    }
                }
                for(LexicalForm form : entry.getAbstractForms()) {
                    if(form.getWrittenRep() != null && checkProps(form, props)) {
                        return form.getWrittenRep().value;
                    }
                }
            }
            return null;
        }

        @Override
        public String description(URI sense) {
            final List<LexicalEntry> entries = entries(sense);
            for(LexicalEntry entry : entries) {
                for(LexicalSense lexSense : entry.getSenses()) {
                    if(lexSense.getReference().equals(sense)) {
                        for (Collection<SenseDefinition> descs : lexSense.getDefinitions().values()) {
                            for(SenseDefinition desc : descs) {
                                if(desc.getValue().language.equals(lexicon.getLanguage())) {
                                    return desc.getValue().value;
                                }
                            }
                            
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public Map<URI, URI> entryProps(URI sense) {
            final List<LexicalEntry> entries = entries(sense);
            final Map<URI,URI> entryProps = new HashMap<URI, URI>();
            for(LexicalEntry entry : entries) {
                for (Map.Entry<Property, Collection<PropertyValue>> props : entry.getPropertys().entrySet()) {
                    for(PropertyValue pv : props.getValue()) {
                        entryProps.put(props.getKey().getURI(),pv.getURI());
                    }
                }
            }
            return entryProps;
        }
        
        
    }
}
