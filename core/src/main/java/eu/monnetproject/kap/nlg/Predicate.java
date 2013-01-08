/****************************************************************************
/* Copyright (c) 2011, Monnet Project
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
 ********************************************************************************/
package eu.monnetproject.kap.nlg;

import java.net.URI;
import java.util.List;

/**
 * An atomic predicate
 * 
 * @author John McCrae
 */
public final class Predicate {
    private final URI predicate;
    private final List<Argument> arguments;
    private final Modality modality;

    public Predicate(URI predicate, List<Argument> arguments, Modality modality) {
        this.predicate = predicate;
        this.arguments = arguments;
        this.modality = modality;
    }

    /**
     * The arguments of the predicate
     * @return A (non-null) list
     */
    public List<Argument> getArguments() {
        return arguments;
    }

    /**
     * Get the modality of the statement
     * @return  The modality
     */
    public Modality getModality() {
        return modality;
    }

    /**
     * The predicate
     * @return The URI indicating the predicate
     */
    public URI getPredicate() {
        return predicate;
    }

    @Override
    public String toString() {
        return "Predicate{" + "predicate=" + predicate + ", arguments=" + arguments + ", modality=" + modality + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Predicate other = (Predicate) obj;
        if (this.predicate != other.predicate && (this.predicate == null || !this.predicate.equals(other.predicate))) {
            return false;
        }
        if (this.arguments != other.arguments && (this.arguments == null || !this.arguments.equals(other.arguments))) {
            return false;
        }
        if (this.modality != other.modality && (this.modality == null || !this.modality.equals(other.modality))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.predicate != null ? this.predicate.hashCode() : 0);
        hash = 89 * hash + (this.arguments != null ? this.arguments.hashCode() : 0);
        hash = 89 * hash + (this.modality != null ? this.modality.hashCode() : 0);
        return hash;
    }
    
    
}
