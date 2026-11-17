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

import com.predic8.soamodel.CreatorContext

import groovy.xml.*
import groovy.namespace.*

/**
 * An `xs:keyref`, which nothing parsed before this.
 *
 * A [Key] plus the name of the key it points at: the nodes the selector picks must carry fields
 * that match a key declared elsewhere in the document. `refer` is the whole point of it, and a
 * keyref read without it says nothing.
 * */
class Keyref extends Unique {

  String referValue
  QName refer

  protected parseAttributes(token, params){
    super.parseAttributes(token, params)
    referValue = token.getAttributeValue( null , 'refer')
    if (referValue) {
      refer = getTypeQName(referValue)
    }
  }

  public getElementName(){
    'keyref'
  }

  def create(creator, CreatorContext ctx){
    creator.createKeyref(this, ctx.clone())
  }
}
