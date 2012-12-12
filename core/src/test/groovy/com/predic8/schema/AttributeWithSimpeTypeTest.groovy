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

import groovy.xml.*

import com.predic8.schema.creator.SchemaCreator
import com.predic8.schema.creator.SchemaCreatorContext
import com.predic8.schema.restriction.*
import com.predic8.xml.util.*

class AttributeWithSimpeTypeTest extends GroovyTestCase {

  def schema

  void setUp(){
    def parser = new SchemaParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse(input:"/schema/simplecontent/attributeWithSimpleType.xsd")
	def strWriter = new StringWriter()
	def creator = new SchemaCreator(builder : new MarkupBuilder(strWriter))
	schema.create(creator, new SchemaCreatorContext())
  }

  void testParsingComplexType() {
    assertNotNull(schema.getElement('shoeSize'))
  }

  void testParsingExtension() {
    assertEquals(new QName('http://www.w3.org/2001/XMLSchema', 'decimal'), schema.getElement('shoeSize').embeddedType.model.extension.base)
    assertEquals('sizing', schema.getElement('shoeSize').embeddedType.model.extension.attributes[0].name)
  }

  void testParsingRestriction() {
    assertTrue(schema.getElement('shoeSize').embeddedType.model.extension.attributes[0].simpleType.restriction instanceof StringRestriction)
    assertEquals(new QName(Schema.SCHEMA_NS, 'string'), schema.getElement('shoeSize').embeddedType.model.extension.attributes[0].simpleType.restriction.base)
    assertEquals(4, schema.getElement('shoeSize').embeddedType.model.extension.attributes[0].simpleType.restriction.enumerationFacets.value.size())
  }
  
  void testGetBuildIntTypeName() {
    assertEquals('string', schema.getElement('shoeSize').embeddedType.model.extension.attributes[0].buildInTypeName)
  }
}

