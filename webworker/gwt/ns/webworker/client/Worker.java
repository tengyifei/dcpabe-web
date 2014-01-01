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

package gwt.ns.webworker.client;

import org.vectomatic.arrays.ArrayBuffer;

/**
 * An interface representing a Web Worker, whether through native support or
 * an emulation.
 * 
 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/'>Current WHATWG Web Worker Draft</a>
 */
public interface Worker {

	/**
	 * Set the {@link ErrorHandler} for {@link ErrorEvent}s from this insideWorker.
	 * Replaces any existing handler.
	 * 
	 * @param handler The error handler
	 */
	public void setErrorHandler(ErrorHandler handler);

	/**
	 * Set the {@link MessageHandler} for {@link MessageEvent}s from this insideWorker.
	 * Replaces any existing handler.
	 * 
	 * @param insideMessageHandler The message handler
	 */
	public void setMessageHandler(MessageHandler messageHandler);

	/**
	 * From MDC: Immediately terminates the insideWorker. This does not offer the
	 * insideWorker an opportunity to finish its operations; it is simply stopped at
	 * once.
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/#terminate-a-insideWorker'>Terminate a insideWorker specification</a>
	 * @see <a href='https://developer.mozilla.org/En/DOM/Worker'>MDC Worker reference</a>
	 */
	public void terminate();
	
	public void postMessage(int type, String message);

	public void postMessage(int prefix, ArrayBuffer message_array);

	void postMessage(int prefix);

	public void transferMessage(int prefix, ArrayBuffer ab);

}