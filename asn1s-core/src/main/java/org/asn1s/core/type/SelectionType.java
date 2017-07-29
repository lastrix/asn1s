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

package org.asn1s.core.type;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

/**
 * X.680, p 30.1
 *
 * @author lastrix
 * @version 1.0
 */
public final class SelectionType extends AbstractType
{
	public SelectionType( @NotNull String name, Ref<Type> ref )
	{
		RefUtils.assertValueRef( name );
		this.name = name;
		this.ref = ref;
	}

	private final String name;
	private Ref<Type> ref;
	private Type type;

	@NotNull
	@Override
	public Type getSibling()
	{
		return type;
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return type.getFamily();
	}

	@Override
	public boolean equals( Object obj )
	{
		return this == obj || obj instanceof SelectionType && toString().equals( obj.toString() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public String toString()
	{
		return name + " < " + ref;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		Type resolved = ref.resolve( scope );
		resolved.validate( scope );

		if( resolved.getFamily() != Family.CHOICE )
			throw new ResolutionException( "Unable to find choice type, found: " + resolved );

		type = resolved.getNamedType( name );
		if( type == null )
			throw new ValidationException( "Type " + ref + " does not have component: " + name );
	}

	@NotNull
	@Override
	public Type copy()
	{
		return new SelectionType( name, ref );
	}

	@Override
	protected void onDispose()
	{
		ref = null;
		type = null;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		type.accept( scope, valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		return type.optimize( scope, valueRef );
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		return type.isConstructedValue( scope, value );
	}

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return type.getEncoding( instructions );
	}
}
