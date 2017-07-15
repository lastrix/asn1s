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

package org.asn1s.io.ber.input;

import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.CollectionOfType;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.CollectionType.Kind;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

final class SequenceOfBerDecoder implements BerDecoder
{
	@Override
	public Value decode( @NotNull BerReader is, @NotNull Scope scope, @NotNull Type type, @NotNull Tag tag, int length ) throws IOException, Asn1Exception
	{
		if( !( type instanceof CollectionOfType ) || ( (CollectionType)type ).getKind() != Kind.SequenceOf )
			throw new IllegalStateException( "Only SequenceOf type supported" );

		if( !tag.isConstructed() )
			throw new IOException( "Must be constructed" );

		return readComponents( is, scope, (CollectionOfType)type, length );
	}

	static Value readComponents( BerReader is, Scope scope, CollectionOfType type, int collectionLength ) throws IOException, Asn1Exception
	{
		if( collectionLength == 0 )
			return is.getValueFactory().collection( false );
		ComponentType componentType = type.getComponentType();
		if( componentType == null )
			throw new IllegalStateException();
		TagEncoding encoding = (TagEncoding)componentType.getEncoding( EncodingInstructions.Tag );

		int start = is.position();

		ValueCollection collection = is.getValueFactory().collection( false );
		scope.setValueLevel( collection );
		scope = componentType.getScope( scope );
		Tag tag = null;
		while( collectionLength == -1 || start + collectionLength > is.position() )
		{
			tag = is.readTag();
			int length = is.readLength();

			if( collectionLength == -1 && tag.isEoc() && length == 0 )
				break;

			if( tag.getTagClass() != encoding.getTagClass() || tag.getTagNumber() != encoding.getTagNumber() )
				throw new Asn1Exception( "Unexpected tag found" );

			Value componentValue = is.readInternal( scope, componentType, tag, length, false );
			collection.add( componentValue );
		}

		is.ensureConstructedRead( start, collectionLength, tag );
		return collection;
	}
}
