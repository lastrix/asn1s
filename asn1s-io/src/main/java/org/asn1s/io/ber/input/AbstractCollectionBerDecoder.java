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
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedList;

abstract class AbstractCollectionBerDecoder implements BerDecoder
{
	private static final Log log = LogFactory.getLog( AbstractCollectionBerDecoder.class );

	@Override
	public final Value decode( @NotNull ReaderContext context ) throws IOException, Asn1Exception
	{
		assert context.getType().getFamily() == getRequiredFamily();
		assert context.getTag().isConstructed();

		if( context.getLength() == 0 )
			return context.getValueFactory().collection( true );

		return new ComponentDecoder( context ).decode();
	}

	@NotNull
	protected abstract Family getRequiredFamily();

	@Nullable
	protected abstract ComponentType chooseComponent( @NotNull Iterable<ComponentType> components, @NotNull Tag tag, int lastIndex );

	private class ComponentDecoder
	{
		private final ReaderContext ctx;
		private final Iterable<ComponentType> components;
		private final ValueCollection collection;
		private int lastIndex = -1;
		private final boolean indefinite;
		private final int ctxLength;
		private final int start;

		private ComponentDecoder( ReaderContext ctx )
		{
			this.ctx = ctx;
			CollectionType type = (CollectionType)ctx.getType();
			components = new LinkedList<>( type.getComponents( true ) );
			start = ctx.position();
			collection = ctx.getValueFactory().collection( true );
			ctx.getScope().setValueLevel( collection );
			ctxLength = ctx.getLength();
			indefinite = ctxLength == -1;
		}

		public ValueCollection decode() throws IOException, Asn1Exception
		{
			while( indefinite || start + ctxLength > ctx.position() )
				if( decodeNextComponent() )
					break;

			ctx.ensureConstructedRead( start, ctxLength, ctx.getTag() );
			return collection;
		}

		private boolean decodeNextComponent() throws IOException, Asn1Exception
		{
			if( ctx.readTagInfoEocPossible( !indefinite ) )
				return true;

			ComponentType component = chooseComponent( components, ctx.getTag(), lastIndex );
			if( component == null )
				onUnknownComponent( ctx );
			else
			{
				collection.addNamed( component.getComponentName(), ctx.readComponentType( component, ctx.getTag(), ctx.getLength() ) );
				lastIndex = component.getIndex();
			}
			return false;
		}

		private void onUnknownComponent( ReaderContext ctx ) throws IOException
		{
			log.warn( "Unable to find sequence component for tag: " + ctx.getTag() + ", skipping." );
			if( ctx.getLength() == -1 )
				ctx.skipToEoc();
			else
				ctx.skip( ctx.getLength() );
		}
	}
}
