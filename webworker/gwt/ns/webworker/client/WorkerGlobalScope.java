/*
 * Copyright 2010 Brendan Kenny
 * Copyright 2009 Google Inc.
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

// This code originated in part from code from SpeedTracer, r3
// http://code.google.com/p/speedtracer/source/detail?r=3

package gwt.ns.webworker.client;

import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * The global scope of a Worker. Window and Document do not exist
 * within a Worker.
 * 
 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/#workerglobalscope'>WorkerGlobalScope in current Web Workers Draft</a>
 */
public interface WorkerGlobalScope {
	/**
	 * When called, any tasks currently queued for this Worker are discarded
	 * and further tasks cannot be queued. According to the Worker
	 * specification, the current task is allowed to continue for a short,
	 * unspecified length of time and then halted (as opposed to
	 * {@link Worker#terminate()}).
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/#dom-workerglobalscope-close'>Specified close() routine</a>
	 */
	public void close();

	/**
	 * Returns the {@link WorkerLocation} object specific to this Worker.
	 * 
	 * @return WorkerLocation for this Worker
	 */
	public WorkerLocation getLocation();

	/**
	 * Import a script into a Worker and execute it. This method is
	 * synchronous. Note that the location of the imported script is evaluated
	 * <em>relative to the Worker's creation script</em>.
	 * 
	 * @param URL of script to import
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/#dom-workerglobalscope-importscripts'>Specified importScripts() routine</a>
	 */
	public void importScript(String url);

	/**
	 * Set the {@link ErrorHandler} for {@link ErrorEvent}s within this Worker.
	 * Replaces any existing handler.
	 * 
	 * @param handler The error handler
	 */
	public void setErrorHandler(ErrorHandler handler);

	/**
	 * Set the {@link MessageHandler} for {@link MessageEvent}s within this
	 * Worker. Replaces any existing handler.
	 * 
	 * @param insideMessageHandler The message handler
	 */
	public void setMessageHandler(MessageHandler messageHandler);

	public void postMessage(int prefix, ArrayBuffer ab);

	public void transferMessage(int prefix, ArrayBuffer message_array);

	void postMessage(int type);
	
	/**
	 * Sends a message to the MessageHandler registered by the Worker's
	 * creator. This accepts a single String as the contents of the message.
	 * 
	 * @param message Message to pass to insideWorker's creator.
	 * 
	 * @see {@link Worker#postMessage(String)}
	 */
	public void postMessage(int prefix, String message);
}
