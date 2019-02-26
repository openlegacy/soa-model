package com.predic8.wstool.creator

import com.predic8.schema.Schema
import com.predic8.schema.Sequence
import com.predic8.wsdl.Binding
import com.predic8.wsdl.BindingOperation
import com.predic8.wsdl.Definitions
import com.predic8.wsdl.Operation
import com.predic8.wsdl.PortType
import com.predic8.wsdl.WSDLParser
import groovy.xml.MarkupBuilder
import org.junit.Assume
import org.junit.Before
import org.junit.Test

import static com.predic8.schema.Schema.STRING;

class SOARequestCreatorWithCreatedWSDLTest {

  Definitions wsdl

  @Before
  void setUp() {
    Assume.assumeTrue(!System.getenv('OFFLINETESTING'))
    WSDLParser parser = new WSDLParser()
    wsdl = parser.parse("http://www.thomas-bayer.com/axis2/services/BLZService?wsdl")
    addOperation()
  }

  private void addOperation() {
    Schema schema = wsdl.getSchema("http://thomas-bayer.com/blz/")
    schema.newElement("listBanks").newComplexType().newSequence()
    Sequence seq = schema.newElement("listBanksResponse").newComplexType().newSequence();
    seq.newElement("bank", STRING);
    seq.newElement("name", STRING);

    PortType pt = wsdl.getPortType("BLZServicePortType");

    Operation op = pt.newOperation("listBanks");
    op.newInput("listBanks").newMessage("listBanks").newPart("parameters", "tns:listBanks");
    op.newOutput("listBanksResponse").newMessage("listBanksResponse").newPart("parameters", "tns:listBanksResponse");

    Binding bnd = wsdl.getBinding("BLZServiceSOAP11Binding");
    BindingOperation bo = bnd.newBindingOperation("listBanks");
    bo.newSOAP11Operation();
    bo.newInput().newSOAP11Body();
    bo.newOutput().newSOAP11Body();
  }

  @Test
  void testDefinitions() {
    assert 2 == wsdl.getBinding('BLZServiceSOAP11Binding').operations.size()
  }

  @Test
  void testSOARequest() {
    StringWriter writer = new StringWriter()
    SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(), new MarkupBuilder(writer))
    creator.createRequest("BLZServicePortType", "listBanks", "BLZServiceSOAP11Binding")
    assert 'listBanks' == new XmlSlurper().parseText(writer.toString()).Envelope.body.listBanks.name()
  }

}
