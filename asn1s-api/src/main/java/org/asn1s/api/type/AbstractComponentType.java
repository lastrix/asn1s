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
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class AbstractComponentType extends AbstractType implements ComponentType
{
	protected AbstractComponentType( int index, String name, @NotNull Ref<Type> componentTypeRef )
	{
		RefUtils.assertValueRef( name );
		this.index = index;
		this.name = name;
		this.componentTypeRef = componentTypeRef;
	}

	private final int index;
	private int version;
	private final String name;
	private Ref<Type> componentTypeRef;
	private Type componentType;

	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public void setVersion( int version )
	{
		this.version = version;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@NotNull
	@Override
	public Ref<Type> getComponentTypeRef()
	{
		return componentTypeRef;
	}

	@NotNull
	@Override
	public Type getComponentType()
	{
		return componentType;
	}

	protected Type getComponentTypeOrNull()
	{
		return componentType;
	}

	@Nullable
	@Override
	public NamedType getNamedType( @NotNull String name )
	{
		return getComponentType().getNamedType( name );
	}

	@NotNull
	@Override
	public List<? extends NamedType> getNamedTypes()
	{
		return getComponentType().getNamedTypes();
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		return getComponentType().getNamedValue( name );
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return getComponentType().getNamedValues();
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return getComponentType().getFamily();
	}

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return getComponentType().getEncoding( instructions );
	}

	@Nullable
	@Override
	public Type getSibling()
	{
		return componentType;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		if( componentType == null )
			componentType = componentTypeRef.resolve( scope );

		if( !( componentType instanceof DefinedType ) )
			componentType.setNamespace( getFullyQualifiedName() + '.' );
		componentType.validate( scope );
	}

	@Override
	protected void onDispose()
	{
		if( !( componentTypeRef instanceof DefinedType ) && componentTypeRef instanceof Type )
			( (Disposable)componentTypeRef ).dispose();
		componentTypeRef = null;

		if( componentType != null && !( componentType instanceof DefinedType ) )
			componentType.dispose();
		componentType = null;
	}
}
