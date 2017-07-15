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

package org.asn1s.schema;

import org.asn1s.api.module.Module;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.module.ModuleSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@SuppressWarnings( "JUnitTestMethodWithNoAssertions" )
public class SimpleParserTest
{
	@Test
	public void testReal() throws Exception
	{
		String schema = "MyModule DEFINITIONS AUTOMATIC TAGS ::= BEGIN MyReal ::= REAL END";
		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultObjectFactory( resolver ) );
		Assert.assertEquals( "Exactly 1 module required", 1, modules.size() );
		Module module = modules.get( 0 );
		module.validate();
		Assert.assertEquals( "Single type expected", 1, module.getTypeResolver().getTypes().size() );
		Assert.assertEquals( "No values expected", 0, module.getValueResolver().getValues().size() );
	}

	@Test
	public void testTaggedType() throws Exception
	{
		String schema = "MyModule DEFINITIONS AUTOMATIC TAGS ::= BEGIN MyType ::= [TAG: APPLICATION 1] EXPLICIT REAL MyType2 ::= [2] IMPLICIT INTEGER END";
		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultObjectFactory( resolver ) );
		Assert.assertEquals( "Exactly 1 module required", 1, modules.size() );
		Module module = modules.get( 0 );
		module.validate();
		Assert.assertEquals( "Two types expected", 2, module.getTypeResolver().getTypes().size() );
		Assert.assertEquals( "No values expected", 0, module.getValueResolver().getValues().size() );
	}

	@Test
	public void testBitStringType() throws Exception
	{
		String schema = "MyModule DEFINITIONS AUTOMATIC TAGS ::= BEGIN MyType ::= BIT STRING MyType2 ::= BIT STRING {a (1), b(2), c(5), d(6), e(my-val), f(4)} my-val INTEGER ::= 10  END";
		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultObjectFactory( resolver ) );
		Assert.assertEquals( "Exactly 1 module required", 1, modules.size() );
		Module module = modules.get( 0 );
		module.validate();
		Assert.assertEquals( "Two types expected", 2, module.getTypeResolver().getTypes().size() );
		Assert.assertEquals( "Single value expected", 1, module.getValueResolver().getValues().size() );
	}

	@Test
	public void valueSetConstraint() throws Exception
	{
		String schema = "MyModule DEFINITIONS AUTOMATIC TAGS ::= BEGIN MySet INTEGER ::= {1 | 2 | 3 | 4 | 5 | 10..100} MyType ::= INTEGER (MySet) value MyType ::= 1  END";
		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultObjectFactory( resolver ) );
		for( Module module : modules )
			module.validate();


	}

	@Test
	public void testObjectParse() throws Exception
	{
		String schema = "MyModule DEFINITIONS AUTOMATIC TAGS ::= BEGIN " +
				" oid OBJECT IDENTIFIER ::= {a(1) b(2) c(100) 1 2}" +
				"Sex ::= INTEGER { male(0), female(1)}  " +
				"HUMAN ::= CLASS { &name UTF8String, &age INTEGER DEFAULT 20, &sex Sex DEFAULT male } WITH SYNTAX { &name [IS &age YEARS OLD[, &sex]]} " +
				"adam HUMAN ::= { &name 'adam' } " +
				"eva HUMAN ::= { &name 'eva', &age  18, &sex female } " +
				"Humans HUMAN ::= { adam | eva } " +
				"END";

		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultObjectFactory( resolver ) );
		for( Module module : modules )
			module.validate();

	}

	@Test
	public void testInstanceOf() throws Exception
	{
		String schema = "World-Schema {iso(1) standard(0) bhsm(17922) contentType(2) bhsmps(1)} DEFINITIONS AUTOMATIC TAGS ::= \n" +
				"BEGIN\n" +
				"    bhsmpsid  OBJECT IDENTIFIER ::=  {iso(1) standard(0) bhsm(17922) contentType(2) bhsmps(1)}\n" +
				"    s-type-id OBJECT IDENTIFIER ::= {bhsmpsid 2}\n" +
				"    h-type-id OBJECT IDENTIFIER ::= {bhsmpsid 3}\n" +
				"  MHS ::= TYPE-IDENTIFIER\n" +
				"  InstanceMHS ::= INSTANCE OF MHS ({SupportedMHS})\n" +
				"  S ::= SEQUENCE { f1 INTEGER, f2 INTEGER}\n" +
				"  H ::= SEQUENCE { f1 REAL, f2 REAL}\n" +
				"  supportedS MHS ::= { S IDENTIFIED BY s-type-id }\n" +
				"  supportedH MHS ::= { H IDENTIFIED BY h-type-id }\n" +
				"  SupportedMHS MHS ::= {supportedS | supportedH}\n" +
				'\n' +
				"  Message ::= SEQUENCE {\n" +
				"    seq InstanceMHS,\n" +
				"    a INTEGER\n" +
				"  }\n" +
				'\n' +
				"    value Message ::= \n" +
				"    {  \n" +
				"        seq { type-id h-type-id, value H:{f1 1, f2 2}},\n" +
				"        a 1\n" +
				"    }\n" +
				'\n' +
				"END";

		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultObjectFactory( resolver ) );
		for( Module module : modules )
			module.validate();
	}

	@Test
	public void testClassFieldRef() throws Exception
	{
		String schema = "World-Schema DEFINITIONS AUTOMATIC TAGS ::= \n" +
				"BEGIN\n" +
				"    FRUITS ::= CLASS {\n" +
				"        &name UTF8String,\n" +
				"        &country UTF8String\n" +
				"    } WITH SYNTAX { &name EXPORTER &country }\n" +
				"    \n" +
				"    apple FRUITS ::= { \"apple\" EXPORTER \"Poland\" }\n" +
				"    orange FRUITS ::= { \"orange\" EXPORTER \"Egypt\" }\n" +
				"    banana FRUITS ::= { \"banana\" EXPORTER \"Nicaragua\" }\n" +
				"    Fruits FRUITS ::= { apple | orange | banana }\n" +
				"    \n" +
				"    Request ::= SEQUENCE {\n" +
				"        name FRUITS.&name ({Fruits}),\n" +
				"        ticket CHOICE {\n" +
				"            paycheck  SEQUENCE {\n" +
				"                country FRUITS.&country ({Fruits}{@name}),\n" +
				"                price INTEGER,\n" +
				"                mass INTEGER\n" +
				"            },\n" +
				"            invoice SEQUENCE {\n" +
				"                country FRUITS.&country ({Fruits}{@name})\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"    \n" +
				"    value Request ::= {\n" +
				"        name \"apple\",\n" +
				"        ticket paycheck : {\n" +
				"            country \"Poland\",\n" +
				"            price 100,\n" +
				"            mass 10\n" +
				"        }\n" +
				"    }\n" +
				"END";

		ModuleSet resolver = new ModuleSet();
		List<Module> modules = SchemaUtils.parseModules( schema, resolver, new DefaultObjectFactory( resolver ) );
		for( Module module : modules )
			module.validate();
	}
}
