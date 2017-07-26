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

package org.asn1s.core.type;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractNestingType;
import org.asn1s.api.type.TaggedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * Defines type that has an encoding override
 */
public final class TaggedTypeImpl extends AbstractNestingType implements TaggedType
{
	public TaggedTypeImpl( @NotNull IEncoding encoding, @NotNull Ref<Type> reference )
	{
		super( reference );
		instructions = encoding.getEncodingInstructions();
		setEncoding( encoding );
	}

	private final EncodingInstructions instructions;
	private final Map<EncodingInstructions, IEncoding> encodingMap = new EnumMap<>( EncodingInstructions.class );

	@Override
	public EncodingInstructions getInstructions()
	{
		return instructions;
	}

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return encodingMap.get( instructions );
	}

	private void setEncoding( @NotNull IEncoding encoding )
	{
		encodingMap.put( encoding.getEncodingInstructions(), encoding );
	}


	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		IEncoding encoding = getEncoding( getInstructions() ).resolve( scope );
		setEncoding( encoding );
		if( !( encoding instanceof TagEncoding ) )
			throw new UnsupportedOperationException();

		super.onValidate( scope );

		TagEncoding enc = (TagEncoding)encoding;
		if( enc.getTagMethod() == TagMethod.IMPLICIT && getSibling().getFamily() == Family.CHOICE )
			setEncoding( TagEncoding.create( enc.getModuleTagMethod(), TagMethod.EXPLICIT, enc.getTagClass(), enc.getTagNumber() ) );
	}

	@Override
	public boolean equals( Object obj )
	{
		return obj == this || obj instanceof TaggedTypeImpl && toString().equals( obj.toString() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public String toString()
	{
		return getEncoding( getInstructions() ) + " " + getSiblingRef();
	}

	@NotNull
	@Override
	public Type copy()
	{
		IEncoding encoding = getEncoding( instructions );
		if( encoding == null )
			throw new IllegalStateException();

		return new TaggedTypeImpl( encoding, cloneSibling() );
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		Type thisType = getSibling();
		while( thisType != null )
		{
			if( thisType instanceof TaggedTypeImpl )
			{
				TagEncoding encoding = (TagEncoding)thisType.getEncoding( EncodingInstructions.TAG );
				if( encoding == null )
					throw new IllegalStateException();

				if( encoding.getTagMethod() == TagMethod.EXPLICIT )
					return true;
			}

			if( thisType.isConstructedValue( scope, value ) )
				return true;

			if( thisType.getSibling() == null )
				break;
			thisType = thisType.getSibling();
		}
		return false;
	}

	@Override
	protected void onDispose()
	{
		encodingMap.clear();
	}
}
