/*
 * Copyright 2009 Brendan Kenny
 * Copyright 2008 Google Inc.
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

// This file originally com/google/gwt/core/linker/SingleScriptTemplate.js r4488
 
function __MODULE_FUNC__() {
  // ---------------- INTERNAL GLOBALS ----------------
 
  // Cache symbols locally for good obfuscation
  // Point $wnd and $doc to worker global scope. Shouldn't be used, but there
  // in case preexisting code uses either as a generic global variable
  var $self = self
  ,$wnd = self
  ,$doc = self
 
  // These variables gate calling gwtOnLoad; all must be true to start
  ,gwtOnLoad

  // Error functions.  Default unset in compiled mode, may be set by meta props.
  // may need in future for development mode
  // ,onLoadErrorFunc, propertyErrorFunc
 
  ; // end of global vars

  // ------------------ TRUE GLOBALS ------------------

  // --------------- INTERNAL FUNCTIONS ---------------

  // Again, may need in future for development mode
  /* function isHostedMode() {
    try {
      return ($wnd.external && $wnd.external.gwtOnLoad &&
          ($wnd.location.search.indexOf('gwt.hybrid') == -1));
    } catch (e) {
      // Defensive: some versions of IE7 reportedly can throw an exception
      // evaluating "external.gwtOnLoad".
      return false;
    }
  } */
 
  // --------------- EXPOSED FUNCTIONS ----------------

  // Called when the compiled script identified by moduleName is done loading.
  //
  __MODULE_FUNC__.onScriptLoad = function(gwtOnLoadFunc) {
    // remove this whole function from the global namespace to allow GC
    __MODULE_FUNC__ = null;
    gwtOnLoad = gwtOnLoadFunc;
    gwtOnLoad(undefined, '__MODULE_NAME__', '');
  }

  // --------------- STRAIGHT-LINE CODE ---------------

  /* if (isHostedMode()) {
    alert("Single-script hosted mode not yet implemented. See issue " +
      "http://code.google.com/p/google-web-toolkit/issues/detail?id=2079");
    return;
  } */
 
  // --------------- WINDOW ONLOAD HOOK ---------------
  
  // TODO: consider need for script injection
// __MODULE_SCRIPTS_BEGIN__
  // Script resources are injected here
// __MODULE_SCRIPTS_END__
}

__MODULE_FUNC__();