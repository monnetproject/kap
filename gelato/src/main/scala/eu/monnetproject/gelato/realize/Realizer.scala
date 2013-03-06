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
package eu.monnetproject.gelato.realize;

import eu.monnetproject.gelato._
import eu.monnetproject.gelato.plan._
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.util._
import scala.collection.JavaConversions._
import scala.math._

/**
 *
 * @author John McCrae
 */

class Realizer(val lingOnto : LinguisticOntology, val ruleGroups : List[RealizationRuleGroup]) {
  val log = Logging.getLogger(this)
  
  def realize(plan : Plan) : String = {
    realize(tree(plan))
  }
  
  def tree(plan : Plan) : Tree = {
    log.config("TREE START")
    treeResult(plan) match {
      case TreeingResult(Some(tree : Tree),_) => tree
      case TreeingResult(None,bestTree) => {
          log.warning("Best tree:" + bestTree)
          throw new RealizationException("Could not make tree")
        }
    }
  }
  
  def treeResult(plan : Plan) : TreeingResult = tree(RealizationState(tree, form, plan, Head("S",Nil)))
  
  def tree(state : RealizationState) : TreeingResult = {
    // Find a rule group with the correct head
    ruleGroups find {
      rule => state.head.compatible(rule.head)
    } match {
      case Some(ruleGroup) => {
          var bestTree : Tree = null
          // For each possible rule
          for(rule <- ruleGroup.rules if rule.planType == state.plan.getClass.getSimpleName.takeWhile(_ != '$') &&
              rule.canPlan(state.plan)) {
            // Generate tree
            val treeOpt = rule.elem.toTree(state.copy(guards=rule.guards))
            // Tree was built 
            if(treeOpt.result != None) {
              return treeOpt
            } else {
              if(bestTree == null || treeOpt.bestTree.size > bestTree.size) {
                bestTree = treeOpt.bestTree
              }
            }
          }
          if(bestTree == null) {
            bestTree = FailNode("NOELEM" + state.plan.getClass.getSimpleName + "/" +state.head)
          }
          return TreeingResult(None,bestTree)
        }
      case None => {
          log.config("Could not find rule group for " + state.head)
          return TreeingResult(None,FailNode("NORULEGRP"))
        }
    }
  }
  
  def realize(tree : Tree) : String = {
    val sb = new StringBuilder()
    def flattenTree(tree2 : Tree) { 
      tree2 match {
        case TreeNode(Head(_,_,false),subelems) => {
            for(subelem <- subelems) {
              flattenTree(subelem)
            }
          }
        case TreeNode(Head(_,_,true),subelems) => 
        case LitNode(value) => sb.append(value).append(" ")
        case EntryNode(form) => sb.append(form).append(" ")
      }
    }
    flattenTree(tree)
    sb.toString.dropRight(1)
  }
  
  private def toPropMap(guards : List[Guard]) = {
    val rv = new java.util.HashMap[Property,java.util.Collection[PropertyValue]]()
    for(PropGuard(key,value) <- guards) {
      val prop = lingOnto.getProperty(key)
      val pv = lingOnto.getPropertyValue(value)
      if(!rv.containsKey(prop)) {
        rv.put(prop, new java.util.LinkedList[PropertyValue]())
      }
      rv.get(prop).add(pv)
    }
    rv;
  }
  
  private def resolveHead(entry : LexicalEntry, headForm : LexicalForm) : String = {
    if(entry.getDecompositions.isEmpty) {
      "ERROR:emptyDecomp!?"
    } else {
      val decomp = entry.getDecompositions.iterator.next
      val sb = new StringBuilder()
      for(comp <- decomp) {
        if(comp.getElement == entry.getHead.getElement) {
          log.info("using inflected head")
          sb.append(headForm.getWrittenRep.value)
        } else {
          val s = LemonModels.resolveForm(comp.getElement, comp.getPropertys)
          if(s == null) {
            sb.append(comp.getElement.getCanonicalForm.getWrittenRep.value)
          } else {
            sb.append(s.getWrittenRep.value)
          }
        }
        sb.append(" ")
      }
      sb.toString
    }
  }
  
  protected def form(entry : LexicalEntry, props : List[Guard]) : Option[String] = {
    if(!props.exists(_.isInstanceOf[PropGuard])) {
      for(prop <- props) {
        prop match {
          case RegexGuard(regex,positive) => {
              if(entry.getCanonicalForm.getWrittenRep.value.matches(regex) ^ positive) {
                return None
              }
            }
          case _ => 
        }
      }
      return Some(entry.getCanonicalForm.getWrittenRep.value)
    } else {
      (entry.getForms map {
          form => {
            val failed = props filterNot {
              case PropGuard(key,value) => {
                  entry.getProperty(lingOnto.getProperty(key)).contains(lingOnto.getPropertyValue(value))
                }
              case RegexGuard(regex,positive) => {
                  form.getWrittenRep.value.matches(regex) ^ !positive
                }
              case FrameGuard(frame) => {
                  true
                }
            }
            if(failed.forall(_.isInstanceOf[PropGuard])) {
              val genForm = LemonModels.resolveForm(entry,toPropMap(failed))
              if(genForm == null) {
                if(entry.getHead != null) {
                  val genForm2 = LemonModels.resolveForm(entry.getHead.getElement, toPropMap(failed))
                  if(genForm2 == null) {
                    None
                  } else {
                    Some(resolveHead(entry,genForm2))
                  }
                } else {
                  None
                }
              } else {
                Some(genForm.getWrittenRep.value)
              }
            } else {
              if(failed.size == 0) {
                Some(form.getWrittenRep.value)
              } else {
                None
              }
            }
          }
        }) foreach {
        case Some(form) => return Some(form)
        case None => 
      }
      return None
    }
  }
  
  protected def canForm(entry : LexicalEntry) = entry.getCanonicalForm.getWrittenRep.value
  
  protected implicit def toPVal(name : String) = lingOnto.getPropertyValue(name)
  protected implicit def toProp(name : String) = lingOnto.getProperty(name)
  protected implicit def hasProp(entry : LemonElement) = new {
    def has (prop : Property, propVal : PropertyValue) = entry.getProperty(prop) contains propVal
  }
}

case class TreeingResult(val result : Option[Tree], val bestTree : Tree)

// Grammar elements from file

case class RealizationRuleGroup(val head : Head, val rules : List[RealizationRule])

case class RealizationRule(val planType : String, val guards : List[Guard], val elem : RealizationElem, val frame : Option[String]) {
  def canPlan(plan : Plan) : Boolean = {
    frame match {
      case Some(trgFrame) => {
          plan.frame match {
            case Some(actFrame) =>  {
                val lastIndex = max(actFrame.toString.lastIndexOf("#"),actFrame.toString.lastIndexOf("/"))
                if(lastIndex > 0) {
                  actFrame.toString.drop(lastIndex+1) == trgFrame
                } else {
                  false
                }
              }
            case None => false
          }
        }
      case None => true //plan.frame == None
    }
  }
} 

trait RealizationElem {
  def toTree(state : RealizationState) : TreeingResult
  def childCount : (Int,Int)
}

case class NonTerminalElem(val head : Head, val subelems : List[RealizationElem]) extends RealizationElem {
  val log = Logging.getLogger(this)
  def toTree(state : RealizationState) = {
    log.config(state.plan.getClass.getSimpleName+"/"+state.head)
    if(head.compatible(state.head)) {
      val (minChildren,maxChildren) = childCount
      // Check rule can match
      if((state.plan.children.size >= minChildren &&
          state.plan.children.size <= maxChildren) || !state.checkChildren) {
        // Build all subtrees
        val subTrees = for(subelem <- subelems) yield { 
          val newHead = subelem match {
            case NonTerminalElem(nh,_) => nh
            case _ => head
          }
          val st = subelem.toTree(state.copy(head=newHead,checkChildren=false)) 
          log.config(st.toString)
          st
        }
        // Confirm all subtrees were built
        if(subTrees.forall(_.result.isInstanceOf[Some[_]])) {
          val t = TreeNode(head, subTrees map { _.result.get })
          TreeingResult(Some(t),t)
        } else {
          val t = TreeNode(head, subTrees map {
              tr => tr.result match {
                case Some(n) => n
                case None => tr.bestTree
              }
            })
          log.config("fail: one subelem did not generate a tree")
          TreeingResult(None,t)
        }
      } else {
        log.config("fail: wrong number of children")
        TreeingResult(None,FailNode("CHILDREN:"+minChildren+"<"+state.plan.children.size+"<"+maxChildren))
      }
    } else {
      log.config("incompatible head " + state.head + " vs " + head)
      TreeingResult(None,FailNode(state.head+"!="+head))
    }
  }
  
  def childCount : (Int,Int) = {
    def flattenChildren(se : List[RealizationElem]) : List[RealizationElem] = se flatMap {
      case NonTerminalElem(_,se2) => flattenChildren(se2)
      case x => Some(x)
    }
    var m = 0
    var x = 0
    for(subelem <- flattenChildren(subelems).toSet[RealizationElem]) {
      val (i,j) = subelem.childCount 
      if(i >= 0 && m >= 0) {
        m += i
      } else {
        m = Integer.MIN_VALUE
      }
      if(j >= 0 && x >= 0) {
        x += j
      } else {
        x = -1
      }
    }
    (m,if(x < 0) { Integer.MAX_VALUE } else { x })
  }
}

case class LiteralElem(val value : String) extends RealizationElem {
  def toTree(state : RealizationState) = TreeingResult(Some(LitNode(value)),LitNode(value))
  def childCount = (0,0)
}

case class NVariableElem(val idx : Int) extends RealizationElem {
  def toTree(state : RealizationState) = if(idx <= state.plan.children.size) {
    state.realizer(state.copy(plan=state.plan.children(idx-1),checkChildren=true))
  } else {
    TreeingResult(None,FailNode("OUTOFRANGE:"+idx))
  }
  def childCount = (1,1)
}

case class TailVariableElem() extends RealizationElem {
  def toTree(state : RealizationState) = state.plan.tailPlan match {
    case Some(tp) => state.realizer(state.copy(plan=tp,checkChildren=true))
    case None => TreeingResult(None,FailNode("NOTAIL"))
  }
  def childCount = (1,Integer.MAX_VALUE)
}

/*case class SelfVariableElem() extends RealizationElem {
 def toTree(state : RealizationState) = state.realizer(state)
 // Child counting is deferred
 def childCount = (Integer.MIN_VALUE,Integer.MAX_VALUE)
 }*/

 case class EntryVariableElem(variant : Option[String] = None) extends RealizationElem {
    def toTree(state : RealizationState) = { 
      def tt(e : LexicalEntry) = {
        state.forms(e,state.guards) match {
          case Some(f) => TreeingResult(Some(EntryNode(f)),EntryNode(f))
          case None => TreeingResult(None,FailNode("NOFORM"))
        }
      }
      state.plan.entry match {
        case Some(e) => variant match {
            case Some(variant) => {
                e.getLexicalVariants find {
                  case (lv,_) => lv.getURI.getFragment == variant
                } match {
                  case Some((_,le)) => tt(le.head)
                  case None => TreeingResult(None,FailNode("NOVAR:"+variant))
                }
              }
            case None => {
                tt(e)
              }
          }
        case None => TreeingResult(None,FailNode("NOPLANENTRY"))
      }
    }
    def childCount = (0,0)
  }

 case class MarkerVariableElem(synArg : String) extends RealizationElem {
    def toTree(state : RealizationState) = {
      state.plan.marker(synArg) match {
        case Some(m) => TreeingResult(Some(LitNode(m)),LitNode(m))
        case None => TreeingResult(None,FailNode("NOMARKER:"+synArg))
      }
    }
    def childCount = (0,0)
  }

 trait Guard

 case class PropGuard(val key : String, val value : String) extends Guard

 case class RegexGuard(val regex : String, val positive : Boolean) extends Guard

 case class FrameGuard(val frameName : String) extends Guard

 case class Head(val value : String, val opts : List[String], val virtual : Boolean = false) {
    def compatible(h : Head) = value == h.value && (
      opts zip h.opts forall { case (l,r) => l == r  || l == "*"}
    ) && opts.size >= h.opts.size
    override def toString = value + (for(o <- opts) yield {
        ":" + o
      }).mkString("") +
    (if(virtual) { 
        "!" 
      } else {
        ""
      })
  
    def nonVirtual = Head(value,opts,false)
  }

// Exception

 class RealizationException(msg : String) extends RuntimeException(msg)

// Trees

 trait Tree {
    def size : Int
  }

 case class TreeNode(val head : Head, val subelems : List[Tree]) extends Tree {
    override def toString = "("+ head +
    (for(subelem <- subelems) yield {
        " " + subelem
      }).mkString("") + ")"
    def size = 1 + (subelems map (_.size) sum)
  }

 case class LitNode(val value : String) extends Tree {
    override def toString = "\""+value+"\""
    def size = 1
  }

 case class EntryNode(val form : String) extends Tree {
    override def toString = "<"+form+">"
    def size = 1
  }

 case class FailNode(msg : String) extends Tree {
    override def toString = "#"+msg+"#"
    def size = 1
  }

// State of realization

 case class RealizationState(val realizer : (RealizationState) => TreeingResult,
                             val forms : (LexicalEntry,List[Guard]) => Option[String],
                             val plan : Plan, val head : Head,
                             val guards : List[Guard] = Nil,
                             val checkChildren : Boolean = true) {
    def copy(realizer : (RealizationState) => TreeingResult = realizer, 
             forms : (LexicalEntry,List[Guard]) => Option[String] = forms,
             plan : Plan = plan, head : Head = head, 
             guards : List[Guard] = guards,
             checkChildren : Boolean = checkChildren) = {
      RealizationState(realizer,forms,plan,head,guards,checkChildren)
    }
    def brief = plan.getClass.getSimpleName+"/"+head
  } 
