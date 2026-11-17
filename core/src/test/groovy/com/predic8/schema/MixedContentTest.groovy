package com.predic8.schema

import com.predic8.schema.creator.SchemaCreator
import com.predic8.schema.creator.SchemaCreatorContext
import com.predic8.xml.util.ClasspathResolver
import groovy.test.GroovyTestCase
import groovy.xml.MarkupBuilder

/**
 * Whether character data may appear between a type's elements.
 *
 * Xsd allows `mixed` in two places - on the `complexType` and on a `complexContent` - and neither
 * was readable. The first was not parsed at all, so a type declaring it that way came through as
 * element-only content; the second assigned the raw attribute value to a boolean, so an explicit
 * `mixed="false"` read as `true` and a consumer could not tell the two apart. Both matter to the
 * same question: a document whose text between the elements carries meaning - HL7 narrative, a
 * clinical note with a reference inside it - says so one of those two ways.
 *
 * The creator is asserted too: once the parser reads the attribute, a document that round trips
 * through this model has to keep it.
 */
class MixedContentTest extends GroovyTestCase {

  def schema

  void setUp() {
    def parser = new SchemaParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse("/mixed/mixed.xsd")
  }

  void testMixedOnTheComplexTypeItself() {
    assertTrue('mixed="true" on the complexType', schema.getType('Narrative').mixed)
  }

  void testExplicitFalseIsNotTrue() {
    assertFalse('mixed="false" said out loud, and it is not the same as saying nothing is allowed',
      schema.getType('Strict').mixed)
  }

  void testSilenceIsElementOnly() {
    assertFalse('no mixed attribute at all, which xsd reads as element-only',
      schema.getType('Silent').mixed)
  }

  void testMixedOnTheContentModel() {
    assertTrue('mixed="true" on the complexContent',
      schema.getType('DerivedNarrative').model.mixed)
    assertFalse('mixed="false" on the complexContent, which used to read as true',
      schema.getType('DerivedStrict').model.mixed)
  }

  void testTheCreatorWritesItBack() {
    def written = new StringWriter()
    def creator = new SchemaCreator(builder: new MarkupBuilder(written))

    schema.create(creator, new SchemaCreatorContext())

    assertTrue("the round trip drops what the document said about its text: $written",
      written.toString().contains("mixed='true'") || written.toString().contains('mixed="true"'))
  }
}
