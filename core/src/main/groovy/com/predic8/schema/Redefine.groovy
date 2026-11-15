/* Copyright 2012 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.schema

import javax.xml.namespace.QName as JQName
import static com.predic8.soamodel.Consts.SCHEMA_NS

/**
 * An <code>xs:redefine</code>, read for everything it brings in.
 *
 * <p>Xsd defines it as an include that may restate some of the components it brings in. The element
 * was in no dispatch at all, so it was skipped whole - and with it the document it names, so every
 * component declared there went missing. A schema that redefines one type lost all of them, and the
 * parse failed on the first reference with "Could not find the referenced type", naming a type that
 * is declared exactly where the document says it is.
 *
 * <p>So this reads the referenced document, which is the include half of the definition and the half
 * that decides whether the schema can be read at all.
 *
 * <p><b>What it does not do</b> is apply the restatements. Their own content models are skipped, and
 * the components keep the definitions the included document gave them. The reason is that a
 * redefinition refers to the component it replaces <i>by the name it is replacing</i> - a redefined
 * type restricts the type it redefines - so putting the new definition in the old one's place makes
 * it derive from itself. Doing that properly needs the original kept addressable while the
 * replacement is built, which is a change to how components are registered rather than to this
 * element.
 *
 * <p>The consequence is stated rather than hidden: where a redefinition narrows a type, the schema
 * read here accepts what the original accepted, which is wider than the document says. Wider rather
 * than narrower is the direction that cannot reject a message the author intended to allow.
 */
class Redefine extends Include {

  public getElementName(){
    new JQName(SCHEMA_NS, 'redefine')
  }

  String toString(){
    "redefine schemaLocation=$schemaLocation"
  }
}
