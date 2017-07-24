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

import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class SetBerEncoder extends AbstractCollectionBerEncoder
{
	static final Tag TAG = new Tag( TagClass.Universal, true, UniversalType.Set.tagNumber() );

	@NotNull
	@Override
	protected Tag getTag( @NotNull Type type )
	{
		return TAG;
	}

	@NotNull
	@Override
	protected Collection<NamedValue> getValues( @NotNull WriterContext context )
	{
		List<NamedValue> values = context.getValue().toValueCollection().asNamedValueList();
		CollectionType type = (CollectionType)context.getType();
		return context.getRules() == BerRules.Der ? sortByTag( type, values ) : values;
	}

	@NotNull
	@Override
	protected Family getRequiredFamily()
	{
		return Family.Set;
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
