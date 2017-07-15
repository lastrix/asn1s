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

package org.asn1s.api;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ModuleReference implements Comparable<ModuleReference>
{
	public ModuleReference( String name )
	{
		this( name, null, null );
	}

	public ModuleReference( @NotNull String name, @Nullable long[] oid, @Nullable String iri )
	{
		this.name = name;
		this.oid = oid != null ? Arrays.copyOf( oid, oid.length ) : null;
		oidString = oid != null ? StringUtils.join( oid, "-" ) : null;
		this.iri = iri;
	}

	private final String name;
	private final long[] oid;
	private final String oidString;
	private final String iri;

	/**
	 * Module name
	 *
	 * @return string
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns copy of OID value;
	 *
	 * @return oid array
	 * @see #getOidString()
	 */
	public long[] getOid()
	{
		return Arrays.copyOf( oid, oid.length );
	}

	/**
	 * Returns oid array as string
	 *
	 * @return string
	 * @see #getOid()
	 */
	public String getOidString()
	{
		return oidString;
	}

	/**
	 * Returns International Resource Identifier (IRI)
	 *
	 * @return iri
	 */
	public String getIri()
	{
		return iri;
	}

	@Override
	public int compareTo( @NotNull ModuleReference o )
	{
		return getName().compareTo( o.getName() );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof ModuleReference ) ) return false;

		ModuleReference reference = (ModuleReference)obj;
		return getName().equals( reference.getName() );
	}

	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}

	@Override
	public String toString()
	{
		if( oid == null && iri == null )
			return getName();

		StringBuilder sb = new StringBuilder();
		sb.append( getName() );
		if( oid != null )
			sb.append( " {" ).append( getOidString() ).append( '}' );

		if( iri != null )
			sb.append( ' ' ).append( getIri() );

		return sb.toString();
	}
}
