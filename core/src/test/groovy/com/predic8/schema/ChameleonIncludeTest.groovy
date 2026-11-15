package com.predic8.schema

import com.predic8.xml.util.ClasspathResolver
import groovy.test.GroovyTestCase

/**
 * A chameleon include: the included components become the including schema's, in its namespace, and
 * the unqualified references written beside them have to follow them there.
 *
 * `EORINumber` restricts `SingleLineString` with no prefix, inside a document that declares no
 * targetNamespace of its own. Resolving that reference as "no namespace" finds nothing, the chain
 * stops one step short of the built-in, and the type reaches a consumer with no primitive behind it.
 * The EU's EORI validation service is exactly this, and published `EORINumber` as an empty
 * complexType - every value of every request dropped, with the call reporting success.
 */
class ChameleonIncludeTest extends GroovyTestCase {

  def schema

  void setUp() {
    def parser = new SchemaParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse("/chameleon/including.xsd")
  }

  void testIncludedTypeIsInTheIncludingNamespace() {
    assertNotNull('the chameleon brought its types in', schema.getType('EORINumber'))
    assertNotNull(schema.getType('SingleLineString'))
  }

  void testUnqualifiedReferenceResolvesToTheIncludingNamespace() {
    def eori = schema.getType('EORINumber')
    def base = eori.restriction.base

    assertEquals('SingleLineString', base.localPart)
    assertEquals('urn:soa-model:test:chameleon', base.namespaceURI)
  }

  void testTheChainReachesTheBuiltIn() {
    def chain = schema.getType('EORINumber').getSuperTypes()*.localPart

    assertTrue("the chain stops short: $chain", chain.contains('string'))
  }
}
