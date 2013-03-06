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
package eu.monnetproject.gelato.plan;

import eu.monnetproject.lemon.model._
import eu.monnetproject.gelato.statements._
import java.net.URI
import scala.collection.JavaConversions._

/**
 *
 * @author John McCrae
 */
trait Plan {
  def children : List[Plan]
  def tailPlan : Option[Plan] = None
  def entry : Option[LexicalEntry] = None
  def frame : Option[URI] = None
  def marker(arg : String) : Option[String] = None
}

  
case class PredicatePlan(  
  val subj : Plan,
  val verb : Plan
) extends Plan {
  def children = subj :: verb :: Nil
  override def frame = verb.frame
  override def marker(arg : String) = verb.marker(arg)
}

case class VerbPlan(
  val obj : List[Plan],
  _entry : LexicalEntry,
  _frame : Frame) extends Plan {
  override def entry = Some(_entry) 
  def children = obj
  override def tailPlan = Some(ObjectPlan(obj))
  override def frame = if(_frame != null) {
    _frame.getTypes().headOption
  } else {
    None
  }
  override def marker(arg : String) = if(_frame != null) {
    _frame.getSynArgs() find {
      case (a,_) => a.getURI.getFragment == arg
    } match {
      case Some((_,ms)) => ms.head.getMarker match {
          case e : LexicalEntry => Option(e.getCanonicalForm.getWrittenRep.value)
          case _ => None
      }
      case None => None
    }
  } else {
    None
  }
}

case class PastPlan(val plan : Plan) extends Plan {
  def children = List(plan)
  override def frame = plan.frame
  override def marker(arg : String) = plan.marker(arg)
}

case class FuturePlan(val plan : Plan) extends Plan {
  def children = List(plan)
  override def frame = plan.frame
  override def marker(arg : String) = plan.marker(arg)
}

case class ObjectPlan(val args : List[Plan]) extends Plan {
  def children = args
}

case class NotVerbPlan(val verbPlan : VerbPlan) extends Plan {
  def children = List(verbPlan)
  override def frame = verbPlan.frame
  override def marker(arg : String) = verbPlan.marker(arg)
}

case class MustVerbPlan(val verbPlan : VerbPlan) extends Plan {
  def children = List(verbPlan)
  override def frame = verbPlan.frame
  override def marker(arg : String) = verbPlan.marker(arg)
}

case class MustNotVerbPlan(val verbPlan : VerbPlan) extends Plan {
  def children = List(verbPlan)
  override def frame = verbPlan.frame
  override def marker(arg : String) = verbPlan.marker(arg)
}

case class MayVerbPlan(val verbPlan : VerbPlan) extends Plan {
  def children = List(verbPlan)
  override def frame = verbPlan.frame
  override def marker(arg : String) = verbPlan.marker(arg)
}

case class MayNotVerbPlan(val verbPlan : VerbPlan) extends Plan {
  def children = List(verbPlan)
  override def frame = verbPlan.frame
  override def marker(arg : String) = verbPlan.marker(arg)
}

case class IfPlan(
  val result : Plan,
  val consequence : Plan
) extends Plan {
  def children = List(result,consequence)
}

case class QuestionPlan(
  val question : Plan) extends Plan {
  def children = List(question)
  }

case class EntryPlan(
  _entry : LexicalEntry
) extends Plan {
  def children = Nil
  override def entry = Some(_entry)
}

case class ConjunctionPlan(
  val plans : List[Plan]
) extends Plan {
  def children = plans
  override def tailPlan = if(plans.tail.isEmpty) {
    None
  } else {
    Some(ConjunctionPlan(plans.tail))
  }
}

case class Epsilon(val ref : Plan) extends Plan { 
  override def toString = "\u03b5\u2192" + ref
  def children = List(ref)
} 

object EmptyPlan extends Plan {
  override def toString = "<empty>"
  def children = Nil
}

object YouPlan extends Plan { 
  override def toString = "you" 
  def children = Nil
}
