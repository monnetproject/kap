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

package eu.monnetproject.gelato.demo

import com.vaadin._
import com.vaadin.data.{Property=>VProperty}
import com.vaadin.ui._
import eu.monnetproject.gelato._
import eu.monnetproject.gelato.plan._
import eu.monnetproject.gelato.realize._
import eu.monnetproject.kap.nlg.{Argument=>GArg,_}
import eu.monnetproject.l10n._
import eu.monnetproject.lang.Language
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.util._
import java.net.URI
import java.io.InputStreamReader
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class GelatoDemo(localizer : Localizer, lingOnto : LinguisticOntology) extends Application {
  def getPath = "/gelato-demo"
  def getWidgetSet = null
  
  private val log = Logging.getLogger(this)
  private lazy val l10n = localizer.getLexicon("gelatodemo")  
  private val lemonSerializer = LemonSerializer.newInstance
  
  private val positivity = List(true,false)
  
  override def init {
    val generator = new GelatoGenerator(lingOnto)
    val lang = Language.ENGLISH
    val window = new Window(l10n.get("GELATO demonstrator",lang))
    val layout = new VerticalLayout()
    layout setSpacing true
    
    val lexiconSelect = new Select(l10n.get("Select a lexicon",lang))
    val al = availLexica
    for(lexicon <- al) {
      lexiconSelect addItem lexicon
      lexiconSelect setItemCaption(lexicon, lexicon.getURI.toString)
    }
    if(al.isEmpty) {
      layout addComponent new Label("!!! No lexica found !!!")
    }
    lexiconSelect setNullSelectionAllowed false
    lexiconSelect setWidth "100%"
    
    val entrySelect = new Select(l10n.get("Select an entry",lang))
    entrySelect setNullSelectionAllowed false
    entrySelect setWidth "100%"
    
    lexiconSelect addListener (new VProperty.ValueChangeListener() {
        def valueChange(event : VProperty.ValueChangeEvent) {
          entrySelect.removeAllItems
          val lexicon = lexiconSelect.getValue.asInstanceOf[Lexicon]
          val senses = (for(entry <- lexicon.getEntrys ; sense <- entry.getSenses) yield {
              sense.getReference
            }).toSet
          for(sense <- senses) {
            entrySelect.addItem(sense)
          }
        }
      })
    
    layout addComponent lexiconSelect
    layout addComponent entrySelect
    
    layout.setMargin(true)
    
    val tabsheet = new TabSheet()
    
    
    
    tabsheet.addTab(matrixTab(lang,lexiconSelect, entrySelect,generator), l10n.get("Matrix",lang),null)
    
    tabsheet.addTab(derivTab(lang,lexiconSelect, entrySelect,generator), l10n.get("Derivation",lang),null)
    
    layout addComponent tabsheet
    
    window.setContent(layout)
    setMainWindow(window)
  }
  
  def matrixTab(lang : Language, lexiconSelect : Select, entrySelect : Select,generator : GelatoGenerator) = {
    val matrixLayout = new VerticalLayout()
    
    val matrix = new Table()
    matrix setWidth "100%"
    
    matrix addContainerProperty("", classOf[String], null)
    matrix addContainerProperty("Continuing",classOf[String],null)
    matrix addContainerProperty("Future", classOf[String],null)
    //matrix addContainerProperty("Habitual",classOf[String],null)
    matrix addContainerProperty("Past",classOf[String],null)
    
    matrixLayout addComponent matrix
    
    val doMatrixButton = new Button(l10n.get("Generate",lang))
    
    doMatrixButton addListener new Button.ClickListener {
      def buttonClick(event : Button#ClickEvent) {
        val lexicon = lexiconSelect.getValue.asInstanceOf[Lexicon]
        val sense = entrySelect.getValue.asInstanceOf[URI]
        matrix.removeAllItems
        var i = 0
        for{ modality <- ModalityType.values() 
            question <- positivity.reverse
            positive <- positivity } {
          val row = Array(modality + " " + (if(question) { "question"} else { "" }) + " " + (if(positive) { "true"} else { "false" })) ++ 
          (for(temporal <- TemporalType.values().reverse if temporal.toString != "habitual") yield {
              val pred = new Predicate(sense,List(GArg.YOU), new Modality(positive,modality,temporal))
              try {
                generator.generate(pred, question, lexicon) : Object
              } catch {
                case x : Exception => /*log.stackTrace(x) ;*/ x.getMessage : Object
              }
            })
          matrix.addItem(row, i)
          i+=1
        }
      }
    }
    
    matrixLayout addComponent doMatrixButton 
    
    
    matrixLayout
  }
  
  def derivTab(lang : Language, lexiconSelect : Select, entrySelect : Select,generator : GelatoGenerator) = {
    val layout = new VerticalLayout()
    
    val modalitySelect = new Select(l10n.get("Modality",lang))
    for(modality <- ModalityType.values()) {
      modalitySelect  addItem modality
    }
    modalitySelect setNullSelectionAllowed false
    modalitySelect select ModalityType.definite
    layout addComponent modalitySelect
    
    val temporalSelect = new Select(l10n.get("Temporality",lang))
    for(temporal <- TemporalType.values()) {
      temporalSelect addItem temporal
    }
    temporalSelect setNullSelectionAllowed false
    temporalSelect select TemporalType.past
    layout addComponent temporalSelect
    
    val positiveX = new CheckBox(l10n.get("Positive",lang),true)
    layout addComponent positiveX
    
    val questionX = new CheckBox(l10n.get("Question",lang))
    layout addComponent questionX
    
    val planArea = new TextArea(l10n.get("Plan",lang))
    planArea setWidth "100%"
    planArea.setRows(10)
    layout addComponent planArea
    
    val treeArea = new TextArea(l10n.get("Tree",lang))
    treeArea setWidth "100%"
    treeArea setRows 10
    layout addComponent treeArea
    
    val realizField = new TextField(l10n.get("Realization",lang))
    realizField setWidth "100%"
    layout addComponent realizField
    
    val button = new Button(l10n.get("Generate",lang))
    layout addComponent button
    
    val planner = new GelatoPlanner()
    
    
    button addListener new Button.ClickListener() {
      def buttonClick(event : Button#ClickEvent) {
        val lexicon = lexiconSelect.getValue.asInstanceOf[Lexicon]
        val sense = entrySelect.getValue.asInstanceOf[URI]
        val positive = positiveX.getValue == java.lang.Boolean.TRUE
        val modality = modalitySelect.getValue.asInstanceOf[ModalityType]
        val temporal = temporalSelect.getValue.asInstanceOf[TemporalType]
        val question = questionX.getValue == java.lang.Boolean.TRUE
        
        val pred = new Predicate(sense,List(GArg.YOU), new Modality(positive,modality,temporal))
              
        val realizor = generator.getPlanner(lexicon.getLanguage).getOrElse(throw new Exception)
        
        val statement = generator.pred2Statement(pred, question)
        
        val plan = planner.plan(statement,lexicon)
        
        planArea.setValue(plan.toString)
    
        val res = realizor.treeResult(plan)
        
        res match {
          case TreeingResult(Some(sent),_) => {
              treeArea.setValue(sent.toString)
    
              val realization = realizor.realize(sent)
        
              realizField.setValue(realization)
            }
          case TreeingResult(None,bestTree) => {
              treeArea.setValue(bestTree.toString)
              
              realizField.setValue("FAILED")
            }
        }
    
      }
    }
        
    layout
  }
  
  def availLexica : Set[Lexicon] = {
    val model_en = lemonSerializer.read(new InputStreamReader(ResourceFinder.getResource("kap-lexicon-en.xml").openStream))
    val model_de = lemonSerializer.read(new InputStreamReader(ResourceFinder.getResource("kap-lexicon-de.xml").openStream))
    val model_nl = lemonSerializer.read(new InputStreamReader(ResourceFinder.getResource("kap-lexicon-nl.xml").openStream))
    
    return Set() ++ model_en.getLexica ++ model_de.getLexica ++ model_nl.getLexica
  }
}

import eu.monnetproject.framework.services.Services._

class GelatoDemoApp extends GelatoDemo(
  get(classOf[Localizer]),
  get(classOf[LinguisticOntology])
)
