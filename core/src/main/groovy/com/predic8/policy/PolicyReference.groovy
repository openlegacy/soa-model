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

package com.predic8.policy

import com.predic8.soamodel.AbstractCreator
import com.predic8.soamodel.AbstractParserContext
import com.predic8.soamodel.CreatorContext
import com.predic8.soamodel.XMLElement

import javax.xml.namespace.QName

class PolicyReference extends XMLElement {

  QName ELEMENTNAME

  String uri
  String Digest

  protected parseAttributes(token, AbstractParserContext ctx) {
    uri = token.getAttributeValue(null, 'URI')
    digest = token.getAttributeValue(null, 'Digest')
  }

  String getNamespaceUri() {
    definitions.targetNamespace
  }

  /**
   * Should return the prefix for the namespace of the element, like wsdl, soap, http & etc.
   * Used in WSDLCreator.
   */
  String getPrefix() {
    getPrefix(ELEMENTNAME.namespaceURI)
  }

  QName getElementName() {
    ELEMENTNAME
  }

  void create(AbstractCreator creator, CreatorContext ctx) {
    creator.createPolicyReference(this, ctx)
  }

}

