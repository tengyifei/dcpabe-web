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

package gwt.ns.webworker.linker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.Util;

/**
 * Handles the compilation of Worker modules in a separate process.
 */
// TODO: generalize build process or expose ability to do modify
public class WorkerCompiler {
	/**
	 * Buffers output of compilation process to specified
	 * logger via a separate thread.
	 */
	static class PipeOutput implements Runnable {
		TreeLogger outLogger;
		BufferedReader in;
		public PipeOutput(final TreeLogger logger, final InputStream is) {
			outLogger = logger;
			in = new BufferedReader(new InputStreamReader(is));
		}
			
		@Override
		public void run() {
			String line;
			try {
				while ((line = in.readLine()) != null) {
					// TODO: read worker requests from child process
					outLogger.log(TreeLogger.INFO, "> " + line);
				}
			} catch (IOException e) {
				outLogger.log(TreeLogger.ERROR, "Error in reading output from compilation.", e);
			}
		}
	}
	
	public static final String RECURSION_FLAG_PROP = "ns.recursed";
	
	private static final String TEMP_WAR_DIR_NAME = "temp_worker_dir";
	private static final File TEMP_WAR_DIR = new File(TEMP_WAR_DIR_NAME);
	
	/**
	 * Compile the worker requests. If this is the initial compilation process,
	 * a new process is started and the worker modules are compiled within. If
	 * this is a child process, requests are sent up to parent process for it
	 * to handle compilation.
	 * 
	 * @param logger
	 * @param requests Workers to compile
	 * 
	 * @return Compiled worker scripts by request or null if nothing compiled
	 * 
	 * @throws UnableToCompleteException
	 */
	public static SortedMap<WorkerRequestArtifact, HashMap<String, Tuple<String, String>>> run(TreeLogger logger,
			final SortedSet<WorkerRequestArtifact> requests)
			throws UnableToCompleteException {
		
		if (!isRecursed()) {
			return runCompiler(logger, requests);
		} else {
			return null; // TODO: recursive module handling
		}
	}
	
	/**
	 * @return Returns true if called within a recursive GWT compiler process.
	 */
	public static boolean isRecursed() {
		return System.getProperty(RECURSION_FLAG_PROP) != null;
	}
	
	
	/**
	 * Compiles requests in new process.
	 * 
	 * @param logger
	 * @param requests
	 * @return Compiled worker scripts.
	 * @throws UnableToCompleteException
	 */
	private static SortedMap<WorkerRequestArtifact, HashMap<String, Tuple<String, String>>> runCompiler(
			TreeLogger logger, SortedSet<WorkerRequestArtifact> requests)
				throws UnableToCompleteException {
		
		List<String> commands = new ArrayList<String>();
		commands.add("java");
		
		 // flag child process as recursive. value unimportant
		commands.add("-D" + RECURSION_FLAG_PROP + "=" + "true");
		commands.add("-Dgwt.persistentunitcache=false");
		
		//inherit classpath from this process
		commands.add("-cp");
		commands.add("\""+System.getProperty("java.class.path")+"\"");
		
		commands.add("com.google.gwt.dev.Compiler");

		// TODO: best practices for fixed directory that will be erased?
		// destination war directory
		commands.add("-war");
		commands.add(TEMP_WAR_DIR_NAME);
		
		//commands.add("-style");
		//commands.add("PRETTY");
		
		commands.add("-optimize");
		commands.add("9");
		
		commands.add("-localWorkers");
		commands.add("6");
		
		// TODO: user specified compiler options...workers, output style, etc
		// are these even visible?
		
		for (WorkerRequestArtifact req : requests) {
			commands.add(req.getCanonicalName());
		}
		
		ProcessBuilder compileBuilder = new ProcessBuilder(commands);
		compileBuilder.redirectErrorStream(true);
		
		TreeLogger compLogger = logger.branch(TreeLogger.INFO, "Recursively compiling Worker modules...");
		
		// default print command so build system mismatches are obvious
		StringBuilder buf = new StringBuilder();
		for (String com : commands) {
			buf.append(com + " ");
		}
		logger.log(TreeLogger.INFO, "Executing cmd: \"" + buf.toString() +"\"");
		
		Process compile;
		try {
			compile = compileBuilder.start();
		} catch (IOException e) {
			compLogger.log(TreeLogger.ERROR, "Unable to compile.", e);
			throw new UnableToCompleteException();
		}
		
		// new thread for piping compiler output to logger
		PipeOutput pipe = new PipeOutput(compLogger, compile.getInputStream());
		new Thread(pipe).start();
		
		int exitValue;
		try {
			// block until compiler finished
			exitValue = compile.waitFor();
		} catch (InterruptedException e) {
			compLogger.log(TreeLogger.ERROR, "Thread interrupted while waiting for compilation.", e);
			throw new UnableToCompleteException();
		}
		if (exitValue != 0) {
			compLogger.log(TreeLogger.ERROR, "Error in compilation. See previous error.");
			throw new UnableToCompleteException();
		}
		
		SortedMap<WorkerRequestArtifact, HashMap<String, Tuple<String, String>>> workerScripts =
				new TreeMap<WorkerRequestArtifact, HashMap<String, Tuple<String, String>>>();
		
		for (WorkerRequestArtifact req : requests) {
			assert (!workerScripts.containsKey(req)) : "Module " + req.getName() + " was likely compiled twice.";
			
			HashMap<String, Tuple<String, String>> perm_map = new HashMap<String, Tuple<String, String>>();
			
			String name = req.getName();
			File mappingFile = new File(TEMP_WAR_DIR, name + File.separator + "mapping_" + name);
			if (!mappingFile.isFile()) {
				compLogger.log(TreeLogger.ERROR, "Mapping file " + mappingFile.getPath() + " not found as expected!");
				throw new UnableToCompleteException();
			}

			String[] mappings = Util.readFileAsString(mappingFile).split("\n");
			for (String mapping:mappings){
				String[] parts = mapping.split(" ");
				if (parts.length!=2) continue;
				
				File scriptFile = new File(TEMP_WAR_DIR, name + File.separator + parts[0]);
				if (!scriptFile.isFile()) {
					compLogger.log(TreeLogger.ERROR, "Script file " + scriptFile.getPath() + " not found as expected. This is likely because the build system is not flexible enough to fit your needs. File an issue!");
					throw new UnableToCompleteException();
				}
				String script = Util.readFileAsString(scriptFile);
				perm_map.put(parts[1], new Tuple<String, String>(script,parts[0]));
			}
			workerScripts.put(req, perm_map);
		}
		
		// delete temp directory
		// TODO: if this was a temp directory, this would feel a lot safer
		Util.recursiveDelete(TEMP_WAR_DIR, false);
		
		if (workerScripts.isEmpty()) {
			workerScripts = null;
		}
		
		return workerScripts;
	}	
}
