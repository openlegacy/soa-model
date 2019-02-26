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

package com.predic8.wsdl.creator

import com.predic8.policy.creator.PolicyCreator
import com.predic8.schema.creator.SchemaCreator
import com.predic8.schema.creator.SchemaCreatorContext
import com.predic8.soamodel.Consts
import com.predic8.wsdl.AbstractAddress
import com.predic8.wsdl.AbstractPortTypeMessage
import com.predic8.wsdl.AbstractSOAPBinding
import com.predic8.wsdl.AbstractSOAPBody
import com.predic8.wsdl.AbstractSOAPHeader
import com.predic8.wsdl.Binding
import com.predic8.wsdl.BindingMessage
import com.predic8.wsdl.BindingOperation
import com.predic8.wsdl.Definitions
import com.predic8.wsdl.Documentation
import com.predic8.wsdl.ExtensibilityOperation
import com.predic8.wsdl.Import
import com.predic8.wsdl.Message
import com.predic8.wsdl.Operation
import com.predic8.wsdl.Part
import com.predic8.wsdl.Port
import com.predic8.wsdl.PortType
import com.predic8.wsdl.Service
import com.predic8.wsdl.Types
import com.predic8.wsdl.WSDLElement
import com.predic8.wsdl.http.HTTPBinding
import groovy.xml.MarkupBuilderHelper

class WSDLCreator extends AbstractWSDLCreator {
  def nameWithPrefix(WSDLElement element) {

    return (element.getPrefix() ? element.getPrefix() + ':' : '') + element.getElementName().getLocalPart();
  }

  def createDefinitions(Definitions definitions, WSDLCreatorContext ctx) {
    def attrs = ['targetNamespace': getDisplayName(definitions.targetNamespace, 'definitions.targetNamespace', ctx.error)]
    if (definitions.name) attrs['name'] = definitions.name

    builder.(nameWithPrefix(definitions))(attrs + getNamespaceAttributes(definitions)) {
      definitions.documentation?.create(this, ctx)

      /**
       * Imports should be created only if inlineWSDLImports in ctx is true.
       */
      definitions.imports.each {
        it.create(this, ctx)
      }

      definitions.types*.create(this, ctx)

      definitions.messages.each {
        it.create(this, ctx)
      }
      definitions.portTypes.each {
        it.create(this, ctx)
      }
      definitions.bindings.each {
        it.create(this, ctx)
      }
      definitions.services.each {
        it.create(this, ctx)
      }

      definitions.policies.values().each {
        it.create(new PolicyCreator(builder: builder), ctx)
      }
    }
  }

  def createImport(Import imp, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(imp))([namespace: imp.namespace, location: imp.location] + getNamespaceAttributes(imp))
  }

  def createTypes(Types types, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(types))() {
      types.documentation?.create(this, ctx)
      types.schemas.each {
        it.create(new SchemaCreator(builder: builder), new SchemaCreatorContext(ctx.clone()))
      }
    }
  }

  def createMessage(Message message, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(message))([name: getDisplayName(message.name, 'definitions.message.name', ctx.error)] + getNamespaceAttributes(message)) {
      message.documentation?.create(this, ctx)
      message.parts.each {
        it.create(this, ctx)
      }
    }
  }

  def createPart(Part part, WSDLCreatorContext ctx) {
    def attrs = [name: part.name]
    if (part.element) {
      attrs.put('element', "${part.getPrefix(part.element.namespaceUri)}:${part.element.name}")
    }
    if (part.type) {
      attrs.put('type', part.typePN)
    }
    builder.(nameWithPrefix(part))(attrs + getNamespaceAttributes(part))
  }

  def createPortType(PortType portType, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(portType))([name: getDisplayName(portType.name, 'definitions.portTypes.name', ctx.error)] + getNamespaceAttributes(portType)) {
      portType.documentation?.create(this, ctx)
      portType.operations.each {
        //TODO call it.create() instead.
        createOperation(it, ctx)
      }
    }
  }

  def createOperation(Operation operation, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(operation))([name: getDisplayName(operation.name, 'definitions.operations.name', ctx.error)] + getNamespaceAttributes(operation)) {
      operation.documentation?.create(this, ctx)
      operation.input?.create(this, ctx)
      operation.output?.create(this, ctx)
      operation.faults.each {
        it.create(this, ctx)
      }
    }
  }

  def createBinding(Binding binding, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(binding))([name: binding.name, type: binding.getTypeString(binding.type)] + getNamespaceAttributes(binding)) {
      binding.policyReference?.create(new PolicyCreator(builder: builder), ctx)
      binding.documentation?.create(this, ctx)
      binding.binding?.create(this, ctx)
      binding.operations.each {
        it.parent = binding
        it.create(this, ctx)
      }
    }
  }

  def createSoapBinding(AbstractSOAPBinding soapBinding, WSDLCreatorContext ctx) {
    builder."${soapBinding.prefix}:binding"([style: soapBinding.style, transport: Consts.SOAP_ENC_NS] + getNamespaceAttributes(soapBinding))
  }

  private getProtocolNamespace(protocol) {
    if (protocol == 'SOAP11') return Consts.WSDL_SOAP11_NS
    Consts.WSDL_SOAP12_NS
  }

  def createHTTPBinding(HTTPBinding binding, ctx) {
    builder."${binding.prefix}:binding"([verb: binding.verb] + getNamespaceAttributes(binding))
  }

  def createBindingOperation(BindingOperation bindingOperation, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(bindingOperation))([name: bindingOperation.name] + getNamespaceAttributes(bindingOperation)) {
      bindingOperation.operation?.create(this, ctx)
      bindingOperation.input?.create(this, ctx)
      bindingOperation.output?.create(this, ctx)
      bindingOperation.faults.each {
        it.create(this, ctx)
      }
    }
  }

  def createSOAPOperation(ExtensibilityOperation soapOperation, WSDLCreatorContext ctx) {
    def attrs = [soapAction: soapOperation.soapAction]
    if (soapOperation.style) attrs['style'] = soapOperation.style
    builder."${soapOperation.prefix}:operation"(attrs + getNamespaceAttributes(soapOperation))
  }

  def createHTTPOperation(ExtensibilityOperation httpOperation, WSDLCreatorContext ctx) {
    builder."${httpOperation.prefix}:operation"([location: httpOperation.location] + getNamespaceAttributes(httpOperation))
  }

  def createPortTypeMessage(AbstractPortTypeMessage portTypeMessage, WSDLCreatorContext ctx) {
    def attrs = [message: "${portTypeMessage.definitions.targetNamespacePrefix}:${portTypeMessage.message.name}"]
    if (portTypeMessage.name) attrs['name'] = portTypeMessage.name
    builder.(nameWithPrefix(portTypeMessage))(attrs + getNamespaceAttributes(portTypeMessage))
  }

  def createBindingMessage(BindingMessage bindingMessage, WSDLCreatorContext ctx) {
    def attrs = [:]
    if (bindingMessage.name) attrs['name'] = bindingMessage.name
    builder.(nameWithPrefix(bindingMessage))(attrs + getNamespaceAttributes(bindingMessage)) {
      bindingMessage.bindingElements.each {
        it.create(this, ctx)
      }
      bindingMessage.policyReference?.create(new PolicyCreator(builder: builder), ctx)
    }
  }

  def createSOAPBody(AbstractSOAPBody body, WSDLCreatorContext ctx) {
    def attrs = [use: body.use]
    def prefix = body.getPrefix(body.ELEMENTNAME.namespaceURI)
    if (body.parts) attrs['parts'] = body.parts.name.join(' ')
    if (body.encodingStyle) attrs['encodingStyle'] = body.encodingStyle
    if (body.namespace) attrs['namespace'] = body.namespace
    builder."${prefix}:body"(attrs + getNamespaceAttributes(body))
  }

  def createSOAPHeader(AbstractSOAPHeader header, WSDLCreatorContext ctx) {
    def prefix = header.getPrefix(header.ELEMENTNAME.namespaceURI)
    def attrs = [message: "${header.message.definitions.targetNamespacePrefix}:${header.message.name}", use: header.use]
    attrs['part'] = "${header.part.definitions.targetNamespacePrefix}:${header.part.name}"
    if (header.encodingStyle) attrs['encodingStyle'] = header.encodingStyle
    if (header.namespace) attrs['namespace'] = header.namespace
    builder."${prefix}:header"(attrs + getNamespaceAttributes(header))
  }

  def createSOAPFault(fault, WSDLCreatorContext ctx) {
    def prefix = fault.getPrefix(fault.ELEMENTNAME.namespaceURI)
    builder."${prefix}:fault"(use: fault.use, name: fault.name)
  }

  def createService(Service service, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(service))(name: getDisplayName(service.name, 'definitions.services.name', ctx.error)) {
      service.documentation?.create(this, ctx)
      service.ports.each {
        it.create(this, ctx)
      }
    }
  }

  def createPort(Port port, WSDLCreatorContext ctx) {
    builder.(nameWithPrefix(port))([name: port.name, binding: "${port.definitions.targetNamespacePrefix}:${port.binding.name}"] + getNamespaceAttributes(port)) {
      port.documentation?.create(this, ctx)
      port.address.create(this, ctx)
    }
  }

  def createAddress(AbstractAddress address, WSDLCreatorContext ctx) {
    builder."${address.prefix}:address"([location: address.location] + getNamespaceAttributes(address))
  }

  private createDocumentation(Documentation doc, WSDLCreatorContext ctx) {
    builder.documentation { new MarkupBuilderHelper(builder).yieldUnescaped(doc) }
  }
}
