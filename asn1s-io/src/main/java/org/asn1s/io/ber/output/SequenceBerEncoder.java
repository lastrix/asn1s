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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

final class SequenceBerEncoder implements BerEncoder
{
	private static final Log log = LogFactory.getLog( SequenceBerEncoder.class );
	public static final Tag TAG = new Tag( TagClass.Universal, true, UniversalType.Sequence.tagNumber() );
	private static final Tag TAG_INSTANCE_OF = new Tag( TagClass.Universal, true, UniversalType.InstanceOf.tagNumber() );

	@Override
	public void encode( @NotNull WriterContext context ) throws IOException, Asn1Exception
	{
		assert context.getType().getFamily() == Family.Sequence;
		assert context.getValue().getKind() == Kind.NamedCollection || context.getValue().getKind() == Kind.Collection && context.getValue().toValueCollection().isEmpty();

		Tag tag = ( (CollectionType)context.getType() ).isInstanceOf() ? TAG_INSTANCE_OF : TAG;

		if( !context.isWriteHeader() )
			writeSequence( context );
		else if( context.isBufferingAvailable() )
		{
			context.startBuffer( -1 );
			writeSequence( context );
			context.stopBuffer( tag );
		}
		else if( context.getRules() == BerRules.Der )
			throw new Asn1Exception( "Buffering is required for DER rules" );
		else
		{
			context.writeHeader( tag, -1 );
			writeSequence( context );
			context.write( 0 );
			context.write( 0 );
		}
	}

	private static void writeSequence( WriterContext ctx ) throws Asn1Exception, IOException
	{
		List<NamedValue> values = ctx.getValue().toValueCollection().asNamedValueList();
		writeCollectionValues( ctx, values );
	}

	static void writeCollectionValues( WriterContext ctx, Collection<NamedValue> values ) throws Asn1Exception, IOException
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

	private static void writeComponentValue( WriterContext ctx, CollectionType type, NamedValue value ) throws Asn1Exception, IOException
	{
		ComponentType component = type.getComponent( value.getName(), true );

		// do not write default values, it's just a waste of time and memory
		if( component != null && !RefUtils.isSameAsDefaultValue( ctx.getScope(), component, value ) )
			ctx.writeComponent( component, value );
	}
}
