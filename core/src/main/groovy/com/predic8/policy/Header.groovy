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

package com.predic8.policy

import javax.xml.namespace.QName

import com.predic8.soamodel.*

class Header extends PolicyOperator{

	/**
	 * ELEMENTNAME will be set at runtime. Depending on the used version,
	 * it should be the one from the XML document cause it will be used
	 * to find the end tag of the XML element.
	 */
	QName ELEMENTNAME
	static final QName VERSION1 = new QName(Consts.SP_NS, 'Header')
	static final QName VERSION2 = new QName(Consts.SP_NS2, 'Header')
	String name
	
	protected parseAttributes( token,  ctx) {
		name = token.getAttributeValue( null , 'Name')
		namespace = token.getAttributeValue( null , 'Namespace')
	}
	
	protected parseChildren(token, child, ctx){
		super.parseChildren(token, child, ctx)
	}

	@Override
	public Object create(Object creator, Object context) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

