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

package org.asn1s.databind;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.databind.builtin.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class Asn1Mapper
{
	private static final String DEFAULT_MODULE_NAME = "Java-Bind-Module";

	public Asn1Mapper( Asn1Factory factory ) throws Asn1Exception
	{
		this( factory, DEFAULT_MODULE_NAME, null );
	}

	public Asn1Mapper( Asn1Factory factory, @Nullable Type[] types ) throws Asn1Exception
	{
		this( factory, DEFAULT_MODULE_NAME, types );
	}

	public Asn1Mapper( Asn1Factory factory, String moduleName, @Nullable Type[] types ) throws Asn1Exception
	{
		this.factory = factory;
		Module module = factory.types().module( new ModuleReference( moduleName ) );
		initBuiltinTypes();
		if( types != null && types.length > 0 )
			context.mapTypes( factory, types );
		module.validate();
	}

	private final TypeMapperContext context = new TypeMapperContext();
	private final Asn1Factory factory;

	private void initBuiltinTypes()
	{
		BuiltinTypeFactory typeFactory = new BuiltinTypeFactory( context, factory );
		typeFactory.generate( IntegerTypeMapper.class, IntegerMapping.values() );
		typeFactory.generate( RealTypeMapper.class, RealMapping.values() );
		typeFactory.generate( BooleanTypeMapper.class, BooleanMapping.values() );
		typeFactory.generate( StringTypeMapper.class, StringMapping.values() );
		typeFactory.generate( DateTypeMapper.class, DateMapping.values() );
		typeFactory.generate( ByteArrayTypeMapper.class, ByteArrayMapping.values() );
	}

	public TypeMapperContext getContext()
	{
		return context;
	}
}
