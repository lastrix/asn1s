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
import org.asn1s.api.Module;
import org.asn1s.api.ModuleResolver;
import org.asn1s.api.ObjectFactory;
import org.asn1s.api.Scope;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.DefinedValue;
import org.asn1s.core.DefaultObjectFactory;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@RunWith( Parameterized.class )
public class SuiteTest
{
	private static final Log log = LogFactory.getLog( SuiteTest.class );

	@Parameters( name = "{0}" )
	public static Collection<Object[]> data()
	{
		List<Object[]> list = new ArrayList<>();
		Utils.createDataFromFolder( list, "asn1s.tests.a", "a" );
		Utils.createDataFromFolder( list, "asn1s.tests.b", "b" );
		list.sort( Comparator.comparing( o -> (String)o[0] ) );
		return list;
	}

	private final String resourceFolder;

	public SuiteTest( @SuppressWarnings( "unused" ) String title, String resourceFolder )
	{
		this.resourceFolder = resourceFolder;
	}

	private String pdu;
	private String schema;
	//private byte[] pduBer;
	private byte[] pduDer;
//	private byte[] pduPer;
	//private byte[] pduUPer;
	//private String pduXml;

	@Before
	public void setUp() throws Exception
	{
		pdu = Utils.getResourceAsStringOrDie( resourceFolder + "pdu.asn" );
		schema = Utils.getResourceAsStringOrDie( resourceFolder + "schema.asn" );
		//pduBer = Utils.getResourceAsBytesOrDie( resourceFolder + "pdu.ber" );
		pduDer = Utils.getResourceAsBytesOrDie( resourceFolder + "pdu.der" );
//		pduPer = Utils.getResourceAsBytesOrDie( resourceFolder + "pdu.per" );
		//pduUPer = Utils.getResourceAsBytesOrDie( resourceFolder + "pdu.uper" );
		//pduXml = Utils.getResourceAsStringOrDie( resourceFolder + "pdu.xml" );
	}

	@Test
	public void testSchemaParse() throws Exception
	{
		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultObjectFactory( resolver ) );
		Assert.assertFalse( "No modules", modules.isEmpty() );
	}

	@Test
	public void testValueParse() throws Exception
	{
		ModuleResolver resolver = new ModuleSet();
		Module module = SchemaUtils.parsePdu( pdu, resolver, new DefaultObjectFactory( resolver ) );
		Assert.assertNotNull( "Null result", module );
		Assert.assertFalse( "No values parsed", module.getValues().isEmpty() );
	}

	@Test
	public void testWrite() throws Exception
	{
		ModuleSet resolver = new ModuleSet();
		ObjectFactory objectFactory = new DefaultObjectFactory( resolver );
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, objectFactory );
		try
		{
			for( Module module : modules )
				module.validate();
		} catch( Exception e )
		{
			log.fatal( "Exception: " + e.getMessage(), e );
			Assert.fail( "Unable to validate modules" );
		}

		Module module = SchemaUtils.parsePdu( pdu, resolver, objectFactory );
		try
		{
			module.validate();
		} catch( Exception e )
		{
			log.error( "Exception: " + e.getMessage(), e );
			Assert.fail( "Unable to validate module: " + e.getMessage() );
		}

		byte[] result = null;
		try( Asn1Writer writer = new DefaultBerWriter( BerRules.Der ) )
		{
			for( DefinedValue value : module.getValues() )
				writer.write( value.getType().getScope( module.createScope() ), value.getType(), value.getValue() );
			result = writer.toByteArray();
		} catch( Exception e )
		{
			log.fatal( "Exception: " + e.getMessage(), e );
			Assert.fail( "Unable to write value" );
		}

		Assert.assertArrayEquals( "Content is not equal", pduDer, result );

		Scope scope = module.createScope();
		try( Asn1Reader reader = new DefaultBerReader( new ByteArrayInputStream( result ), objectFactory ) )
		{
			for( DefinedValue value : module.getValues() )
			{
				scope = value.getType().getScope( scope );
				Value actual = reader.read( scope, value.getType() );
				Assert.assertTrue( "Values are not equal: " + value + " != " + actual, value.isEqualTo( actual ) );
			}
		}
	}
}
