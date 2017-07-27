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

package org.asn1s.io.ber.output;

import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

abstract class AbstractCollectionBerEncoder implements BerEncoder
{
	@Override
	public final void encode( @NotNull WriterContext context ) throws IOException, Asn1Exception
	{
		assert context.getType().getFamily() == getRequiredFamily();
		assert context.getValue().getKind() == Kind.NAMED_COLLECTION || context.getValue().getKind() == Kind.COLLECTION && context.getValue().toValueCollection().isEmpty();

		if( context.isWriteHeader() )
		{
			if( context.getRules() == BerRules.DER && !context.isBufferingAvailable() )
				throw new Asn1Exception( "Buffering is required for DER rules" );
			encodeWithHeader( context );
		}
		else
			writeCollectionValues( context, getValues( context ) );
	}

	private void encodeWithHeader( WriterContext context ) throws Asn1Exception, IOException
	{
		Tag tag = getTag( context.getType() );
		if( context.isBufferingAvailable() )
		{
			context.startBuffer( -1 );
			writeCollectionValues( context, getValues( context ) );
			context.stopBuffer( tag );
		}
		else
		{
			context.writeHeader( tag, -1 );
			writeCollectionValues( context, getValues( context ) );
			context.write( 0 );
			context.write( 0 );
		}
	}

	private static void writeCollectionValues( WriterContext ctx, Collection<NamedValue> values ) throws Asn1Exception, IOException
	{
		CollectionType type = (CollectionType)ctx.getType();
		if( values.isEmpty() )
		{
			if( type.isAllComponentsOptional() )
				return;
			throw new IllegalValueException( "Type does not accepts empty collections" );
		}

		for( NamedValue value : values )
			writeComponentValue( ctx, type, value );
	}

	private static void writeComponentValue( WriterContext ctx, Type type, NamedValue value ) throws Asn1Exception, IOException
	{
		ComponentType component = type.getNamedType( value.getName() );

		// do not write default values, it's just a waste of time and memory
		if( component != null && !RefUtils.isSameAsDefaultValue( ctx.getScope(), component, value ) )
			ctx.writeComponent( component, value );
	}


	@NotNull
	protected abstract Tag getTag( @NotNull Type type );

	@NotNull
	protected abstract Collection<NamedValue> getValues( @NotNull WriterContext context );

	@NotNull
	protected abstract Family getRequiredFamily();
}
