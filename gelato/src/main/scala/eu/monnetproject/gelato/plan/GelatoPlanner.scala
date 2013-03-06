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

import eu.monnetproject.gelato._
import eu.monnetproject.gelato.statements.{Argument=>GArgument,_}
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import java.net.URI
import scala.collection.JavaConversions._

/**
 *
 * @author John McCrae
 */
class GelatoPlanner extends Planner {
  def plan(statement : GelatoStatement, lexicon : Lexicon) = {
    statement match {
      case DefiniteStatement(Nil,rhs) => plan(rhs,lexicon)
      case DefiniteStatement(lhs,rhs) => IfPlan(plan(rhs,lexicon),
                                                plan(lhs,lexicon))
      case QuestionStatement(target) => QuestionPlan(plan(target,lexicon))
    }
  }
  
  def plan(lhs : List[Mode], lexicon : Lexicon) : Plan = {
    lhs match {
      case Nil => EmptyPlan
      case x :: Nil => plan(x,lexicon)
      case x :: xs => ConjunctionPlan(
          plan(x,lexicon) :: plan(x, xs, lexicon)
        )
    }
  }
  
  def plan(last : Mode, lhs : List[Mode],lexicon : Lexicon) : List[Plan] = {
    lhs match {
      case x :: xs => {
          (last.predicate match {
              case UnaryPredicate(_,arg) => {
                  plan(arg,x.predicate,lexicon,x)
                }
              case BinaryPredicate(_,arg,_) => {
                  plan(arg,x.predicate,lexicon,x)
                }
            }) :: plan(x,xs,lexicon)
        }
      case Nil => Nil
    }
  }
  
  def plan(arg : GArgument, pred : Predicate, lexicon : Lexicon, mode : Mode) : Plan =  pred match {
    case UnaryPredicate(pred,arg2) if arg == arg2 => {
                       
        val (entry,frame) = resolvePred(pred,lexicon,1)
        PredicatePlan(Epsilon(plan(arg,lexicon)),
                      plan(lexicon,Nil,entry,frame,mode)
        )
      }
    case UnaryPredicate(pred,args) => {
        val (entry,frame) = resolvePred(pred,lexicon,1)
        PredicatePlan(plan(arg,lexicon),
                      plan(lexicon,Nil,entry,frame,mode)
                      )
      }
    case BinaryPredicate(pred,arg1,arg2) if arg == arg2 => {
        val (entry,frame) = resolvePred(pred,lexicon,2)
        PredicatePlan(Epsilon(plan(arg,lexicon)),
                      plan(lexicon,List(arg2),entry,frame,mode)
        ) 
      }
    case BinaryPredicate(pred,arg1,arg2) => {
        val (entry,frame) = resolvePred(pred,lexicon,2)
        PredicatePlan(plan(arg1,lexicon),
                      plan(lexicon,List(arg2),entry,frame,mode)
        )
      }
  }
  
  def plan(rhs : Mode, lexicon : Lexicon) : Plan = {
    rhs match {
      case Past(mode) => planPast(rhs.predicate,lexicon,rhs)
      case Future(mode) => planFuture(rhs.predicate,lexicon,rhs)
      case rhs =>  plan(rhs.predicate,lexicon,rhs)
    }
  }
  
  def plan(pred : Predicate, lexicon : Lexicon, mode : Mode) : Plan = {
    pred match {
      case UnaryPredicate(pred,arg) => {
          val (entry,frame) = resolvePred(pred,lexicon,1)
          PredicatePlan(plan(arg,lexicon),
                        plan(lexicon,Nil,entry,frame,mode)
          )
        }
      case BinaryPredicate(pred,arg1,arg2) => {
          val (entry,frame) = resolvePred(pred,lexicon,2)
          PredicatePlan(plan(arg1,lexicon), 
                        plan(lexicon,List(arg2),entry,frame,mode)
          )
        }
    }
  }
  
  def planPast(pred : Predicate, lexicon : Lexicon, mode : Mode) : Plan = {
    pred match {
      case UnaryPredicate(pred,arg) => {
          val (entry,frame) = resolvePred(pred,lexicon,1)
          PredicatePlan(plan(arg,lexicon),
                        PastPlan(plan(lexicon,Nil,entry,frame,mode))
          )
        }
      case BinaryPredicate(pred,arg1,arg2) => {
          val (entry,frame) = resolvePred(pred,lexicon,2)
          PredicatePlan(plan(arg1,lexicon), 
                        PastPlan(plan(lexicon,List(arg2),entry,frame,mode))
          )
        }
    }
  }
  
  def planFuture(pred : Predicate, lexicon : Lexicon, mode : Mode) : Plan = {
    pred match {
      case UnaryPredicate(pred,arg) => {
          val (entry,frame) = resolvePred(pred,lexicon,1)
          PredicatePlan(plan(arg,lexicon),
                        FuturePlan(plan(lexicon,Nil,entry,frame,mode))
          )
        }
      case BinaryPredicate(pred,arg1,arg2) => {
          val (entry,frame) = resolvePred(pred,lexicon,2)
          PredicatePlan(plan(arg1,lexicon), 
                        FuturePlan(plan(lexicon,List(arg2),entry,frame,mode))
          )
        }
    }
  }
  
  def plan(lexicon : Lexicon, args : List[GArgument], entry : LexicalEntry, frame : Option[Frame], mode : Mode) : Plan = {
    mode match {
      case Future(mode) => plan(lexicon,args,entry,frame,mode)
      case Past(mode) => plan(lexicon,args,entry,frame,mode)
      case _ : Is => VerbPlan(args map { arg => plan(arg,lexicon)},entry,frame getOrElse null)
      case _ : Not => NotVerbPlan(VerbPlan(args map { arg => plan(arg,lexicon)},entry,frame getOrElse null))
      case _ : May => MayVerbPlan(VerbPlan(args map { arg => plan(arg,lexicon)},entry,frame getOrElse null))
      case _ : MayNot => MayNotVerbPlan(VerbPlan(args map { arg => plan(arg,lexicon)},entry,frame getOrElse null))
      case _ : Must => MustVerbPlan(VerbPlan(args map { arg => plan(arg,lexicon)},entry,frame getOrElse null))
      case _ : MustNot => MustNotVerbPlan(VerbPlan(args map { arg => plan(arg,lexicon)},entry,frame getOrElse null))
    } 
  }
  
  def plan(arg : GArgument, lexicon : Lexicon) : Plan = {
    arg match {
      case Variable(name) => YouPlan
      case Constant(uri) => {
          val (entry,_) = resolvePred(uri,lexicon,0)
          EntryPlan(entry)
        }
    }
  }
  
  def resolvePred(pred : URI, lexicon : Lexicon, arity : Int) : (LexicalEntry,Option[Frame]) = {
    LemonModels.getEntryByReference(lexicon.getModel, pred) find {
      entry => lexicon.hasEntry(entry)
    } match {
      case Some(entry) => {
          entry.getSynBehaviors.find {
            frame => {
              frame.getSynArgs.values().flatMap(x => x).size == arity
            } 
          } match {
            case Some(frame) => (entry,Some(frame))
            case None => if(arity == 0) {
                (entry,None)
              } else {
                throw new UnresolvablePredicateException("No frame with correct arity for "+entry.getURI)
              }
          }  
        }
      case None => throw new UnresolvablePredicateException("No entry with reference " + pred)
    }
  }
}

class UnresolvablePredicateException(msg : String) extends RuntimeException(msg)