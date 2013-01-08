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
import java.util.List;
import java.util.Map;
import static eu.monnetproject.util.CollectionFunctions.*;

/**
 *
 * @author John McCrae
 */
public class LAIFUserCall implements LAIFCall {
    private final URI function;
    private final List<LAIFValue> args;

    public LAIFUserCall(URI function, List<LAIFValue> args) {
        this.function = function;
        this.args = args;
    }

    @Override
    public String apply(LAIFLexicon lexicon, Map<LAIFVariable, LAIFValue> arguments, Map<URI, LAIFRule> calls) {
        if(!calls.containsKey(function)) {
            throw new LAIFApplyException("Rule for " + function + " not found");
        }
        final LAIFRule rule = calls.get(function);
        return rule.apply(lexicon, mapArgs(rule,args,arguments), calls);
    }
    
    public String functionFrag() { return function.getFragment(); }

    public URI function() {
        return function;
    }

    
    
    @Override
    public List<LAIFValue> args() {
        return args;
    }

    private List<LAIFValue> mapArgs(LAIFRule rule, List<LAIFValue> args, final Map<LAIFVariable, LAIFValue> arguments) {
        return map(args, new Converter<LAIFValue, LAIFValue>() {

            @Override
            public LAIFValue f(LAIFValue e) {
                if(e instanceof LAIFVariable) {
                    if(!arguments.containsKey((LAIFVariable)e)) {
                        throw new LAIFApplyException("Wrong number of arguments");
                    } else {
                        return arguments.get((LAIFVariable)e);
                    }
                } else {
                    return e;
                }
            }
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LAIFUserCall other = (LAIFUserCall) obj;
        if (this.function != other.function && (this.function == null || !this.function.equals(other.function))) {
            return false;
        }
        if (this.args != other.args && (this.args == null || !this.args.equals(other.args))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.function != null ? this.function.hashCode() : 0);
        hash = 59 * hash + (this.args != null ? this.args.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[ ");
        if(function.getScheme().equals("local")) {
            sb.append(":").append(function.getSchemeSpecificPart());
        } else {
            sb.append("<").append(function).append(">");
        }
        for(LAIFValue arg : args) {
            sb.append(" ").append(arg);
        }
        return sb.append(" ]").toString();
    }
    
    
    
}
