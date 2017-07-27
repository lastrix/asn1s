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

import org.asn1s.api.Ref;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.CollectionOfType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

final class SequenceOfBerEncoder implements BerEncoder
{
	@Override
	public void encode( @NotNull WriterContext context ) throws IOException, Asn1Exception
	{
		assert context.getType().getFamily() == Family.SEQUENCE_OF;
		assert context.getValue().getKind() == Kind.COLLECTION || context.getValue().getKind() == Kind.NAMED_COLLECTION;

		if( !context.isWriteHeader() )
			writeCollection( context );
		else if( context.isBufferingAvailable() )
		{
			context.startBuffer( -1 );
			writeCollection( context );
			context.stopBuffer( SequenceBerEncoder.TAG );
		}
		else if( context.getRules() == BerRules.DER )
			throw new Asn1Exception( "Buffering is required for DER rules" );
		else
		{
			context.writeHeader( SequenceBerEncoder.TAG, -1 );
			writeCollection( context );
			context.write( 0 );
			context.write( 0 );
		}
	}

	static void writeCollection( WriterContext ctx ) throws IOException, Asn1Exception
	{
		ValueCollection collection = ctx.getValue().toValueCollection();
		CollectionOfType type = (CollectionOfType)ctx.getType();
		if( collection.isEmpty() )
			return;

		ComponentType componentType = type.getComponentType();
		ctx.getScope().setValueLevel( collection );
		for( Ref<Value> ref : collection.asValueList() )
		{
			if( !( ref instanceof Value ) )
				throw new IllegalValueException( "Unable to use references: " + ref );
			ctx.writeComponent( componentType, (Value)ref );
		}
	}
}
