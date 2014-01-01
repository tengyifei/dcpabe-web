/*
 * Copyright 2010 Brendan Kenny
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

package gwt.ns.webworker.rebind;

import gwt.ns.webworker.client.Worker;
import gwt.ns.webworker.client.WorkerImplNative;
import gwt.ns.webworker.linker.WorkerCompilationLinker;
import gwt.ns.webworker.linker.WorkerRequestArtifact;

import java.io.PrintWriter;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Commits a request for {@link WorkerCompilationLinker} to compile a module as
 * a Worker and generates a simple class to instantiate that Worker from the
 * resulting script.
 */
public class NativeWorkerFactoryGenerator extends WorkerFactoryGenerator {
	static final String SOURCE_NAME_SUFFIX = "Native";

	@Override
	public void generateWorkerFactory(TreeLogger logger,
			GeneratorContext context, JClassType sourceType,
			String genName, PrintWriter out,
			ModuleDef modDef, JClassType workerEntryType)
			throws UnableToCompleteException {
		
		// native worker, so request worker compilation
		WorkerRequestArtifact request = new WorkerRequestArtifact(
				modDef.getCanonicalName(), modDef.getName());
		context.commitArtifact(logger, request);
		
		ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
		        sourceType.getPackage().getName(), genName);
		
		// imports and interface
		f.addImport(Worker.class.getName());
		f.addImport(WorkerImplNative.class.getName());
		f.addImplementedInterface(sourceType.getName());
		
		// new generated source file
		if (out != null) {
			SourceWriter sw = f.createSourceWriter(context, out);
			
			// @Override
			// public Worker createAndStart() {
			//   return WorkerImplNative.create("PLACEHOLDER_PATH"); }
			// Note: placeholder path will be replaced by linker
	        sw.println("@Override");
			sw.println("public Worker createAndStart() {");
	        sw.indent();
	        sw.print("return WorkerImplNative.create(\"");
	        sw.print(request.getRelativePlaceholderUrl());
	        sw.println("\");");
	        sw.outdent();
	        sw.println("}");
	        
	        sw.commit(logger);
		}
	}
	
	@Override
	public String getSourceSuffix() {
		return SOURCE_NAME_SUFFIX;
	}
}
