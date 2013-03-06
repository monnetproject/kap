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
package eu.monnetproject.gelato.demo

import aQute.bnd.annotation.component.{Component,Reference,Activate}
import eu.monnetproject.gelato._
import eu.monnetproject.kap.nlg.{Argument=>GArg,_}
import eu.monnetproject.lang.Language
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.generator._
import eu.monnetproject.lemon.model.{Component=>_,Text=>_,_}
import eu.monnetproject.ontology._
import eu.monnetproject.util._
import java.net._
import javax.servlet._
import javax.servlet.http._
import scala.collection.JavaConversions._
import scala.math._
import scala.xml._

/**
 * 
 * @author John McCrae
 */
@Component(provide=Array[java.lang.Class[_]](classOf[HttpServlet]))
class MatrixServlet(ontoSerializer : OntologySerializer, generator : LemonGenerator, lingOnto : LinguisticOntology) extends HttpServlet {
  private val log = Logging.getLogger(this)
  private val gelato = new GelatoGenerator(lingOnto)
  
  def getPath = "/gelato-matrix"
  
  @Activate def activate {
    log.info("Starting gelato matrix")
  }
  
  private val CLASS = 0
  private val PROP = 1
  private val IND = 2
  
  override def service(req : HttpServletRequest, resp : HttpServletResponse) {
    log.info("Got request")
    req.getParameter("label") match {
      case null => {
          log warning "No label"
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"No label")
        }
      case label => {
          val semanticType = req.getParameter("semanticType") match {
            case "class" => CLASS
            case "property" => PROP
            case "individual" => IND
            case _ => -1
          }
          if(semanticType >= 0) {
            req.getParameter("language") match {
              case null => {
                  log warning "no language"
                  resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"No language")
                }
              case l => {
                  val out = resp.getWriter
                  log.info("Sending response " + Language.get(l))
                  val matrix = makeMatrix(label,semanticType,Language.get(l))
                  log.info(matrix.toString)
                  out write matrix.toString
                  out flush()
                  resp setContentType "text/xml"
                }
            }
          } else {
            log warning "bad semantic type"
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"missing or bad semantic type")
          }
        }
    }
  }
  
  private val positivity = List(true,false)
  
  implicit def pimpElem(elem : Elem) = new {
    def %%(attrs : (String,String)*) = {
      var elem2 = elem
      var attr2 : MetaData = Null
      for(attr <- attrs) {
        attr2 = Attribute(None,attr._1,Text(attr._2),attr2)
        elem2 = elem % attr2
      }
      elem2
    }
      
  }
  
  private def makeMatrix(label : String, semanticType : Int, lang : Language) = {
    log.info("lang="+lang)
    val onto = makeOntology(label,semanticType,lang)
    val uri = URI.create("file:input#"+URLEncoder.encode(label, "UTF-8"))
    val config = new LemonGeneratorConfig()
    val model = LemonSerializer.newInstance().create(null)
    config.inferLang = false
    config.unlanged = lang
    generator.doGeneration(model, onto, config)
    val lexicon = model.getLexica.head
    val xmlRv = <gelato>{
        for{ modality <- ModalityType.values() 
            question <- positivity.reverse
            positive <- positivity 
            temporal <- TemporalType.values().reverse 
            if temporal.toString != "habitual" } yield {
          <result>{
              val pred = new Predicate(uri,List(GArg.YOU), new Modality(positive,modality,temporal))
              try {
                gelato.generate(pred, question, lexicon) 
              } catch {
                case x : Exception => x.getMessage 
              }
            }</result> %% ("modality" -> modality.toString, "question" -> question.toString, "positive" -> positive.toString, "temporal" -> temporal.toString)
        }
      }</gelato>
    xmlRv
  }
  
  private val random = new java.util.Random()
  
  private def makeOntology(label : String, semanticType : Int, lang : Language) : Ontology = {
    val onto = ontoSerializer.create(URI.create("file:input#ontology" + abs(random.nextLong())))
    val factory = onto.getFactory
    val uri = URI.create("file:input#"+URLEncoder.encode(label, "UTF-8"))
    val entity = semanticType match {
      case CLASS => {
          val clazz = factory.makeClass(uri)
          onto.addClass(clazz)
          clazz
        }
      case PROP => {
          val prop = factory.makeObjectProperty(uri)
          onto.addObjectProperty(prop)
          prop
        }
      case IND => {
          val ind = factory.makeIndividual(uri)
          onto.addIndividual(ind)
          ind
        }
    }
    entity.addAnnotation(factory.makeAnnotationProperty(URI.create("""http://www.w3.org/2000/01/rdf-schema#label""")),
                         factory.makeLiteralWithLanguage(label, lang.toString))
    onto
  }
  
}

import eu.monnetproject.framework.services.Services._

class MatrixServletApp extends MatrixServlet(
  get(classOf[OntologySerializer]),
  get(classOf[LemonGenerator]),
  get(classOf[LinguisticOntology])
)