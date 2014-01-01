/*
 * Copyright 2009 Brendan Kenny
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// This file largely adapted from
// com.google.gwt.core.linker.SingleScriptLinker r6520

package gwt.ns.webworker.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;
import com.google.gwt.dev.About;
import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.dev.util.Util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * A Linker for producing a single JavaScript file from a GWT module and
 * packaging it as a Web Worker. The use of this Linker requires that the
 * module have exactly one distinct compilation result. If it does not, a
 * (hopefully useful) list of properties that would cause multiple compilation
 * results will be logged.
 * 
 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/'>Web Worker Draft Spec</a>
 */
@LinkerOrder(Order.PRIMARY)
public class WorkerModuleLinker extends SelectionScriptLinker {
  public static final String WORKER_EXTENSION = ".cache.js";

  @Override
  public String getDescription() {
    return "Web Worker Module";
  }

  @Override
  public ArtifactSet link(TreeLogger logger, LinkerContext context,
      ArtifactSet artifacts) throws UnableToCompleteException {
	  
    ArtifactSet toReturn = new ArtifactSet(artifacts);
    
    // Find the single CompilationResult
    Set<CompilationResult> results = artifacts.find(CompilationResult.class);
    
    StringBuffer sb = new StringBuffer();
    for (CompilationResult result:results){
    	EmittedArtifact ea = emitSelectionScript(logger, context, artifacts, result);
    	toReturn.add(ea);
    	boolean out = false;
    	for (SortedMap<SelectionProperty, String> map : result.getPropertyMap()) {
    	      for (Map.Entry<SelectionProperty, String> entry : map.entrySet()) {
    	        if (entry.getKey().getName().contentEquals("user.agent")){
    	        	sb.append(ea.getPartialPath()+" "+entry.getValue()+"\n");
    	        	out = true;
    	        	break;
    	        }
    	      }
    	      if (out) break;
    	}
    }
    
    toReturn.add(emitString(logger, sb.toString(), "mapping_"+context.getModuleName()));
    
    return toReturn;
  }

  protected Collection<Artifact<?>> doEmitCompilation(TreeLogger logger,
      LinkerContext context, CompilationResult result, ArtifactSet artifacts)
      throws UnableToCompleteException {
	  
    if (result.getJavaScript().length != 1) {
      logger.branch(TreeLogger.ERROR,
          "The module must not have multiple fragments when using the "
              + getDescription() + " Linker.", null);
      throw new UnableToCompleteException();
    }
    
    return super.doEmitCompilation(logger, context, result, artifacts);
  }

  protected EmittedArtifact emitSelectionScript(TreeLogger logger,
      LinkerContext context, ArtifactSet artifacts,
      CompilationResult result)
      throws UnableToCompleteException {

    DefaultTextOutput out = new DefaultTextOutput(true);
    
    // Emit the selection script from template
    String bootstrap = generateSelectionScript(logger, context, artifacts);
    bootstrap = context.optimizeJavaScript(logger, bootstrap);
    out.print(bootstrap);
    out.newlineOpt();

    // Emit the module's JS within a closure.
    out.print("(function () {");
    out.newlineOpt();
    out.print("var $gwt_version = \"" + About.getGwtVersionNum() + "\";");
    out.newlineOpt();
    
    /* Point $wnd and $doc to insideWorker global scope. Shouldn't be used, but there
     * in case preexisting code uses either as a generic global variable
     * normal access of $wnd and $doc attributes and methods will be broken,
     * per Worker spec
     */
    out.print("var $self = self;");
    out.newlineOpt();
    out.print("var $wnd = self;");
    out.newlineOpt();
    out.print("var $doc = self;");
    out.newlineOpt();
    
    out.print("var $moduleName, $moduleBase;"); //needed if no stats/error handling?
    out.newlineOpt();
    out.print("var $stats = null;");
    out.newlineOpt();

    // append module code

    //CompilationResult result = results.iterator().next();
    
    out.print("var $strongName = '" + result.getStrongName() + "';");
    out.newlineOpt();

    // get actual compiled javascript and output
    // only one fragment currently supported (no runAsync)
    String[] js = result.getJavaScript();
    if (js.length != 1) {
      logger.log(TreeLogger.ERROR,
          "The module must not have multiple fragments when using the "
              + getDescription() + " Linker. Use of GWT.runAsync within Worker"
              + " code is the most likely cause of this error.", null);
      throw new UnableToCompleteException();
    }
    out.print(js[0]);

    // Generate the call to tell the bootstrap code that we're ready to go.
    out.newlineOpt();
    out.print("if (" + context.getModuleFunctionName() + ") "
        + context.getModuleFunctionName() + ".onScriptLoad(gwtOnLoad);");
    out.newlineOpt();
    out.print("})();");
    out.newlineOpt();

    // TODO: this naming scheme helps WorkerCompilationLinker, but users
    // compiling separate worker scripts may desire a strong file name
    return emitString(logger, out.toString(), context.getModuleName()
    	+"_"
    	+Util.computeStrongName(out.toString().getBytes())
        + WORKER_EXTENSION);
  }

  /**
   * Output the deferred binding properties to the logger to help the user of
   * this linker determine what is causing multiple compilation permutations.
   * 
   * @param logger the TreeLogger to record to
   * @param properties The deferred binding properties
   */
  protected void logPermutationProperties(TreeLogger logger,
      SortedSet<SelectionProperty> properties) {
    logger.log(TreeLogger.INFO, "Deferred binding properties of current " +
      "module:");
    for (SelectionProperty property : properties) {
      String name = property.getName();
      String value = property.tryGetValue();
      if (value == null) {
        value = "**Varies. Probable cause of multiple permutations.**";
      } else {
        value = value + ". (constant)";
      }

      logger.log(TreeLogger.INFO, "Property Name: " + name);
      logger.log(TreeLogger.INFO, "        Value: " + value);
    }
  }

  /**
   * Unimplemented. Normally required by
   * {@link #doEmitCompilation(TreeLogger, LinkerContext, CompilationResult)}.
   */
  @Override
  protected String getCompilationExtension(TreeLogger logger,
      LinkerContext context) throws UnableToCompleteException {
    throw new UnableToCompleteException();
  }

  /**
   * Unimplemented. Normally required by
   * {@link #doEmitCompilation(TreeLogger, LinkerContext, CompilationResult)}.
   */
  @Override
  protected String getModulePrefix(TreeLogger logger, LinkerContext context,
      String strongName) throws UnableToCompleteException {
    throw new UnableToCompleteException();
  }

  /**
   * Unimplemented. Normally required by
   * {@link #doEmitCompilation(TreeLogger, LinkerContext, CompilationResult)}.
   */
  @Override
  protected String getModuleSuffix(TreeLogger logger, LinkerContext context)
      throws UnableToCompleteException {
    throw new UnableToCompleteException();
  }

  @Override
  protected String getSelectionScriptTemplate(TreeLogger logger,
      LinkerContext context) throws UnableToCompleteException {
    return "gwt/ns/webworker/linker/WorkerScriptTemplate.js";
  }
}
