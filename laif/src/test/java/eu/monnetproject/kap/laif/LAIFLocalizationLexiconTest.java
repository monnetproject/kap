package eu.monnetproject.kap.laif;

import eu.monnetproject.kap.laif.rules.LAIFCase;
import eu.monnetproject.kap.laif.rules.LAIFEntryPropCall;
import eu.monnetproject.kap.laif.rules.LAIFFormCall;
import eu.monnetproject.kap.laif.rules.LAIFLiteral;
import eu.monnetproject.kap.laif.rules.LAIFMatch;
import eu.monnetproject.kap.laif.rules.LAIFUserCall;
import eu.monnetproject.kap.laif.rules.LAIFValue;
import eu.monnetproject.kap.laif.rules.LAIFVariable;
import eu.monnetproject.lang.Language;
import eu.monnetproject.lemon.LemonModel;
import eu.monnetproject.lemon.LemonModels;
import eu.monnetproject.lemon.LemonSerializer;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.Lexicon;
import eu.monnetproject.lemon.model.Text;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import net.lexinfo.LexInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jmccrae
 */
public class LAIFLocalizationLexiconTest {

    public LAIFLocalizationLexiconTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test1() {
        System.err.println("test1");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:Add"), "Add", new ArrayList<LAIFVariable>(0), Arrays.asList((LAIFValue) new LAIFLiteral("Hinzuf\u00fcgen"))));
        final String result = lll.get("Add", Language.GERMAN);
        assertEquals("Hinzuf\u00fcgen", result);
    }

    @Test
    public void test2() {
        System.err.println("test2");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        final URI ifrsAsset = URI.create("http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset");
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:Asset"), "Asset", new ArrayList<LAIFVariable>(0), Arrays.asList((LAIFValue) new LAIFFormCall(ifrsAsset, null))));
        final LemonSerializer serializer = LemonSerializer.newInstance();
        final LemonModel model = serializer.create();
        final Lexicon lexicon = model.addLexicon(URI.create("example:Lexicon"), "de");
        LemonModels.addEntryToLexicon(lexicon, URI.create("example:Aktiva_entry"), "Aktiva", ifrsAsset);
        lll.addLexicon(lexicon);
        final String result = lll.get("Asset", Language.GERMAN);
        assertEquals("Aktiva", result);
    }

    @Test
    public void test3() {
        System.err.println("test3");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        final URI ifrsAsset = URI.create("http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset");
        final LexInfo lingOnto = new LexInfo();
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:Assets"), "Assets", new ArrayList<LAIFVariable>(0), Arrays.asList(
                (LAIFValue) new LAIFFormCall(ifrsAsset, Collections.singletonMap(lingOnto.getProperty("number").getURI(), lingOnto.getPropertyValue("plural").getURI())))));
        final LemonSerializer serializer = LemonSerializer.newInstance();
        final LemonModel model = serializer.create();
        final Lexicon lexicon = model.addLexicon(URI.create("example:Lexicon"), "de");
        final LexicalEntry entry = LemonModels.addEntryToLexicon(lexicon, URI.create("example:Aktiva_entry"), "Aktiva", ifrsAsset);
        final LexicalForm otherForm = model.getFactory().makeForm();
        otherForm.addProperty(lingOnto.getProperty("number"), lingOnto.getPropertyValue("plural"));
        otherForm.setWrittenRep(new Text("Aktiven", "de"));
        entry.addOtherForm(otherForm);
        lll.addLexicon(lexicon);
        final String result = lll.get("Assets", Language.GERMAN);
        assertEquals("Aktiven", result);
    }

    @Test
    public void test4() {
        System.err.println("test4");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        final URI ifrsAsset = URI.create("http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset");
        final LexInfo lingOnto = new LexInfo();
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:Show_assets"), "Show assets", new ArrayList<LAIFVariable>(0), Arrays.asList(
                (LAIFValue) new LAIFFormCall(ifrsAsset, Collections.singletonMap(lingOnto.getProperty("number").getURI(), lingOnto.getPropertyValue("plural").getURI())),
                (LAIFValue) new LAIFLiteral(" zeigen"))));
        final LemonSerializer serializer = LemonSerializer.newInstance();
        final LemonModel model = serializer.create();
        final Lexicon lexicon = model.addLexicon(URI.create("example:Lexicon"), "de");
        final LexicalEntry entry = LemonModels.addEntryToLexicon(lexicon, URI.create("example:Aktiva_entry"), "Aktiva", ifrsAsset);
        final LexicalForm otherForm = model.getFactory().makeForm();
        otherForm.addProperty(lingOnto.getProperty("number"), lingOnto.getPropertyValue("plural"));
        otherForm.setWrittenRep(new Text("Aktiven", "de"));
        entry.addOtherForm(otherForm);
        lll.addLexicon(lexicon);
        final String result = lll.get("Show assets", Language.GERMAN);
        assertEquals("Aktiven zeigen", result);
    }

    @Test
    public void test5() {
        System.err.println("test5");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:Profits_of_company_x"), "Profits of company x",
                Arrays.asList(new LAIFVariable("example:company_x")),
                Arrays.asList((LAIFValue) new LAIFLiteral("Gewinne von "),
                (LAIFValue) new LAIFVariable("example:company_x"))));
        final String result = lll.get("Profits of company x", Collections.singletonList((Object) "SAP"), Language.GERMAN);
        assertEquals("Gewinne von SAP", result);

    }

    @Test
    public void test6() {
        System.err.println("test6");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:Profits_of_SAP"), "Profits of SAP",
                new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("example:profits_of"), Arrays.asList((LAIFValue) new LAIFLiteral("SAP"))))));
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:profits_of"), "Profits of X",
                Arrays.asList(new LAIFVariable("example:X")),
                Arrays.asList((LAIFValue) new LAIFLiteral("Gewinne von "), (LAIFValue) new LAIFVariable("example:X"))));

        @SuppressWarnings("unchecked")
        final String result = lll.get("Profits of SAP", Collections.EMPTY_LIST, Language.GERMAN);
        assertEquals("Gewinne von SAP", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test7() {
        System.err.println("test7");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        final LexInfo lingOnto = new LexInfo();
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:quant"), "quant",
                Arrays.asList(new LAIFVariable("example:number"), new LAIFVariable("example:noun")),
                Arrays.asList((LAIFValue) new LAIFVariable("example:number"),
                (LAIFValue) new LAIFLiteral(" "),
                (LAIFValue) new LAIFFormCall(new LAIFVariable("example:noun"),
                Collections.singletonMap(lingOnto.getProperty("number").getURI(), lingOnto.getPropertyValue("plural").getURI())))));
        final URI ontoCompURI = URI.create("http://www.example.com/ontology#Company");
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:three_companies"), "3 companies",
                new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("example:quant"),
                Arrays.asList((LAIFValue) new LAIFLiteral("3"),
                (LAIFValue) new LAIFUserCall(ontoCompURI, Collections.EMPTY_LIST))))));
        final LemonSerializer serializer = LemonSerializer.newInstance();
        final LemonModel model = serializer.create();
        final Lexicon lexicon = model.addLexicon(URI.create("example:Lexicon"), "de");
        final LexicalEntry entry = LemonModels.addEntryToLexicon(lexicon, URI.create("example:Firma_entry"), "Firma", ontoCompURI);
        final LexicalForm otherForm = model.getFactory().makeForm();
        otherForm.addProperty(lingOnto.getProperty("number"), lingOnto.getPropertyValue("plural"));
        otherForm.setWrittenRep(new Text("Firmen", "de"));
        entry.addOtherForm(otherForm);
        lll.addLexicon(lexicon);
        final String result = lll.get("3 companies", Language.GERMAN);
        assertEquals("3 Firmen", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test8() {
        System.err.println("test8");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        final LexInfo lingOnto = new LexInfo();
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:quant"), "quant",
                Arrays.asList(new LAIFVariable(URI.create("example:number").toString()), new LAIFVariable(URI.create("example:noun").toString())),
                Arrays.asList((LAIFValue) new LAIFMatch(new LAIFVariable(URI.create("example:number").toString()), Collections.singletonMap("1",
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFVariable(URI.create("example:number").toString()),
                (LAIFValue) new LAIFLiteral(" "),
                (LAIFValue) new LAIFFormCall(new LAIFVariable(URI.create("example:noun").toString()), Collections.EMPTY_MAP)))),
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFVariable(URI.create("example:number").toString()),
                (LAIFValue) new LAIFLiteral(" "),
                (LAIFValue) new LAIFFormCall(new LAIFVariable(URI.create("example:noun").toString()), Collections.singletonMap(lingOnto.getProperty("number").getURI(), lingOnto.getPropertyValue("plural").getURI()))))))));

        final URI ontoCompURI = URI.create("http://www.example.com/ontology#Company");
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:three_companies"), "3 companies",
                new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("example:quant"),
                Arrays.asList((LAIFValue) new LAIFLiteral("3"),
                (LAIFValue) new LAIFUserCall(ontoCompURI, Collections.EMPTY_LIST))))));
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:one_company"), "1 company",
                new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("example:quant"),
                Arrays.asList((LAIFValue) new LAIFLiteral("1"),
                (LAIFValue) new LAIFUserCall(ontoCompURI, Collections.EMPTY_LIST))))));
        final LemonSerializer serializer = LemonSerializer.newInstance();
        final LemonModel model = serializer.create();
        final Lexicon lexicon = model.addLexicon(URI.create("example:Lexicon"), "de");
        final LexicalEntry entry = LemonModels.addEntryToLexicon(lexicon, URI.create("example:Firma_entry"), "Firma", ontoCompURI);
        final LexicalForm otherForm = model.getFactory().makeForm();
        otherForm.addProperty(lingOnto.getProperty("number"), lingOnto.getPropertyValue("plural"));
        otherForm.setWrittenRep(new Text("Firmen", "de"));
        entry.addOtherForm(otherForm);
        lll.addLexicon(lexicon);
        final String result = lll.get("3 companies", Language.GERMAN);
        assertEquals("3 Firmen", result);
        final String result2 = lll.get("1 company", Language.GERMAN);
        assertEquals("1 Firma", result2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test9() {
        System.err.println("test9");

        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("example");
        final LexInfo lingOnto = new LexInfo();
        final HashMap<URI, LAIFCase> map = new HashMap<URI, LAIFCase>();
        map.put(lingOnto.getPropertyValue("masculine").getURI(),
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFLiteral("neuer "), (LAIFValue) new LAIFVariable(URI.create("example:noun").toString()))));
        map.put(lingOnto.getPropertyValue("feminine").getURI(),
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFLiteral("neue "), (LAIFValue) new LAIFVariable(URI.create("example:noun").toString()))));
        map.put(lingOnto.getPropertyValue("neuter").getURI(),
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFLiteral("neues "), (LAIFValue) new LAIFVariable(URI.create("example:noun").toString()))));
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:new_noun"), "new_noun",
                Arrays.asList(new LAIFVariable(URI.create("example:noun").toString())),
                Arrays.asList((LAIFValue) new LAIFEntryPropCall(lingOnto.getProperty("gender").getURI(), new LAIFVariable(URI.create("example:noun").toString()), map, null))));
        final URI ontoCompURI = URI.create("http://www.example.com/ontology#Company");
        lll.addRule(Language.GERMAN, new LAIFRule(URI.create("example:new_company"), "new company", new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("example:new_noun"),
                Arrays.asList((LAIFValue) new LAIFFormCall(ontoCompURI, Collections.EMPTY_MAP))))));
        final LemonSerializer serializer = LemonSerializer.newInstance();
        final LemonModel model = serializer.create();
        final Lexicon lexicon = model.addLexicon(URI.create("example:Lexicon"), "de");
        final LexicalEntry entry = LemonModels.addEntryToLexicon(lexicon, URI.create("example:Firma_entry"), "Firma", ontoCompURI);
        entry.addProperty(lingOnto.getProperty("gender"), lingOnto.getPropertyValue("feminine"));
        final LexicalForm otherForm = model.getFactory().makeForm();
        otherForm.addProperty(lingOnto.getProperty("number"), lingOnto.getPropertyValue("plural"));
        otherForm.setWrittenRep(new Text("Firmen", "de"));
        entry.addOtherForm(otherForm);
        lll.addLexicon(lexicon);

        final String result = lll.get("new company", Language.GERMAN);
        assertEquals("neue Firma", result);
    }
//    :new_noun laif:param ( :noun ) ;
//   laif:value (
//      [ laif:var [ laif:entryProp ( :noun lexinfo:gender ) ]
//        laif:match (
//           [ laif:case lexinfo:masculine ;
//             laif:value ( "neuer " [ laif:form :noun ] ) ] 
//           [ laif:case lexinfo:feminine ;
//             laif:value ( "neue " [ laif:form :noun ] ) ] 
//           [ laif:case lexinfo:neuter ;
//             laif:value ( "neues " [ laif:form :noun ] ) ]
//        ) 
//   ] ) .
}
