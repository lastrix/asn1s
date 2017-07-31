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

package org.asn1s.api.type.x681;

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassFieldFromSourceRef implements Ref<Type>
{
	public ClassFieldFromSourceRef( @NotNull Ref<?> source, @Nullable String path, @NotNull String name )
	{
		if( StringUtils.isBlank( name ) )
			throw new IllegalArgumentException( "name" );
		this.source = source;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.path = path;
		this.name = name;
	}

	private final Ref<?> source;
	private final String path;
	private final String name;

	@Override
	public Type resolve( Scope scope ) throws ResolutionException
	{
		if( !StringUtils.isBlank( path ) )
			throw new ResolutionException( "Paths are not supported" );

		Object resolve = source.resolve( scope );
		if( resolve instanceof Type )
		{
			ClassType classType = resolveClassType( scope, (Type)resolve );
			ClassFieldType<Type> field = classType.getField( name );
			if( field == null )
				throw new ResolutionException( "Unable to find field '" + name + "' in class: " + resolve );

			return field;
		}

		throw new UnsupportedOperationException();
	}

	private static ClassType resolveClassType( Scope scope, Type type ) throws ResolutionException
	{
		RefUtils.resolutionValidate( scope, type );
		while( type instanceof DefinedType )
			type = type.getSibling();

		assert type instanceof ClassType;
		return (ClassType)type;
	}

	@Override
	public String toString()
	{
		return source + "." + name;
	}
}
