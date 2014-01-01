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
import gwt.ns.webworker.client.WorkerImplProxy;

import java.io.PrintWriter;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Generates a simple class that wraps the entry point class in a
 * Worker-emulating proxy and returns that proxy. No request artifact is
 * emitted because no module needs to be compiled.
 */
public class EmulatedWorkerFactoryGenerator extends WorkerFactoryGenerator {
	static final String SOURCE_NAME_SUFFIX = "Emulated";

	@Override
	public void generateWorkerFactory(TreeLogger logger,
			GeneratorContext context, JClassType sourceType,
			String genName, PrintWriter out,
			ModuleDef modDef, JClassType workerEntryType)
			throws UnableToCompleteException {
		
		ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
		        sourceType.getPackage().getName(), genName);
		
		// imports and interface
		f.addImport(Worker.class.getName());
		f.addImport(WorkerImplProxy.class.getName());
		f.addImport(workerEntryType.getQualifiedSourceName());
		f.addImplementedInterface(sourceType.getName());
		
		// new generated source file
		SourceWriter sw = f.createSourceWriter(context, out);
		
		// @Override
		// public Worker createAndStart() {
		//  WorkerImplProxy proxy = new WorkerImplProxy(new RationalsWorker());
		//  return proxy;
		// }
        sw.println("@Override");
		sw.println("public Worker createAndStart() {");
        sw.indent();
        
        sw.print("WorkerImplProxy proxy = new WorkerImplProxy(new ");
        sw.print(workerEntryType.getSimpleSourceName());
        sw.println("());");
        sw.println("return proxy;");
        
        sw.outdent();
        sw.println("}");
        
        sw.commit(logger);
	}
	
	@Override
	public String getSourceSuffix() {
		return SOURCE_NAME_SUFFIX;
	}
}
