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
package eu.monnetproject.kap.laif.rules;

import eu.monnetproject.kap.laif.LAIFApplyException;
import eu.monnetproject.kap.laif.LAIFLexicon;
import eu.monnetproject.kap.laif.LAIFRule;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class LAIFEntryPropCall implements LAIFCall {

    private final URI entryProp;
    private final LAIFVariable variable;
    private final Map<URI,LAIFCase> matches;
    private final LAIFCase defaultCase;

    public LAIFEntryPropCall(URI entryProp, LAIFVariable variable, Map<URI, LAIFCase> matches, LAIFCase defaultCase) {
        this.entryProp = entryProp;
        this.variable = variable;
        this.matches = matches;
        this.defaultCase = defaultCase;
    }
    
    @Override
    public List<LAIFValue> args() {
        return new ArrayList<LAIFValue>(0);
    }
    
    @Override
    public String apply(LAIFLexicon lexicon, Map<LAIFVariable, LAIFValue> arguments, Map<URI, LAIFRule> calls) {
        final URI sense;
        if (!arguments.containsKey(variable)) {
            throw new LAIFApplyException("Wrong number of arguments");
        }
        if (arguments.get(variable) instanceof LAIFFormCall) {
            sense = ((LAIFFormCall) arguments.get(variable)).sense(arguments);
        } else if (arguments.get(variable) instanceof LAIFUserCall) {
            sense = ((LAIFUserCall) arguments.get(variable)).function();
        } else {
            throw new LAIFApplyException("Variable not bound to form call, cannot get entry properties");
        }
        final Map<URI, URI> entryProps = lexicon.entryProps(sense);
        if (entryProps.containsKey(entryProp)) {
            final URI propVal = entryProps.get(entryProp);
            final LAIFCase caseMatched = matches.containsKey(propVal) ?
                    matches.get(propVal) : defaultCase;
            if (caseMatched == null) {
                throw new LAIFApplyException("Could not find matching case " + propVal + " and no default");
            }
            return caseMatched.apply(lexicon, arguments, calls);
        } else {
            throw new LAIFApplyException("Could not extract entry property from lexicon");
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
        final LAIFEntryPropCall other = (LAIFEntryPropCall) obj;
        if (this.entryProp != other.entryProp && (this.entryProp == null || !this.entryProp.equals(other.entryProp))) {
            return false;
        }
        if (this.variable != other.variable && (this.variable == null || !this.variable.equals(other.variable))) {
            return false;
        }
        if (this.matches != other.matches && (this.matches == null || !this.matches.equals(other.matches))) {
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
        hash = 67 * hash + (this.entryProp != null ? this.entryProp.hashCode() : 0);
        hash = 67 * hash + (this.variable != null ? this.variable.hashCode() : 0);
        hash = 67 * hash + (this.matches != null ? this.matches.hashCode() : 0);
        hash = 67 * hash + (this.defaultCase != null ? this.defaultCase.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{ ").append(variable).append(" <").append(entryProp).append(">");
        for (Map.Entry<URI, LAIFCase> match : matches.entrySet()) {
            sb.append(" case <").append(match.getKey()).append("> => ").append(match.getValue());
        }
        if(defaultCase != null) {
            sb.append(" default => ").append(defaultCase);
        }
        sb.append(" }");
        return sb.toString();
    }
    
    
}
