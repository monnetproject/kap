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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class LAIFDescriptionCall implements LAIFCall {
    private final LAIFValue form;

    public LAIFDescriptionCall(LAIFValue form) {
        this.form = form;
    }
    
    @Override
    public String apply(LAIFLexicon lexicon, Map<LAIFVariable, LAIFValue> arguments, Map<URI, LAIFRule> calls) {
        String form2 = lexicon.description(formURI(arguments));
        if (form2 == null) {
            if (form instanceof LAIFUserCall) {
                return ((LAIFUserCall) form).functionFrag();
            } else {
                return "err";
            }
        } else {
            return form2;
        }
    }

    private URI formURI(Map<LAIFVariable, LAIFValue> arguments) {
        if (form instanceof LAIFUserCall) {
            return ((LAIFUserCall) form).function();
        } else if (form instanceof LAIFVariable) {
            if (arguments.containsKey((LAIFVariable) form)) {
                final LAIFValue formDerefed = arguments.get((LAIFVariable) form);
                if (formDerefed instanceof LAIFUserCall) {
                    return ((LAIFUserCall) formDerefed).function();
                } else {
                    throw new LAIFApplyException("Could not derefernce variable to user call");
                }
            } else {
                throw new LAIFApplyException("Could not resolve form URI " + form);
            }
        } else {
            throw new LAIFApplyException("Could not resolve form URI " + form);
        }
    }

    @Override
    public List<LAIFValue> args() {
        return new ArrayList<LAIFValue>(0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LAIFDescriptionCall other = (LAIFDescriptionCall) obj;
        if (this.form != other.form && (this.form == null || !this.form.equals(other.form))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.form != null ? this.form.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "[ :Description " + form + " ]";
    }
    
    
}
