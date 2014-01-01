/*
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
package gwt.ns.webworker.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A WorkerLocation object represents an absolute URL set at a Worker's
 * creation. The WorkerLocation interface also has the complement of URL
 * decomposition IDL attributes, protocol, host, port, hostname, pathname,
 * search, and hash.
 * 
 * <p>Note that these values will likely vary if this location
 * represents a Worker location proper or that of the main script.</p>
 * 
 * @see <a href='http://www.whatwg.org/specs/web-workers/current-work/#workerlocation'>WorkerLocation Specification</a>
 */
public class WorkerLocation extends JavaScriptObject {

	protected WorkerLocation() {
		// protected constructor required for JavaScriptObject overlay.
	}

	public final native String getHash() /*-{
    	return this.hash;
	}-*/;

	public final native String getHost() /*-{
		return this.host;
	}-*/;

	public final native String getHostname() /*-{
		return this.hostname;
	}-*/;

	/**
	 * @return The absolute URL that the object represents.
	 */
	public final native String getHref() /*-{
		return this.href;
	}-*/;

	public final native String getPathname() /*-{
		return this.pathname;
	}-*/;

	public final native String getPort() /*-{
		return this.port;
	}-*/;

	public final native String getProtocol() /*-{
		return this.protocol;
	}-*/;

	public final native String getSearch() /*-{
		return this.search;
	}-*/;
}
