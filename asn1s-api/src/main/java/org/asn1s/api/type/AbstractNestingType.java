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

import org.asn1s.api.Disposable;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.ElementSetSpecs;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class AbstractNestingType extends AbstractType
{
	protected AbstractNestingType( @Nullable Ref<Type> siblingRef )
	{
		this.siblingRef = siblingRef;
	}

	private Ref<Type> siblingRef;
	private Type sibling;

	public Ref<Type> getSiblingRef()
	{
		return siblingRef;
	}

	@NotNull
	@Override
	public Type getSibling()
	{
		return sibling;
	}

	protected void setSibling( Type sibling )
	{
		this.sibling = sibling;
	}

	@Override
	public boolean hasSibling()
	{
		return sibling != null;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		sibling.accept( getScope( scope ), valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		return sibling.optimize( getScope( scope ), valueRef );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		sibling = siblingRef.resolve( scope );
		if( !( sibling instanceof DefinedType ) )
			sibling.setNamespace( getSiblingNamespace() );
		sibling.validate( scope );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return sibling.getFamily();
	}

	@Nullable
	@Override
	public <T extends NamedType> T getNamedType( @NotNull String name )
	{
		return sibling.getNamedType( name );
	}

	@NotNull
	@Override
	public <T extends NamedType> List<T> getNamedTypes()
	{
		return sibling.getNamedTypes();
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		return sibling.isConstructedValue( scope, value );
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		return sibling.getNamedValue( name );
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return sibling.getNamedValues();
	}

	@Nullable
	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return sibling.getEncoding( instructions );
	}

	protected Ref<Type> cloneSibling()
	{
		return siblingRef instanceof Type ? ( (Type)siblingRef ).copy() : siblingRef;
	}

	@Override
	public ElementSetSpecs asElementSetSpecs()
	{
		return sibling.asElementSetSpecs();
	}

	@Override
	public boolean hasElementSetSpecs()
	{
		return sibling.hasElementSetSpecs();
	}

	@Override
	protected void onDispose()
	{
		if( siblingRef instanceof Disposable && !( siblingRef instanceof DefinedType ) )
			( (Disposable)siblingRef ).dispose();

		siblingRef = null;
		sibling = null;
	}

	protected String getSiblingNamespace()
	{
		return getNamespace();
	}
}
