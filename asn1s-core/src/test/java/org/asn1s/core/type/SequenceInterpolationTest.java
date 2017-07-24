////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2017. Lapinin "lastrix" Sergey.                          /
//                                                                             /
// Permission is hereby granted, free of charge, to any person                 /
// obtaining a copy of this software and associated documentation              /
// files (the "Software"), to deal in the Software without                     /
// restriction, including without limitation the rights to use,                /
// copy, modify, merge, publish, distribute, sublicense, and/or                /
// sell copies of the Software, and to permit persons to whom the              /
// Software is furnished to do so, subject to the following                    /
// conditions:                                                                 /
//                                                                             /
// The above copyright notice and this permission notice shall be              /
// included in all copies or substantial portions of the Software.             /
//                                                                             /
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,             /
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES             /
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                    /
// NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT                /
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,                /
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING                /
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE                  /
// OR OTHER DEALINGS IN THE SOFTWARE.                                          /
////////////////////////////////////////////////////////////////////////////////

package org.asn1s.core.type;

import org.asn1s.api.Ref;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.*;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Test;

@SuppressWarnings( "ALL" )
public class SequenceInterpolationTest
{
	@Test
	public void testInterpolates() throws Exception
	{
		/*
World-Schema DEFINITIONS AUTOMATIC TAGS ::=
BEGIN
CCC ::= [APPLICATION 7] INTEGER
A ::= SEQUENCE { a INTEGER }
B ::= SEQUENCE{ COMPONENTS OF A, b INTEGER}
C ::= SEQUENCE {COMPONENTS OF B, c CCC, ..., e INTEGER, ..., d INTEGER}
END
		 */
		TypeFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();

		Ref<Type> intRef = factory.builtin( "INTEGER" );
		DefinedType cccType = factory.define( "CCC", factory.tagged( TagEncoding.application( 7 ), intRef ), null );

		CollectionType aSeq = factory.collection( Type.Family.Sequence );
		aSeq.addComponent( ComponentType.Kind.Primary, "a", factory.tagged( TagEncoding.context( 0, TagMethod.Implicit ), intRef ), true, null );
		DefinedType aType = factory.define( "A", aSeq, null );

		CollectionType bSeq = factory.collection( Type.Family.Sequence );
		bSeq.addComponentsFromType( ComponentType.Kind.Primary, aType.toRef() );
		bSeq.addComponent( ComponentType.Kind.Primary, "b", intRef, true, null );
		DefinedType bType = factory.define( "B", bSeq, null );

		CollectionType chChoice = factory.collection( Type.Family.Choice );
		chChoice.addComponent( ComponentType.Kind.Primary, "a", intRef );
		chChoice.addComponent( ComponentType.Kind.Primary, "b", intRef );
		DefinedType chType = factory.define( "CH", chChoice, null );

		CollectionType cSeq = factory.collection( Type.Family.Sequence );
		cSeq.setExtensible( true );
		cSeq.addComponent( ComponentType.Kind.Extension, "e", intRef );
		cSeq.addComponentsFromType( ComponentType.Kind.Secondary, bType.toRef() );
		cSeq.addComponent( ComponentType.Kind.Secondary, "ch", chType, true, null );
		cSeq.addComponent( ComponentType.Kind.Secondary, "c", cccType.toRef() );
		cSeq.addComponent( ComponentType.Kind.Secondary, "d", intRef );//factory.tagged( TagEncoding.context( 8, TagMethod.Implicit ), intRef )
		DefinedType cType = factory.define( "C", cSeq, null );

		module.validate();
	}
}
