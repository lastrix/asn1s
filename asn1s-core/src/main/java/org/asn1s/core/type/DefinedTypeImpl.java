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
import org.asn1s.api.constraint.ElementSetSpecs;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DefinedTypeImpl extends AbstractType implements DefinedType
{
	public DefinedTypeImpl( @NotNull Module module, @NotNull String name, @NotNull Ref<Type> reference )
	{
		RefUtils.assertTypeRef( name );
		this.module = module;
		this.name = name;
		this.reference = reference;

		if( reference instanceof Type )
			type = (Type)reference;
	}

	private Module module;
	private final String name;
	private Ref<Type> reference;
	private Type type;

	@NotNull
	@Override
	public Scope createScope()
	{
		return module.createScope().typedScope( this );
	}

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return createScope();
	}

	Module getModule()
	{
		return module;
	}

	@Override
	public final String getName()
	{
		return name;
	}

	final Ref<Type> getReference()
	{
		return reference;
	}

	@Nullable
	@Override
	public Type getSibling()
	{
		return type;
	}

	final Type getType()
	{
		return type;
	}

	final void setType( @Nullable Type type )
	{
		this.type = type;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		type.accept( getScope( scope ), valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = scope.typedScope( this );
		return type.optimize( scope, valueRef );
	}

	@Nullable
	@Override
	public final NamedType getNamedType( @NotNull String name )
	{
		if( type == null )
			return null;
		return type.getNamedType( name );
	}

	@Nullable
	@Override
	public final NamedValue getNamedValue( @NotNull String name )
	{
		return type.getNamedValue( name );
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return type.getNamedValues();
	}

	@NotNull
	@Override
	public List<? extends NamedType> getNamedTypes()
	{
		return type.getNamedTypes();
	}

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return type.getEncoding( instructions );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return type.getFamily();
	}

	@Override
	protected void onDispose()
	{
		reference = null;
		type = null;
		module = null;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );

		if( type == null )
			setType( reference.resolve( scope ) );
		type.validate( scope );
	}

	@NotNull
	@Override
	public Type copy()
	{
		Ref<Type> sibling = Objects.equals( reference, type ) ? type.copy() : reference;
		return new DefinedTypeImpl( module, getName(), sibling );
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		return type.isConstructedValue( scope, value );
	}

	@Override
	public Ref<Type> toRef()
	{
		return new TypeNameRef( getName(), module.getModuleName() );
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public ElementSetSpecs asElementSetSpecs()
	{
		return type.asElementSetSpecs();
	}

	@Override
	public boolean hasElementSetSpecs()
	{
		return type.hasElementSetSpecs();
	}
}
