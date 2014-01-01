/*
 * Copyright 2009 Brendan Kenny
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

// This code originated in code from SpeedTracer, r3
// http://code.google.com/p/speedtracer/source/detail?r=3

package gwt.ns.webworker.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Event structure returned whenever an uncaught runtime script error occurs in
 * one of the insideWorker's scripts. See {@link ErrorHandler}
 * 
 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/#fire-a-insideWorker-error-event'>Web Worker ErrorEvent Specification</a>
 */
public class ErrorEvent extends JavaScriptObject {
	/*
	 * Note: since currently errors do nothing in emulated Workers, ErrorEvents
	 * are found only as JSO representations of native events
	 */
	
	
	protected ErrorEvent() {
		// constructors must be protected in JavaScriptObject overlays.
	}

	/**
	 * @return The absolute URL of the script in which the error originally
	 * occurred
	 */
	public final native String getFilename() /*-{
		return this.filename;
	}-*/;

	/**
	 * @return The line number where the error occurred in the script
	 */
	public final native int getLineNumber() /*-{
		return this.lineno;
	}-*/;

	/**
	 * @return A human-readable error message
	 */
	public final native String getMessage() /*-{
		return this.message;
	}-*/;
}
