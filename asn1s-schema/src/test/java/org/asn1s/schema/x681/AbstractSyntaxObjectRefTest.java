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

package org.asn1s.schema.x681;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.type.x681.ClassType;
import org.asn1s.api.value.Value;
import org.asn1s.core.module.ModuleImpl;
import org.asn1s.core.module.ModuleSet;
import org.asn1s.core.type.DefinedTypeImpl;
import org.asn1s.core.type.x680.collection.SequenceType;
import org.asn1s.core.type.x681.ClassTypeImpl;
import org.asn1s.core.type.x681.TypeFieldType;
import org.asn1s.core.type.x681.ValueFieldType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( AbstractSyntaxParser.class )
public class AbstractSyntaxObjectRefTest
{
	@Test( expected = IllegalArgumentException.class )
	public void testIllegalConstructorParamFail()
	{
		//noinspection ResultOfObjectAllocationIgnored
		new AbstractSyntaxObjectRef( "EXAMPLE TO FAIL" );
		Assert.fail( "Must fail" );
	}

	@Test( expected = ResolutionException.class )
	public void testParserFail() throws Exception
	{
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		AbstractSyntaxObjectRef ref = new AbstractSyntaxObjectRef( "{ ID &id }" );
		ClassType classType = new ClassTypeImpl();
		Scope scope = classType.getScope( module.createScope() );
		AbstractSyntaxParser parser = PowerMockito.mock( AbstractSyntaxParser.class );
		PowerMockito.whenNew( AbstractSyntaxParser.class ).withAnyArguments().thenReturn( parser );
		when( parser.parse( anyString() ) ).thenThrow( new IllegalStateException() );
		ref.resolve( scope );
	}

	@Test
	public void testParser() throws Exception
	{
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		ClassTypeImpl classType = new ClassTypeImpl();
		classType.setSyntaxList( Arrays.asList( "&Type", "IDENTIFIED", "BY", "&id", "[", "CONSTRAINED", "BY", "&TypeConstraint", "]" ) );
		classType.add( new ValueFieldType( "&id", UniversalType.OBJECT_IDENTIFIER.ref(), true, false ) );
		classType.add( new TypeFieldType( "&Type", false, null ) );
		classType.add( new TypeFieldType( "&TypeConstraint", false, null ) );
		classType.validate( module.createScope() );

		Ref<Value> ref = new AbstractSyntaxObjectRef( "{Super-Type IDENTIFIED BY { rootOid 3 } CONSTRAINED BY TYPE-Constraint}" );
		Value value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{OBJECT IDENTIFIER IDENTIFIED BY { rootOid 3 } }" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );
	}

	@Test( expected = ResolutionException.class )
	public void testNoClassType() throws Exception
	{
		AbstractSyntaxObjectRef ref = new AbstractSyntaxObjectRef( "{ ID &id }" );
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		DefinedTypeImpl myInt = new DefinedTypeImpl( module, "MyInt", UniversalType.INTEGER.ref() );

		ref.resolve( myInt.createScope() );
	}

	@Test( expected = ResolutionException.class )
	public void testParserValueFail() throws Exception
	{
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		ClassTypeImpl classType = new ClassTypeImpl();
		classType.setSyntaxList( Arrays.asList( "&Type", "IDENTIFIED", "BY", "&id", "[", "CONSTRAINED", "BY", "&TypeConstraint", "]" ) );
		classType.add( new ValueFieldType( "&id", UniversalType.OBJECT_IDENTIFIER.ref(), true, false ) );
		classType.add( new TypeFieldType( "&Type", false, null ) );
		classType.add( new TypeFieldType( "&TypeConstraint", false, null ) );
		classType.validate( module.createScope() );

		Ref<Value> ref = new AbstractSyntaxObjectRef( "{BIT STRING IDENTIFIED BY NAME { rootOid 3 } CONSTRAINED BY TYPE-Constraint}" );
		Value value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );
	}

	@Test
	public void testParserValueSequence() throws Exception
	{
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		ClassTypeImpl classType = new ClassTypeImpl();
		classType.setSyntaxList( Arrays.asList( "&Type", "IDENTIFIED", "BY", "&id", "[", "CONSTRAINED", "BY", "&TypeConstraint", "]" ) );
		classType.add( new ValueFieldType( "&id", new SequenceType( true ), true, false ) );
		classType.add( new TypeFieldType( "&Type", false, null ) );
		classType.add( new TypeFieldType( "&TypeConstraint", false, null ) );
		classType.validate( module.createScope() );

		Ref<Value> ref = new AbstractSyntaxObjectRef( "{INTEGER IDENTIFIED BY { rootOid 3, a 2 } CONSTRAINED BY TYPE-Constraint}" );
		Value value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{CHARACTER STRING IDENTIFIED BY { rootOid 3, a 2 } CONSTRAINED BY TYPE-Constraint}" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{OCTET STRING IDENTIFIED BY { rootOid 3, a 2 } CONSTRAINED BY TYPE-Constraint}" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{EMBEDDED PDV IDENTIFIED BY { rootOid 3, a 2 } CONSTRAINED BY TYPE-Constraint}" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{EMBEDDED PDV IDENTIFIED BY 1 CONSTRAINED BY TYPE-Constraint}" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{EMBEDDED PDV IDENTIFIED BY 1.0 CONSTRAINED BY TYPE-Constraint}" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{EMBEDDED PDV IDENTIFIED BY 'AF'H CONSTRAINED BY TYPE-Constraint}" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{EMBEDDED PDV IDENTIFIED BY '0101'B CONSTRAINED BY TYPE-Constraint}" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );

		ref = new AbstractSyntaxObjectRef( "{EMBEDDED PDV IDENTIFIED BY 'Word' CONSTRAINED BY TYPE-Constraint}" );
		value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );
	}

	@Test( expected = ResolutionException.class )
	public void testParserFailTypeExpected() throws Exception
	{
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		ClassTypeImpl classType = new ClassTypeImpl();
		classType.setSyntaxList( Arrays.asList( "&Type", "IDENTIFIED", "BY", "&id", "[", "CONSTRAINED", "BY", "&TypeConstraint", "]" ) );
		classType.add( new ValueFieldType( "&id", UniversalType.OBJECT_IDENTIFIER.ref(), true, false ) );
		classType.add( new TypeFieldType( "&Type", false, null ) );
		classType.add( new TypeFieldType( "&TypeConstraint", false, null ) );
		classType.validate( module.createScope() );

		Ref<Value> ref = new AbstractSyntaxObjectRef( "{1 IDENTIFIED BY { rootOid 3 } CONSTRAINED BY TYPE-Constraint}" );
		Value value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );
	}

	@Test
	public void testParserExternalRef() throws Exception
	{
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		ClassTypeImpl classType = new ClassTypeImpl();
		classType.setSyntaxList( Arrays.asList( "&Type", "IDENTIFIED", "BY", "&id", "[", "CONSTRAINED", "BY", "&TypeConstraint", "]" ) );
		classType.add( new ValueFieldType( "&id", UniversalType.OBJECT_IDENTIFIER.ref(), true, false ) );
		classType.add( new TypeFieldType( "&Type", false, null ) );
		classType.add( new TypeFieldType( "&TypeConstraint", false, null ) );
		classType.validate( module.createScope() );

		Ref<Value> ref = new AbstractSyntaxObjectRef( "{ My-Module.Type-External IDENTIFIED BY { rootOid 3 } CONSTRAINED BY TYPE-Constraint}" );
		Value value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );
	}

	@Test( expected = ResolutionException.class )
	public void testParserOptionalFail() throws Exception
	{
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		ClassTypeImpl classType = new ClassTypeImpl();
		classType.setSyntaxList( Arrays.asList( "&Type", "IDENTIFIED", "BY", "&id", "[", "CONSTRAINED", "BY", "&TypeConstraint", "]" ) );
		classType.add( new ValueFieldType( "&id", UniversalType.OBJECT_IDENTIFIER.ref(), true, false ) );
		classType.add( new TypeFieldType( "&Type", false, null ) );
		classType.add( new TypeFieldType( "&TypeConstraint", false, null ) );
		classType.validate( module.createScope() );

		Ref<Value> ref = new AbstractSyntaxObjectRef( "{ My-Module.Type-External IDENTIFIED BY { rootOid 3 } CONSTRAINED BY type-Constraint}" );
		Value value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );
	}

	@Test( expected = ResolutionException.class )
	public void testParserOptionalFailValue() throws Exception
	{
		ModuleImpl module = ModuleImpl.newDummy( new ModuleSet() );
		ClassTypeImpl classType = new ClassTypeImpl();
		classType.setSyntaxList( Arrays.asList( "&Type", "IDENTIFIED", "BY", "&id", "[", "CONSTRAINED", "BY", "&TypeConstraint", "]" ) );
		classType.add( new ValueFieldType( "&id", UniversalType.OBJECT_IDENTIFIER.ref(), true, false ) );
		classType.add( new TypeFieldType( "&Type", false, null ) );
		classType.add( new TypeFieldType( "&TypeConstraint", false, null ) );
		classType.validate( module.createScope() );

		Ref<Value> ref = new AbstractSyntaxObjectRef( "{ My-Module.Type-External IDENTIFIED BY}" );
		Value value = ref.resolve( classType.getScope( module.createScope() ) );
		Assert.assertNotNull( "No result", value );
	}
}
