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
package eu.monnetproject.kap.laif.rules;

import eu.monnetproject.kap.laif.LAIFApplyException;
import eu.monnetproject.kap.laif.LAIFLexicon;
import eu.monnetproject.kap.laif.LAIFRule;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class LAIFFormCall implements LAIFCall {

    private final URI sense;
    private final LAIFVariable variable;
    private final Map<URI, URI> props;

    public LAIFFormCall(URI sense, Map<URI, URI> props) {
        this.sense = sense;
        this.variable = null;
        this.props = props == null ? new HashMap<URI, URI>() : props;
    }

    public LAIFFormCall(LAIFVariable variable, Map<URI, URI> props) {
        this.sense = null;
        this.variable = variable;
        this.props = props == null ? new HashMap<URI, URI>() : props;
    }

    @Override
    public String apply(LAIFLexicon lexicon, Map<LAIFVariable, LAIFValue> arguments, Map<URI, LAIFRule> calls) {
        String form2 = lexicon.form(sense(arguments), props);
        if (form2 == null) {
            return sense.getFragment();
        } else {
            return form2;
        }
    }

    @Override
    public List<LAIFValue> args() {
        return new ArrayList<LAIFValue>(0);
    }

    public URI sense(Map<LAIFVariable, LAIFValue> arguments) {
        if (sense != null) {
            return sense;
        } else {
            if (!arguments.containsKey(variable)) {
                throw new LAIFApplyException("Could not resolve variable " + variable);
            } else {
                final LAIFValue value = arguments.get(variable);
                if (value instanceof LAIFUserCall && ((LAIFUserCall) value).args().isEmpty()) {
                    return ((LAIFUserCall) value).function();
                } else {
                    throw new LAIFApplyException("Variable does not resolve to URI");
                }
            }
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
        final LAIFFormCall other = (LAIFFormCall) obj;
        if (this.sense != other.sense && (this.sense == null || !this.sense.equals(other.sense))) {
            return false;
        }
        if (this.variable != other.variable && (this.variable == null || !this.variable.equals(other.variable))) {
            return false;
        }
        if (this.props != other.props && (this.props == null || !this.props.equals(other.props))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.sense != null ? this.sense.hashCode() : 0);
        hash = 29 * hash + (this.variable != null ? this.variable.hashCode() : 0);
        hash = 29 * hash + (this.props != null ? this.props.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[ :form ").append(sense != null ? ("<" + sense + "> ") : variable);
        if (props != null) {
            for (Map.Entry<URI, URI> entry : props.entrySet()) {
                sb.append("; <").append(entry.getKey()).append("> <").append(entry.getValue()).append("> ");
            }
        }
        return sb.append("]").toString();
    }
}
