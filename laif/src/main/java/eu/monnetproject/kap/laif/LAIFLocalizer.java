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

import eu.monnetproject.l10n.Localizer;
import eu.monnetproject.lang.Language;
import eu.monnetproject.lemon.LemonModel;
import eu.monnetproject.lemon.LemonSerializer;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import eu.monnetproject.util.ResourceFinder;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static eu.monnetproject.util.CollectionFunctions.*;
import java.util.HashSet;

/**
 *
 * @author John McCrae
 */
public class LAIFLocalizer implements LAIFController, Localizer {

    private final Map<String, LAIFLocalizationLexicon> lexica = new HashMap<String, LAIFLocalizationLexicon>();

    public LAIFLocalizer() {
    }

    private static class LAIFLocalizationLexiconImpl extends LAIFLocalizationLexicon {

        public LAIFLocalizationLexiconImpl(String appKey) {
            super(appKey);
        }

        @Override
        protected LAIFRuleSet rules(Language lang) {
            final LAIFRuleSet r = super.rules(lang);
            if (r == null) {
                Reader artifact = null;
                try {
                    artifact = ResourceFinder.getResourceAsReader(appKey + "." + lang + ".laif");
                    if (artifact == null) {
                        return null;
                    }
                    final LAIFRuleSet ruleSet = LAIFReader.read(artifact);
                    rules.put(lang, ruleSet);
                    return ruleSet;
                } catch (IOException x) {
                    return null;
                } finally {
                    if (artifact != null) {
                        try {
                            artifact.close();
                        } catch (Exception x) {
                        }
                    }
                }
            } else {
                return r;
            }
        }

        @Override
        protected Lexicon lexica(Language lang) {
            final Lexicon l = super.lexica(lang);
            if (l != null) {
                return l;
            } else {
                try {
                    final LAIFRuleSet r = rules(lang);
                    if (r != null && r.getLexicon() != null) {
                        URL lexiconURL = r.getLexicon().toURL();
                        final LemonModel model = LemonSerializer.modelFromURL(lexiconURL);
                        for (Lexicon lexicon : model.getLexica()) {
                            if (lexicon.getLanguage().equals(lang.toString())) {
                                lexica.put(lang, lexicon);
                                return lexicon;
                            }
                        }
                    }
                    return null;
                } catch (Exception x) {
                    x.printStackTrace();
                    return null;
                }
            }
        }
        
        public void updateRules(Language lang, LAIFRuleSet ruleSet) {
            rules.put(lang,ruleSet);
        }
        
        public Set<URI> getLexicaURIs() {
            return map(new HashSet<Lexicon>(lexica.values()), new Converter<Lexicon, URI>() {

                @Override
                public URI f(Lexicon e) {
                    return e.getURI();
                }
            });
        }
        
        public Set<URI> getSenses(URI lexicaURI) {
            final HashSet<URI> senses = new HashSet<URI>();
            for(Lexicon lexicon : lexica.values()) {
                if(lexicon.getURI().equals(lexicaURI)) {
                for(LexicalEntry entry : lexicon.getEntrys()) {
                    for(LexicalSense sense : entry.getSenses()) {
                        senses.add(sense.getReference());
                    }
                }
                }
            }
            return senses;
        }
        
    }

    @Override
    public LAIFLocalizationLexicon getLexicon(String appKey) {
        if (lexica.containsKey(appKey)) {
            return lexica.get(appKey);
        } else {
            final LAIFLocalizationLexiconImpl lll = new LAIFLocalizationLexiconImpl(appKey);
            lexica.put(appKey, lll);
            return lll;
        }
    }

    @Override
    public void updateRules(String appKey, Language lang, LAIFRuleSet rules) {
        ((LAIFLocalizationLexiconImpl)getLexicon(appKey)).updateRules(lang, rules);
    }


    @Override
    public Set<URI> getLexicaURIs(String appKey) {
        return ((LAIFLocalizationLexiconImpl)getLexicon(appKey)).getLexicaURIs();
    }

    @Override
    @Deprecated
    public Set<URI> getForms(String appKey, URI lexicaURI) {
        return ((LAIFLocalizationLexiconImpl)getLexicon(appKey)).getSenses(lexicaURI);
    }
}
