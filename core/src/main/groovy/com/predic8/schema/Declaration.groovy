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

import groovy.namespace.QName

import javax.xml.namespace.QName as JQName
import static com.predic8.soamodel.Consts.SCHEMA_NS

abstract class Declaration extends SchemaComponent {
  QName type

  /**
   * The form default in force where this declaration was WRITTEN, captured as it was parsed.
   *
   * It cannot be read off the schema afterwards. `xs:include` re-parses the included document's
   * `<xs:schema>` element onto the SAME Schema object, so its `elementFormDefault` and
   * `attributeFormDefault` are overwritten by whatever the included document says - and one Schema
   * object then stands for two documents that may disagree. Xsd has the two disagree legitimately:
   * the form default belongs to a document, and a document may be included into a namespace it
   * never names.
   *
   * Measured on the EU's EORI service: an inline wsdl schema with no `elementFormDefault` (so
   * unqualified) whose first child includes a chameleon declaring `qualified`. Everything the
   * including document declares after that include - `eoriResponse` and its ten children - read back
   * as qualified, and a client of the original, which sends them unqualified, had every one of those
   * values rejected.
   *
   * Captured per declaration, so each document leaves its own answer on the components it wrote.
   */
  String formDefault

  /**
   * Whether an instance writes this declaration's name in its schema's namespace.
   *
   * The rule, in the order xsd gives it: the declaration's own `form`, else the form default of the
   * document that declared it, else unqualified. A top-level declaration is always qualified - it is
   * IN the target namespace by definition - and says so by answering true whatever the defaults are.
   */
  boolean isQualified() {
    if (metaClass.hasProperty(this, 'toplevel') && toplevel) return true
    def own = metaClass.hasProperty(this, 'form') ? form : null
    if (own) return own.equalsIgnoreCase('qualified')
    formDefault?.equalsIgnoreCase('qualified') ?: false
  }
  
  /**
   * for wsdl:arrayType
   */
  String arrayType 
  
	QName getQname() {
		new QName(namespaceUri, name)
	}
	
  void setType(QName type){ //without this groovy 1.8.5 tries to cast QName to JQName and throws ClassCastException
    this.type = type
  }

  QName setType(JQName type){
    this.type = new QName(type.namespaceURI, type.localPart)
  }
  
  protected parseAttributes(token, params){
    super.parseAttributes(token, params)
    arrayType = token.getAttributeValue( 'http://schemas.xmlsoap.org/wsdl/' , 'arrayType')
  }
  
  String getBuildInTypeName(){
    if(type?.namespaceURI == SCHEMA_NS) return type.localPart
    if(type && schema.getType(type)) return schema.getType(type).buildInTypeName
    buildInTypeNameLocal
  }
}