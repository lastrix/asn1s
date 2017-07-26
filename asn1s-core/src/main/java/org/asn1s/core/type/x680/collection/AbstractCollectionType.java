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

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.State;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.core.type.BuiltinType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractCollectionType extends BuiltinType implements CollectionType
{
	AbstractCollectionType( boolean automaticTags )
	{
		this.automaticTags = automaticTags;
	}

	private final boolean automaticTags;
	private final List<Type> components = new ArrayList<>();
	private final List<Type> componentsLast = new ArrayList<>();
	private final List<Type> extensions = new ArrayList<>();
	private boolean extensible;
	private int maxVersion = 1;
	private List<ComponentType> actualComponents;

	boolean isAutomaticTags()
	{
		return automaticTags;
	}

	@Override
	public final void setExtensible( boolean value )
	{
		extensible = value;
	}

	@Override
	public final boolean isExtensible()
	{
		return extensible;
	}

	int getMaxVersion()
	{
		return maxVersion;
	}

	void updateIndices()
	{
		if( actualComponents == null )
			throw new IllegalStateException();

		for( ComponentType type : actualComponents )
			if( type.getVersion() > 1 )
				maxVersion = Math.max( maxVersion, type.getVersion() );
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		return value.getKind() == Value.Kind.COLLECTION || value.getKind() == Value.Kind.NAMED_COLLECTION;
	}

	List<Type> getComponents()
	{
		return Collections.unmodifiableList( components );
	}

	List<Type> getComponentsLast()
	{
		return Collections.unmodifiableList( componentsLast );
	}

	List<Type> getExtensions()
	{
		return Collections.unmodifiableList( extensions );
	}

	@Override
	public ComponentType addComponent( @NotNull Kind kind, @NotNull String name, @NotNull Ref<Type> typeRef )
	{
		ComponentType component = new ComponentTypeImpl( components.size() + extensions.size() + componentsLast.size(), name, typeRef );
		addComponent( kind, component );
		return component;
	}

	@Override
	public void addComponentsFromType( Kind kind, @NotNull Ref<Type> typeRef )
	{
		Type component = new ComponentsFromType( typeRef, getFamily() );
		addComponent( kind, component );
	}

	private void addComponent( Kind kind, Type component )
	{
		switch( kind )
		{
			case PRIMARY:
				addComponent( component );
				break;

			case EXTENSION:
				addExtension( component );
				break;

			case SECONDARY:
				addComponentLast( component );
				break;

			default:
		}
	}

	@Override
	public void addExtensionGroup( @NotNull Type extensionGroup )
	{
		extensions.add( extensionGroup );
	}

	private void addComponent( Type component )
	{
		components.add( component );
	}

	private void addComponentLast( Type componentLast )
	{
		componentsLast.add( componentLast );
	}

	private void addExtension( Type extension )
	{
		extensions.add( extension );
	}

	@Nullable
	@Override
	public ComponentType getComponent( @NotNull String name, boolean withExtensions )
	{
		for( ComponentType component : actualComponents )
			if( component.getComponentName().equals( name ) && ( withExtensions || component.getVersion() == 1 ) )
				return component;

		return null;
	}

	@Override
	public List<ComponentType> getComponents( boolean withExtensions )
	{
		if( withExtensions )
			return Collections.unmodifiableList( actualComponents );

		List<ComponentType> result = new ArrayList<>();
		for( ComponentType component : actualComponents )
			if( component.getVersion() == 1 )
				result.add( component );

		return result;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List<Type> getRawComponents()
	{
		List<Type> list = new ArrayList<>( components.size() + componentsLast.size() );
		list.addAll( components );
		list.addAll( componentsLast );
		return list;
	}

	void setActualComponents( List<ComponentType> actualComponents )
	{
		this.actualComponents = new ArrayList<>( actualComponents );
	}

	@Override
	public boolean isAllComponentsOptional()
	{
		if( getState() != State.DONE )
			throw new IllegalStateException();

		for( ComponentType component : actualComponents )
			if( component.isRequired() && component.getVersion() == 1 )
				return false;

		return true;
	}

	@NotNull
	@Override
	public final Type copy()
	{
		AbstractCollectionType type = onCopy();
		type.setExtensible( extensible );

		for( Type component : components )
			type.addComponent( component.copy() );

		for( Type component : componentsLast )
			type.addComponentLast( component.copy() );

		for( Type component : extensions )
			type.addExtension( component.copy() );

		return type;
	}

	@Override
	protected void onDispose()
	{
		for( Type component : components )
			component.dispose();

		components.clear();

		for( Type component : componentsLast )
			component.dispose();

		componentsLast.clear();

		for( Type extension : extensions )
			extension.dispose();

		extensions.clear();
		if( actualComponents != null )
		{
			for( ComponentType component : actualComponents )
				component.dispose();

			actualComponents.clear();
			actualComponents = null;
		}
	}

	protected abstract AbstractCollectionType onCopy();
}
