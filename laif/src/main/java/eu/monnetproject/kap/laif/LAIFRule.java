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

import eu.monnetproject.kap.laif.rules.LAIFVariable;
import eu.monnetproject.kap.laif.rules.LAIFValue;
import eu.monnetproject.util.CollectionFunctions;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class LAIFRule {

    public final URI resource;
    public final String key;
    public final List<LAIFVariable> param;
    public final List<LAIFValue> value;

    public LAIFRule(URI resource, String key, List<LAIFVariable> param, List<LAIFValue> value) {
        this.resource = resource;
        this.key = key.trim();
        this.param = param;
        this.value = value;
        assert(!value.isEmpty());
    }
    
     public String apply(final LAIFLexicon lexicon, final List<LAIFValue> arguments, final Map<URI,LAIFRule> calls) {
         return CollectionFunctions.foldLeft(value, "", new CollectionFunctions.FoldFunction<String, LAIFValue>() {

            @Override
            public String f(String f, LAIFValue e) {
                return f + e.apply(lexicon, zipArgs(arguments), calls);
            }
        });
     }
     
     public Map<LAIFVariable,LAIFValue> zipArgs(List<LAIFValue> arguments) {
        final Iterator<LAIFVariable> iterator = param.iterator();
        final Iterator<LAIFValue> iterator2 = arguments.iterator();
        final HashMap<LAIFVariable, LAIFValue> rval = new HashMap<LAIFVariable, LAIFValue>();
        while(iterator.hasNext() && iterator2.hasNext()) {
            rval.put(iterator.next(),iterator2.next());
        }
        return rval;
     }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LAIFRule other = (LAIFRule) obj;
        if (this.resource != other.resource && (this.resource == null || !this.resource.equals(other.resource))) {
            return false;
        }
        if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
            return false;
        }
        if (this.param != other.param && (this.param == null || !this.param.equals(other.param))) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.resource != null ? this.resource.hashCode() : 0);
        hash = 23 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 23 * hash + (this.param != null ? this.param.hashCode() : 0);
        hash = 23 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Key: ").append(key).append(System.getProperty("line.separator")).append("URI: <").append(resource).append(">").append(System.getProperty("line.separator"));
        
        if(!param.isEmpty()) {
            sb.append("Params: ");
            for(LAIFVariable v : param) {
                sb.append(v.id()).append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append(System.getProperty("line.separator"));
        }
        sb.append("Rule:");
        for(LAIFValue val : value) {
            sb.append(" ").append(val);
        }
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
     
     
}

