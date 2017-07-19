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
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type.Family;
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
	public Value decode( @NotNull ReaderContext context ) throws IOException, Asn1Exception
	{
		assert context.getType().getFamily() == Family.Set;
		assert context.getTag().isConstructed();
		return readSet( context );
	}

	private static Value readSet( @NotNull ReaderContext ctx ) throws IOException, Asn1Exception
	{
		CollectionType type = (CollectionType)ctx.getType();
		int setLength = ctx.getLength();
		if( setLength == 0 )
			return ctx.getValueFactory().collection( true );

		Iterable<ComponentType> components = new LinkedList<>( type.getComponents( true ) );
		int start = ctx.position();
		ValueCollection collection = ctx.getValueFactory().collection( true );
		ctx.getScope().setValueLevel( collection );
		boolean indefinite = setLength == -1;
		while( indefinite || start + setLength > ctx.position() )
		{
			if( ctx.readTagInfoEocPossible( !indefinite ) )
				break;

			ComponentType component = chooseComponentByEncoding( components, ctx.getTag() );
			if( component == null )
			{
				log.warn( "Unable to find set component for tag: " + ctx.getTag() + ", skipping." );
				if( ctx.getLength() == -1 )
					ctx.skipToEoc();
				else
					ctx.skip( ctx.getLength() );
				continue;
			}
			collection.addNamed( component.getComponentName(), ctx.readComponentType( component, ctx.getTag(), ctx.getLength() ) );
		}

		ctx.ensureConstructedRead( start, setLength, ctx.getTag() );
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
