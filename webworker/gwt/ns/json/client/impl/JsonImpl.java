/*
 * Copyright 2009 Brendan Kenny
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gwt.ns.json.client.impl;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JSNI methods for using JSON in browsers with native JSON implementations.
 */
public class JsonImpl {
	public final native String strigify(JavaScriptObject jso) /*-{
		return $wnd.JSON.stringify(jso);
	}-*/;

	public final native <T extends JavaScriptObject> T parse(String json) /*-{
		return $wnd.JSON.parse(json);
	}-*/;
}
