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

package org.asn1s.obsolete.databind;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.DefinedType;
import org.asn1s.obsolete.databind.mapper.DefaultTypeMapper;
import org.asn1s.obsolete.databind.mapper.MappedType;
import org.asn1s.obsolete.databind.mapper.MapperUtils;
import org.asn1s.obsolete.databind.mapper.TypeMapper;
import org.asn1s.obsolete.databind.marshaller.Marshaller;
import org.asn1s.obsolete.databind.marshaller.MarshallerImpl;
import org.asn1s.obsolete.databind.unmarshaller.Unmarshaller;
import org.asn1s.obsolete.databind.unmarshaller.UnmarshallerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Asn1Context
{
	public Asn1Context( @NotNull Module contextModule, @NotNull Asn1Factory asn1Factory )
	{
		this( contextModule, asn1Factory, null );
	}

	public Asn1Context( @NotNull Module contextModule, @NotNull Asn1Factory asn1Factory, @Nullable TypeMapper mapper )
	{
		this.contextModule = contextModule;
		this.asn1Factory = asn1Factory;
		asn1Factory.types().setModule( contextModule );
		this.mapper = mapper == null ? new DefaultTypeMapper( this ) : mapper;
	}

	private final Module contextModule;
	private final Asn1Factory asn1Factory;
	private final TypeMapper mapper;

	/**
	 * Map for ASN.1 type definitions, that may be referenced by MappedType
	 */
	private final Map<String, DefinedType> java2DefinedTypeMap = new HashMap<>();

	/**
	 * Map for mapped types, which contains all required metadata for I/O
	 */
	private final Map<String, MappedType> mappedTypeMap = new HashMap<>();

	private final Map<String, Object> globalParameters = new HashMap<>();

	public Module getContextModule()
	{
		return contextModule;
	}

	@Nullable
	public MappedType getMappedTypeByClass( Class<?> aClass )
	{
		DefinedType definedType = java2DefinedTypeMap.get( aClass.getCanonicalName() );
		return getMappedType( aClass.getCanonicalName(), definedType == null ? MapperUtils.getAsn1TypeNameForClass( aClass ) : definedType.getName() );
	}

	public Asn1Factory getAsn1Factory()
	{
		return asn1Factory;
	}

	public DefinedType getDefinedTypeByClassName( String className )
	{
		return java2DefinedTypeMap.get( className );
	}

	public void putDefinedTypeForClassName( String className, DefinedType type )
	{
		java2DefinedTypeMap.put( className, type );
	}

	public MappedType getMappedType( String className, String asn1TypeName )
	{
		String key = className + "=>" + asn1TypeName;
		return mappedTypeMap.get( key );
	}

	public void putMappedType( String className, String asn1TypeName, MappedType mappedType )
	{
		String key = className + "=>" + asn1TypeName;
		mappedTypeMap.put( key, mappedType );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getGlobalParameter( String key )
	{
		return (T)globalParameters.get( key );
	}

	public void putGlobalParameter( String key, Object parameter )
	{
		globalParameters.put( key, parameter );
	}

	public MappedType mapType( Type type )
	{
		return mapper.mapType( type );
	}

	public Marshaller createMarshaller()
	{
		return new MarshallerImpl( this );
	}

	public Unmarshaller createUnmarshaller()
	{
		return new UnmarshallerImpl( this );
	}
}
