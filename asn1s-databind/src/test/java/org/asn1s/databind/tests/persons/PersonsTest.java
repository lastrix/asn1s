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

package org.asn1s.databind.tests.persons;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.core.DefaultAsn1Factory;
import org.asn1s.core.module.ModuleImpl;
import org.asn1s.core.module.ModuleSet;
import org.asn1s.databind.Asn1Context;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@SuppressWarnings( "ALL" )
public class PersonsTest
{
	@Rule
	public TestRule benchmarkRun = new BenchmarkRule();

	private static Asn1Context context;
	private static byte[] expectedBytes;
	private static Person expectedPerson = new Person(
			"Jack",
			"Smith",
			29,
			new Person[]{
					new Person( "Sally", "Smith", 25 ),
					new Person( "Kate", "Smith", 8 )
			} );
	private static ObjectMapper jsonMapper;
	private static String expectedString;
	private static ObjectReader objectReader;
	private static ObjectWriter objectWriter;

	@BeforeClass
	public static void setUp() throws Exception
	{
		ModuleSet moduleSet = new ModuleSet();
		ModuleImpl module = new ModuleImpl( new ModuleReference( "My-Module" ), moduleSet );
		context = new Asn1Context( module, new DefaultAsn1Factory( moduleSet ) );
		context.mapType( Person.class );
		module.validate();

		try( ByteArrayOutputStream os = new ByteArrayOutputStream() )
		{
			context.createMarshaller().marshall( expectedPerson, os );
			expectedBytes = os.toByteArray();
		}

		Person result;
		try( ByteArrayInputStream is = new ByteArrayInputStream( expectedBytes ) )
		{
			result = context.createUnmarshaller().unmarshal( Person.class, is );
		}

		Assert.assertEquals( "Values are not equal", expectedPerson, result );

		jsonMapper = new ObjectMapper();
		objectReader = jsonMapper.readerFor( Person.class );
		objectWriter = jsonMapper.writerFor( Person.class );
		expectedString = objectWriter.writeValueAsString( expectedPerson );
	}

	@BenchmarkOptions( benchmarkRounds = 100000, warmupRounds = 10000 )
	@Test
	public void testPersonsWrite() throws Exception
	{
		byte[] bytes;
		try( ByteArrayOutputStream os = new ByteArrayOutputStream() )
		{
			context.createMarshaller().marshall( expectedPerson, os );
			bytes = os.toByteArray();
		}
		Assert.assertArrayEquals( "Values are not equal", expectedBytes, bytes );
	}

	@BenchmarkOptions( benchmarkRounds = 100000, warmupRounds = 10000 )
	@Test
	public void testPersonsRead() throws Exception
	{
		Person result;
		try( ByteArrayInputStream is = new ByteArrayInputStream( expectedBytes ) )
		{
			result = context.createUnmarshaller().unmarshal( Person.class, is );
		}

		Assert.assertEquals( "Values are not equal", expectedPerson, result );
	}

	@BenchmarkOptions( benchmarkRounds = 100000, warmupRounds = 10000 )
	@Test
	public void testPersonsJsonWrite() throws Exception
	{
		String result = objectWriter.writeValueAsString( expectedPerson );
		Assert.assertEquals( "Values are not equal", expectedString, result );
	}

	@BenchmarkOptions( benchmarkRounds = 100000, warmupRounds = 10000 )
	@Test
	public void testPersonsJsonRead() throws Exception
	{
		Person result = objectReader.readValue( expectedString );
		Assert.assertEquals( "Values are not equal", expectedPerson, result );
	}
}
