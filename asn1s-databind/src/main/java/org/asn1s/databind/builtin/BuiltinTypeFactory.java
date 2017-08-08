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

package org.asn1s.databind.builtin;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.Ref;
import org.asn1s.api.UniversalType;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class BuiltinTypeFactory
{
	public BuiltinTypeFactory( TypeMapperContext context, Asn1Factory factory )
	{
		this.context = context;
		this.factory = factory;
		module = factory.types().getModule();
		assert module != null;
	}

	private final TypeMapperContext context;
	private final Asn1Factory factory;
	private final Module module;

	public void generate( Class<? extends TypeMapper> mapperClass, BuiltinMapping[] mappings )
	{
		for( BuiltinMapping mapping : mappings )
			generateMapping( mapperClass, mapping );
	}

	private void generateMapping( Class<? extends TypeMapper> mapperClass, BuiltinMapping mapping )
	{
		if( mapping.getAsnTypeName() != null )
		{
			NamedType asnType = module.getTypeResolver().getType( mapping.getAsnTypeName() );
			if( asnType == null )
				asnType = createAsnType( mapping );
			makeMapper( mapperClass, mapping.getJavaType(), asnType, true );
		}

		makeMapper( mapperClass, mapping.getJavaType(), getBuiltinType( mapping.getUniversalType() ), mapping.isRegisterAsDefault() );
	}

	@NotNull
	private NamedType createAsnType( BuiltinMapping mappings )
	{
		Ref<Type> typeRef = mappings.getUniversalType().ref();
		if( mappings.hasConstraint() )
			typeRef = createConstrainedType( factory, mappings, typeRef );

		assert mappings.getAsnTypeName() != null;
		return factory.types().define( mappings.getAsnTypeName(), typeRef, null );
	}

	@NotNull
	private static Ref<Type> createConstrainedType( Asn1Factory factory, BuiltinMapping mappings, Ref<Type> typeRef )
	{
		ConstraintTemplate template = factory.constraints().valueRange(
				mappings.getMinValue( factory.values() ),
				false,
				mappings.getMaxValue( factory.values() ),
				false
		);
		typeRef = factory.types().constrained( template, typeRef );
		return typeRef;
	}

	private NamedType getBuiltinType( UniversalType universalType )
	{
		NamedType type = module.getCoreModule().getTypeResolver().getType( universalType.typeName().getName() );
		assert type != null;
		return type;
	}

	private void makeMapper( Class<? extends TypeMapper> mapperClass, Class<?> javaType, NamedType asnType, boolean isDefault )
	{
		TypeMapper mapper = newMapperInstance( mapperClass, javaType, asnType );
		context.registerTypeMapper( mapper );
		if( isDefault )
			context.registerJavaClassForNamedType( mapper.getJavaType(), mapper.getAsn1Type() );
	}

	private static TypeMapper newMapperInstance( Class<? extends TypeMapper> mapperClass, Class<?> javaType, NamedType asnType )
	{
		try
		{
			//noinspection JavaReflectionMemberAccess
			Constructor<? extends TypeMapper> constructor = mapperClass.getDeclaredConstructor( Class.class, NamedType.class );
			return constructor.newInstance( javaType, asnType );
		} catch( NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e )
		{
			throw new IllegalStateException( "Unable to create instance of " + mapperClass.getTypeName(), e );
		}
	}
}
