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

package org.asn1s.obsolete.databind.mapper;

import org.asn1s.api.type.DefinedType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

public class SequenceMappedType implements MappedType
{
	SequenceMappedType( Type javaType, Constructor<?> constructor, String[] constructorParameters )
	{
		this.javaType = javaType;
		this.constructor = constructor;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.constructorParameters = constructorParameters;
	}

	private final Type javaType;
	private DefinedType asnType;
	private final Constructor<?> constructor;
	private final String[] constructorParameters;
	private MappedField[] fields;

	public MappedField getFieldOrDie( String property )
	{
		for( MappedField field : fields )
			if( field.getPropertyName().equals( property ) )
				return field;

		throw new IllegalStateException( "Unable to find property: " + property );
	}

	@Override
	public Type getJavaType()
	{
		return javaType;
	}

	@Override
	public DefinedType getAsnType()
	{
		return asnType;
	}

	void setAsnType( DefinedType asnType )
	{
		this.asnType = asnType;
	}

	public Constructor<?> getConstructor()
	{
		return constructor;
	}

	public String[] getConstructorParameters()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return constructorParameters;
	}

	public MappedField[] getFields()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return fields;
	}

	void setFields( MappedField[] fields )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.fields = fields;
	}

	@Override
	public String toString()
	{
		return "BasicMappedType{" + javaType.getTypeName() + " => " + asnType.getName() + '}';
	}
}
