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
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
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
	AbstractCollectionType( @NotNull Kind kind, boolean automaticTags )
	{
		this.kind = kind;
		this.automaticTags = automaticTags;
	}

	@NotNull
	private final Kind kind;
	private final boolean automaticTags;
	private final List<Type> components = new ArrayList<>();
	private final List<Type> componentsLast = new ArrayList<>();
	private final List<Type> extensions = new ArrayList<>();
	private boolean extensible;
	private int extensionIndexStart = Integer.MAX_VALUE;
	private int extensionIndexEnd = Integer.MIN_VALUE;
	private int maxVersion = 1;

	private List<ComponentType> actualComponents;

	@Override
	@NotNull
	public Kind getKind()
	{
		return kind;
	}

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

	@Override
	public int getExtensionIndexStart()
	{
		return extensionIndexStart;
	}

	@Override
	public int getExtensionIndexEnd()
	{
		return extensionIndexEnd;
	}

	@Override
	public int getMaxVersion()
	{
		return maxVersion;
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		return true;
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
	public void addComponent( @NotNull ComponentType.Kind kind, @NotNull String name, @NotNull Ref<Type> typeRef, boolean optional, @Nullable Ref<Value> defaultValue )
	{
		Type component = new ComponentTypeImpl( components.size() + extensions.size() + componentsLast.size(), 0, name, typeRef, optional, defaultValue );
		addComponent( kind, component );
	}

	@Override
	public void addComponentsFromType( ComponentType.Kind kind, @NotNull Ref<Type> typeRef )
	{
		Type component = new ComponentsFromType( typeRef, getFamily() );
		addComponent( kind, component );
	}

	private void addComponent( ComponentType.Kind kind, Type component )
	{
		switch( kind )
		{
			case Primary:
				addComponent( component );
				break;

			case Extension:
				addExtension( component );
				break;

			case Secondary:
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

	@Override
	public boolean isAllComponentsOptional()
	{
		if( getState() != State.Done )
			throw new IllegalStateException();

		for( ComponentType component : actualComponents )
			if( component.isRequired() && component.getVersion() == 1 )
				return false;

		return true;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		for( Type component : components )
			component.validate( scope );

		for( Type component : componentsLast )
			component.validate( scope );

		for( Type component : extensions )
			component.validate( scope );

		interpolateComponents( scope );

		for( ComponentType type : actualComponents )
			type.validate( scope );
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

	private void interpolateComponents( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		switch( kind )
		{
			case Sequence:
				actualComponents = new SequenceComponentsInterpolator( scope, this ).interpolate();
				break;

			case Choice:
				actualComponents = new ChoiceComponentsInterpolator( scope, this ).interpolate();
				break;

			case Set:
				actualComponents = new SetComponentsInterpolator( scope, this ).interpolate();
				break;

			case SequenceOf:
			case SetOf:
				ComponentType componentType = interpolateSingleComponent();
				componentType.validate( scope );
				actualComponents = Collections.singletonList( componentType );
				break;

			default:
				throw new IllegalStateException();
		}

		for( ComponentType type : actualComponents )
		{
			if( type.getVersion() > 1 )
			{
				extensionIndexStart = Math.min( extensionIndexStart, type.getIndex() );
				extensionIndexEnd = Math.max( extensionIndexEnd, type.getIndex() );
				maxVersion = Math.max( maxVersion, type.getVersion() );
			}
		}
	}

	@NotNull
	private ComponentType interpolateSingleComponent() throws ValidationException
	{
		if( components.size() != 1 || !componentsLast.isEmpty() || !extensions.isEmpty() )
			throw new ValidationException( "SequenceOf and SetOf must have only single componentType" );

		Type type = components.get( 0 );
		if( !( type instanceof ComponentTypeImpl ) )
			throw new ValidationException( "SequenceOf and SetOf requires ComponentType" );
		ComponentType componentType = (ComponentType)type;
		return new ComponentTypeImpl(
				0,
				1,
				componentType.getName(),
				componentType.getComponentType(),
				componentType.isOptional(),
				componentType.getDefaultValue() );
	}

	protected abstract AbstractCollectionType onCopy();
}
