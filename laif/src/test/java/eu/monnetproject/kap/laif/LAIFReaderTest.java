/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import net.lexinfo.LexInfo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author jmccrae
 */
public class LAIFReaderTest {

    public LAIFReaderTest() {
    }
    private String test1 = "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: Add\n"
            + "Rule: \"Hinzuf\u00fcgen\"";
    private String test2 = "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: Asset\n"
            + "Rule: [ :form <http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset> ]\n";
    private String test3 = "@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>\n"
            + "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: Assets\n"
            + "Rule: [ :form <http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset> ; lexinfo:number lexinfo:plural ]\n";
    private String test4 = "@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>\n"
            + "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: Show assets\n"
            + "Rule: [ :form <http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset> ; lexinfo:number lexinfo:plural ] \" zeigen\"\n";
    private String test5 = "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: Profits of company x\n"
            + "Params: x\n"
            + "Rule: \"Gewinne von \" $x\n";
    private String test6 = "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: Profits of SAP\n"
            + "Rule: [ :profits_of \"SAP\" ]\n"
            + "\n"
            + "Key: Profits of X\n"
            + "URI: :profits_of\n"
            + "Params: X\n"
            + "Rule: \"Gewinne von \" $X\n";
    private String test7 = "@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>\n"
            + "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: quant\n"
            + "Params: number,noun\n"
            + "Rule: $number \" \" [ :form $noun ; lexinfo:number lexinfo:plural ]\n"
            + "\n"
            + "Key: 3 companies\n"
            + "Rule: [ :quant \"3\" <http://www.example.com/ontology#Company> ]\n";
    private String test8 = "@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>\n"
            + "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: quant\n"
            + "Params: number,noun\n"
            + "Rule: { $number \n"
            + "  case \"1\" => ( $number \" \" [ :form $noun ] )\n"
            + "  default => ( $number \" \" [ :form $noun ; lexinfo:number lexinfo:plural ] )"
            + "}\n"
            + "\n"
            + "Key: 3 companies\n"
            + "Rule: [ :quant \"3\" <http://www.example.com/ontology#Company> ]\n"
            + "\n"
            + "Key: 1 company\n"
            + "Rule: [ :quant \"1\" <http://www.example.com/ontology#Company> ]\n";
    private String test9 = "@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>\n"
            + "AppKey: test\n"
            + "Language: de\n"
            + "\n"
            + "Key: new_noun\n"
            + "Params: noun\n"
            + "Rule: { $noun lexinfo:gender\n"
            + "  case lexinfo:masculine => ( \"neuer \" $noun ) \n"
            + "  case lexinfo:feminine => ( \"neue \" $noun ) \n"
            + "  case lexinfo:neuter => ( \"neues \" $noun ) \n"
            + "}";
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRead1() {
        System.err.println("testRead1");
        final LAIFRule rule = new LAIFRule(URI.create("local:Add"), "Add", new ArrayList<LAIFVariable>(0), Arrays.asList((LAIFValue) new LAIFLiteral("Hinzuf\u00fcgen")));
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test1));
        assertEquals(rule, ruleSet.keyMap().get("Add"));
    }

    @Test
    public void testRead2() {
        System.err.println("testRead2");
        final URI ifrsAsset = URI.create("http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset");
        final LAIFRule rule = new LAIFRule(URI.create("local:Asset"), "Asset", new ArrayList<LAIFVariable>(0), Arrays.asList((LAIFValue) new LAIFFormCall(ifrsAsset, null)));
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test2));
        assertEquals(rule, ruleSet.keyMap().get("Asset"));
    }

    @Test
    public void testRead3() {
        System.err.println("testRead3");
        final URI ifrsAsset = URI.create("http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset");
        final LexInfo lingOnto = new LexInfo();
        final LAIFRule rule = new LAIFRule(URI.create("local:Assets"), "Assets", new ArrayList<LAIFVariable>(0), Arrays.asList(
                (LAIFValue) new LAIFFormCall(ifrsAsset, Collections.singletonMap(lingOnto.getProperty("number").getURI(), lingOnto.getPropertyValue("plural").getURI()))));
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test3));
        assertEquals(rule, ruleSet.keyMap().get("Assets"));
    }

    @Test
    public void testRead4() {
        System.err.println("testRead4");
        final URI ifrsAsset = URI.create("http://xbrl.iasb.org/taxonomy/2009-04-01/ifrs/Asset");
        final LexInfo lingOnto = new LexInfo();
        final LAIFRule rule = new LAIFRule(URI.create("local:Show_assets"), "Show assets", new ArrayList<LAIFVariable>(0), Arrays.asList(
                (LAIFValue) new LAIFFormCall(ifrsAsset, Collections.singletonMap(lingOnto.getProperty("number").getURI(), lingOnto.getPropertyValue("plural").getURI())),
                (LAIFValue) new LAIFLiteral(" zeigen")));
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test4));
        assertEquals(rule, ruleSet.keyMap().get("Show assets"));
    }

    @Test
    public void testRead5() {
        System.err.println("testRead5");
        final LAIFRule rule = new LAIFRule(URI.create("local:Profits_of_company_x"), "Profits of company x",
                Arrays.asList(new LAIFVariable("x")),
                Arrays.asList((LAIFValue) new LAIFLiteral("Gewinne von "),
                (LAIFValue) new LAIFVariable("x")));
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test5));
        assertEquals(rule, ruleSet.keyMap().get("Profits of company x"));
    }

    @Test
    public void testRead6() {
        System.err.println("testRead6");
        final LAIFRule rule1 = new LAIFRule(URI.create("local:Profits_of_SAP"), "Profits of SAP",
                new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("local:profits_of"), Arrays.asList((LAIFValue) new LAIFLiteral("SAP")))));
        final LAIFRule rule2 = new LAIFRule(URI.create("local:profits_of"), "Profits of X",
                Arrays.asList(new LAIFVariable("X")),
                Arrays.asList((LAIFValue) new LAIFLiteral("Gewinne von "), (LAIFValue) new LAIFVariable("X")));
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test6));
        assertEquals(rule1, ruleSet.keyMap().get("Profits of SAP"));
        assertEquals(rule2, ruleSet.keyMap().get("Profits of X"));

    }

    @Test
    public void testRead7() {
        System.err.println("testRead7");
        final LexInfo lingOnto = new LexInfo();
        final LAIFRule rule1 = new LAIFRule(URI.create("local:quant"), "quant",
                Arrays.asList(new LAIFVariable("number"), new LAIFVariable("noun")),
                Arrays.asList((LAIFValue) new LAIFVariable("number"),
                (LAIFValue) new LAIFLiteral(" "),
                (LAIFValue) new LAIFFormCall(new LAIFVariable("noun"),
                Collections.singletonMap(lingOnto.getProperty("number").getURI(), lingOnto.getPropertyValue("plural").getURI()))));
        final URI ontoCompURI = URI.create("http://www.example.com/ontology#Company");
        @SuppressWarnings("unchecked")
        final LAIFRule rule2 = new LAIFRule(URI.create("local:3_companies"), "3 companies",
                new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("local:quant"),
                Arrays.asList((LAIFValue) new LAIFLiteral("3"),
                (LAIFValue) new LAIFUserCall(ontoCompURI, Collections.EMPTY_LIST)))));
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test7));
        assertEquals(rule1, ruleSet.keyMap().get("quant"));
        assertEquals(rule2, ruleSet.keyMap().get("3 companies"));
    }

    @Test
    public void testRead8() {
        System.err.println("testRead8");
        final LexInfo lingOnto = new LexInfo();
        @SuppressWarnings("unchecked")
        final LAIFRule rule1 = new LAIFRule(URI.create("local:quant"), "quant",
                Arrays.asList(new LAIFVariable(URI.create("number").toString()), new LAIFVariable(URI.create("noun").toString())),
                Arrays.asList((LAIFValue) new LAIFMatch(new LAIFVariable(URI.create("number").toString()), Collections.singletonMap("1",
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFVariable(URI.create("number").toString()),
                (LAIFValue) new LAIFLiteral(" "),
                (LAIFValue) new LAIFFormCall(new LAIFVariable(URI.create("noun").toString()), Collections.EMPTY_MAP)))),
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFVariable(URI.create("number").toString()),
                (LAIFValue) new LAIFLiteral(" "),
                (LAIFValue) new LAIFFormCall(new LAIFVariable(URI.create("noun").toString()), Collections.singletonMap(lingOnto.getProperty("number").getURI(), lingOnto.getPropertyValue("plural").getURI())))))));
        final URI ontoCompURI = URI.create("http://www.example.com/ontology#Company");
        @SuppressWarnings("unchecked")
        final LAIFRule rule2 = new LAIFRule(URI.create("local:3_companies"), "3 companies",
                new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("local:quant"),
                Arrays.asList((LAIFValue) new LAIFLiteral("3"),
                (LAIFValue) new LAIFUserCall(ontoCompURI, Collections.EMPTY_LIST)))));
        @SuppressWarnings("unchecked")
        final LAIFRule rule3 = new LAIFRule(URI.create("local:1_company"), "1 company",
                new ArrayList<LAIFVariable>(0),
                Arrays.asList((LAIFValue) new LAIFUserCall(URI.create("local:quant"),
                Arrays.asList((LAIFValue) new LAIFLiteral("1"),
                (LAIFValue) new LAIFUserCall(ontoCompURI, Collections.EMPTY_LIST)))));
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test8));
        assertEquals(rule1, ruleSet.keyMap().get("quant"));
        assertEquals(rule2, ruleSet.keyMap().get("3 companies"));
        assertEquals(rule3, ruleSet.keyMap().get("1 company"));

    }

    @Test
    public void testRead9() {
        final LexInfo lingOnto = new LexInfo();
        final HashMap<URI, LAIFCase> map = new HashMap<URI, LAIFCase>();
        map.put(lingOnto.getPropertyValue("masculine").getURI(),
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFLiteral("neuer "), (LAIFValue) new LAIFVariable("noun"))));
        map.put(lingOnto.getPropertyValue("feminine").getURI(),
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFLiteral("neue "), (LAIFValue) new LAIFVariable("noun"))));
        map.put(lingOnto.getPropertyValue("neuter").getURI(),
                new LAIFCase(Arrays.asList((LAIFValue) new LAIFLiteral("neues "), (LAIFValue) new LAIFVariable("noun"))));
        final LAIFRule rule = new LAIFRule(URI.create("local:new_noun"), "new_noun",
                Arrays.asList(new LAIFVariable("noun")),
                Arrays.asList((LAIFValue) new LAIFEntryPropCall(lingOnto.getProperty("gender").getURI(), new LAIFVariable("noun"), map, null)));
        
        final LAIFRuleSet ruleSet = LAIFReader.read(new StringReader(test9));
        assertEquals(rule, ruleSet.keyMap().get("new_noun"));
        
    }
    
    private void testRW(String test) {
        final LAIFRuleSet ruleSet1 = LAIFReader.read(new StringReader(test));
        String s2 = ruleSet1.toString();
        System.err.println(s2);
        final LAIFRuleSet ruleSet2 = LAIFReader.read(new StringReader(s2));
        assertEquals(ruleSet1.keyMap(), ruleSet2.keyMap());
    }
    
    @Test
    public void testRWs() {
        testRW(test1);
        testRW(test2);
        testRW(test3);
        testRW(test4);
        testRW(test5);
        testRW(test6);
        testRW(test7);
        testRW(test8);
        testRW(test9);
    }
    
    @Test
    public void testReadLong() throws FileNotFoundException {
        System.err.println("testReadLong");
        final LAIFRuleSet ruleSet = LAIFReader.read(new FileReader("src/test/resources/lemonsource.en.laif"));
        assertTrue(!ruleSet.keyMap().isEmpty());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testNonword() {
        System.err.println("testNonword");
        final LAIFLocalizationLexicon lll = new LAIFLocalizationLexicon("test");
        lll.addRule(Language.ENGLISH, new LAIFRule(URI.create("local:test"), "test", Collections.EMPTY_LIST, Arrays.asList((LAIFValue)new LAIFLiteral("test"))));
        final String resource = lll.get("<<", Language.ENGLISH);
        assertEquals(resource, "<<");
    }
}

