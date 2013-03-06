/****************************************************************************
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
 ********************************************************************************/

package eu.monnetproject.gelato.realize

import scala.util.parsing.combinator._
import java.util.regex._

/**
 * 
 * @author John McCrae
 */
class GrammarParser extends JavaTokenParsers {
  
  def document = comment.* ~> ruleBlock.*
  def ruleBlock = (head <~ "{" <~ comment.*) ~ ((rule <~ comment.*).* <~ "}") ^^ {
    case x ~ y => RealizationRuleGroup(x,y)
  }
  def head = "\\w+".r ~ (":" ~> "(\\w+|%[0-9]+|\\*)".r).* ~ "!".?  ^^ {
    case x ~ y ~ z => Head(x,y,z!=None)
  }
  def rule = (ruleHead <~ "=>") ~ (ruleElem | variable) ^^ {
    case x ~ fg ~ y ~ z => RealizationRule(x,y,z,fg)
  }
  def ruleHead = "\\w+".r ~ frameGuard.? ~ ruleGuard 
  def ruleGuard = ("?" ~> properties).?  ~ regexGuard.? ^^ {
    case Some(y) ~ z => (y map { case (a, b) => PropGuard(a,b) }) ::: z.toList
    case None ~ z => z.toList
  }
  def regexGuard = ( ("=~" ~ "/[^/]*/".r) | ("!~" ~ "/[^/]*/".r) ) ^^ {
    case "=~" ~ r => RegexGuard(r.drop(1).dropRight(1),true)
    case "!~" ~ r => RegexGuard(r.drop(1).dropRight(1),false)
  }
  def frameGuard = ( "[" ~> "\\w+".r <~ "] ") ^^ {
    case x => x
  }
  def variable = regex("\\$(\\*|[0-9]+)".r) ^^ {
    case "$*" => TailVariableElem()
    case x => NVariableElem(x.drop(1).toInt)
  } | ("$_" ~> ("." ~> "\\w+".r).?) ^^ {
    case x => EntryVariableElem(x)
  } | ("\\$M\\w+".r) ^^ {
    case x => MarkerVariableElem(x.drop(2))
  }
  def properties : Parser[List[(String,String)]] = property ~ ("and" ~> properties).? ^^ {
    case x ~ Some(xs) => x :: xs
    case x ~ None => x :: Nil
  }
  
  def property = ("\\w+".r <~ "=") ~ "\\w+".r ^^ {
    case x ~ y => (x,y)
  } 
  def ruleElem : Parser[RealizationElem] = "(" ~> (head ~ (ruleElem | variable | lit).*) <~ ")" ^^ {
    case x ~ y => NonTerminalElem(x,y)
  }
  def lit = "\"" ~> "[^\"]*".r <~ "\"" ^^ {
    case x => LiteralElem(removeUnicode(x))
  }
  def comment = "//[^\n]*\n".r
  
  private def removeUnicode(str : String) = {
    val matcher = Pattern.compile("\\\\u([0-9a-f]{4})").matcher(str)
    var rv = new StringBuilder(str)
    var offset = 0;
    while(matcher.find) {
      rv.replace(matcher.start-offset, matcher.end-offset, "" + Integer.parseInt(matcher.group(1),16).asInstanceOf[Char])
      offset += 5
    }
    rv.toString
  }
}

object GrammarParser {
  
  def read(in : java.io.Reader) : List[RealizationRuleGroup] = {
    val parser = new GrammarParser()
    parser.parseAll(parser.document,in) match {
      case parser.Success(x,_) => validate(x)
      case parser.Failure(x,in) => throw new GELATORealizerParseException(x+ " @ l" + in.pos.line + " c"+in.pos.column)
      case parser.Error(x,in) =>  throw new GELATORealizerParseException(x+ " @ l" + in.pos.line + " c"+in.pos.column)
    }
  }
  
  def validate(ruleGroups : List[RealizationRuleGroup]) = {
    var outs = Set[Head]()
    var ins = Set[Head]()
    def buildOuts(elem : RealizationElem, head : Head) {
      elem match {
        case NonTerminalElem(head, subelems) => for(subelem <- subelems) {
            buildOuts(subelem,head)
          }
        case NVariableElem(_) => outs += head.nonVirtual
        case TailVariableElem() => outs += head.nonVirtual
        case _ =>
      }
    }
    
    for(ruleGrp <- ruleGroups) {
      var done = Map[(String,List[Guard],Option[String]),Head]()
      for(rule <- ruleGrp.rules) {
        // Check not invalid head
        rule.elem match {
          case NonTerminalElem(head,_) => {
              if(!head.compatible(ruleGrp.head)) {
                throw new GELATORealizerParseException(head + " in group " + ruleGrp.head)
              }
          
              ins += head.nonVirtual
              
              val key = (rule.planType,rule.guards,rule.frame)
              if(done.contains(key) && done.get(key) == head) {
                throw new GELATORealizerParseException("Duplicate head " + key + " => " + head)
              }
              done += (key -> head)
            }
        }
        // While we are here we will build the outs
        buildOuts(rule.elem,ruleGrp.head)
      }
    }
    if(outs != ins) {
      if(!(outs -- ins).isEmpty) {
        throw new GELATORealizerParseException("Some rules may not be applicable: using the following symbols " + (outs--ins))
      }
      if(!(ins -- outs).isEmpty) {
        throw new GELATORealizerParseException("Some rules are never used: " + (ins--outs))
      }
    }
    ruleGroups
  }
}

class GELATORealizerParseException(msg : String) extends RuntimeException(msg)
