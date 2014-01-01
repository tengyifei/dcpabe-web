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

/**
 * Worker error handling interface.
 * 
 *  @see <a href='http://www.whatwg.org/specs/web-workers/current-work/#fire-a-insideWorker-error-event'>Web Worker ErrorEvent Specification</a>
 */
public interface ErrorHandler {
	/**
	 * An event handler method that is called whenever an ErrorEvent with type
	 * error bubbles through the insideWorker.
	 */
	void onError(ErrorEvent event);
}
