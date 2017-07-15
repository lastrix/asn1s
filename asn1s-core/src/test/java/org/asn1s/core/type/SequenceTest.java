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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Module;
import org.asn1s.api.ObjectFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

@SuppressWarnings( "ALL" )
public class SequenceTest
{
	private static final Log log = LogFactory.getLog( SequenceTest.class );

	@Test
	public void testSequenceValidates() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();

		Ref<Type> intType = factory.builtin( "INTEGER" );
		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( Kind.Primary, "a", intType, true, null );
		sequenceType.addComponent( Kind.Primary, "b", intType, true, null );
		sequenceType.addComponent( Kind.Extension, "c", intType, true, null );
		sequenceType.addComponent( Kind.Secondary, "d", intType, false, null );

		TagEncoding encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Explicit, TagClass.Application, 1 );
		DefinedType type =
				factory.define( "MyType", factory.tagged( encoding, sequenceType ), null );

		module.validate();
		Assert.assertNotEquals( "Empty actual components", 0, sequenceType.getComponents( true ) );

		doTest( factory, module, type, true );
	}

	@Test
	public void testSequenceTemplateValidates() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();

		Ref<?> reference = new TypeNameRef( "S-Type", null );
		Ref<Type> intType = factory.builtin( "INTEGER" );
		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( Kind.Primary, "a", intType, true, null );
		sequenceType.addComponent( Kind.Primary, "b", intType, true, null );
		sequenceType.addComponent( Kind.Extension, "c", intType, true, null );
		sequenceType.addComponent( Kind.Secondary, "d", intType, false, null );

		TagEncoding encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Explicit, TagClass.Application, 1 );
		DefinedType templateType =
				factory.define( "MyType", factory.tagged( encoding, sequenceType ),
				                Collections.singletonList( factory.templateParameter( 0, reference, null ) ) );


		Ref<Type> instance = factory.typeTemplateInstance( templateType.toRef(), Collections.singletonList( intType ) );
		DefinedType type = factory.define( "My-Type-Instance", instance, null );
		module.validate();

		doTest( factory, module, type, true );
	}

	@Test
	public void testSequenceFullCompUsage() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();

		Ref<Type> intType = factory.builtin( "INTEGER" );
		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( Kind.Primary, "a", intType, true, null );
		sequenceType.addComponent( Kind.Primary, "b", intType, true, null );
		sequenceType.addComponent( Kind.Extension, "c", intType, true, null );
		sequenceType.addComponent( Kind.Secondary, "d", intType, false, null );

		TagEncoding encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Explicit, TagClass.Application, 1 );
		DefinedType type =
				factory.define( "MyType", factory.tagged( encoding, sequenceType ), null );

		module.validate();
		Assert.assertNotEquals( "Empty actual components", 0, sequenceType.getComponents( true ) );

		doTest( factory, module, type, false );
	}

	private static void doTest( ValueFactory factory, Module module, Type type, boolean addExtensions )
	{
		ValueCollection collection = factory.collection( true );
		collection.addNamed( "a", factory.integer( 1 ) );
		collection.addNamed( "b", factory.integer( 20 ) );
		if( addExtensions )
			collection.addNamed( "c", factory.integer( 3000 ) );
		collection.addNamed( "d", factory.integer( 40000 ) );
		try
		{
			type.accept( module.createScope(), collection );
		} catch( ValidationException | ResolutionException e )
		{
			log.fatal( "Unable to accept value: " + collection, e );
			Assert.fail( "Failed to accept value: " + collection );
		}
	}
}
