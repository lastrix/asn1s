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

package org.asn1s.api.type;

import java.util.Collections;
import java.util.List;

public class RelationItem
{
	public RelationItem( String name, List<String> path, int level )
	{
		this.name = name;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.path = path;
		this.level = level;
	}

	private final String name;
	private final List<String> path;
	private final int level;

	public String getName()
	{
		return name;
	}

	public List<String> getPath()
	{
		return Collections.unmodifiableList( path );
	}

	public int getLevel()
	{
		return level;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( '@' );
		for( int i = 0; i < level; i++ )
			sb.append( '.' );
		path.forEach( e -> sb.append( e ).append( '.' ) );
		sb.append( name );
		return sb.toString();
	}
}
