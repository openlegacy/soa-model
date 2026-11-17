package com.predic8.schema

import com.predic8.schema.creator.SchemaCreator
import com.predic8.schema.creator.SchemaCreatorContext
import com.predic8.xml.util.ClasspathResolver
import groovy.test.GroovyTestCase
import groovy.xml.MarkupBuilder

/**
 * Whether a global element may appear in an instance at all.
 *
 * `abstract="true"` says it may not: a member of the substitution group it heads appears in its
 * place. Nothing parsed the attribute, so a consumer building a choice out of a group could only
 * guess whether the head belonged in it, and guessed from the head's TYPE - which is a statement
 * about a different component and is false exactly where the attribute is needed. The published
 * choice then accepted the head, and the abstract members with it, which the original rejects.
 *
 * The creator is asserted for the same reason it is for `mixed`: once the attribute is read, a
 * document that round trips through this model has to keep it - and `substitutionGroup`, which was
 * parsed and never written, with it.
 */
class AbstractElementTest extends GroovyTestCase {

  def schema

  void setUp() {
    def parser = new SchemaParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse("/substitution/substitution.xsd")
  }

  private element(String name) {
    schema.elements.find { it.name == name }
  }

  void testTheHeadSaysItMayNotAppear() {
    assertTrue('abstract="true" on the head of the group', element('shape').abstractElement)
  }

  void testOneIsXsdsOtherSpellingOfTrue() {
    assertTrue('abstract="1", which xsd\'s lexical space for a boolean allows',
      element('roundish').abstractElement)
  }

  void testSilenceMeansItMayAppear() {
    assertFalse('no abstract attribute at all, which xsd reads as concrete',
      element('circle').abstractElement)
  }

  void testExplicitFalseIsNotTrue() {
    assertFalse('abstract="false" said out loud, and coercing the raw value used to read it as true',
      element('square').abstractElement)
  }

  void testTheCreatorWritesBothBack() {
    def written = new StringWriter()
    def creator = new SchemaCreator(builder: new MarkupBuilder(written))

    schema.create(creator, new SchemaCreatorContext())
    def document = written.toString()

    assertTrue("the round trip drops that the head may not appear: $document",
      document.contains("abstract='true'") || document.contains('abstract="true"'))
    assertTrue("the round trip drops which head a member stands in for: $document",
      document.contains("substitutionGroup='tns:shape'") || document.contains('substitutionGroup="tns:shape"'))
  }
}
