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
package eu.monnetproject.gelato

import org.junit._
import eu.monnetproject.lang._
import eu.monnetproject.gelato._
import eu.monnetproject.gelato.plan._
import eu.monnetproject.gelato.realize._
import eu.monnetproject.gelato.statements._
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.framework.services._
import java.io.FileReader
import java.net.URI
import Assert._
import scala.collection.JavaConversions._

class GelatoTest {
  
  private var lingOnto : LinguisticOntology = null
  
  implicit def toURI(str : String) : URI = URI.create(str)
  implicit def toPVal(name : String) = lingOnto.getPropertyValue(name)
  implicit def toProp(name : String) = lingOnto.getProperty(name)
  implicit def toStr(lang : Language) = lang.toString
  
  @Test
  def example = {
    try {
      val lemonSerializer = LemonSerializer.newInstance
      lingOnto = Services.get(classOf[LinguisticOntology])
    
      val statement = DefiniteStatement(
        Is(
          BinaryPredicate("example:generates",
                          Constant("example:program"),
                          Constant("example:text")
          )
        ) :: Nil,
        Is(
          UnaryPredicate("example:beHappy",
                         Constant("example:John")
          )
        )
      )
    
      val model = lemonSerializer create null
      val lexicon = model.addLexicon("example:Lexicon",Language.ENGLISH.toString.toString)
      val factory = model.getFactory()
    
      val beHappy = LemonModels.addEntryToLexicon(lexicon,"example:beHappyEntry","be happy","example:beHappy")
      val beHappy3ps = factory makeForm()
      beHappy3ps.setWrittenRep(new Text("is happy",Language.ENGLISH.toString))
      beHappy3ps.addProperty(lingOnto.getProperty("number"),lingOnto.getPropertyValue("singular"))
      beHappy3ps.addProperty(lingOnto.getProperty("person"),lingOnto.getPropertyValue("thirdPerson"))
      beHappy3ps.addProperty(lingOnto.getProperty("tense"),lingOnto.getPropertyValue("present"))
      beHappy3ps.addProperty(lingOnto.getProperty("mood"),lingOnto.getPropertyValue("indicative"))
      beHappy addOtherForm beHappy3ps
      val beHappyFrame = factory makeFrame()
      beHappyFrame addSynArg (SynArg.synArg, factory makeArgument())
      beHappy addSynBehavior beHappyFrame
    
      val generates = LemonModels.addEntryToLexicon(lexicon,"example:generatesEntry","generate","example:generates")
      val generates3ps = factory makeForm()
      generates3ps.setWrittenRep(new Text("generates",Language.ENGLISH.toString))
      generates3ps.addProperty(lingOnto.getProperty("number"),lingOnto.getPropertyValue("singular"))
      generates3ps.addProperty(lingOnto.getProperty("person"),lingOnto.getPropertyValue("thirdPerson")) 
      generates3ps.addProperty(lingOnto.getProperty("tense"),lingOnto.getPropertyValue("present")) 
      generates3ps.addProperty(lingOnto.getProperty("mood"),lingOnto.getPropertyValue("indicative")) 
      generates addOtherForm generates3ps
      val generatesFrame = factory makeFrame()
      generatesFrame addSynArg (SynArg.synArg, factory makeArgument())
      generatesFrame addSynArg (SynArg.synArg, factory makeArgument())
      generates addSynBehavior generatesFrame
    
      val john = LemonModels.addEntryToLexicon(lexicon,"example:JohnEntry","John","example:John")
      john.addProperty(lingOnto.getProperty("partOfSpeech"),lingOnto.getPropertyValue("properNoun"))
      LemonModels.addEntryToLexicon(lexicon,"example:programEntry","program","example:program")
      val text = LemonModels.addEntryToLexicon(lexicon,"example:textEntry","text","example:text")
      text.getCanonicalForm() addProperty("number","massNoun")
    
      val planner = new GelatoPlanner
    
      val plan = planner.plan(statement,lexicon)
    
      val realizor = new Realizer(lingOnto, GrammarParser.read(new FileReader("src/main/resources/grammars/en.gram.gelato")))
    
      val sent = realizor.tree(plan)
    
      val realization = realizor.realize(sent)
    
      println("Statement: " + statement)
      println("Plan: " + plan)
      println("Tree: " + sent)
      println("NL:" + realization)
    
      assertEquals("John is happy if a program generates some text",realization)
    } finally {
    }
  }

  def buildLexicon(lemonSerializer : LemonSerializer) : Lexicon = {
    val model = lemonSerializer create null
    val lexicon = model.addLexicon("example:Lexicon",Language.ENGLISH.toString)
    val factory = model.getFactory()
    
    val over18 = LemonModels.addEntryToLexicon(lexicon,"example:over18Entry","over 18","example:over18")
    val over18Form = factory makeForm()
    over18Form setWrittenRep new Text("over 18",Language.ENGLISH.toString)
    over18Form addProperty ("number","singular")
    over18Form addProperty ("person","thirdPerson")
    over18Form addProperty ("tense","present")
    over18Form addProperty ("mood","indicative")
    over18 addOtherForm over18Form
    val over18Frame = factory makeFrame()
    over18Frame addSynArg (SynArg.synArg,factory.makeArgument)
    over18Frame addType (lingOnto.getFrameClass("AdjectivePredicativeFrame"))
    over18 addSynBehavior over18Frame
    
    
    val liveInHolland = LemonModels.addEntryToLexicon(lexicon,"example:liveInHollandEntry","live in Holland","example:liveInHolland")
    val livesInHollandForm = factory makeForm()
    livesInHollandForm setWrittenRep new Text("lives in Holland",Language.ENGLISH.toString)
    livesInHollandForm addProperty ("number","singular")
    livesInHollandForm addProperty ("person","thirdPerson")
    livesInHollandForm addProperty ("tense","present")
    livesInHollandForm addProperty ("mood","indicative")
    liveInHolland addOtherForm livesInHollandForm
    val liveInHollandFrame = factory makeFrame()
    liveInHollandFrame addSynArg (SynArg.synArg,factory.makeArgument)
    liveInHolland addSynBehavior liveInHollandFrame
    
    val haveValidDriversLicense = LemonModels.addEntryToLexicon(lexicon,"example:haveValidDriversLicenseEntry","have a valid driver's license","example:haveValidDriversLicense")
    val hasValidDriversLicenseForm = factory makeForm()
    hasValidDriversLicenseForm setWrittenRep new Text("has a valid driver's license",Language.ENGLISH.toString)
    hasValidDriversLicenseForm addProperty ("number","singular")
    hasValidDriversLicenseForm addProperty ("person","thirdPerson")
    hasValidDriversLicenseForm addProperty ("tense","present")
    hasValidDriversLicenseForm addProperty ("mood","indicative")
    haveValidDriversLicense addOtherForm hasValidDriversLicenseForm
    val haveValidDriversLicenseFrame = factory makeFrame()
    haveValidDriversLicenseFrame addSynArg (SynArg.synArg,factory.makeArgument)
    haveValidDriversLicense addSynBehavior haveValidDriversLicenseFrame
    
    val workAs = LemonModels.addEntryToLexicon(lexicon,"example:workAsEntry","work as","example:workAs")
    val worksAsForm = factory makeForm()
    worksAsForm setWrittenRep new Text("work as",Language.ENGLISH.toString)
    worksAsForm addProperty ("number","singular")
    worksAsForm addProperty ("person","thirdPerson")
    worksAsForm addProperty ("tense","present")
    worksAsForm addProperty ("mood","indicative")
    workAs addOtherForm worksAsForm
    val workAsFrame = factory makeFrame()
    workAsFrame addSynArg (SynArg.synArg,factory.makeArgument)
    workAsFrame addSynArg (SynArg.synArg,factory.makeArgument)
    workAs addSynBehavior workAsFrame
    
    val truckDriver = LemonModels.addEntryToLexicon(lexicon,"example:truckDriverEntry","truck driver","example:truckDriver")
    
    
    lexicon
  }
  
  def buildDeLexicon(lemonSerializer : LemonSerializer) : Lexicon = {
    val model = lemonSerializer create null
    val lexicon = model.addLexicon("example:Lexicon",Language.GERMAN)
    val factory = model.getFactory()
    
    val over18 = LemonModels.addEntryToLexicon(lexicon,"example:over18Entry","mehr als 18 jahre alt","example:over18")
    val over18Frame = factory makeFrame()
    over18Frame addSynArg (SynArg.synArg,factory.makeArgument)
    over18Frame addType (lingOnto.getFrameClass("AdjectivePredicativeFrame"))
    over18 addSynBehavior over18Frame
    
    
    val liveInHolland = LemonModels.addEntryToLexicon(lexicon,"example:liveInHollandEntry","wohnen in holland","example:liveInHolland")
    val livesInHollandForm = factory makeForm()
    livesInHollandForm setWrittenRep new Text("wohnt in holland",Language.GERMAN)
    livesInHollandForm addProperty ("number","singular")
    livesInHollandForm addProperty ("person","thirdPerson")
    livesInHollandForm addProperty ("tense","present")
    livesInHollandForm addProperty ("mood","indicative")
    liveInHolland addOtherForm livesInHollandForm
    val livesInHollandInfForm = factory makeForm()
    livesInHollandInfForm setWrittenRep new Text("in holland wohnen",Language.GERMAN)
    livesInHollandInfForm addProperty ("verbFormMood","infinitive")
    liveInHolland addOtherForm livesInHollandInfForm
    assertEquals(3,liveInHolland.getForms().size())
    
    val wohnen = LemonModels.addEntryToLexicon(lexicon,"example:wohnenEntry","wohnen",null)
    val inHolland = LemonModels.addEntryToLexicon(lexicon,"example:inHollandEntry","in holland",null)
    
    liveInHolland.addLexicalVariant(lingOnto.getLexicalVariant("head"),wohnen)
    liveInHolland.addLexicalVariant(lingOnto.getLexicalVariant("lexicalComplement"),inHolland)
    
    val liveInHollandFrame = factory makeFrame()
    liveInHollandFrame addSynArg (SynArg.synArg,factory.makeArgument)
    liveInHolland addSynBehavior liveInHollandFrame
    
    val haveValidDriversLicense = LemonModels.addEntryToLexicon(lexicon,"example:haveValidDriversLicenseEntry","haben einen gültigen führerschein","example:haveValidDriversLicense")
    val hasValidDriversLicenseForm = factory makeForm()
    hasValidDriversLicenseForm setWrittenRep new Text("hat einen gültigen führerschein",Language.GERMAN)
    hasValidDriversLicenseForm addProperty ("number","singular")
    hasValidDriversLicenseForm addProperty ("person","thirdPerson")
    hasValidDriversLicenseForm addProperty ("tense","present")
    hasValidDriversLicenseForm addProperty ("mood","indicative")
    haveValidDriversLicense addOtherForm hasValidDriversLicenseForm
    val hasValidDriversLicenseInfForm = factory makeForm()
    hasValidDriversLicenseInfForm setWrittenRep new Text("einen gültigen führerschein haben",Language.GERMAN)
    hasValidDriversLicenseInfForm addProperty ("verbFormMood","infinitive")
    haveValidDriversLicense addOtherForm hasValidDriversLicenseInfForm
    val haveValidDriversLicenseFrame = factory makeFrame()
    haveValidDriversLicenseFrame addSynArg (SynArg.synArg,factory.makeArgument)
    haveValidDriversLicense addSynBehavior haveValidDriversLicenseFrame
    
    val haben = LemonModels.addEntryToLexicon(lexicon,"example:haben","haben",null)
    val gueltigenFuehrerschein = LemonModels.addEntryToLexicon(lexicon,"example:gf","einen gültigen führerschein",null)
    
    haveValidDriversLicense addLexicalVariant(lingOnto.getLexicalVariant("head"),haben)
    haveValidDriversLicense addLexicalVariant(lingOnto.getLexicalVariant("lexicalComplement"),gueltigenFuehrerschein)
    
    val workAs = LemonModels.addEntryToLexicon(lexicon,"example:workAsEntry","arbeiten","example:workAs")
    val worksAsForm = factory makeForm()
    worksAsForm setWrittenRep new Text("arbeitet",Language.GERMAN)
    worksAsForm addProperty ("number","singular")
    worksAsForm addProperty ("person","thirdPerson")
    worksAsForm addProperty ("tense","present")
    worksAsForm addProperty ("mood","indicative")
    workAs addOtherForm worksAsForm
    val worksAsInfForm = factory makeForm()
    worksAsInfForm setWrittenRep new Text("arbeiten",Language.GERMAN)
    worksAsInfForm addProperty ("verbFormMood","infinitive")
    workAs addOtherForm worksAsInfForm
    val workAsFrame = factory makeFrame()
    workAsFrame addSynArg (lingOnto.getSynArg("subject"),factory.makeArgument)
    val prepArg = factory makeArgument()
    val als = LemonModels.addEntryToLexicon(lexicon,"example:als","als",null)
    prepArg setMarker als
    workAsFrame addSynArg (lingOnto.getSynArg("prepositionalAdjunct"),prepArg)
    workAsFrame addType lingOnto.getFrameClass("IntransitivePPFrame")
    workAs addSynBehavior workAsFrame
    
    val truckDriver = LemonModels.addEntryToLexicon(lexicon,"example:truckDriverEntry","lkw-fahrer","example:truckDriver")
    truckDriver addProperty ("gender","masculine")
    
    lexicon
  }
  
  @Test
  def example2 {
    try {
      val lemonSerializer = LemonSerializer.newInstance
      lingOnto = Services.get(classOf[LinguisticOntology])
      val statement = DefiniteStatement(
        Is(UnaryPredicate(
            "example:over18",
            Variable('you)
          )) ::
        Is(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          )) ::
        Is(UnaryPredicate(
            "example:haveValidDriversLicense",
            Variable('you)
          )) :: Nil,
        May(BinaryPredicate(
            "example:workAs",
            Variable('you),
            Constant("example:truckDriver")
          )
        )
      )
    
      val lexicon = buildLexicon(lemonSerializer)
            
      val planner = new GelatoPlanner
    
      val plan = planner.plan(statement,lexicon)
    
      val realizor = new Realizer(lingOnto, GrammarParser.read(new FileReader("src/main/resources/grammars/en.gram.gelato")))
    
      val sent = realizor.tree(plan)
    
      val realization = realizor.realize(sent)
    
      println("Statement: " + statement)
      println("Plan: " + plan)
      println("Tree: " + sent)
      println("NL:" + realization)
    
      assertEquals("you may work as a truck driver if you are over 18 , live in Holland and have a valid driver's license",realization)
    } finally {
    }
  }
  
  
  @Test
  def example3 {
    try {
      val lemonSerializer = LemonSerializer.newInstance()
      lingOnto = Services.get(classOf[LinguisticOntology])
      val lexicon = buildLexicon(lemonSerializer)
      val statement = QuestionStatement(
        Is(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          )) 
      )
            
      val statement2 = QuestionStatement(
        Must(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          ))
      )
            
      val statement3 = QuestionStatement(
        Is(UnaryPredicate(
            "example:over18",
            Variable('you)
          ))
      )
    
            
      val planner = new GelatoPlanner
    
      val plan = planner.plan(statement,lexicon)
    
      val realizor = new Realizer(lingOnto, GrammarParser.read(new FileReader("src/main/resources/grammars/en.gram.gelato")))
    
      val sent = realizor.tree(plan)
    
      val realization = realizor.realize(sent)
    
      println("Statement: " + statement)
      println("Plan: " + plan)
      println("Tree: " + sent)
      println("NL:" + realization)
            
      assertEquals("do you live in Holland ?",realization)
            
      val plan2 = planner.plan(statement2,lexicon)
    
      val sent2 = realizor.tree(plan2)
    
      val realization2 = realizor.realize(sent2)
    
      println("Statement: " + statement2)
      println("Plan: " + plan2)
      println("Tree: " + sent2)
      println("NL:" + realization2)
            
      assertEquals("must you live in Holland ?",realization2)
            
      val plan3 = planner.plan(statement3,lexicon)
    
      val sent3 = realizor.tree(plan3)
    
      val realization3 = realizor.realize(sent3)
    
      println("Statement: " + statement3)
      println("Plan: " + plan3)
      println("Tree: " + sent3)
      println("NL:" + realization3)
            
      assertEquals("are you over 18 ?",realization3)
    } finally {
    }
  }
  
  @Test
  def example2de {
    try {
      val lemonSerializer = LemonSerializer.newInstance()
      lingOnto = Services.get(classOf[LinguisticOntology])
      val statement = DefiniteStatement(
        Is(UnaryPredicate(
            "example:over18",
            Variable('you)
          )) ::
        Is(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          )) ::
        Is(UnaryPredicate(
            "example:haveValidDriversLicense",
            Variable('you)
          )) :: Nil,
        May(BinaryPredicate(
            "example:workAs",
            Variable('you),
            Constant("example:truckDriver")
          )
        )
      )
    
      val lexicon = buildDeLexicon(lemonSerializer)
            
      val planner = new GelatoPlanner
    
      val plan = planner.plan(statement,lexicon)
    
      val realizor = new Realizer(lingOnto, GrammarParser.read(new FileReader("src/main/resources/grammars/de.gram.gelato")))
    
      val sent = realizor.tree(plan)
    
      val realization = realizor.realize(sent)
    
      println("Statement: " + statement)
      println("Plan: " + plan)
      println("Tree: " + sent)
      println("NL:" + realization)
    
      assertEquals("Sie k\u00f6nnen als einen lkw-fahrer arbeiten , wenn Sie mehr als 18 jahre alt sind , in holland wohnen und einen g\u00fcltigen führerschein haben",realization)
    } finally {    }
  }
  
  @Test
  def example3de {
    try {
      val lemonSerializer = LemonSerializer.newInstance
      lingOnto = Services.get(classOf[LinguisticOntology])
      val lexicon = buildDeLexicon(lemonSerializer)
      val statement = QuestionStatement(
        Is(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          )) 
      )
            
      val statement2 = QuestionStatement(
        Must(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          ))
      )
            
      val statement3 = QuestionStatement(
        Is(UnaryPredicate(
            "example:over18",
            Variable('you)
          ))
      )
    
            
      val planner = new GelatoPlanner
    
      val realizor = new Realizer(lingOnto, GrammarParser.read(new FileReader("src/main/resources/grammars/de.gram.gelato")))
    
            
      val plan2 = planner.plan(statement2,lexicon)
    
      val sent2 = realizor.tree(plan2)
    
      val realization2 = realizor.realize(sent2)
    
      println("Statement: " + statement2)
      println("Plan: " + plan2)
      println("Tree: " + sent2)
      println("NL:" + realization2)
            
      assertEquals("müssen Sie in holland wohnen ?",realization2)
            
      val plan = planner.plan(statement,lexicon)
    
      val sent = realizor.tree(plan)
    
      val realization = realizor.realize(sent)
    
      println("Statement: " + statement)
      println("Plan: " + plan)
      println("Tree: " + sent)
      println("NL:" + realization)
            
      assertEquals("wohnen Sie in holland ?",realization)
            
            
      val plan3 = planner.plan(statement3,lexicon)
    
      val sent3 = realizor.tree(plan3)
    
      val realization3 = realizor.realize(sent3)
    
      println("Statement: " + statement3)
      println("Plan: " + plan3)
      println("Tree: " + sent3)
      println("NL:" + realization3)
            
      assertEquals("sind Sie mehr als 18 jahre alt ?",realization3)
    } finally {
    }
  }
  
  
  def buildNlLexicon(lemonSerializer : LemonSerializer) : Lexicon = {
    val model = lemonSerializer create null
    val lexicon = model.addLexicon("example:Lexicon",Language.DUTCH)
    val factory = model.getFactory()
    
    val over18 = LemonModels.addEntryToLexicon(lexicon,"example:over18Entry","ouder dan 18 jaar","example:over18")
    val over18Frame = factory makeFrame()
    over18Frame addSynArg (SynArg.synArg,factory.makeArgument)
    over18Frame addType (lingOnto.getFrameClass("AdjectivePredicativeFrame"))
    over18 addSynBehavior over18Frame
    
    
    val liveInHolland = LemonModels.addEntryToLexicon(lexicon,"example:liveInHollandEntry","wonen in nederland","example:liveInHolland")
    val livesInHollandForm = factory makeForm()
    livesInHollandForm setWrittenRep new Text("woont in nederland",Language.DUTCH)
    livesInHollandForm addProperty ("number","singular")
    livesInHollandForm addProperty ("person","thirdPerson")
    livesInHollandForm addProperty ("tense","present")
    livesInHollandForm addProperty ("mood","indicative")
    liveInHolland addOtherForm livesInHollandForm
    val livesInHollandInfForm = factory makeForm()
    livesInHollandInfForm setWrittenRep new Text("in nederland wonen",Language.DUTCH)
    livesInHollandInfForm addProperty ("mood","infinitive")
    liveInHolland addOtherForm livesInHollandInfForm
    assertEquals(3,liveInHolland.getForms().size())
    
    val wohnen = LemonModels.addEntryToLexicon(lexicon,"example:wonenEntry","wonen",null)
    val inHolland = LemonModels.addEntryToLexicon(lexicon,"example:inHollandEntry","in nederland",null)
    
    liveInHolland.addLexicalVariant(lingOnto.getLexicalVariant("head"),wohnen)
    liveInHolland.addLexicalVariant(lingOnto.getLexicalVariant("lexicalComplement"),inHolland)
    
    val liveInHollandFrame = factory makeFrame()
    liveInHollandFrame addSynArg (SynArg.synArg,factory.makeArgument)
    liveInHolland addSynBehavior liveInHollandFrame
    
    val haveValidDriversLicense = LemonModels.addEntryToLexicon(lexicon,"example:haveValidDriversLicenseEntry","hebben een geldig rijbewijs","example:haveValidDriversLicense")
    val hasValidDriversLicenseForm = factory makeForm()
    hasValidDriversLicenseForm setWrittenRep new Text("heeft een geldig rijbewijs",Language.DUTCH)
    hasValidDriversLicenseForm addProperty ("number","singular")
    hasValidDriversLicenseForm addProperty ("person","thirdPerson")
    hasValidDriversLicenseForm addProperty ("tense","present")
    hasValidDriversLicenseForm addProperty ("mood","indicative")
    haveValidDriversLicense addOtherForm hasValidDriversLicenseForm
    val hasValidDriversLicenseInfForm = factory makeForm()
    hasValidDriversLicenseInfForm setWrittenRep new Text("een geldig rijbewijs hebben",Language.DUTCH)
    hasValidDriversLicenseInfForm addProperty ("mood","infinitive")
    haveValidDriversLicense addOtherForm hasValidDriversLicenseInfForm
    val haveValidDriversLicenseFrame = factory makeFrame()
    haveValidDriversLicenseFrame addSynArg (SynArg.synArg,factory.makeArgument)
    haveValidDriversLicense addSynBehavior haveValidDriversLicenseFrame
    
    val haben = LemonModels.addEntryToLexicon(lexicon,"example:hebben","hebben",null)
    val gueltigenFuehrerschein = LemonModels.addEntryToLexicon(lexicon,"example:gr","een geldig rijbewijs",null)
    
    haveValidDriversLicense addLexicalVariant(lingOnto.getLexicalVariant("head"),haben)
    haveValidDriversLicense addLexicalVariant(lingOnto.getLexicalVariant("lexicalComplement"),gueltigenFuehrerschein)
    
    val workAs = LemonModels.addEntryToLexicon(lexicon,"example:workAsEntry","werken","example:workAs")
    val worksAsForm = factory makeForm()
    worksAsForm setWrittenRep new Text("werkt",Language.GERMAN)
    worksAsForm addProperty ("number","singular")
    worksAsForm addProperty ("person","thirdPerson")
    worksAsForm addProperty ("tense","present")
    worksAsForm addProperty ("mood","indicative")
    workAs addOtherForm worksAsForm
    val worksAsInfForm = factory makeForm()
    worksAsInfForm setWrittenRep new Text("werken",Language.GERMAN)
    worksAsInfForm addProperty ("verbFormMood","infinitive")
    workAs addOtherForm worksAsInfForm
    val workAsFrame = factory makeFrame()
    workAsFrame addSynArg (lingOnto.getSynArg("subject"),factory.makeArgument)
    val prepArg = factory makeArgument()
    val als = LemonModels.addEntryToLexicon(lexicon,"example:als","als",null)
    prepArg setMarker als
    workAsFrame addSynArg (lingOnto.getSynArg("prepositionalAdjunct"),prepArg)
    workAsFrame addType lingOnto.getFrameClass("IntransitivePPFrame")
    workAs addSynBehavior workAsFrame
    
    val truckDriver = LemonModels.addEntryToLexicon(lexicon,"example:truckDriverEntry","vrachtwagenchauffeur","example:truckDriver")
    truckDriver addProperty ("gender","masculine")
    //val writer = new java.io.OutputStreamWriter(System.out)
    //lemonSerializer.writeLexicon(model,lexicon, new net.lexinfo.LexInfo(), writer)
    //writer.flush
    
    
    lexicon
  }
  
  
  @Test
  def simpleNL {
      val lemonSerializer = LemonSerializer.newInstance
      lingOnto = Services.get(classOf[LinguisticOntology])
    
      val statement = Is(UnaryPredicate(
            "example:over18",
            Variable('you)
          ))
        
      println("Statement: " + statement)
    
      val lexicon = buildNlLexicon(lemonSerializer)
            
      val planner = new GelatoPlanner
    
      val plan = planner.plan(statement,lexicon)
    
      println("Plan: " + plan)
      
      val realizor = new Realizer(lingOnto, GrammarParser.read(new FileReader("src/main/resources/grammars/nl.gram.gelato")))
    
      val sent = realizor.tree(plan)
    
      val realization = realizor.realize(sent)
      println("Tree: " + sent)
      println("NL:" + realization)
    
      assertEquals("u zijn ouder dan 18 jaar",realization)
    
  }
  
  @Test
  def example2nl {
    try {
      val lemonSerializer = LemonSerializer.newInstance
      lingOnto = Services.get(classOf[LinguisticOntology])
      val statement = DefiniteStatement(
        Is(UnaryPredicate(
            "example:over18",
            Variable('you)
          )) ::
        Is(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          )) ::
        Is(UnaryPredicate(
            "example:haveValidDriversLicense",
            Variable('you)
          )) :: Nil,
        May(BinaryPredicate(
            "example:workAs",
            Variable('you),
            Constant("example:truckDriver")
          )
        )
      )
      println("Statement: " + statement)
    
      val lexicon = buildNlLexicon(lemonSerializer)
            
      val planner = new GelatoPlanner
    
      val plan = planner.plan(statement,lexicon)
    
      println("Plan: " + plan)
      
      val realizor = new Realizer(lingOnto, GrammarParser.read(new FileReader("src/main/resources/grammars/nl.gram.gelato")))
    
      val sent = realizor.tree(plan)
    
      println("Tree: " + sent)
    
      val realization = realizor.realize(sent)
      println("NL:" + realization)
    
      //assertEquals("u kunnen als een vrachtwagenchauffeur werken als u ouder dan 18 jaar zijn , in nederland wonen en een geldig rijbewijs hebben",realization)
    } finally {
    }
  }
  
  @Test
  def example3nl {
    try {
      val lemonSerializer = LemonSerializer.newInstance
      lingOnto = Services.get(classOf[LinguisticOntology])
      val lexicon = buildNlLexicon(lemonSerializer)
      val statement = QuestionStatement(
        Is(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          )) 
      )
            
      val statement2 = QuestionStatement(
        Must(UnaryPredicate(
            "example:liveInHolland",
            Variable('you)
          ))
      )
            
      val statement3 = QuestionStatement(
        Is(UnaryPredicate(
            "example:over18",
            Variable('you)
          ))
      )
    
            
      val planner = new GelatoPlanner
    
      val realizor = new Realizer(lingOnto, GrammarParser.read(new FileReader("src/main/resources/grammars/nl.gram.gelato")))
    
            
      val plan2 = planner.plan(statement2,lexicon)
    
      val sent2 = realizor.tree(plan2)
    
      val realization2 = realizor.realize(sent2)
    
      println("Statement: " + statement2)
      println("Plan: " + plan2)
      println("Tree: " + sent2)
      println("NL:" + realization2)
            
      //assertEquals("moeten u in nederland wonen ?",realization2)
            
      val plan = planner.plan(statement,lexicon)
    
      val sent = realizor.tree(plan)
    
      val realization = realizor.realize(sent)
    
      println("Statement: " + statement)
      println("Plan: " + plan)
      println("Tree: " + sent)
      println("NL:" + realization)
            
      //assertEquals("wonen u in nederland ?",realization)
            
            
      val plan3 = planner.plan(statement3,lexicon)
    
      val sent3 = realizor.tree(plan3)
    
      val realization3 = realizor.realize(sent3)
    
      println("Statement: " + statement3)
      println("Plan: " + plan3)
      println("Tree: " + sent3)
      println("NL:" + realization3)
            
      //assertEquals("zijn u ouder dan 18 jaar ?",realization3)
    } finally {
    }
  }
}
