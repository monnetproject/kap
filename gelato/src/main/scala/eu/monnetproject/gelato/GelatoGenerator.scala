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

package eu.monnetproject.gelato

import eu.monnetproject.kap.nlg._
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model.{Argument=>_,_}
import eu.monnetproject.gelato.plan._
import eu.monnetproject.gelato.realize._
import eu.monnetproject.gelato.statements.{Predicate=>GPredicate,Argument=>GArgument,_}
import eu.monnetproject.util._
import eu.monnetproject.util.Logging
import java.io._
import java.util.{Set=>JSet,List=>JList}
import org.osgi.framework._
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class GelatoGenerator(lingOnto : LinguisticOntology) extends ComplexGenerator {
  private val log = Logging.getLogger(this)
  private val gelatoGrammarFile = "(...?)\\.gram\\.gelato".r
  
  private lazy val planner = new GelatoPlanner()
  private var realizers = Map[String,Realizer]()
    
  
  def generate(rules : JSet[Rule], lexicon : Lexicon) : String = {
    if(lexicon.getLanguage.equals("en")) {
      ""
    } else {
      throw new IllegalArgumentException("Only English supported")
    }
  }
  
  def generate(pred : Predicate, question : Boolean, lexicon : Lexicon) : String = {
    val plan = planner.plan(pred2Statement(pred,question), lexicon)
    log.info(lexicon.getLanguage)
    if(!realizers.contains(lexicon.getLanguage)) {
      val resource = ResourceFinder.getResourceAsReader("grammars/"+lexicon.getLanguage + ".gram.gelato")
      if (resource != null) {
        realizers += (lexicon.getLanguage -> new Realizer(lingOnto,GrammarParser.read(resource)))
      }
    }
    realizers.get(lexicon.getLanguage).getOrElse({
        throw new IllegalArgumentException("Unsupported language " + lexicon.getLanguage)
      }).realize(plan)
  }
  
  def pred2Statement(pred : Predicate, question : Boolean) : GelatoStatement = {
    val gpred = pred.getArguments.size match {
      case 1 => UnaryPredicate(pred.getPredicate,pred.getArguments.get(0))
      case 2 => BinaryPredicate(pred.getPredicate,pred.getArguments.get(0),pred.getArguments.get(1))
      case _ => throw new IllegalArgumentException()
    }
    val mode2 = pred.getModality.getModalityType match {
      case ModalityType.compulsory => if(pred.getModality.isPositive) {
          Must(gpred)
        } else {
          MustNot(gpred)
        }
      case ModalityType.definite => if(pred.getModality.isPositive) {
          Is(gpred)
        } else {
          Not(gpred)
        }
      case ModalityType.possible => if(pred.getModality.isPositive) {
          May(gpred)
        } else {
          MayNot(gpred)
        }
    }
    val mode = pred.getModality.getTemporalType match {
      case TemporalType.past => Past(mode2)
      case TemporalType.future => Future(mode2)
      case _ => mode2
    }
    if(question) {
      QuestionStatement(mode)
    } else {
      DefiniteStatement(Nil,mode)
    }
  }
  
  private implicit def arg2GArg(arg : Argument) : GArgument = arg match {
    case Argument.YOU => Variable('you)
    case uriArg : URIArg => Constant(uriArg.getValue)
  }
  
  def getPlanner(lang : String) = realizers.get(lang)
}
