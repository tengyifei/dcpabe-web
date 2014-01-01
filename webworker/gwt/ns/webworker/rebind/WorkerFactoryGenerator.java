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

import gwt.ns.webworker.client.WorkerEntryPoint;
import gwt.ns.webworker.client.WorkerModuleDef;
import gwt.ns.webworker.client.WorkerFactory;
import gwt.ns.webworker.linker.WorkerCompilationLinker;

import java.io.PrintWriter;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.ModuleDefLoader;

/**
 * A class for generating a WorkerFactory, which will in turn produce a
 * Worker appropriate for the calling permutation.
 * 
 * <p>Much of this class has to with validating the Worker request. An artifact
 * is then committed, requesting the {@link WorkerCompilationLinker} do the
 * heavy lifting in compiling the Worker and inserting it correctly. As many
 * configuration errors as possible should be caught here so that the compile
 * fails early.<p>
 */
public abstract class WorkerFactoryGenerator extends Generator {
	
	@Override
	public String generate(TreeLogger logger, GeneratorContext context,
			String typeName) throws UnableToCompleteException {
		
		TypeOracle typeOracle = context.getTypeOracle();
		
		// find type, subinterface of WorkerFactory
		JClassType sourceType = typeOracle.findType(typeName);
		if (sourceType == null) {
			logger.log(TreeLogger.ERROR, "Could not find type " + typeName);
			throw new UnableToCompleteException();
		}
		
		// name of class to be
		String genName = sourceType.getSimpleSourceName() + "Impl"
				+ getSourceSuffix();
		String pkgName = sourceType.getPackage().getName();
		String fullName = pkgName + "." + genName;
		
		PrintWriter out = context.tryCreate(logger, pkgName, genName);
		
		// finished if already generated
		if (out != null) {
			// retrieve name of Worker module from annotation
			WorkerModuleDef workerDef = 
				sourceType.getAnnotation(WorkerModuleDef.class);
			if (workerDef == null) {
				logger.log(TreeLogger.ERROR, "WorkerFactory declaration must be "
						+ "annotated with module name.");
				throw new UnableToCompleteException();
			}
			String moduleName = workerDef.value();
			
			// better way? should be cached after first load (in this process)
			ModuleDef modDef;
			try {
				modDef = ModuleDefLoader.loadFromClassPath(logger, moduleName, true);
			} catch (UnableToCompleteException e) {
				logger.log(TreeLogger.ERROR, "An attempt to load the Worker module annotated, \"" + moduleName + "\", failed. See preceeding error.");
				throw e;
			}
			
			// basic module validity check: one entry point extending valid
			// Worker entry point class.
			// checked here so as to fail as soon as possible
			
			// only one entrypoint is supported for now
			String[] entryPoints = modDef.getEntryPointTypeNames();
			if (entryPoints.length < 1) {
				logger.log(TreeLogger.WARN, "Worker defined in " + moduleName + " must have an entry point specified.");
				throw new UnableToCompleteException();
			} else if (entryPoints.length > 1) {
				logger.log(TreeLogger.WARN, "Only a single entry point is currently supported. " + moduleName + " appears to define more than one:");
				for (String enPoint : entryPoints)
					logger.log(TreeLogger.WARN, "EntryPoint: " + enPoint);
				String[] entryPoints2 = new String[1];
				entryPoints2[0]=entryPoints[entryPoints.length-1];
				entryPoints = entryPoints2;
				logger.log(TreeLogger.WARN, "Selected EntryPoint: " + entryPoints[0]);
				//throw new UnableToCompleteException();
			}
			
			// check to make sure entrypoint is worker compatible
			JClassType workerEntryType = typeOracle.findType(entryPoints[0]);
			JClassType superEntryType = typeOracle.findType(
					WorkerEntryPoint.class.getCanonicalName());
			if (!workerEntryType.isAssignableTo(superEntryType)) {
				logger.log(TreeLogger.ERROR, "Worker EntryPoint "
						+ workerEntryType.getName() + " must be assignable to "
						+ superEntryType.getName());
				throw new UnableToCompleteException();
			}
			
			// generate WebWorker factory
			generateWorkerFactory(logger, context, sourceType, genName, out, modDef, workerEntryType);
		}
		
		return fullName;
	}
	
	/**
	 * Lengthy method signature for generating {@link WorkerFactory}
	 * appropriate for native or emulated worker support.
	 * 
	 * @param logger
	 * @param context
	 * @param sourceType
	 * @param genName
	 * @param out
	 * @param modDef
	 * @param workerEntryType
	 * @throws UnableToCompleteException
	 */
	public abstract void generateWorkerFactory(TreeLogger logger,
			GeneratorContext context, JClassType sourceType,
			String genName, PrintWriter out,
			ModuleDef modDef, JClassType workerEntryType)
			throws UnableToCompleteException;
	
	/**
	 * @return Return the suffix to id generated type
	 */
	public abstract String getSourceSuffix();
}
