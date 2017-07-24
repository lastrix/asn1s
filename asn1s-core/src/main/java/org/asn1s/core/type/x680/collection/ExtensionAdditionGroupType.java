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

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractType;
import org.asn1s.api.type.CollectionTypeExtensionGroup;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ExtensionAdditionGroupType extends AbstractType implements CollectionTypeExtensionGroup
{
	public ExtensionAdditionGroupType( Family family )
	{
		this.family = family;
	}

	private final Family family;
	private int version = -1;
	private final Collection<Type> components = new ArrayList<>();
	private List<ComponentType> actual;

	@Override
	public ComponentType addComponent( @NotNull Kind kind, @NotNull String name, @NotNull Ref<Type> typeRef )
	{
		if( kind != Kind.Extension )
			throw new IllegalArgumentException();

		ComponentType componentType = new ComponentTypeImpl( 0, name, typeRef );
		components.add( componentType );
		return componentType;
	}

	@Override
	public void addComponentsFromType( Kind kind, @NotNull Ref<Type> typeRef )
	{
		if( kind != Kind.Extension )
			throw new IllegalArgumentException();
		components.add( new ComponentsFromType( typeRef, family ) );
	}

	@Override
	public void addExtensionGroup( @NotNull Type extensionGroup )
	{
		throw new UnsupportedOperationException( "Unable to add extension group to other extension group" );
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	@Override
	public void setVersion( int version )
	{
		if( version < 2 )
			throw new IllegalArgumentException( "Version must be >= 2" );
		this.version = version;
	}

	@Override
	public List<ComponentType> getComponents()
	{
		return Collections.unmodifiableList( actual );
	}

	@Override
	public boolean equals( Object obj )
	{
		return obj == this || obj instanceof ExtensionAdditionGroupType && toString().equals( toString() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public String toString()
	{
		if( version != -1 )
			return "[[" + version + ": " + StringUtils.join( components, ", " ) + "]]";
		return "[[" + StringUtils.join( components, ", " ) + "]]";
	}

	@NotNull
	@Override
	public Type copy()
	{
		ExtensionAdditionGroupType type = new ExtensionAdditionGroupType( family );
		if( version != -1 )
			type.setVersion( version );

		for( Type component : components )
			type.addComponent( component.copy() );
		return type;
	}

	private void addComponent( Type type )
	{
		components.add( type );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		for( Type component : components )
		{
			component.setNamespace( getNamespace() );
			component.validate( scope );
		}

		actual = new ArrayList<>();
		for( Type component : components )
		{
			if( component instanceof ComponentType )
				actual.add( (ComponentType)component );
			else if( component instanceof ComponentsFromType )
				actual.addAll( ( (ComponentsFromType)component ).getComponents() );
			else
				throw new ValidationException( "Unable to process type class: " + component.getClass().getName() );
		}
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return family;
	}

	@Override
	protected void onDispose()
	{
		components.clear();
		if( actual != null )
			actual.clear();
	}
}
