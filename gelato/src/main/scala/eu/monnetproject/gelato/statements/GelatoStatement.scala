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
package eu.monnetproject.gelato.statements

import java.net.URI
import scala.util.parsing.combinator._

/**
 * A gelato statement
 *
 * @author John McCrae
 */
trait GelatoStatement

case class DefiniteStatement(val lhs : List[Mode], val rhs : Mode) extends GelatoStatement {
  override def toString = lhs.mkString(", ") + " -> " + rhs
}

case class QuestionStatement(val target : Mode) extends GelatoStatement {
  override def toString = "?" + target
}
  
sealed trait Mode {
  def predicate : Predicate
}
 
case class Past(val mode : Mode) extends Mode {
  def predicate = mode.predicate
}

case class Future(val mode : Mode) extends Mode {
  def predicate = mode.predicate
}

case class Is(val predicate : Predicate) extends Mode {
  override def toString = predicate.toString
}
  
case class Not(val predicate : Predicate) extends Mode {
  override def toString = "!" + predicate
}
  
case class May(val predicate : Predicate) extends Mode {
  override def toString = "%" + predicate
}
  
case class MayNot(val predicate : Predicate) extends Mode {
  override def toString = "%!" + predicate
}
  
case class Must(val predicate : Predicate) extends Mode {
  override def toString = "#"+predicate
}
  
case class MustNot(val predicate : Predicate) extends Mode {
  override def toString = "#!"+predicate
}
  
sealed trait Predicate
  
case class UnaryPredicate(pred : URI, arg : Argument) extends Predicate {
  override def toString = "<"+pred+">("+arg+")"
}
  
case class BinaryPredicate(pred : URI, arg1 : Argument, arg2 : Argument) extends Predicate {
  override def toString = "<"+pred+">("+arg1+","+arg2+")"
}
  
sealed trait Argument
  
case class Variable(name : Symbol) extends Argument {
  override def toString = "?"+name.name
}
  
case class Constant(individual : URI) extends Argument {
  override def toString = "<"+individual+">"
}
  
object GelatoParser extends JavaTokenParsers {
    
  def stat = ((mode*) <~ " -> ") ~ (mode*)
    
  def mode = 
    "%" ~> "!" ~> pred ^^ { case p => MayNot(p) } |
  "#" ~> "!" ~> pred ^^ { case p => MustNot(p) } |
  "!" ~> pred ^^ { case p => Not(p) } |
  "%" ~> pred ^^ { case p => May(p) } |
  "#" ~> pred ^^ { case p => Must(p) } |
  pred ^^ { case p => Is(p) }
    
  def pred = (pred2 <~ "(") ~ (arg <~  ")") ^^ { case p ~ a => UnaryPredicate(p,a) } |
  (pred2 <~ "(") ~ (arg <~  ",") ~ (arg <~ ")") ^^ { 
    case p ~ a1 ~ a2 => BinaryPredicate(p,a1,a2)
  }
      
  def pred2 = "<" ~> ("""[^>]+""") <~ ">" ^^ { case uri => URI.create(uri)}
    
  def arg = "?" ~> ("""\w+""") ^^ { case sym => Variable(Symbol(sym))} |
  "<" ~> ("""[^>]+""") <~ ">" ^^ { case uri => Constant(URI.create(uri))}
}

