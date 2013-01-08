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
 * *******************************************************************************
 */
package eu.monnetproject.kap.laif;

import eu.monnetproject.kap.laif.rules.LAIFAllLower;
import eu.monnetproject.kap.laif.rules.LAIFAllUpper;
import eu.monnetproject.kap.laif.rules.LAIFCase;
import eu.monnetproject.kap.laif.rules.LAIFDescriptionCall;
import eu.monnetproject.kap.laif.rules.LAIFEntryPropCall;
import eu.monnetproject.kap.laif.rules.LAIFFormCall;
import eu.monnetproject.kap.laif.rules.LAIFLiteral;
import eu.monnetproject.kap.laif.rules.LAIFLowerFirst;
import eu.monnetproject.kap.laif.rules.LAIFMatch;
import eu.monnetproject.kap.laif.rules.LAIFUpperFirst;
import eu.monnetproject.kap.laif.rules.LAIFUserCall;
import eu.monnetproject.kap.laif.rules.LAIFValue;
import eu.monnetproject.kap.laif.rules.LAIFVariable;
import eu.monnetproject.lang.Language;
import eu.monnetproject.lang.LanguageCodeFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author John McCrae
 */
public class LAIFReader {

    public static LAIFRuleSet read(Reader in) {
        return new LAIFReaderInst().read(in);
    }

    private static class LAIFReaderInst {

        private Map<String, String> prefixes = new HashMap<String, String>();

        public LAIFRuleSet read(Reader in2) {
            try {
                BufferedReader in;
                if (in2 instanceof BufferedReader) {
                    in = (BufferedReader) in2;
                } else {
                    in = new BufferedReader(in2);
                }
                final LAIFRuleSet ruleSet = readHeader(in);
                readRules(in, ruleSet);
                return ruleSet;
            } catch (IOException x) {
                throw new LAIFParseException(x);
            } catch (LanguageCodeFormatException x) {
                throw new LAIFParseException(x);
            }
        }

        private String readLine(BufferedReader in) throws IOException {
            while (true) {
                String s = in.readLine();
                if (s == null) {
                    return null;
                }
                if (s.startsWith("@prefix")) {
                    final Matcher matcher = Pattern.compile("@prefix\\s+(\\S*):\\s*<(.*)>\\s*\\.?\\s*").matcher(s);
                    if (matcher.matches()) {
                        prefixes.put(matcher.group(1), matcher.group(2));
                    }
                } else if (!s.matches("\\s*#.*") && !s.matches("\\s*;.*") && !s.matches("\\s*//.*")) {
                    return s;
                }
            }
        }

        private LAIFRuleSet readHeader(BufferedReader in) throws IOException {
            String line = readLine(in);
            if (line.matches("[Aa][Pp][Pp][Kk][Ee][Yy]: .*")) {
                final String appKey = line.substring(8);
                line = readLine(in);
                if (line.matches("[Ll][Aa][Nn][Gg][Uu][Aa][Gg][Ee]: .*")) {
                    final Language language = Language.get(line.substring(10));


                    line = readLine(in);
                    URI lexicon = null;
                    while (!line.matches("\\s*")) {
                        if (line.matches("[Ll][Ee][Xx][Ii][Cc][Oo][Nn]: .*")) {
                            lexicon = URI.create(line.substring(9));
                            line = readLine(in);
                        } else {
                            throw new LAIFParseException("Expected \"lexicon:\" got " + line);
                        }
                    }
                    final LAIFRuleSet ruleSet = new LAIFRuleSet(lexicon, language, appKey);

                    return ruleSet;
                } else {
                    throw new LAIFParseException("Expected \"Language:\" got " + line);
                }
            } else {
                throw new LAIFParseException("Expected \"AppKey:\" got " + line);
            }
        }

        private void readRules(BufferedReader in, LAIFRuleSet ruleSet) throws IOException {
            LAIFRule rule;
            do {
                rule = readRule(in);
                if (rule != null) {
                    ruleSet.add(rule);
                }
            } while (rule != null);
        }

        private LAIFRule readRule(BufferedReader in) throws IOException {
            String line = readLine(in);
            while (line != null && line.matches("\\s*")) {
                line = readLine(in);
            }
            if (line == null) {
                return null;
            }
            StringBuffer key;
            if (line.matches("[Kk][Ee][Yy]: .+")) {
                key = new StringBuffer(line.substring(5));
                line = readLine(in);
                while(line.startsWith(" ")) {
                    key.append("\n").append(line.replaceAll("^\\s+", ""));
                    line = readLine(in);
                }
            } else {
                throw new LAIFParseException("Expected \"Key: \" got " + line);
            }
            URI resource;
            if (line.matches("[Uu][Rr][Ii]: .*")) {
                resource = readURI(new StringBuilder(line.substring(5)));
                line = readLine(in);
            } else {
                resource = URI.create("local:" + key.toString().replaceAll(" ", "_").replaceAll("\\W", ""));
            }
            final List<LAIFVariable> params = new LinkedList<LAIFVariable>();
            if (line.matches("[Pp][Aa][Rr][Aa][Mm][Ss]: .*")) {
                final String[] paramNames = line.substring(8).split(",");
                for (String paramName : paramNames) {
                    if (!paramName.trim().matches("\\w+")) {
                        throw new LAIFParseException("Bad parameter name " + paramName);
                    }
                    params.add(new LAIFVariable(paramName.trim()));
                }
                line = readLine(in);
            }
            StringBuilder ruleStr = new StringBuilder();
            if (line.matches("[Rr][Uu][Ll][Ee]: .*")) {
                ruleStr.append(line.substring(6));
                line = readLine(in);
                while (line != null && !line.matches("\\s*")) {
                    ruleStr.append(line);
                    line = readLine(in);
                }
                final List<LAIFValue> body = ruleBody(ruleStr);
                return new LAIFRule(resource, key.toString(), params, body);
            } else {
                throw new LAIFParseException("Expected \"Rule:\" got " + line);
            }
        }

        private URI readURI(StringBuilder ruleStr) throws LAIFParseException {
            final Matcher m1 = Pattern.compile("\\s*<([^>]+)>").matcher(ruleStr);
            final Matcher m2 = Pattern.compile("\\s*(\\w*):(\\w+)").matcher(ruleStr);
            if (m1.find()) {
                URI prop = URI.create(m1.group(1));
                ruleStr.delete(0, m1.end());
                return prop;
            } else if (m2.find()) {
                if(m2.group(1).equals("")) {
                    URI prop = URI.create("local:"+m2.group(2));
                    ruleStr.delete(0, m2.end());
                    return prop;
                } else if (prefixes.containsKey(m2.group(1))) {
                    URI prop = URI.create(prefixes.get(m2.group(1)) + m2.group(2));
                    ruleStr.delete(0, m2.end());
                    return prop;
                } else {
                    throw new LAIFParseException("Unknown prefix " + m2.group(1));
                }
            } else {
                throw new LAIFParseException("Expected URI but got " + ruleStr + " (or literal not in \"double quotes\")");
            }
        }

        private List<LAIFValue> ruleBody(StringBuilder ruleStr) {
            return ruleBody(ruleStr, '\n');
        }

        private List<LAIFValue> ruleBody(StringBuilder ruleStr, char closer) {
            final List<LAIFValue> ruleBody = new LinkedList<LAIFValue>();
            while (true) {
                final LAIFValue value = readValue(ruleStr, closer);
                if(value != null) {
                    ruleBody.add(value);
                } else {
                    return ruleBody;
                }
            }
        }

        private LAIFValue readValue(StringBuilder ruleStr, char closer) {
            while (ruleStr.length() != 0) {
                if (Character.isWhitespace(ruleStr.charAt(0))) {
                    ruleStr.deleteCharAt(0);
                } else if (ruleStr.charAt(0) == '"') {
                    return readLiteral(ruleStr);
                } else if (ruleStr.charAt(0) == '$') {
                    return readVariable(ruleStr);
                } else if (ruleStr.charAt(0) == '[') {
                    return readFunction(ruleStr);
                } else if (ruleStr.charAt(0) == '{') {
                    return readMatch(ruleStr);
                } else if (ruleStr.charAt(0) == closer) {
                    ruleStr.deleteCharAt(0);
                    return null;
                } else {
                    return readFunctionInline(ruleStr);
                }
            }
            return null;
        }

        private LAIFValue readLiteral(StringBuilder ruleStr) {
            final Matcher matcher = Pattern.compile("^(\".*?(?<!\\\\)\")").matcher(ruleStr);
            if (matcher.find()) {
                final String s = matcher.group(1);
                final LAIFLiteral lit = new LAIFLiteral(s.substring(1, s.length() - 1));
                ruleStr.delete(0, matcher.end());
                return lit;
            } else {
                throw new LAIFParseException("Unclosed literal: " + ruleStr);
            }
        }

        private LAIFVariable readVariable(StringBuilder ruleStr) {
            final Matcher matcher = Pattern.compile("^(\\$\\w+)\\s*").matcher(ruleStr);
            if (matcher.find()) {
                final String s = matcher.group(1);
                final LAIFVariable laifVariable = new LAIFVariable(s.trim().substring(1));
                ruleStr.delete(0, matcher.end());
                return laifVariable;
            } else {
                throw new LAIFParseException("Bad variable: " + ruleStr);
            }
        }

        private LAIFValue readFunction(StringBuilder ruleStr) {
            final Matcher matcher1 = Pattern.compile("^\\[\\s*:(\\w+)\\s+").matcher(ruleStr);
            final Matcher matcher2 = Pattern.compile("^\\[\\s*<([^>]+)>\\s*").matcher(ruleStr);
            final Matcher matcher3 = Pattern.compile("^\\[\\s*(\\w+):(\\w+)\\s+").matcher(ruleStr);
            if (matcher1.find()) {
                final String functionName = matcher1.group(1);
                if (functionName.equalsIgnoreCase("AllLower")) {
                    ruleStr.delete(0, matcher1.end());
                    final List<LAIFValue> ruleBody = ruleBody(ruleStr, ']');
                    if (ruleBody.size() != 1) {
                        throw new LAIFParseException("AllLower requires one argument");
                    }
                    return new LAIFAllLower(ruleBody.get(0));
                } else if (functionName.equalsIgnoreCase("AllUpper")) {
                    ruleStr.delete(0, matcher1.end());
                    final List<LAIFValue> ruleBody = ruleBody(ruleStr, ']');
                    if (ruleBody.size() != 1) {
                        throw new LAIFParseException("AllUpper requires one argument");
                    }
                    return new LAIFAllUpper(ruleBody.get(0));
                } else if (functionName.equalsIgnoreCase("LowerFirst")) {
                    ruleStr.delete(0, matcher1.end());
                    final List<LAIFValue> ruleBody = ruleBody(ruleStr, ']');
                    if (ruleBody.size() != 1) {
                        throw new LAIFParseException("LowerFirst requires one argument");
                    }
                    return new LAIFLowerFirst(ruleBody.get(0));
                } else if (functionName.equalsIgnoreCase("UpperFirst")) {
                    ruleStr.delete(0, matcher1.end());
                    final List<LAIFValue> ruleBody = ruleBody(ruleStr, ']');
                    if (ruleBody.size() != 1) {
                        throw new LAIFParseException("UpperFirst requires one argument");
                    }
                    return new LAIFUpperFirst(ruleBody.get(0));
                } else if (functionName.equalsIgnoreCase("Description")) {
                    ruleStr.delete(0, matcher1.end());
                    final List<LAIFValue> ruleBody = ruleBody(ruleStr, ']');
                    if (ruleBody.size() != 1) {
                        throw new LAIFParseException("Desription requires one argument");
                    }
                    return new LAIFDescriptionCall(ruleBody.get(0));
                } else if (functionName.equalsIgnoreCase("Form")) {
                    ruleStr.delete(0, matcher1.end());
                    final LAIFValue ruleBody = readValue(ruleStr, ']');
                    while(ruleStr.length() > 0 && Character.isWhitespace(ruleStr.charAt(0))) {
                        ruleStr.deleteCharAt(0);
                    }
                    final Map<URI, URI> props = new HashMap<URI, URI>();
                    while (ruleStr.length() > 0 && ruleStr.charAt(0) == ';') {
                        ruleStr.deleteCharAt(0);
                        props.put(readURI(ruleStr), readURI(ruleStr));
                        while (ruleStr.length() > 0 && Character.isWhitespace(ruleStr.charAt(0))) {
                            ruleStr.deleteCharAt(0);
                        }
                    }
                    if(ruleStr.charAt(0) == ']') {
                        ruleStr.deleteCharAt(0);
                    } else {
                        throw new LAIFParseException("Expected \"]\" got "+ ruleStr);
                    }
                    if (ruleBody instanceof LAIFVariable) {
                        return new LAIFFormCall((LAIFVariable) ruleBody, props);
                    } else if (ruleBody instanceof LAIFUserCall) {
                        return new LAIFFormCall(((LAIFUserCall) ruleBody).function(), props);
                    } else {
                        throw new LAIFParseException(ruleBody + " is not a valid parameter to a form call");
                    }
                } else {
                    URI functionURI = URI.create("local:" + functionName);
                    ruleStr.delete(0, matcher1.end());
                    final List<LAIFValue> ruleBody = ruleBody(ruleStr, ']');
                    return new LAIFUserCall(functionURI, ruleBody);
                }
            } else if (matcher2.find()) {
                URI functionURI = URI.create(matcher2.group(1));
                ruleStr.delete(0, matcher2.end());
                final List<LAIFValue> ruleBody = ruleBody(ruleStr, ']');
                return new LAIFUserCall(functionURI, ruleBody);
            } else if (matcher3.find()) {
                if (prefixes.containsKey(matcher3.group(1))) {
                    URI functionURI = URI.create(prefixes.get(matcher3.group(1)) + matcher3.group(2));
                    ruleStr.delete(0, matcher3.end());
                    final List<LAIFValue> ruleBody = ruleBody(ruleStr, ']');
                    return new LAIFUserCall(functionURI, ruleBody);
                } else {
                    throw new LAIFParseException("Unknown namespace " + matcher3.group(1));
                }
            } else {
                throw new LAIFParseException("Expected function name got " + ruleStr);
            }
        }

        private LAIFValue readMatch(StringBuilder ruleStr) {
            while (ruleStr.charAt(0) == '{' || Character.isWhitespace(ruleStr.charAt(0))) {
                ruleStr.deleteCharAt(0);
            }
            final LAIFVariable variable = readVariable(ruleStr);
            if (ruleStr.toString().matches("\\s*(case|default)\\s+.*")) {
                final Map<String, LAIFCase> cases = readCases(ruleStr, true);
                final LAIFCase defaultCase;
                if (cases.containsKey("")) {
                    defaultCase = cases.remove("");
                } else {
                    defaultCase = null;
                }
                return new LAIFMatch(variable, cases, defaultCase);
            } else {
                final URI property = readURI(ruleStr);
                final Map<String, LAIFCase> cases = readCases(ruleStr, false);
                final Map<URI, LAIFCase> cases2 = new HashMap<URI, LAIFCase>();
                final LAIFCase defaultCase;
                if (cases.containsKey("")) {
                    defaultCase = cases.remove("");
                } else {
                    defaultCase = null;
                }
                for (Map.Entry<String, LAIFCase> entry : cases.entrySet()) {
                    cases2.put(URI.create(entry.getKey()), entry.getValue());
                }
                return new LAIFEntryPropCall(property, variable, cases2, defaultCase);
            }
        }

        @SuppressWarnings("unchecked")
        private LAIFValue readFunctionInline(StringBuilder ruleStr) {
            final URI uri = readURI(ruleStr);
            return new LAIFUserCall(uri, Collections.EMPTY_LIST);
        }

        private Map<String, LAIFCase> readCases(StringBuilder ruleStr, boolean literal) {
            Map<String, LAIFCase> cases = new HashMap<String, LAIFCase>();
            while (!ruleStr.toString().matches("\\s*}")) {
                final Matcher m1 = Pattern.compile("^\\s*case\\s+").matcher(ruleStr);
                final Matcher m2 = Pattern.compile("^\\s*default").matcher(ruleStr);
                final String caseString;
                if (m1.find()) {
                    ruleStr.delete(0, m1.end());
                    final LAIFValue caseValue = readValue(ruleStr, '\n');
                    if (caseValue instanceof LAIFLiteral && literal) {
                        caseString = ((LAIFLiteral) caseValue).value();
                    } else if (caseValue instanceof LAIFUserCall && !literal && ((LAIFUserCall) caseValue).args().isEmpty()) {
                        caseString = ((LAIFUserCall) caseValue).function().toString();
                    } else {
                        throw new LAIFParseException("Bad case " + caseValue);
                    }
                } else if (m2.find()) {
                    ruleStr.delete(0, m2.end());
                    caseString = "";
                } else {
                    throw new LAIFParseException("Expected \"case\" or \"default\" got " + ruleStr);
                }
                final Matcher m3 = Pattern.compile("^\\s*=>\\s*\\(").matcher(ruleStr);
                if (!m3.find()) {
                    throw new LAIFParseException("Expected \"=> (\" got " + ruleStr);
                }
                ruleStr.delete(0, m3.end());
                final List<LAIFValue> body = ruleBody(ruleStr, ')');
                if (cases.put(caseString, new LAIFCase(body)) != null) {
                    throw new LAIFParseException("Duplicate case " + caseString);
                }

            }
            while(ruleStr.length() > 0 && (Character.isWhitespace(ruleStr.charAt(0)) || ruleStr.charAt(0) == '}')) {
                ruleStr.deleteCharAt(0);
            }
            return cases;
        }
    }
}
