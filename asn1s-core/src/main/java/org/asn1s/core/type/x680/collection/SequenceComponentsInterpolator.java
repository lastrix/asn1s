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

package org.asn1s.core.type.x680.collection;

import org.asn1s.api.Scope;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.ComponentType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

final class SequenceComponentsInterpolator extends AbstractComponentInterpolator
{
	SequenceComponentsInterpolator( @NotNull Scope scope, @NotNull AbstractCollectionType type )
	{
		super( scope, type );
	}

	@Override
	protected void assertTagAmbiguity( Collection<ComponentType> components ) throws ValidationException
	{
		Collection<ComponentType> optionalBlock = new LinkedList<>();
		assertComponentsTagAmbiguity( components, optionalBlock );

		if( !getType().getComponentsLast().isEmpty() )
			assertComponentsLastTagAmbiguity( components );
	}

	private static void assertComponentsLastTagAmbiguity( Iterable<ComponentType> components ) throws ValidationException
	{
		Collection<ComponentType> lastComponents = getLastComponents( components );
		for( ComponentType component : components )
			if( component.getVersion() > 1 )
				CoreCollectionUtils.assertTags( component, lastComponents );
	}

	@NotNull
	private static Collection<ComponentType> getLastComponents( Iterable<ComponentType> components )
	{
		Collection<ComponentType> lastComponents = new ArrayList<>();
		int version = 1;
		for( ComponentType component : components )
		{
			if( component.getVersion() == 1 && version > 1 )
			{
				lastComponents.add( component );
				if( component.isRequired() )
					break;
			}
			else
				version = component.getVersion();
		}
		return lastComponents;
	}

	private static void assertComponentsTagAmbiguity( Iterable<ComponentType> components, Collection<ComponentType> optionalBlock ) throws ValidationException
	{
		for( ComponentType component : components )
		{
			if( !component.isRequired() )
			{
				optionalBlock.add( component );
				continue;
			}

			if( !optionalBlock.isEmpty() )
			{
				optionalBlock.add( component );
				validateBlock( optionalBlock );
				optionalBlock.clear();
			}
		}
	}

	private static void validateBlock( Collection<ComponentType> list ) throws ValidationException
	{
		Iterator<ComponentType> iterator = list.iterator();
		while( iterator.hasNext() )
		{
			ComponentType component = iterator.next();
			if( list.isEmpty() )
				break;
			iterator.remove();
			CoreCollectionUtils.assertTags( component, list );
		}
	}
}
