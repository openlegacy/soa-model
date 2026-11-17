package com.predic8.schema

import com.predic8.schema.creator.SchemaCreator
import com.predic8.schema.creator.SchemaCreatorContext
import com.predic8.xml.util.ClasspathResolver
import groovy.test.GroovyTestCase
import groovy.xml.MarkupBuilder

/**
 * What a schema says about derivation, substitution and identity - none of which was readable.
 *
 * `block` and `final` are the author's statement about what may stand in for a declaration and what
 * may be derived from it, and `blockDefault` / `finalDefault` say it once for a whole document.
 * `xs:key` and `xs:keyref` are two thirds of xsd's identity constraints, and only `xs:unique` was
 * parsed - so a document stating identity the usual way said nothing a consumer could see.
 *
 * The creator is asserted alongside the parser, as it is for `mixed` and `abstract`: a document that
 * round trips through this model must come out saying the same things.
 */
class DerivationControlTest extends GroovyTestCase {

  def schema

  void setUp() {
    def parser = new SchemaParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse("/derivation/control.xsd")
  }

  private element(String name) {
    schema.elements.find { it.name == name }
  }

  void testBlockAndFinalOnAComplexType() {
    def coverage = schema.getType('CoverageType')

    assertEquals('restriction', coverage.blockValue)
    assertEquals('extension', coverage.finalValue)
  }

  void testFinalOnASimpleType() {
    assertEquals('#all', schema.getType('CurrencyCode').finalValue)
  }

  void testBlockOnAnElement() {
    assertEquals('restriction', element('coverage').blockValue)
  }

  void testTheDocumentsOwnDefaults() {
    assertEquals('substitution', schema.blockDefault)
    assertEquals('restriction', schema.finalDefault)
  }

  void testSilenceStaysSilent() {
    // the document's defaults apply to it, and the declaration itself states nothing - which is the
    // difference a reader has to be able to see
    assertNull('an element that says nothing of its own about substitution', element('policy').blockValue)
    assertNull('nor about derivation', element('policy').finalValue)
  }

  void testAKeyIsReadWithItsSelectorAndFields() {
    def key = element('policy').keys.find { it.name == 'coverageKey' }

    assertNotNull('xs:key was not parsed at all before', key)
    assertEquals('tns:coverage', key.selector.xpath)
    assertEquals(['@cd'], key.fields.collect { it.xpath })
  }

  void testAKeyrefIsReadWithTheKeyItRefersTo() {
    def keyref = element('policy').keyrefs.find { it.name == 'claimCoverage' }

    assertNotNull('xs:keyref was not parsed at all before', keyref)
    assertEquals('coverageKey', keyref.refer.localPart)
    assertEquals('tns:claim', keyref.selector.xpath)
  }

  void testTheUniqueNextToThemIsUnchanged() {
    assertEquals('coverageIsDistinct', element('policy').unique.name)
  }

  void testTheCreatorWritesThemAllBack() {
    def written = new StringWriter()
    def creator = new SchemaCreator(builder: new MarkupBuilder(written))

    schema.create(creator, new SchemaCreatorContext())
    def document = written.toString()

    ['block=', 'final=', 'xsd:key', 'xsd:keyref', 'refer=', 'xsd:unique'].each {
      assertTrue("the round trip drops $it: $document", document.contains(it))
    }
  }
}
