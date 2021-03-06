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

package com.predic8.wsdl.usage


import com.predic8.schema.SchemaComponent
import com.predic8.wsdl.Definitions
import com.predic8.wsdl.Operation

class OperationUsageUtil {

  Definitions wsdl
  List<OperationUseVisitorContext> usageList = []


  def summary(operations = wsld.operations) {
    operations.each {
      usageList << op.usedSchemaComponents
    }
    usageList
  }

  List<Operation> findOperations4SchemaComponent(SchemaComponent sc) {
    usageList.findAll { ctx ->
      (ctx.elements + ctx.complexTypes + ctx.simpleTypes).contains(sc)
    }.operation
  }
}
