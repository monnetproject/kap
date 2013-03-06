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

import eu.monnetproject.lang._
import eu.monnetproject.lemon._
import eu.monnetproject.lemon.model._
import eu.monnetproject.gelato.plan._
import eu.monnetproject.gelato.realize._
import java.io._
import org.junit._
import org.junit.Assert._
import scala.collection.JavaConversions._

/**
 * 
 * @author John McCrae
 */
class ParsersTest {
  val testGrammar = """// Sentence block, matches when an S node is needed
S {
 // Simple rule $1 and $2 are substitution points for the first and second subplan
 IfPlan => (S (S $1) (SBAR (CC "if") (S $2)))
 QuestionPlan => (S (CP $1) "?") 
 // VP:3ps is a tree with a property 3ps
 PredicatePlan => (S (DP:3ps $1) (VP:3ps $2))
 PredicatePlan => (S (DP:other $1) (VP:other $2))
 // Rules are applied in order, hence this rule is preferred
 // All subplans must be matched hence this only matches plans with
 // exactly two subplans
 ConjunctionPlan => (S (S $1) (SBAR (CC "and") (S $2)))
 // $* means all remaining subplans compiled into a subplan of the
 // same type
 ConjunctionPlan => (S (S $1) (SBAR (CC ",") (S $*)))
 ConjunctionPlan => (S $1)
}

CP {
 PredicatePlan => (CP (C "may") (IP (DP $1)) (VP:may $2))
 PredicatePlan => (CP (C "may") (IP (DP $1)) (VP:mayneg $2))
 PredicatePlan => (CP (C "must") (IP (DP $1)) (VP:must $2))
 PredicatePlan => (CP (C "must") (IP (DP $1)) (VP:mustneg $2))
 PredicatePlan => (CP (C "will") (IP (DP $1)) (VP:will $2))
 PredicatePlan => (CP (C "will") (IP (DP $1)) (VP:willneg $2))
 PredicatePlan[AdjectivePredicativeFrame] => (CP (C "is") (IP (DP:3ps $1)) (VP:comp $2))
 PredicatePlan[AdjectivePredicativeFrame] => (CP (C "are") (IP (DP:other $1)) (VP:comp $2))
 PredicatePlan => (CP (C:3ps "does") (IP (DP:3ps $1) (VP:neg $2)))
 PredicatePlan => (CP (C:other "do") (IP (DP:other $1) (VP:neg $2)))
 PredicatePlan => (CP (C:3ps "does") (IP (DP:3ps $1) (VP:other $2)))
 PredicatePlan => (CP (C:other "do") (IP (DP:other $1) (VP:other $2)))
}

DP {
 // ? is used for "guards" that find a form with the approriate properties
 // $_ means insert the entry
 EntryPlan ? partOfSpeech=properNoun => (DP:3ps (NP (NN $_)))
 EntryPlan ? number=massNoun => (DP:other (DT "some") (NP (NN $_)))
 // =~ is used for regular expression based matching to the form
 EntryPlan =~ /[aeiou].*/ => (DP:3ps (DT "an") (NP (NN $_)))
 // !~ is the negative of =~
 EntryPlan !~ /[aeiou].*/ => (DP:3ps (DT "a") (NP (NN $_)))
 YouPlan => (DP:other (PRP "you"))
 // :%1 means the property is inhertied from the first child
 // ! means the node will not be realized as a string
 Epsilon => (DP:3ps (Z:3ps! $1)) 
 Epsilon => (DP:other (Z:other! $1)) 
 ObjectPlan => (DP $1)
}

VP {
 VerbPlan [AdjectivePredicativeFrame] => (VP:3ps (V "is") (PP $_))
 VerbPlan [AdjectivePredicativeFrame] => (VP:other (V "are") (PP $_))
 VerbPlan  => (VP:3ps (V $_) (DP $*))
 VerbPlan  => (VP:other (V $_) (DP $*))
 VerbPlan  => (VP:3ps (V $_))
 VerbPlan  => (VP:other (V $_))
 VerbPlan  => (VP:aux (V $_))
 VerbPlan [AdjectivePredicativeFrame] => (VP:comp (V $_))
 NotVerbPlan => (VP:neg (RP "not") (VP:other $1))
 NotVerbPlan => (VP:3ps (V "does") (RP "not") (VP:other $1))
 NotVerbPlan => (VP:other (V "do") (RP "not") (VP:other $1))
 NotVerbPlan => (VP:aux (RP "not") (VP:other $1))
 MayVerbPlan => (VP:may (VP:other $1))
 MayVerbPlan => (VP:3ps (V "may") (VP:other $1))
 MayVerbPlan => (VP:other (V "may") (VP:other $1))
 MayVerbPlan => (VP:aux (ADV "possibly") (VP:other $1))
 MayNotVerbPlan => (VP:mayneg (RP "not") (VP:other $1))
 MayNotVerbPlan => (VP:3ps (V "may") (RP "not") (VP:other $1))
 MayNotVerbPlan => (VP:other (V "may") (RP "not") (VP:other $1))
 MayNotVerbPlan => (VP:aux (FRAG "probably not") (VP:other  $1))
 MustVerbPlan => (VP:must (VP:other $1))
 MustVerbPlan => (VP:3ps (V "must") (VP:other $1))
 MustVerbPlan => (VP:other (V "must") (VP:other $1))
 MustVerbPlan => (VP:aux (V "have to") (VP:other $1))
 MustNotVerbPlan => (VP:mustneg (RP "not") (VP:other $1))
 MustNotVerbPlan => (VP:3ps (V "must") (RP "not") (VP:other $1))
 MustNotVerbPlan => (VP:other (V "must") (RP "not") (VP:other $1))
 MustNotVerbPlan => (VP:aux (V "not have to") (VP:other $1))
 PastPlan => (VP:3ps (V "has") (VP:other $1))
 PastPlan => (VP:other (V "have") (VP:other $1))
 FuturePlan => (VP:3ps (V "will") (VP:aux $1))
 FuturePlan => (VP:other (V "will") (VP:aux $1))
 FuturePlan => (VP:will (VP:aux $1))
 FuturePlan => (VP:willneg (VP:neg $1))
}

Z {
  EntryPlan ? number=massNoun => (Z:other)
  EntryPlan => (Z:3ps)
  YouPlan => (Z:other)
}"""
  
  implicit def str2Entry(value : String) : LexicalEntry = new DummyLexicalEntry() {
    override def getCanonicalForm() = new DummyForm() {
      override def getWrittenRep() = new Text(value,Language.ENGLISH.toString)
      override def getProperty(prop : Property) = Nil
    }
    override def getForms() = List(getCanonicalForm())
    override def getProperty(prop : Property) = Nil
    override def getURI() = java.net.URI.create("example:"+value)
  }
  
  @Test
  def testHead {
    val head1 = Head("DP","3ps"::Nil)
    val head2 = Head("DP",Nil)
    assertTrue(head1.value == head2.value)
    assertTrue(head1.opts zip head2.opts forall { case (l,r) => l == r })
    assertTrue(head1.opts.size >= head2.opts.size)
    assertTrue(head1.compatible(head2))
  }
  
  @Test
  def testRealizer {
    
    val grammar = GrammarParser.read(new StringReader(testGrammar)) 
    assertEquals(5,grammar.size)
    assertEquals(7,grammar(0).rules.size)
    assertEquals((2,2),grammar(0).rules(0).elem.childCount)
    assertEquals((2,Integer.MAX_VALUE),grammar(0).rules(5).elem.childCount)
    assertEquals((2,2),grammar(1).rules(4).elem.childCount)
    val testPlan = IfPlan(PredicatePlan(EntryPlan("student"),VerbPlan(Nil,"works",null)), PredicatePlan(EntryPlan("professor"),VerbPlan(Nil,"plays",null)))
    val realizer = new Realizer(new DummyLinguisticOntology(),grammar)
    
    assertTrue(None != realizer.realize(testPlan))
    val realization = realizer.realize(testPlan)
    assertEquals("a student works if a professor plays",realization)
  }
  
}
