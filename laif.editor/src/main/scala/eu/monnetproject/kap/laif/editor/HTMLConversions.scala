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

package eu.monnetproject.kap.laif.editor

import eu.monnetproject.kap.laif._
import eu.monnetproject.kap.laif.rules._
import eu.monnetproject.lang.Language
import eu.monnetproject.l10n.LocalizationLexicon
import java.util.logging.Logger
import java.net.URI
import java.io._
import javax.servlet.http._
import org.apache.commons.fileupload._
import org.apache.commons.fileupload.disk._
import org.apache.commons.fileupload.servlet._
import org.osgi.framework._
import scala.collection.JavaConversions._
import scala.xml._
import scala.xml.Utility._
import scalasemweb.rdf.model._

/**
 * 
 * @author John McCrae
 */
object HTMLConversions {
  private val log = Logger.getLogger(this.getClass().getName())
  
  
  implicit def pimpElem(elem : Elem) = new {
    def %%(attrs : (String,String)*) = {
      var elem2 = elem
      for(attr <- attrs) {
        elem2 = elem % Attribute(None,attr._1,Text(attr._2),Null)
      }
      elem2
    }
      
  }

  def makeValue(values : List[LAIFValue]) :Seq[Elem] = {
    val htmlValues = for(value <- values) yield {
      (value match {
          case resource : LAIFVariable => <span class="laifvariable">{resource.id()}</span>
          case value : LAIFLiteral => <span class="laifliteral">{value.value()}</span>
          case userCall : LAIFUserCall =>
            <span>
              <span class="laifusercall_function">{
                  userCall.functionFrag()
                }</span>
              <span class="laifusercall_args">{
                  makeValue(userCall.args().toList)
                }</span>
            </span> %% ("class" -> (if(userCall.args.isEmpty) { "laifusercall_hidden"} else { "laifusercall"}))
          case formCall : LAIFFormCall =>
            <span class="laifformcall">
              <span class="laifformcall_form">{makeValue(formCall.args().toList)}</span>
              {
                /*for((key,value) <- formCall.props) yield {
                  <span class="laiffrom_call_props">
                    <span class="laifformcall_propkey">{nn2Str(key)}</span>
                    <span class="laifformcall_propval">{nn2Str(value)}</span>
                  </span>
                } */
              <span>todo</span>
              }
            </span>
          case form : LAIFDescriptionCall => <span class="laifdescriptioncall">DESC: {makeValue(form.args().toList)}</span>
          case epCall : LAIFEntryPropCall =>
            <span class="laifentrypropcall">
              TODO
            </span>
          case arg : LAIFLowerFirst => <span class="laiflowerfirst">Lower First: {makeValue(arg.args().toList)}</span>
          case arg : LAIFUpperFirst => <span class="laifupperfirst">Upper First: {makeValue(arg.args().toList)}</span>
          case arg : LAIFAllLower => <span class="laifalllower">All Lower: {makeValue(arg.args().toList)}</span>
          case arg : LAIFAllUpper => <span class="laifallupper">All Upper: {makeValue(arg.args().toList)}</span>
          case _ => <span/>
        }) 
    }
    if(htmlValues.isEmpty || (htmlValues.last \ "@class" == "laifliteral" && htmlValues.head \ "@class" == "laifliteral")) {
      htmlValues
    } else if(htmlValues.head \ "@class" == "laifliteral") {
      htmlValues :+ <span class="laifliteral">&#160;</span>
    } else if(htmlValues.last \ "@class" == "laifliteral") {
      <span class="laifliteral">&#160;</span> +: htmlValues
    } else {
      <span class="laifliteral">&#160;</span> +: htmlValues :+ <span class="laifliteral">&#160;</span>
    }
  }
  
  private def clean(nodes : Elem) : Elem = {
    nodes.copy(child = 
      for(node <- nodes.child if
          (node \ "@class").text != "laifliteral" || node.text != "") yield {
          node match {
            case elem : Elem => clean(elem)
            case other => other
          }
        }
    )
  }
  
  def unmake(nodes : Node) : Seq[LAIFValue] = {
    unmake2(clean(trim(nodes).asInstanceOf[Elem]))
  }
  
  private def unmake2(nodes : Node) : Seq[LAIFValue] = {
    if(nodes.label == "head") {
      trim(nodes).child flatMap {
        node => unmake(node)
      }
    } else {
      for(node <- trim(nodes)) yield {
        (node \ "@class").text  match {
          case "laifvariable" => new LAIFVariable(node.child.head.text)
          case "laifliteral" => new LAIFLiteral(node.text)
          case "laifusercall" => new LAIFUserCall(URI.create(node.child.head.text),node.child(1).child flatMap { unmake(_)} toList)
          case "laifusercall_hidden" => new LAIFUserCall(URI.create(node.child.head.text),node.child(1).child flatMap { unmake(_)} toList)
          case "laifformcall" => new LAIFFormCall(URI.create(node.child.head.child(0).text), (for(arg <- node.child.tail) yield {
                  arg match {
                    case <span><span>{propkey}</span><span>{propval}</span></span> => {
                        unmakeRes(propkey).uri -> unmakeRes(propval).uri
                      }
                  }
                }).toMap)
          case "laifdescriptioncall" => new LAIFDescriptionCall(unmake(node.child(1)).head)
          case "laifentrypropcall" => new LAIFEntryPropCall(URI.create(node.child(0).head.text),
                                                        unmake(node.child(1).head).head.asInstanceOf[LAIFVariable],
                                                        (for(matcher <- node.child(2).child) yield {
                  matcher match {
                    case <span><span>{value}</span><span>{generates}</span></span> => {
                        value.uri -> new LAIFCase(unmake(generates).toList)
                      }
                  }
                }).toMap[URI,LAIFCase],null)
          case "laiflowerfirst" => new LAIFLowerFirst(unmake(node.child(0)).head)
          case "laifupperfirst" => new LAIFUpperFirst(unmake(node.child(0)).head)
          case "laifalllower" => new LAIFAllLower(unmake(node.child(0)).head)
          case "laifallupper" => new LAIFAllUpper(unmake(node.child(0)).head)
        }
      }
    } 
  }
  
  private val httpRegex = """<?(http://[^>]*)>?""".r
  private val qnameRegex = """(.*):(.*)""".r
  
  private var nsMap = Map[String,NameSpace]("lemon" -> NameSpace("lemon","http://www.monnet-project.eu/lemon#"),
                                            "laif" -> NameSpace("laif","http://www.monnet-project.eu/laif#"),
                                            "lexinfo" -> NameSpace("lexinfo","http://www.lexinfo.net/ontology/2.0/lexinfo#"))
  private def nameSpace(ns : String) : NameSpace = nsMap.get(ns).getOrElse({
      log.warning("failed to resolve ns " + ns)
      RDF.base
    })
  
  def updateNameSpace(nn : Resource) = {
    nn match {
    case QName(ns,_) => {
        nsMap += (ns.id -> ns)
    }
    case _ =>
  }
  }
  
  private implicit def unmakeRes(res : Node) : NamedNode = unmakeName(res.text)
  
  def unmakeName(string : String) = string match {
    case httpRegex(uriString) => uriString.uri
    case qnameRegex(prefix,suffix) => nameSpace(prefix) & suffix
  }
  
  
  implicit def nn2Str(nn : Resource) = nn match {
    case QName(ns,_) => nsMap += (ns.id -> ns) ; nn.toString()
    case _ => nn.toString()
  }
}
