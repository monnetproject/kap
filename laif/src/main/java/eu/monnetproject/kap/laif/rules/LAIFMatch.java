/**********************************************************************************
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
 *********************************************************************************/
package eu.monnetproject.kap.laif.rules;

import eu.monnetproject.kap.laif.LAIFApplyException;
import eu.monnetproject.kap.laif.LAIFLexicon;
import eu.monnetproject.kap.laif.LAIFRule;
import java.net.URI;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class LAIFMatch implements LAIFValue{
    private final LAIFVariable variable;
    private final Map<String,LAIFCase> cases;
    private final LAIFCase defaultCase;

    public LAIFMatch(LAIFVariable variable, Map<String, LAIFCase> cases, LAIFCase defaultCase) {
        this.variable = variable;
        this.cases = cases;
        this.defaultCase = defaultCase;
    }


    @Override
    public String apply(LAIFLexicon lexicon, Map<LAIFVariable, LAIFValue> arguments, Map<URI, LAIFRule> calls) {
        if(!arguments.containsKey(variable)) {
            throw new LAIFApplyException("Could not resolve variable " + variable);
        }
        final String value = arguments.get(variable).apply(lexicon, arguments, calls);
        if(cases.containsKey(value)) {
            return cases.get(value).apply(lexicon, arguments, calls);
        } else if(defaultCase != null) {
            return defaultCase.apply(lexicon, arguments, calls);
        } else {
            throw new LAIFApplyException(value + " does not match a case and not default case");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LAIFMatch other = (LAIFMatch) obj;
        if (this.variable != other.variable && (this.variable == null || !this.variable.equals(other.variable))) {
            return false;
        }
        if (this.cases != other.cases && (this.cases == null || !this.cases.equals(other.cases))) {
            return false;
        }
        if (this.defaultCase != other.defaultCase && (this.defaultCase == null || !this.defaultCase.equals(other.defaultCase))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.variable != null ? this.variable.hashCode() : 0);
        hash = 17 * hash + (this.cases != null ? this.cases.hashCode() : 0);
        hash = 17 * hash + (this.defaultCase != null ? this.defaultCase.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{ " + variable);
        for (Map.Entry<String, LAIFCase> entry : cases.entrySet()) { 
            sb.append(" case \"").append(entry.getKey().replaceAll("\"", "\\\\\\\"")).append("\" => ").append(entry.getValue());
            
        }
        if(defaultCase != null) {
            sb.append(" default => ").append(defaultCase.toString());
        }
        return sb.append("}").toString();
    }
    
    
}
