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

package org.asn1s.io.ber.output;

import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.type.x680.collection.SequenceType;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.asn1s.core.value.x680.NamedValueImpl;
import org.asn1s.core.value.x680.RealValueFloat;
import org.asn1s.core.value.x680.ValueCollectionImpl;
import org.asn1s.io.ber.BerRules;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class SequenceBerEncoderTest
{
	public static final Tag TAG = new Tag( TagClass.Universal, true, UniversalType.Sequence.tagNumber() );
	private static final Tag TAG_INSTANCE_OF = new Tag( TagClass.Universal, true, UniversalType.InstanceOf.tagNumber() );

	@Test
	public void testWriteSet_Buffered() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), true, null );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ValueCollection value = new ValueCollectionImpl( true );
		NamedValue namedValue = new NamedValueImpl( "a", new IntegerValueInt( 0 ) );
		value.add( namedValue );
		try( AbstractBerWriter writer = new DefaultBerWriter( BerRules.Ber ) )
		{
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, true )
			{
				@Override
				public boolean isBufferingAvailable()
				{
					return false;
				}
			} );
			byte[] bytes = writer.toByteArray();
			Assert.assertArrayEquals( "Arrays are not equal", new byte[]{0x30, (byte)0x80, (byte)0x80, 0x01, 0x00, 0x00, 0x00}, bytes );
		}
	}

	@Test
	public void testWriteSet_NonBuffered() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), true, null );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ValueCollection value = new ValueCollectionImpl( true );
		NamedValue namedValue = new NamedValueImpl( "a", new IntegerValueInt( 0 ) );
		value.add( namedValue );
		try( AbstractBerWriter writer = new DefaultBerWriter( BerRules.Ber ) )
		{
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, true )
			{
				@Override
				public boolean isBufferingAvailable()
				{
					return false;
				}
			} );
			byte[] bytes = writer.toByteArray();
			Assert.assertArrayEquals( "Arrays are not equal", new byte[]{0x30, (byte)0x80, (byte)0x80, 0x01, 0x00, 0x00, 0x00}, bytes );
		}
	}

	@Test( expected = Asn1Exception.class )
	public void testWriteSet_NonBuffered_Der() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), true, null );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ValueCollection value = new ValueCollectionImpl( true );
		NamedValue namedValue = new NamedValueImpl( "a", new IntegerValueInt( 0 ) );
		value.add( namedValue );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			when( writer.isBufferingAvailable() ).thenReturn( false );
			when( writer.getRules() ).thenReturn( BerRules.Der );
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			fail( "Must fail" );
		}
	}

	@Test
	public void testWriteSet_NoHeader() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), true, null );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ValueCollection value = new ValueCollectionImpl( true );
		NamedValue namedValue = new NamedValueImpl( "a", new IntegerValueInt( 0 ) );
		value.add( namedValue );
		try( AbstractBerWriter writer = new DefaultBerWriter( BerRules.Der ) )
		{
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			byte[] bytes = writer.toByteArray();
			Assert.assertArrayEquals( "Arrays are not equal", new byte[]{(byte)0x80, 0x01, 0x00}, bytes );
		}
	}

	@Test( expected = IllegalValueException.class )
	public void testWriteSet_NoHeader_NonExtensible() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), true, null );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ValueCollection value = new ValueCollectionImpl( true );
		NamedValue namedValue = new NamedValueImpl( "a", new IntegerValueInt( 0 ) );
		value.add( namedValue );
		value.addNamed( "c", new RealValueFloat( 0.0f ) );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Ber );
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = IllegalValueException.class )
	public void testWriteSet_NoHeader_Extensible() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), true, null );
		type.setExtensible( true );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ValueCollection value = new ValueCollectionImpl( true );
		NamedValue namedValue = new NamedValueImpl( "a", new IntegerValueInt( 0 ) );
		value.add( namedValue );
		value.addNamed( "c", new RealValueFloat( 0.0f ) );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Ber );
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			verify( writer, times( 1 ) ).getRules();
			verify( writer ).writeInternal( any() );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteSet_NoHeader_Default() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), false, new IntegerValueInt( 1 ) );
		type.setExtensible( true );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ValueCollection value = new ValueCollectionImpl( true );
		NamedValue namedValue = new NamedValueImpl( "a", new IntegerValueInt( 0 ) );
		value.add( namedValue );
		value.addNamed( "b", new IntegerValueInt( 1 ) );
		try( AbstractBerWriter writer = new DefaultBerWriter( BerRules.Der ) )
		{
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			byte[] bytes = writer.toByteArray();
			Assert.assertArrayEquals( "Arrays are not equal", new byte[]{(byte)0x80, 0x01, 0x00}, bytes );
		}
	}

	@Test
	public void testWriteSet_NoHeader_NotDefault() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), false, new IntegerValueInt( 1 ) );
		type.setExtensible( true );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ComponentType componentB = type.getNamedType( "b" );
		Assert.assertNotNull( "No component b", componentB );
		ValueCollection value = new ValueCollectionImpl( true );
		NamedValue namedValue = new NamedValueImpl( "a", new IntegerValueInt( 0 ) );
		value.add( namedValue );
		NamedValue bValue = new NamedValueImpl( "b", new IntegerValueInt( 0 ) );
		value.add( bValue );
		try( AbstractBerWriter writer = new DefaultBerWriter( BerRules.Der ) )
		{
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			byte[] bytes = writer.toByteArray();
			Assert.assertArrayEquals( "Arrays are not equal", new byte[]{(byte)0x80, 0x01, 0x00, (byte)0x81, 0x01, 0x00}, bytes );
		}
	}

	@Test( expected = IllegalValueException.class )
	public void testWriteSet_NoHeader_Empty() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceType type = new SequenceType( true );
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.addComponent( Kind.Primary, "b", UniversalType.Integer.ref(), false, new IntegerValueInt( 1 ) );
		type.setExtensible( true );
		type.validate( scope );
		ComponentType componentA = type.getNamedType( "a" );
		Assert.assertNotNull( "No component a", componentA );
		ComponentType componentB = type.getNamedType( "b" );
		Assert.assertNotNull( "No component b", componentB );
		ValueCollection value = new ValueCollectionImpl( true );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Der );
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		Value value = new ValueCollectionImpl( true );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new NullBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_value() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = new SequenceType( true );
		Value value = BooleanValue.TRUE;
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new SequenceBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}
}
