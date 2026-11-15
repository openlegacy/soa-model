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

import com.predic8.wsdl.WSDLParser
import com.predic8.xml.util.ClasspathResolver
import groovy.test.GroovyTestCase

class InheritedPrefixWithIncludeTest extends GroovyTestCase {

  def schema

  void setUp() {
    def parser = new WSDLParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse("/InheritedPrefixWithInclude.wsdl").schemas[0]
  }

  /**
   * An xs:include re-parses the schema element from the included document, and XMLElement.parse
   * begins by taking its parent from the context - which a fresh sub-context did not carry. The
   * schema therefore came out of its own include detached from the wsdl above it.
   *
   * Which matters because declaring xmlns:xsd on wsdl:definitions and using it inside wsdl:types is
   * ordinary, legal xml that JAX-WS RI publishes. Such a contract parsed until an include was added
   * to it, and then failed on xsd:string with "No namespace declared for prefix 'xsd'".
   */
  void testPrefixDeclaredOnDefinitionsSurvivesAnInclude() {
    def msg = schema.getElement('Ping').embeddedType.model.getElement('Msg')

    assertNotNull(msg)
    assertEquals('http://www.w3.org/2001/XMLSchema', msg.type.namespaceURI)
    assertEquals('string', msg.type.localPart)
  }

  void testTheIncludedComponentIsThereToo() {
    assertNotNull(schema.getType('Identifier'))
  }
}
