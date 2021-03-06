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

import eu.monnetproject.kap.laif.LAIFLexicon;
import eu.monnetproject.kap.laif.LAIFRule;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class LAIFLowerFirst implements LAIFCall {
    private final LAIFValue arg;

    public LAIFLowerFirst(LAIFValue args) {
        this.arg = args;
    }

    @Override
    public String apply(LAIFLexicon lexicon, Map<LAIFVariable, LAIFValue> arguments, Map<URI, LAIFRule> calls) {
        final String res = arg.apply(lexicon, arguments, calls);
        return res.substring(0,1).toLowerCase() + res.substring(1);
    }

    @Override
    public List<LAIFValue> args() {
        return Arrays.asList(arg);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LAIFLowerFirst other = (LAIFLowerFirst) obj;
        if (this.arg != other.arg && (this.arg == null || !this.arg.equals(other.arg))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.arg != null ? this.arg.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "[ :LowerFirst " +arg + " ]";
    }
    
    
}
