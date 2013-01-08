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

/**
 * A modality
 * 
 * @author John McCrae
 */
public final class Modality {
    private final boolean positive;
    private final ModalityType modalityType;
    private final TemporalType temporalType;

    public Modality(boolean positive, ModalityType modalityType, TemporalType temporalType) {
        this.positive = positive;
        this.modalityType = modalityType;
        this.temporalType = temporalType;
    }

    /**
     * Get the modality type
     * @return The modality type
     */
    public ModalityType getModalityType() {
        return modalityType;
    }

    /**
     * Is the predicate positive
     * @return true if the predicate is positive
     */
    public boolean isPositive() {
        return positive;
    }

    /**
     * Get the temporal type
     * @return The temporal type
     */
    public TemporalType getTemporalType() {
        return temporalType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Modality other = (Modality) obj;
        if (this.positive != other.positive) {
            return false;
        }
        if (this.modalityType != other.modalityType) {
            return false;
        }
        if (this.temporalType != other.temporalType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.positive ? 1 : 0);
        hash = 97 * hash + (this.modalityType != null ? this.modalityType.hashCode() : 0);
        hash = 97 * hash + (this.temporalType != null ? this.temporalType.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Modality{" + "positive=" + positive + ", modalityType=" + modalityType + ", temporalType=" + temporalType + '}';
    }
}
