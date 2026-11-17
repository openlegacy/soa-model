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

import java.util.List;

import com.predic8.schema.restriction.BaseRestriction;
import com.predic8.xml.util.PrefixedName

import groovy.xml.*
import groovy.namespace.*

abstract class TypeDefinition extends SchemaComponent {

	QName qname

	/**
	 * `block` and `final`: what may SUBSTITUTE for a declaration of this type in an instance, and
	 * what may be DERIVED from it.
	 *
	 * Neither was parsed, and both are statements an author makes on purpose - insurance's `Coverage`
	 * is `block="restriction"`, and the schema around it sets a `finalDefault` as well. A consumer
	 * that cannot read them republishes a type open to every derivation and substitution the original
	 * forbids.
	 *
	 * Kept as the raw attribute value, which is a list over `#all`, `extension`, `restriction`,
	 * `substitution`, `list` and `union`: that is what a reader has to put back, and xsd gives the
	 * same two attributes different vocabularies on a complexType and on a simpleType.
	 * */
	String blockValue
	String finalValue

	List<Attribute> attributes = []
	List<AttributeGroup> attributeGroups = []
	AnyAttribute anyAttribute

	protected parseAttributes(token, params){
		super.parseAttributes(token, params)
		name = token.getAttributeValue( null , 'name')
		blockValue = token.getAttributeValue( null , 'block')
		finalValue = token.getAttributeValue( null , 'final')
		if (name) {
			def preName = new PrefixedName(name)
			qname = new QName(schema.targetNamespace, preName.localName)
		}
	}

	protected parseChildren(token, child, params){
		super.parseChildren(token, child, params)
		switch (child ){
			case 'attribute' :
				def attr = new Attribute(schema: schema)
				attr.parse(token, params)
					attributes << attr ; break
			case 'attributeGroup' :
				def attributeGroup = new AttributeGroup(schema: schema)
				attributeGroup.parse(token, params)
					attributeGroups << attributeGroup ; break
			case 'anyAttribute' :
				anyAttribute = new AnyAttribute(schema: schema)
					anyAttribute.parse(token, params) ; break
		}
	}

	List<Attribute> getAllAttributes(){
		def attrs = []
		attrs.addAll(attributes)
		attributeGroups.each {
			attrs.addAll(it.allAttributes)
		}
		if(!metaClass.hasProperty(this, 'model') || !model) return attrs //simpleType does not have a model
		if(model.metaClass.hasProperty(model, 'derivation')){
			if(model.derivation){
				if(model.derivation instanceof BaseRestriction) attrs.addAll([model.derivation.anyAttribute] - null)
				else attrs.addAll(model.derivation.allAttributes)
				attrs.addAll(schema.getType(model.derivation.base)?.getAllAttributes()?:[])
			}
		}
		attrs
	}

	Attribute getAttribute(name) {
		attributes.find{ it.name == name }
	}

	AttributeGroup getAttributeGroup(String name){
		attributeGroups.find{it.name == name}
	}
}

