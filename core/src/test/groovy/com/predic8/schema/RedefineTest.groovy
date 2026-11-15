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

import com.predic8.xml.util.ClasspathResolver
import groovy.test.GroovyTestCase

class RedefineTest extends GroovyTestCase {

  def schema

  void setUp() {
    def parser = new SchemaParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse("/redefine/redefining.xsd")
  }

  /**
   * xs:redefine is an include that may restate what it brings in, and the element was in no
   * dispatch at all - so it was skipped whole, the document it names was never read, and every
   * component declared there went missing. An HR payroll contract then failed with "Could not find
   * the referenced type 'PayslipType'", naming a type declared exactly where the document said.
   */
  void testRedefineBringsInTheDocumentItNames() {
    assertNotNull('PayslipType came in through the redefine', schema.getType('PayslipType'))
    assertNotNull('and so did everything beside it', schema.getType('AmountType'))
  }

  void testRedefineIsRecordedAsOne() {
    assertEquals(1, schema.redefines.size())
    assertEquals('base.xsd', schema.redefines[0].schemaLocation)
  }

  /**
   * The element referencing a redefined type resolves, which is the whole point of reading the
   * document. What the restatement narrows is NOT applied - see Redefine - so the type here is the
   * one the included document declared.
   */
  void testElementOfARedefinedTypeResolves() {
    def element = schema.getElement('Payslip')
    assertNotNull(element)
    assertEquals('PayslipType', element.type.localPart)
  }
}
