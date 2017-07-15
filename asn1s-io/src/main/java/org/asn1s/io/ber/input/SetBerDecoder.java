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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.CollectionType.Kind;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class SetBerDecoder implements BerDecoder
{
	private static final Log log = LogFactory.getLog( SetBerDecoder.class );

	@Override
	public Value decode( @NotNull BerReader is, @NotNull Scope scope, @NotNull Type type, @NotNull Tag tag, int length ) throws IOException, Asn1Exception
	{
		if( !( type instanceof CollectionType ) || ( (CollectionType)type ).getKind() != Kind.Set )
			throw new IOException();

		if( !tag.isConstructed() )
			throw new IOException( "Must be constructed" );

		return readSet( is, scope, (CollectionType)type, length );
	}

	private static Value readSet( BerReader is, Scope scope, CollectionType type, int setLength ) throws IOException, Asn1Exception
	{
		if( setLength == 0 )
			return is.getValueFactory().collection( true );

		Iterable<ComponentType> components = new LinkedList<>( type.getComponents( true ) );
		int start = is.position();
		ValueCollection collection = is.getValueFactory().collection( true );
		scope.setValueLevel( collection );
		Tag tag = null;
		while( setLength == -1 || start + setLength > is.position() )
		{
			tag = is.readTag();
			int length = is.readLength();

			if( setLength == -1 && tag.isEoc() && length == 0 )
				break;

			ComponentType component = chooseComponentByEncoding( components, tag );
			if( component == null )
			{
				log.warn( "Unable to find set component for tag: " + tag + ", skipping." );
				if( length == -1 )
					is.skipToEoc();
				else
					is.skip( length );
				continue;
			}
			collection.addNamed( component.getComponentName(), is.readInternal( component.getScope( scope ), component, tag, length, false ) );
		}

		is.ensureConstructedRead( start, setLength, tag );
		return collection;
	}

	@Nullable
	private static ComponentType chooseComponentByEncoding( Iterable<ComponentType> components, Tag tag )
	{
		Iterator<ComponentType> iterator = components.iterator();
		while( iterator.hasNext() )
		{
			ComponentType component = iterator.next();
			TagEncoding encoding = (TagEncoding)component.getEncoding( EncodingInstructions.Tag );
			if( tag.getTagClass() == encoding.getTagClass() && tag.getTagNumber() == encoding.getTagNumber() )
			{
				iterator.remove();
				return component;
			}
		}
		return null;
	}
}
