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

import com.predic8.util.*
import com.predic8.xml.util.*
import javax.xml.stream.*
import com.predic8.soamodel.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.xml.namespace.QName as JQName
import static com.predic8.soamodel.Consts.SCHEMA_NS

class Include extends SchemaComponent {

  private static final Logger log = LoggerFactory.getLogger(Include.class)

  String schemaLocation

   protected parseAttributes(token, ctx){
    schemaLocation = token.getAttributeValue( null , 'schemaLocation')
		if(schema.includedPaths.contains(HTTPUtil.getLocation(schema.baseDir,schemaLocation))) return
		schema.includedPaths << HTTPUtil.getLocation(schema.baseDir,schemaLocation)
    parseIncludedSchema(ctx)
  }

  private parseIncludedSchema(ctx){
    def resource = schema.resourceResolver.resolve(this, schema.baseDir)

    def inputFactory = ctx.xmlInputFactory
    if (inputFactory == null) {
      inputFactory = XMLInputFactory.newInstance()
      inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false)
      inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
    }

    def incToken = inputFactory.createXMLStreamReader(resource)
    while(incToken.hasNext()) {
      if(incToken.startElement) {
        if(incToken.name.getLocalPart() =='schema'){
          break
        }
      }
      if(incToken.hasNext()) incToken.next()
    }
    def origBaseDir = schema.baseDir
    schema.baseDir = HTTPUtil.updateBaseDir(schemaLocation , schema.baseDir)
    log.debug("includedSchema.baseDir ${schema.baseDir}")
    ctx.ns.push(schema)

    // An xs:include re-parses THIS schema element from the included document, and XMLElement.parse
    // begins by taking its parent from the context. A fresh sub-context carries no parent, so the
    // schema came out of its own include detached from the tree it sits in - and a prefix declared
    // above it then resolved to nothing.
    //
    // Which matters for an inline schema in a wsdl: declaring xmlns:xsd on wsdl:definitions and
    // using it inside wsdl:types is ordinary, legal xml, and JAX-WS RI publishes exactly that. Such
    // a contract parsed until an include was added to it, and then failed on xsd:string with "No
    // namespace declared for prefix 'xsd'" - naming the one reference that was never in doubt.
    def includedCtx = ctx.createNewSubContext([targetNamespace: schema.targetNamespace])
    includedCtx.parent = schema.parent
    schema.parse(incToken, includedCtx)
    ctx.ns.pop(schema)
    schema.baseDir = origBaseDir
  }

  public getElementName(){
    new JQName(SCHEMA_NS, 'include')
  }

  String toString(){
    "schemaLocation=$schemaLocation"
  }

}
