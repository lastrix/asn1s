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

import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

final class SequenceBerEncoder implements BerEncoder
{
	public static final Tag TAG = new Tag( TagClass.Universal, true, UniversalType.Sequence.tagNumber() );
	private static final Tag TAG_INSTANCE_OF = new Tag( TagClass.Universal, true, UniversalType.InstanceOf.tagNumber() );

	@Override
	public void encode( @NotNull BerWriter os, @NotNull Scope scope, @NotNull Type type, @NotNull Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		if( !( type instanceof CollectionType ) || ( (CollectionType)type ).getKind() != CollectionType.Kind.Sequence )
			throw new IOException( "Only SequenceType is allowed." );

		if( value.getKind() == Kind.NamedCollection )
		{
			Tag tag = ( (CollectionType)type ).isInstanceOf() ? TAG_INSTANCE_OF : TAG;

			if( writeHeader )
			{
				if( os.isBufferingAvailable() )
				{
					os.startBuffer( -1 );
					writeSequence( scope, os, (CollectionType)type, value.toValueCollection().asNamedValueList() );
					os.stopBuffer( tag );
				}
				else if( os.getRules() == BerRules.Der )
					throw new IOException( "Buffering is required for DER rules" );
				else
				{
					os.writeHeader( tag, -1 );
					writeSequence( scope, os, (CollectionType)type, value.toValueCollection().asNamedValueList() );
					os.write( 0 );
					os.write( 0 );
				}
			}
			else
				writeSequence( scope, os, (CollectionType)type, value.toValueCollection().asNamedValueList() );
		}
		else
			throw new IllegalValueException( "Unable to write value of kind: " + value.getKind() );
	}

	private static void writeSequence( Scope scope, BerWriter os, CollectionType type, Collection<NamedValue> values ) throws Asn1Exception, IOException
	{
		if( values.isEmpty() )
		{
			if( type.isAllComponentsOptional() )
				return;
			throw new IllegalValueException( "Type does not accepts empty collections" );
		}

		int previousComponentIndex = -1;
		int version = 1;
		for( NamedValue value : values )
		{
			ComponentType component = type.getComponent( value.getName(), true );

			if( component == null )
			{
				if( type.isExtensible() && previousComponentIndex >= type.getExtensionIndexStart() && previousComponentIndex <= type.getExtensionIndexEnd() )
					continue;

				throw new IllegalValueException( "Type does not have component with name: " + value.getName() );
			}

			if( component.getIndex() <= previousComponentIndex )
				throw new IllegalValueException( "ComponentType order is illegal for: " + value );

			version = Math.max( version, component.getVersion() );
			type.assertComponentsOptionalityInRange( previousComponentIndex, component.getIndex(), version );

			previousComponentIndex = component.getIndex();

			// do not write default values, it's just a waste of time and memory
			if( RefUtils.isSameAsDefaultValue( scope, component, value ) )
				continue;

			os.writeInternal( component.getScope( scope ), component, value, true );
		}

		type.assertComponentsOptionalityInRange( previousComponentIndex, -1, version );
	}
}
