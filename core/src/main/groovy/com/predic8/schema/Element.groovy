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

import com.predic8.soamodel.Consts;
import groovy.xml.*
import groovy.namespace.*

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.predic8.soamodel.AbstractDiffGenerator
import com.predic8.soamodel.CreatorContext
import com.predic8.soamodel.DiffGeneratorContext
import com.predic8.wadl.creator.JsonCreator
import com.predic8.wadl.creator.JsonCreatorContext
import com.predic8.wstool.creator.*

class Element extends Declaration {

  private static final Logger log = LoggerFactory.getLogger(Element.class)

  TypeDefinition embeddedType
  boolean toplevel = false
  Annotation annotation
  String refValue
  QName ref
  String substitutionGroupValue
  QName substitutionGroup
  String minOccurs = 1
  String maxOccurs = 1
  String defaultValue //can only be used if the element's content is a simple type or text only
  String fixedValue //can only be used if the element's content is a simple type or text only
  String form // Possible values: qualified/unqualified. Cannot be used if element is top level
  boolean nillable = false

  /**
   * `abstract="true"`: this declaration may not appear in an instance at all, and a member of the
   * substitution group it heads appears in its place.
   *
   * Not parsed before, and nothing downstream could ask - so a consumer building a choice out of a
   * substitution group had to guess whether the head belonged in it, and offered it unless its TYPE
   * was abstract, which is a different statement about a different component. Read the way
   * `ComplexType.mixed` is, over xsd's lexical space for a boolean rather than by coercing the raw
   * attribute value, so that an `abstract="false"` said out loud is false.
   * */
  boolean abstractElement

  /**
   * `block` and `final` on a declaration: which substitutions this element refuses in an instance,
   * and which derivations may head a group under it. Unparsed before - see
   * `TypeDefinition.blockValue` for what they cost.
   * */
  String blockValue
  String finalValue

  Unique unique

  /**
   * The `xs:key` and `xs:keyref` constraints declared here, which nothing parsed at all.
   *
   * `xs:unique` was read and these two were not, so a document stating its identity the usual way -
   * a key and the references to it - said nothing a consumer could even see, and a report about
   * identity constraints was silently about a third of them.
   *
   * Held as [Unique] and [Keyref]: a key IS a unique with a stronger claim - every selected node
   * must carry the fields, not merely differ in them - and a keyref is a key plus the name of the
   * key it refers to.
   * */
  List<Unique> keys = []
  List<Keyref> keyrefs = []
	String expectedContentTypes

  protected parseAttributes(token, params){
    super.parseAttributes(token, params)
    // Taken now, while the schema still holds the form default of the document being parsed - see
    // Declaration.formDefault for why it cannot be read back later.
    formDefault = schema?.elementFormDefault
      if (token.getAttributeValue( null , 'type'))
        type = getTypeQName(token.getAttributeValue( null , 'type'))
    minOccurs = token.getAttributeValue( null , 'minOccurs') ?: 1
    maxOccurs = token.getAttributeValue( null , 'maxOccurs') ?: 1

    refValue = token.getAttributeValue( null , 'ref')
    if (refValue)
	    ref = getTypeQName(refValue)

		substitutionGroupValue = token.getAttributeValue( null , 'substitutionGroup')
			if (substitutionGroupValue)
				substitutionGroup = getTypeQName(substitutionGroupValue)

		// Element can have a default value OR a fixed value specified.
		defaultValue = token.getAttributeValue( null , 'default')
		fixedValue = token.getAttributeValue( null , 'fixed')
		nillable = (token.getAttributeValue( null , 'nillable') == 'true')
		blockValue = token.getAttributeValue( null , 'block')
		finalValue = token.getAttributeValue( null , 'final')
		def abstractValue = token.getAttributeValue( null , 'abstract')
		abstractElement = abstractValue == 'true' || abstractValue == '1'
		form = token.getAttributeValue( null , 'form')
		expectedContentTypes = token.getAttributeValue(Consts.XML_MIME_NS, 'expectedContentTypes')

		log.debug "element attribute parsed: [name=$name, type=$type, ref=$ref, nillable=$nillable, abstract=$abstractElement]"
  }


  protected parseChildren(token, child, params){
		//		Can't use super.parseChildren(token, child, params) to parse annotation!
    switch (child ){
      case 'annotation' :
        annotation = new Annotation(schema: schema)
          annotation.parse(token, params) ; break
      case 'complexType' :
        embeddedType = new ComplexType(schema:schema)
          embeddedType.parse(token, params) ; break
      case 'simpleType' :
        embeddedType = new SimpleType(schema:schema, parent: this)
          embeddedType.parse(token, params) ; break
			case 'unique' :
				unique = new Unique(schema:schema, parent: this)
					unique.parse(token, params) ; break
			case 'key' :
				def key = new Key(schema:schema, parent: this)
					key.parse(token, params)
					keys << key ; break
			case 'keyref' :
				def keyref = new Keyref(schema:schema, parent: this)
					keyref.parse(token, params)
					keyrefs << keyref ; break
    }
    log.debug "child [$child] of element [$name] parsed!"
  }

  public getElementName(){
    'element'
  }

  def create(creator, CreatorContext ctx){
    creator.createElement(this, ctx.clone())
  }

  def compare(AbstractDiffGenerator generator, other, DiffGeneratorContext ctx = new DiffGeneratorContext()){
    generator.compareElement(this, other, ctx)
  }

  String getRequest(formParams){
    def writer = new StringWriter()
    create(new RequestCreator(builder:new MarkupBuilder(writer)),new RequestCreatorContext(path:"xpath:/",formParams:formParams))
    writer.toString()
  }

	String getAsJson(JsonCreatorContext ctx = new JsonCreatorContext()) {
		new JsonCreator().getElementAsJson(this)
	}

  String getHtmlForm(){
    def writer = new StringWriter()
    create(new FormCreator(builder:new MarkupBuilder(writer)),new FormCreatorContext(path:'xpath:/'))
    writer.toString()
  }

  String getBuildInTypeNameLocal(){
	  if (embeddedType)
	  	return embeddedType.buildInTypeName

  	  // @TODO same for ref simpleTypes
  }

  ComplexType newComplexType(){
    embeddedType = new ComplexType(schema:schema)
  }

  String toString(){
    "element[name=$name,type=$type,ref=$ref,embeddedType=$embeddedType]"
  }
}
