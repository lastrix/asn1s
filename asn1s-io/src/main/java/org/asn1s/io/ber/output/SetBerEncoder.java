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
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

final class SetBerEncoder implements BerEncoder
{
	static final Tag TAG = new Tag( TagClass.Universal, true, UniversalType.Set.tagNumber() );

	@Override
	public void encode( @NotNull BerWriter os, @NotNull Scope scope, @NotNull Type type, @NotNull Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		assert type.getFamily() == Family.Set;
		assert value.getKind() == Kind.NamedCollection || value.getKind() == Kind.Collection && value.toValueCollection().isEmpty();

		List<NamedValue> values = value.toValueCollection().asNamedValueList();
		if( !writeHeader )
			writeSet( scope, os, (CollectionType)type, values );
		else if( os.isBufferingAvailable() )
		{
			os.startBuffer( -1 );
			writeSet( scope, os, (CollectionType)type, values );
			os.stopBuffer( TAG );
		}
		else if( os.getRules() == BerRules.Der )
			throw new Asn1Exception( "Buffering is required for DER rules" );
		else
		{
			os.writeHeader( TAG, -1 );
			writeSet( scope, os, (CollectionType)type, values );
			os.write( 0 );
			os.write( 0 );
		}
	}

	private static void writeSet( Scope scope, BerWriter os, CollectionType type, Collection<NamedValue> values ) throws IOException, Asn1Exception
	{
		if( values.isEmpty() )
		{
			if( type.isAllComponentsOptional() )
				return;
			throw new IllegalValueException( "Required components not found" );
		}
		Collection<ComponentType> unusedComponents = new HashSet<>( type.getComponents( true ) );

		if( os.getRules() == BerRules.Der )
			values = sortByTag( type, values );

		int version = 1;
		Collection<String> extensibleRequired = new HashSet<>();
		for( NamedValue value : values )
		{
			ComponentType component = type.getComponent( value.getName(), true );
			if( component == null )
			{
				if( !type.isExtensible() )
					throw new IllegalValueException( "Type has no components with name: " + value.getName() );

				extensibleRequired.add( value.getName() );
				continue;
			}

			version = Math.max( version, component.getVersion() );

			if( !unusedComponents.remove( component ) )
				throw new IllegalValueException( "Component occurs more than once: " + component.getName() );

			if( RefUtils.isSameAsDefaultValue( scope, component, value ) )
				continue;

			os.writeInternal( scope, component, value, true );
		}

		for( ComponentType component : unusedComponents )
			if( component.isRequired() && component.getVersion() <= version )
				throw new IllegalValueException( "Required component is not used: " + component.getName() );

		if( !extensibleRequired.isEmpty() && version < type.getMaxVersion() )
			throw new IllegalValueException( "Unable to accept components: " + extensibleRequired );
	}

	private static Collection<NamedValue> sortByTag( CollectionType type, Collection<NamedValue> values )
	{
		List<NamedValue> result = new ArrayList<>( values );
		Map<String, TagEncoding> encodingMap = new HashMap<>();
		for( NamedValue value : values )
			encodingMap.put( value.getName(), getTagEncoding( type, value.getName() ) );
		result.sort( ( o1, o2 ) -> compareByTag( encodingMap.get( o1.getName() ), encodingMap.get( o2.getName() ) ) );
		return result;
	}

	private static int compareByTag( TagEncoding t1, TagEncoding t2 )
	{
		int res = t1.getTagClass().compareTo( t2.getTagClass() );
		if( res != 0 )
			return res;
		return Integer.compare( t1.getTagNumber(), t2.getTagNumber() );
	}

	private static TagEncoding getTagEncoding( CollectionType type, String name )
	{
		ComponentType component = type.getComponent( name, true );
		if( component == null )
			throw new IllegalStateException();
		IEncoding encoding = component.getEncoding( EncodingInstructions.Tag );
		if( encoding == null )
			throw new IllegalStateException();
		return (TagEncoding)encoding;
	}
}
