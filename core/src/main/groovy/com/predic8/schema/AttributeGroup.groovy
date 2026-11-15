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

package com.predic8.schema;

import java.util.List;

import groovy.xml.*
import groovy.namespace.*
import com.predic8.soamodel.CreatorContext
import com.predic8.wstool.creator.*
import javax.xml.stream.*

class AttributeGroup extends SchemaComponent{

  QName ref
  List<Attribute> attributes = []
  List<AttributeGroup> attributeGroups = []
  AnyAttribute anyAttribute

  protected parseAttributes(token, params){
    super.parseAttributes(token, params)
    if (token.getAttributeValue( null , 'ref'))
      ref = getTypeQName(token.getAttributeValue( null , 'ref'))
  }

  protected parseChildren(token, child, params){
    switch (child ){
      case 'attribute' :
        def attribute = new Attribute(schema:schema)
        attribute.parse(token, params)
          attributes << attribute ; break
      case 'attributeGroup' :
        def attributeGroup = new AttributeGroup(schema:schema)
        attributeGroup.parse(token, params)
          attributeGroups << attributeGroup ; break
      case 'anyAttribute' :
        anyAttribute = new AnyAttribute(schema: schema)
        anyAttribute.parse(token, params) ; break
    }
  }

  Attribute getAttribute(String name){
    attributes.find{it.name == name}
  }

  List<Attribute> getAttributesFromRef(){
    if(!ref) return
      schema.getAttributeGroup(ref).allAttributes?.flatten()
  }

  /**
   * Every attribute this group contributes: its own, or the ones the group it references has, plus
   * every nested group's.
   *
   * Built into a new list. It used to append into whichever list it started from, which for a group
   * with attributes of its own was `attributes` - the group's own field - so asking twice grew the
   * model, and what it appended was a nested List rather than its elements. `flatten()` hid that in
   * the answer and not in the field, so a later reader of `attributes` got an ArrayList where an
   * Attribute belongs and a ClassCastException with it.
   *
   * A group that names neither attributes nor a reference contributes nothing, which is an empty
   * list rather than a null - every caller here adds the result to a list of its own.
   */
  List<Attribute> getAllAttributes(){
    def res = new ArrayList<Attribute>()
    res.addAll(attributes ?: attributesFromRef ?: [])
    attributeGroups.each { res.addAll(it.allAttributes ?: []) }
    res
  }

  public getElementName(){
    'attributeGroup'
  }

  def create(creator, CreatorContext ctx){
    creator.createAttributeGroup(this, ctx.clone())
  }

  def compare(generator, other){
    generator.compareGroups(this, other)
  }

  String toString(){
    "attributeGroup[name= $name, ref=$ref,attributes=$attributes, attributeGroups=$attributeGroups]"
  }
}
