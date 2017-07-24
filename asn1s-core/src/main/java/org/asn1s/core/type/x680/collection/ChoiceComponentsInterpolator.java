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
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.ComponentType;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

final class ChoiceComponentsInterpolator extends AbstractComponentInterpolator
{
	ChoiceComponentsInterpolator( Scope scope, AbstractCollectionType type )
	{
		super( scope, type );
	}

	@Override
	List<ComponentType> interpolate() throws ValidationException, ResolutionException
	{
		if( !getType().getComponentsLast().isEmpty() )
			throw new ValidationException( "Choice type may not have any Secondary components. Allowed: Primary, Extension" );

		return super.interpolate();
	}

	@Override
	protected void assertTagAmbiguity( Collection<ComponentType> components ) throws ValidationException
	{
		Iterable<ComponentType> list = new LinkedList<>( components );
		Iterator<ComponentType> iterator = list.iterator();
		while( iterator.hasNext() )
		{
			ComponentType component = iterator.next();
			iterator.remove();
			CoreCollectionUtils.assertTags( component, list );
		}
	}
}
