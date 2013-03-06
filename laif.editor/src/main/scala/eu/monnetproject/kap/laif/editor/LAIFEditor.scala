/****************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Monnet Project nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
import java.util.logging.Logger
import java.io._
import java.net.URI
import javax.servlet.http._
import org.osgi.framework._
import scala.collection.JavaConversions._
import scala.xml._
import scalasemweb.rdf.model._

/**
 *
 * @author John McCrae
 */
class LAIFEditor(controller: LAIFController) extends HttpServlet {

  import HTMLConversions._

  private val log = Logger.getLogger(this.getClass().getName())
  // private var context : BundleContext = null
  private val l10n = controller.getLexicon("laifeditor")

  def getPath = "/laifeditor"


  private val lexiconPath = """/([^/]+)/(...?)""".r
  private val addLexiconPath = """/([^/]+)/addlang/(...?)/(...?)""".r
  private val createPath = """/([^/]+)/create/(...?)""".r

  override def service(req: HttpServletRequest, res: HttpServletResponse) {
    req.getPathInfo() match {
      case addLexiconPath(appKey, srcLang, trgLang) => {
        log.info("adding a language")
        val rules = Option(controller.getLexicon(appKey).getRules(Language.get(srcLang))).getOrElse(new LAIFRuleSet(null, Language.get(srcLang), appKey))
        controller.updateRules(appKey, Language.get(trgLang), rules)
        res.sendRedirect("/laifeditor/" + appKey + "/" + trgLang)
      }
      case lexiconPath(appKey, trgLangStr) => {
        log.info("main page")
        val out = res.getWriter()
        val lang = req.getParameter("lang") match {
          case null => Language.ENGLISH
          case lang2 => Language.get(lang2)
        }
        val trgLang = Language.get(trgLangStr)
        controller.getLexicon(appKey).getRules(trgLang) match {
          case null => {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "No rules for " + appKey + " in " + trgLang + " got to " + appKey + "/create/"+lang+" to create it")
          }
          case ruleSet => {
            val context = LAIFEditorContext(appKey, ruleSet, trgLang, lang)
            req.getSession.setAttribute("ctxt", context)
            out.println(buildBody(context, lang).toString)
            res.setStatus(HttpServletResponse.SC_OK)
          }
        }
      }
      case createPath(appKey, lang) => {
        controller.updateRules(appKey, Language.get(lang), new LAIFRuleSet(null,Language.get(lang),appKey))
        res.setStatus(HttpServletResponse.SC_OK)
        res.getWriter.println("Created " + appKey)
      }
      case "/ajax/elemchange" => {
        val id = req.getParameter("id")
        val content = req.getParameter("content")
        log.info(id)
        log.info(content)
        try {
          val rule = unmake(XML.loadString("<head>" + content.toString.replaceAll("&nbsp;", "") + "</head>"))
          val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
          val resRule = context.rules.ruleMap.values() find {
            rule => Option(rule.key).getOrElse("").replaceAll("\\W", "") == id
          } getOrElse ({
            log.info("notfound" + id); throw new IllegalArgumentException()
          })
          val newRule = new LAIFRule(resRule.resource, resRule.key, resRule.param, rule.toList)
          context.rules.add(newRule)
          val newContext = LAIFEditorContext(context.appKey, context.rules, context.lang, context.uiLang)
          req.getSession.setAttribute("ctxt", newContext)
        } catch {
          case x: Exception => {
            log.warning(x.getMessage())
          }
        }
      }
      case "/ajax/removelexicon" => {
        log.info("ajax/removelexicon")
        val id = req.getParameter("id")
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val lexiconURI = context.rules.getLexicon()
        log.info(lexiconURI.toString())
        if (context.rules.getLexicon == lexiconURI) {
          context.rules.setLexicon(lexiconURI)
        }
        val newContext = LAIFEditorContext(context.appKey, context.rules, context.lang, context.uiLang)
        req.getSession.setAttribute("ctxt", newContext)
      }
      case "/ajax/addLexicon" => {
        log.info("ajax/addlexicon")
        val lexiconURI = URI.create(req.getParameter("uri"))
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        context.rules.setLexicon(lexiconURI)
        val newContext = LAIFEditorContext(context.appKey, context.rules, context.lang, context.uiLang)
        req.getSession.setAttribute("ctxt", newContext)
        val out = res.getWriter
        val resText = buildLexiconEntry(lexiconURI).toString()
        log.info(resText)
        out.println(resText)
        res.setStatus(HttpServletResponse.SC_OK)
        res.setContentType("text/plain")
      }
      case "/ajax/removerule" => {
        log.info("ajax/removerule")
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val id = req.getParameter("id")
        val rule = context.rules.ruleMap.values() find {
          rule => Option(rule.key).getOrElse("").replaceAll("\\W", "") == id
        } getOrElse (throw new IllegalArgumentException())
        context.rules.remove(rule)
        val newContext = LAIFEditorContext(context.appKey, context.rules, context.lang, context.uiLang)
        req.getSession.setAttribute("ctxt", newContext)
      }
      case "/ajax/addrule" => {
        log.info("ajax/addrule")
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val key = req.getParameter("key")
        val argCount = req.getParameter("argcount") match {
          case null => 0
          case x => x.toInt
        }
        val targetLexicon = controller.getLexicon(context.appKey)
        val resrc = URI.create("unknown:" + key.replaceAll("\\W", "").take(30))
        val rule = new LAIFRule(resrc, key, makeParams(resrc, argCount), new LAIFLiteral(key) :: Nil)
        context.rules.add(rule)
        val newContext = LAIFEditorContext(context.appKey, context.rules, context.lang, context.uiLang)
        req.getSession.setAttribute("ctxt", newContext)
        val out = res.getWriter
        val resText = (if (argCount > 0) {
          buildFuncRule(rule, context.uiLang)
        } else {
          buildNonFuncRule(rule, context.uiLang, targetLexicon, context)
        }).toString()
        log.info(resText)
        out.println(resText)
        res.setStatus(HttpServletResponse.SC_OK)
        res.setContentType("text/plain")
      }
      case "/ajax/evalrule" => {
        log.info("ajax/evalrule")
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val id = req.getParameter("id")
        val rule = context.rules.ruleMap().values() find {
          rule => Option(rule.key).getOrElse("").replaceAll("\\W", "") == id
        } getOrElse ({
          log.info("notfound" + id); throw new IllegalArgumentException()
        })
        val targetLexicon = controller.getLexicon(context.appKey)
        try {
          val result = rule.apply(targetLexicon.sparqlLexicon(context.lang), Nil, context.rules.ruleMap)
          val out = res.getWriter
          out.println(result)
          res.setStatus(HttpServletResponse.SC_OK)
          res.setContentType("text/plain")
        } catch {
          case x: Exception => {
            val out = res.getWriter
            out.println(x.getMessage())
            res.setStatus(HttpServletResponse.SC_OK)
            res.setContentType("text/plain")
          }
        }
      }
      case "/ajax/getfuncrules" => {
        log.info("ajax/funcrules")
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val result = for (rule <- context.rules.ruleMap.values() if !rule.param.isEmpty) yield {
          <option>
            {rule.resource}
          </option>
        }
        val out = res.getWriter
        out.println(result.toString())
        res.setStatus(HttpServletResponse.SC_OK)
        res.setContentType("text/html")
      }
      case "/ajax/addusercall" => {
        log.info("ajax/addusercall")
        val id = req.getParameter("id")
        val call = req.getParameter("call").drop(1)
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val rule = context.rules.ruleMap.values() find {
          rule => Option(rule.key).getOrElse("").replaceAll("\\W", "") == id
        } getOrElse ({
          log.info("notfound" + id); throw new IllegalArgumentException()
        })
        val callRule = context.rules.ruleMap.values() find {
          rule => rule.resource.toString() == ":" + call
        } getOrElse ({
          log.info("notfound" + call); throw new IllegalArgumentException()
        })
        val ns = rule.resource.asInstanceOf[QName].nameSpace
        val newRule = new LAIFRule(rule.resource, rule.key, rule.param, rule.value :+ new LAIFUserCall(ns & call, (for (i <- 1 to callRule.param.size) yield {
          new LAIFLiteral(i.toString)
        }).toList))
        val result = <span contenteditable="true">
          {makeValue(newRule.value.toList)}
        </span>%%
        ("onkeyup" -> ("doChangeCall('" + id + "')"), "id" -> id)
        context.rules.remove(rule)
        context.rules.add(newRule)
        val newContext = LAIFEditorContext(context.appKey, context.rules, context.lang, context.uiLang)
        req.getSession.setAttribute("ctxt", newContext)
        val out = res.getWriter
        out.println(result.toString())
        res.setStatus(HttpServletResponse.SC_OK)
        res.setContentType("text/html")
      }

      case "/ajax/getforms" => {
        log.info("ajax/getforms")
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val result = for (sense <- controller.getForms(context.rules.getLexicon().toString(), null)) yield {
          <option>
            {sense.toString()}
          </option>
        }
        val out = res.getWriter
        out.println(result.toString())
        res.setStatus(HttpServletResponse.SC_OK)
        res.setContentType("text/html")
      }
      case "/ajax/addformcall" => {
        log.info("ajax/addformcall")
        val id = req.getParameter("id")
        val call = req.getParameter("call")
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val rule = context.rules.ruleMap.values() find {
          rule => Option(rule.key).getOrElse("").replaceAll("\\W", "") == id
        } getOrElse ({
          log.info("notfound" + id); throw new IllegalArgumentException()
        })
        val formCallObj = unmakeName(call)
        val newRule = new LAIFRule(rule.resource.uri, rule.key, rule.param, rule.value :+ new LAIFFormCall(formCallObj.uri, Map[URI, URI]()))
        val result = <span contenteditable="true">
          {makeValue(newRule.value.toList)}
        </span>%%
        ("onkeyup" -> ("doChangeCall('" + id + "')"), "id" -> id)
        context.rules.remove(rule)
        context.rules.add(newRule)
        val newContext = LAIFEditorContext(context.appKey, context.rules, context.lang, context.uiLang)
        req.getSession.setAttribute("ctxt", newContext)
        val out = res.getWriter
        out.println(result.toString())
        res.setStatus(HttpServletResponse.SC_OK)
        res.setContentType("text/html")
      }
      case "/save" => {
        val context = req.getSession.getAttribute("ctxt").asInstanceOf[LAIFEditorContext]
        val out = new PrintWriter("load/" + context.appKey + "." + context.lang + ".laif")
        //LAIFWriter.write("http://test/", context.rules, out)
      }
      case path => {
        if (path == "/" || path == "" || path == null) {

        } else {
          val thisBundle = FrameworkUtil.getBundle(this.getClass)
          val resource = if (thisBundle == null) {
            this.getClass.getResource(path)
          } else {
            thisBundle.getResource(path)
          }
          if (resource != null) {
            log.info("resource " + resource)
            val resIn = resource.openStream
            val out = res.getOutputStream
            val buf = new Array[Byte](2048)
            var n = 0
            while ( {
              n = resIn.read(buf, 0, 2048); n
            } != -1) {
              out.write(buf, 0, n)
            }
            out.flush
            if (req.getPathInfo.endsWith(".js")) {
              res.setContentType("text/javascript")
            } else if (req.getPathInfo.endsWith(".css")) {
              res.setContentType("text/css")
            } else if (req.getPathInfo.endsWith(".png")) {
              res.setContentType("image/png")
            } else {
              log.warning("Unknown mime type: " + req.getPathInfo)
            }
            res.setStatus(HttpServletResponse.SC_OK)
          } else {
            log.info("404: " + path)
            res.sendError(HttpServletResponse.SC_NOT_FOUND)
          }
        }
      }
    }
  }

  private def makeParams(res: Resource, count: Int): List[LAIFVariable] = if (count > 0) {
    val countVar = count match {
      case 1 => "_X"
      case 2 => "_Y"
      case 3 => "_Z"
      case higher => "_" + higher
    }
    (res match {
      case QName(ns, suffix) => new LAIFVariable(QName(ns, suffix + countVar))
    }) :: makeParams(res, count - 1)
  } else {
    Nil
  }


  private def buildLexiconEntry(lexiconURI: URI) = {
    if(lexiconURI == null) {
    <tr>
      <td width="100%">null lexicon</td>
    </tr>
    } else {
    val id = lexiconURI.toString().replaceAll("\\W", "")
    <tr>
      <td width="100%">
        {<a>
        {lexiconURI.toString()}
      </a> %
        Attribute(None, "href", Text(lexiconURI.toString()), Null)}
      </td>
      <td>
        {<div class="closebutton">
        &#x00d7;
      </div> %% ("onclick" -> ("closeLexicon('" + id + "')"))}
      </td>
    </tr> %
      Attribute(None, "id", Text(id), Null)
    }
  }

  private def buildFuncRule(rule: LAIFRule, lang: Language) = {
    val id = Option(rule.key).getOrElse("").replaceAll("\\W", "")
    (<tr>
      <td style="width:10em;">
        <b>
          {l10n.get("Key", lang) + ":"}
        </b>
      </td>
      <td style="width:80em;">
        {Option(rule.key).getOrElse("") + " ("}{var first = true
      for (p <- rule.param) yield {
        if (first) {
          first = false
          makeValue(p :: Nil)
        } else {
          Text(", ") :+ makeValue(p :: Nil)
        }
      }}{")"}
      </td>
    </tr> %% ("id" -> (id + "_1"))) +:
      (<tr class="laiflastrow">
        <td style="width:10em;">
          <i>
            {l10n.get("Rule", lang) + ":"}
          </i>
        </td>
        <td style="width:80em;">
          {val id = Option(rule.key).getOrElse("")
        <span contenteditable="true">
          {makeValue(rule.value.toList)}
        </span>%
        Attribute(None, "onkeyup", Text("doChangeCall('" + id + "')"), Null) %
          Attribute(None, "id", Text(id), Null)}
        </td>

        <td>
          {<div class="closebutton">
          &#x00d7;
        </div> %% ("onclick" -> ("deleteFuncRule('" + id + "')"))}
        </td>
      </tr> %% ("id" -> (id + "_2")))
  }

  private def buildNonFuncRule(rule: LAIFRule, lang: Language,
                               targetLexicon: LAIFLocalizationLexicon,
                               lec: LAIFEditorContext) = {
    val id = Option(rule.key).getOrElse("").replaceAll("\\W", "")
    (<tr>
      <td style="width:10em;">
        <b>
          {l10n.get("Key", lang) + ":"}
        </b>
      </td>
      <td style="width:80em;">
        {Option(rule.key).getOrElse("")}
      </td>
    </tr> %% ("id" -> (id + "_1"))) +:
      (<tr>
        <td style="width:10em;">
          <i>
            {l10n.get("Rule", lang) + ":"}
          </i>
        </td>{<td style="width:80em;">
          {<span contenteditable="true">
            {makeValue(rule.value.toList)}
          </span>%
          Attribute(None, "onkeyup", Text("doChangeCall('" + id + "')"), Null) %
            Attribute(None, "id", Text(id), Null)}
        </td> %% ("id" -> (id + "_editable"))}<td>
          {<span class="closebutton">
            &#x00d7;
          </span> %% ("onclick" -> ("deleteNonFuncRule('" + id + "')")) +: Text(" ") +:
            <span class="addusercall">
              {l10n.get("User Call", lang)}
            </span> %% ("onclick" -> ("addUserCallDialog('" + id + "')")) +: Text(" ") +:
            <span class="addformcall">
              {l10n.get("lemon Form", lang)}
            </span> %% ("onclick" -> ("addFormCallDialog('" + id + "')"))}
        </td>
      </tr> %% ("id" -> (id + "_2"))) +:
      (<tr class="laiflastrow">
        <td style="width:10em;">
          <i>
            {l10n.get("Result", lang) + ":"}
          </i>
        </td>{<td style="width:80em;">
          {rule.key match {
            case null => "#NOKEY#"
            case k => try {
              targetLexicon.get(k, lec.lang)
            } catch {
              case x: LAIFApplyException => x.getMessage()
            }
          }}
        </td> %% {
          "id" -> (id + "_result")
        }}
      </tr> %% ("id" -> (id + "_3")))
  }

  private def buildBody(lec: LAIFEditorContext, lang: Language) =
    <html>
      <head>
        <title>
          {l10n.get("LAIF Editor", lang)}
        </title>
        <script type="text/javascript" src="/laifeditor/js/jquery-1.6.2.min.js"/>
        <script type="text/javascript" src="/laifeditor/js/jquery-ui-1.8.16.min.js"/>
        <script type="text/javascript" src="/laifeditor/js/laifeditor.js"/>
        <link href="/laifeditor/css/jquery-ui-1.8.16.smoothness.css" rel="stylesheet" type="text/css"/>
        <link href="/laifeditor/css/laifeditor.css" rel="stylesheet" type="text/css"/>
      </head>
      <body>
        <h1>LAIF Editor</h1>{val (nonFunctionRules, functionRules) = lec.rules.ruleMap().values() partition {
        _.param.isEmpty()
      }
      val targetLexicon = controller.getLexicon(lec.appKey)
      <table id="lexicatable">
        <tr class="ui-widget-header">
          <td width="100%">
            <h3>
              {l10n.get("Lexica", lang)}
            </h3>
          </td>
          <td>
            <span class="addbutton" onclick="addLexicon()">+</span>
          </td>
        </tr>{buildLexiconEntry(lec.rules.getLexicon())}
      </table>
        <div>

          <table>
            <tr class="ui-widget-header">
              <td width="100%">
                <h3>
                  {l10n.get("Function Rules", lang)}
                </h3>
              </td>
              <td>
                <span class="addbutton" onclick="addFuncRule()">+</span>
              </td>
            </tr>
          </table>
          <table id="funcruletable">
            {for (rule <- functionRules) yield {
            buildFuncRule(rule, lang)
          }}
          </table>
          <table>
            <tr class="ui-widget-header">
              <td width="100%">
                <h3>
                  {l10n.get("Simple Rules", lang)}
                </h3>
              </td>
              <td>
                <span class="addbutton" onclick="addNonFuncRule()">+</span>
              </td>
            </tr>
          </table>
          <table id="nonfuncruletable">
            {for (rule <- nonFunctionRules) yield {
            updateNameSpace(rule.resource)
            buildNonFuncRule(rule, lang, targetLexicon, lec)
          }}
          </table>
        </div>}<button onclick="confirmSave()">
        {l10n.get("Save", lang)}
      </button>{<div id="confirm_save_dialog" style="display:none">
        <p>
          {l10n.get("Are you sure you want to save?", lang)}
        </p>
        <button onclick="window.location='/laifeditor/save'">
          {l10n.get("Yes", lang)}
        </button>
        <button onclick="closeSaveDialog()">
          {l10n.get("No", lang)}
        </button>
      </div> %% ("title" -> l10n.get("Confirm Save", lang))}{<div id="add_lexicon_dialog" style="display:none">
        <div>
          {l10n.get("Enter lexicon URI", lang) + ": "}<input id="add_lexicon_dialog_lexiconURI" size="58" type="text" name="lexiconURI" value="http://"/>
          <br/>
          <button onclick="doAddLexicon()">
            {l10n.get("OK", lang)}
          </button>
        </div>
      </div> %% ("title" -> l10n.get("Add Lexicon", lang))}{<div id="add_funcrule_dialog" style="display:none">
        <div>
          {l10n.get("Enter key", lang) + ": "}<br/>
          <textarea id="add_funcrule_dialog_key" cols="40" rows="10" value=" "/>
          <br/>{l10n.get("Number of Arguments", lang) + ": "}<input id="add_funcrule_dialog_argcount" size="10" type="text" value="1"/>
          <br/>
          <button onclick="doAddFuncRule()">
            {l10n.get("OK", lang)}
          </button>
        </div>
      </div> %% ("title" -> l10n.get("Add Functional Rule", lang))}{<div id="add_nonfuncrule_dialog" style="display:none">
        <div>
          {l10n.get("Enter key", lang) + ": "}<br/>
          <textarea id="add_nonfuncrule_dialog_key" cols="40" rows="10" value=" "/>
          <br/>
          <button onclick="doAddNonFuncRule()">
            {l10n.get("OK", lang)}
          </button>
        </div>
      </div> %% ("title" -> l10n.get("Add Simple Rule", lang))}{<div id="add_usercall_dialog" style="display:none">
        <div>
          {l10n.get("Functional Rule", lang) + ": "}<select id="add_usercall_select"/>
          <br/>
          <button onclick="doAddUserCall()">
            {l10n.get("OK", lang)}
          </button>
        </div>
      </div> %% ("title" -> l10n.get("Add User Call", lang))}{<div id="add_formcall_dialog" style="display:none">
        <div>
          {l10n.get("Ontology URI", lang) + ": "}<select id="add_formcall_select"/>
          <br/>
          <button onclick="doAddFormCall()">
            {l10n.get("OK", lang)}
          </button>
        </div>
      </div> %% ("title" -> l10n.get("Add lemon Form", lang))}
      </body>
    </html>

}

case class LAIFEditorContext(val appKey: String, val rules: LAIFRuleSet, val lang: Language, val uiLang: Language)
