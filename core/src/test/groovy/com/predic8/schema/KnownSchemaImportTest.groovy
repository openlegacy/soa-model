package com.predic8.schema

import com.predic8.xml.util.ClasspathResolver
import groovy.namespace.QName
import groovy.test.GroovyTestCase

/**
 * Which document an `xs:import` of a namespace this library knows resolves to.
 *
 * A copy of eight well-known namespaces ships here, and it used to REPLACE the document the author
 * pointed at. That is wrong whenever the two differ, and they do: a namespace is not a document, and
 * two services may both declare `.../Serialization/Arrays` and declare different array types in it.
 * The copy here carries `ArrayOfguid`, `ArrayOfunsignedInt` and `ArrayOfbase64Binary`; Microsoft's
 * ad-platform contract declares `ArrayOflong`. With the copy substituted every element typed
 * `ArrayOflong` resolved to no type at all - nine of them in one contract - and a consumer built
 * from it refused the requests the real service accepts.
 *
 * The copy stays a fallback, for the case it was added for: an `xs:import` with no `schemaLocation`
 * is legal and says only that components of that namespace may appear, leaving the document to the
 * consumer - see [ImportKnownNSWithoutLocationTest], which covers that half.
 */
class KnownSchemaImportTest extends GroovyTestCase {

  def schema

  void setUp() {
    def parser = new SchemaParser(resourceResolver: new ClasspathResolver())
    schema = parser.parse("/knownschema/importing.xsd")
  }

  void testTheAuthorsDocumentIsTheOneRead() {
    def arrays = schema.imports[0].importSchema

    assertNotNull('the import resolved to nothing', arrays)
    assertNotNull("the author's own document declares ArrayOflong: ${arrays.complexTypes*.name}",
      arrays.complexTypes.find { it.name == 'ArrayOflong' })
  }

  void testTheCopyShippedHereDoesNotShadowIt() {
    def arrays = schema.imports[0].importSchema

    assertNull("the copy that ships here was read instead: ${arrays.complexTypes*.name}",
      arrays.complexTypes.find { it.name == 'ArrayOfguid' })
  }

  void testTheTypeResolves() {
    def ns = 'http://schemas.microsoft.com/2003/10/Serialization/Arrays'
    def type = schema.getType(new QName(ns, 'ArrayOflong'))

    assertNotNull('a type the author declared resolved to nothing, so every element of it is lost', type)
  }
}
