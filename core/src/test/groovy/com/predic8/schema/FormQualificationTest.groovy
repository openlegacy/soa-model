package com.predic8.schema

import com.predic8.xml.util.ClasspathResolver
import groovy.test.GroovyTestCase

/**
 * Whether a local declaration is namespace-qualified in an instance.
 *
 * `elementFormDefault` and `attributeFormDefault` are properties of the **document** that writes a
 * declaration, not of the namespace it lands in. A chameleon include therefore moves declarations
 * into the includer's namespace while leaving their form alone: the includer saying `qualified`
 * does not reach into the included document. Xerces agrees - an instance of the type below is
 * accepted with `fromInner` unprefixed and rejected with it prefixed.
 *
 * Both halves used to be wrong here. The parser re-parses an included `<xs:schema>` onto the same
 * `Schema` object, which overwrote the includer's form defaults with the chameleon's for everything
 * declared after the `<xs:include>`; and a declaration carried no form at all, so a consumer had to
 * read the schema's current default and got whichever document happened to be parsed last.
 */
class FormQualificationTest extends GroovyTestCase {

  def schema

  void setUp() {
    def parser = new SchemaParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse("/form/outer.xsd")
  }

  private element(type, name) {
    schema.getType(type).model.particles.find { it.name == name }
  }

  private attribute(type, name) {
    schema.getType(type).attributes.find { it.name == name }
  }

  void testIncludedDocumentKeepsItsOwnFormDefault() {
    assertFalse('the chameleon declares no form default, so unqualified',
      element('InnerType', 'fromInner').isQualified())
    assertFalse(attribute('InnerType', 'innerAttribute').isQualified())
  }

  void testIncludingDocumentIsNotOverwrittenByTheInclude() {
    assertTrue('declared after the <xs:include>, in a qualified document',
      element('OuterType', 'fromOuter').isQualified())
    assertTrue(attribute('OuterType', 'outerAttribute').isQualified())
  }

  void testOwnFormWinsOverTheDocumentDefault() {
    assertFalse(element('OuterType', 'saysUnqualified').isQualified())
    assertFalse(attribute('OuterType', 'unqualifiedAttribute').isQualified())
  }

  void testGlobalDeclarationIsAlwaysQualified() {
    assertTrue(schema.getElement('outerElement').isQualified())
  }
}
