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

package org.asn1s.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Asn1Factory;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.value.DefinedValue;
import org.asn1s.api.value.Value;
import org.asn1s.core.DefaultAsn1Factory;
import org.asn1s.core.module.ModuleSet;
import org.asn1s.io.Asn1Reader;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.BerRules;
import org.asn1s.io.ber.input.DefaultBerReader;
import org.asn1s.io.ber.output.DefaultBerWriter;
import org.asn1s.schema.SchemaUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.ByteArrayInputStream;
import java.util.*;

@RunWith( Parameterized.class )
public class ConstraintSuiteTest
{
	private static final Log log = LogFactory.getLog( ConstraintSuiteTest.class );

	@Parameters( name = "{0}" )
	public static Collection<Object[]> data()
	{
		List<Object[]> list = new ArrayList<>();
		Utils.createDataFromFolder( list, "asn1s.tests.constraint.value", "Constraint:Value" );
		Utils.createDataFromFolder( list, "asn1s.tests.constraint.value-range", "Constraint:ValueRange" );
		Utils.createDataFromFolder( list, "asn1s.tests.constraint.size", "Constraint:Size" );
		Utils.createDataFromFolder( list, "asn1s.tests.constraint.inner-type", "Constraint:InnerType" );
		Utils.createDataFromFolder( list, "asn1s.tests.constraint.inner-types", "Constraint:InnerTypes" );
		Utils.createDataFromFolder( list, "asn1s.tests.constraint.pattern", "Constraint:Pattern" );
		Utils.createDataFromFolder( list, "asn1s.tests.constraint.permitted-alphabet", "Constraint:PermittedAlphabet" );
		list.sort( Comparator.comparing( o -> (String)o[0] ) );
		return list;
	}

	private final String resourceFolder;

	public ConstraintSuiteTest( @SuppressWarnings( "unused" ) String title, String resourceFolder )
	{
		this.resourceFolder = resourceFolder;
	}

	private String pdu;
	private String pduFail;
	private String schema;

	@Before
	public void setUp() throws Exception
	{
		pdu = Utils.getResourceAsStringOrDie( resourceFolder + "pdu.asn" );
		pduFail = Utils.getResourceAsStringOrDie( resourceFolder + "pdu-fail.asn" );
		schema = Utils.getResourceAsStringOrDie( resourceFolder + "schema.asn" );
	}

	@Test
	public void testSchemaParseAndValidate() throws Exception
	{
		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultAsn1Factory( resolver ) );
		Assert.assertFalse( "No modules", modules.isEmpty() );

		for( Module module : modules )
			module.validate();
	}

	@Test
	public void testValueParse() throws Exception
	{
		ModuleResolver resolver = new ModuleSet();
		Module module = SchemaUtils.parsePdu( pdu, resolver, new DefaultAsn1Factory( resolver ) );
		Assert.assertNotNull( "Null result", module );
		Assert.assertFalse( "No values parsed", module.getValueResolver().getValues().isEmpty() );
	}

	@Test
	public void testFailValueParse() throws Exception
	{
		ModuleSet resolver = new ModuleSet();
		Module module = SchemaUtils.parsePdu( pduFail, resolver, new DefaultAsn1Factory( resolver ) );
		Assert.assertNotNull( "Null result", module );
		Assert.assertFalse( "No values parsed", module.getValueResolver().getValues().isEmpty() );
	}

	@Test
	public void testValuesAccepted() throws Exception
	{
		ModuleSet resolver = new ModuleSet();
		Asn1Factory asn1Factory = new DefaultAsn1Factory( resolver );
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, asn1Factory );

		for( Module module : modules )
			module.validate();
		Module module = SchemaUtils.parsePdu( pdu, resolver, asn1Factory );
		module.validate( false, false );

		Scope scope = module.createScope();
		Collection<String> failed = new HashSet<>();
		for( DefinedValue value : module.getValueResolver().getValues() )
		{
			try
			{
				value.validate( scope );
			} catch( Asn1Exception e )
			{
				log.warn( "Unable to validate value: " + value.getName(), e );
				failed.add( value.getName() + " = " + value.getValue() );
			}
		}

		Assert.assertTrue( "Some values was not accepted by type, but should: " + failed, failed.isEmpty() );
	}

	@Test
	public void testAcceptedValuesWriteRead() throws Exception
	{
		ModuleSet resolver = new ModuleSet();
		Asn1Factory asn1Factory = new DefaultAsn1Factory( resolver );
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, asn1Factory );

		for( Module module : modules )
			module.validate();
		Module module = SchemaUtils.parsePdu( pdu, resolver, asn1Factory );
		Assert.assertNotNull( "Module must not be null", module );
		module.validate();

		Scope scope = module.createScope();
		for( DefinedValue value : module.getValueResolver().getValues() )
		{
			checkValueWriteRead( scope, value, BerRules.BER, asn1Factory );
			checkValueWriteRead( scope, value, BerRules.DER, asn1Factory );
		}
	}

	private static void checkValueWriteRead( Scope scope, DefinedValue value, BerRules rules, Asn1Factory asn1Factory ) throws Exception
	{
		scope = value.getType().getScope( scope );
		byte[] written;
		try( Asn1Writer writer = new DefaultBerWriter( rules ) )
		{
			writer.write( scope, value.getType(), value.getValue() );
			written = writer.toByteArray();
		}

		Value actual;
		try( Asn1Reader reader = new DefaultBerReader( new ByteArrayInputStream( written ), asn1Factory.values() ) )
		{
			actual = reader.read( scope, value.getType() );
		}

		if( value.compareTo( actual ) != 0 )
			throw new IllegalStateException( "Values are not equal: expected: " + value + ", actual: " + actual );
	}

	@Test
	public void testValuesNotAccepted() throws Exception
	{
		ModuleSet resolver = new ModuleSet();
		Asn1Factory asn1Factory = new DefaultAsn1Factory( resolver );
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, asn1Factory );

		for( Module module : modules )
			module.validate();
		Module module = SchemaUtils.parsePdu( pduFail, resolver, asn1Factory );
		module.validate( false, false );
		Scope scope = module.createScope();
		Collection<String> notFailed = new HashSet<>();
		for( DefinedValue value : module.getValueResolver().getValues() )
		{
			try
			{
				value.validate( scope );
				notFailed.add( value.getName() + " = " + value.getValue() );
			} catch( Asn1Exception e )
			{
				if( log.isDebugEnabled() )
					log.debug( "Unable to validate value: " + value.getName(), e );
			}
		}

		Assert.assertTrue( "Some values was accepted by type, but shouldn't: " + notFailed, notFailed.isEmpty() );
	}
}
