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
import org.asn1s.api.State;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.CollectionTypeExtensionGroup;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.core.type.TaggedTypeImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

abstract class AbstractComponentInterpolator
{
	private final Scope scope;
	private final AbstractCollectionType type;
	private final Map<String, Family> componentFamilyMap = new HashMap<>();

	AbstractComponentInterpolator( Scope scope, AbstractCollectionType type )
	{
		this.scope = scope;
		this.type = type;
	}

	private boolean mayUseAutomaticTags( @NotNull Iterable<ComponentType> componentTypes )
	{
		String namespace = type.getNamespace();
		for( ComponentType componentType : componentTypes )
		{
			if( componentType.getNamespace() == null )
				throw new IllegalStateException();
			if( componentType.getNamespace().equals( namespace ) && componentType.isExplicitlyTagged() )
				return false;
		}

		return true;
	}

	protected abstract void assertTagAmbiguity( Collection<ComponentType> components ) throws ValidationException;

	private Scope getScope()
	{
		return scope;
	}

	AbstractCollectionType getType()
	{
		return type;
	}

	List<ComponentType> interpolate() throws ValidationException, ResolutionException
	{
		validateComponents( type.getComponents() );
		validateComponents( type.getExtensions() );
		validateComponents( type.getComponentsLast() );
		new CollectionValidator( getType() ).validate();
		List<ComponentType> components = interpolateComponents();
		assertTagAmbiguity( components );
		return components;
	}

	private void validateComponents( Iterable<Type> components ) throws ResolutionException, ValidationException
	{
		for( Type component : components )
		{
			component.setNamespace( type.getNamespace() );
			component.validate( scope );
		}
	}

	@NotNull
	private List<ComponentType> interpolateComponents() throws ValidationException, ResolutionException
	{
		List<ComponentType> components = new ComponentsBuilder( getType(), componentFamilyMap ).build();
		for( ComponentType component : components )
			component.validate( getScope() );

		return type.isAutomaticTags() && mayUseAutomaticTags( components ) ? new ComponentTagger( componentFamilyMap, components, getScope() ).applyAutomaticTags() : components;
	}

	private static final class ComponentsBuilder
	{
		private final AbstractCollectionType type;
		private final Map<String, Family> componentFamilyMap;
		@SuppressWarnings( "unchecked" )
		// 0 - components, 1 - componentsLast, 2 - extensions
		private final Collection<ComponentType>[] _comps = new Collection[]{new ArrayList<ComponentType>(), new ArrayList<ComponentType>(), new ArrayList<ComponentType>()};
		private final List<ComponentType> result = new ArrayList<>();
		private int index;

		private ComponentsBuilder( AbstractCollectionType type, Map<String, Family> componentFamilyMap )
		{
			this.type = type;
			this.componentFamilyMap = componentFamilyMap;
		}

		private List<ComponentType> build() throws ValidationException
		{
			resolveComponentsImpl( _comps[0], type.getComponents(), -1 );
			resolveComponentsImpl( _comps[1], type.getComponentsLast(), -1 );
			resolveComponentsImpl( _comps[2], type.getExtensions(), 2 );
			registerComponents( _comps[0] );
			registerComponents( _comps[2] );
			registerComponents( _comps[1] );
			return result;
		}

		private void registerComponents( Iterable<ComponentType> source )
		{
			for( ComponentType component : source )
				//noinspection ValueOfIncrementOrDecrementUsed
				addComponentType( result, component, component.getVersion(), index++ );
		}

		private void resolveComponentsImpl( Collection<ComponentType> list, Iterable<Type> sources, int version ) throws ValidationException
		{
			if( version == -1 )
				for( Type source : sources )
					resolveComponentTypeSource( list, source, 1 );
			else
				for( Type source : sources )
					//noinspection ValueOfIncrementOrDecrementUsed
					resolveComponentTypeSource( list, source, version++ );
		}

		private void resolveComponentTypeSource( Collection<ComponentType> list, Type source, int version ) throws ValidationException
		{
			if( source instanceof ComponentType )
				addComponentType( list, (ComponentType)source, version, -1 );
			else if( source instanceof ComponentsFromType )
				resolveComponentTypeSourceForCompsFromType( list, (ComponentsFromType)source, version );
			else if( source instanceof CollectionTypeExtensionGroup )
				resolveComponentTypeSourceForExtGroup( list, (CollectionTypeExtensionGroup)source, version );
			else
				throw new IllegalStateException( "Unable to use type: " + source );
		}

		private void resolveComponentTypeSourceForExtGroup( Collection<ComponentType> list, CollectionTypeExtensionGroup source, int version ) throws ValidationException
		{
			int groupVersion = source.getVersion();
			if( groupVersion != -1 )
			{
				if( groupVersion < version )
					throw new ValidationException( "Version must be greater than previous group" );
				version = groupVersion;
			}
			for( ComponentType componentType : source.getComponents() )
				addComponentType( list, componentType, version, -1 );
		}

		private void resolveComponentTypeSourceForCompsFromType( Collection<ComponentType> list, ComponentsFromType source, int version )
		{
			for( ComponentType componentType : source.getComponents() )
				addComponentType( list, componentType, version, -1 );
		}

		private void addComponentType( Collection<ComponentType> list, ComponentType source, int version, int index )
		{
			if( source.getState() == State.DONE )
				componentFamilyMap.put( source.getComponentName(), source.getFamily() );

			list.add( isShouldKeep( source, version, index ) ? source : createComponentType( source, version, index ) );
		}

		private static boolean isShouldKeep( ComponentType source, int version, int index )
		{
			return source.getVersion() == version && index != -1 && source.getIndex() == index;
		}

		@NotNull
		private static ComponentType createComponentType( ComponentType source, int version, int index )
		{
			ComponentTypeImpl componentType =
					new ComponentTypeImpl( index == -1 ? source.getIndex() : index,
					                       source.getComponentName(),
					                       source.getComponentTypeRef() );
			componentType.setVersion( version );
			componentType.setOptional( source.isOptional() );
			componentType.setDefaultValueRef( source.getDefaultValueRef() );
			componentType.setNamespace( source.getNamespace() );
			return componentType;
		}
	}

	private static final class CollectionValidator
	{
		private final AbstractCollectionType type;
		private final Collection<String> names;

		private CollectionValidator( AbstractCollectionType type )
		{
			this.type = type;
			names = new HashSet<>();
		}

		public void validate() throws ValidationException
		{
			assertNames( type.getComponents(), names );
			assertNames( type.getExtensions(), names );
			assertNames( type.getComponentsLast(), names );

			if( isExtensionVersionProhibited() )
				assertExtensionGroupHasNoVersion();
			else
				assertExtensionVersions();
		}

		private static void assertNames( Iterable<Type> items, Collection<String> names ) throws ValidationException
		{
			for( Type item : items )
				assertName( item, names );
		}

		private static void assertName( Type item, Collection<String> names ) throws ValidationException
		{
			if( item instanceof ComponentType )
				assertNameForComponentType( (ComponentType)item, names );
			else if( item instanceof CollectionTypeExtensionGroup )
				assertNameForExtensionGroup( (CollectionTypeExtensionGroup)item, names );
			else if( item instanceof ComponentsFromType )
				assertNameForComponentsFromType( (ComponentsFromType)item, names );
		}

		private static void assertNameForComponentsFromType( ComponentsFromType item, Collection<String> names ) throws ValidationException
		{
			for( ComponentType componentType : item.getComponents() )
				assertName( componentType, names );
		}

		private static void assertNameForExtensionGroup( CollectionTypeExtensionGroup item, Collection<String> names ) throws ValidationException
		{
			for( ComponentType componentType : item.getComponents() )
				assertName( componentType, names );
		}

		private static void assertNameForComponentType( ComponentType item, Collection<String> names ) throws ValidationException
		{
			String componentName = item.getComponentName();
			if( names.contains( componentName ) )
				throw new ValidationException( "ComponentType with name '" + componentName + "' already exist" );
			names.add( componentName );
		}

		private void assertExtensionVersions() throws ValidationException
		{
			int prevVersion = 1;
			for( Type extension : type.getExtensions() )
			{
				CollectionTypeExtensionGroup group = (CollectionTypeExtensionGroup)extension;
				if( prevVersion >= group.getVersion() )
					throw new ValidationException( "Extension group version is greater than previous" );

				prevVersion = group.getVersion();
			}
		}

		private void assertExtensionGroupHasNoVersion() throws ValidationException
		{
			for( Type extension : type.getExtensions() )
				if( extension instanceof CollectionTypeExtensionGroup && ( (CollectionTypeExtensionGroup)extension ).getVersion() != -1 )
					throw new ValidationException( "Extension group version is prohibited: " + type );
		}

		private boolean isExtensionVersionProhibited()
		{
			for( Type extension : type.getExtensions() )
				if( isVersionProhibitedFor( extension ) )
					return true;

			return false;
		}

		private static boolean isVersionProhibitedFor( Type extension )
		{
			return extension instanceof ComponentType
					|| extension instanceof ComponentsFromType
					|| extension instanceof CollectionTypeExtensionGroup && ( (CollectionTypeExtensionGroup)extension ).getVersion() == -1;
		}
	}

	private static final class ComponentTagger
	{
		private final Map<String, Family> componentFamilyMap;
		private final List<ComponentType> components;
		private final Scope scope;
		private final List<ComponentType> result;
		private int tagNumber;

		private ComponentTagger( Map<String, Family> componentFamilyMap, List<ComponentType> components, Scope scope )
		{
			this.componentFamilyMap = componentFamilyMap;
			this.components = components;
			this.scope = scope;
			result = new ArrayList<>( components.size() );
		}

		@NotNull
		private List<ComponentType> applyAutomaticTags() throws ResolutionException, ValidationException
		{
			applyTags( true );
			applyTags( false );
			result.sort( Comparator.comparingInt( ComponentType:: getIndex ) );
			return result;
		}

		private void applyTags( boolean main ) throws ResolutionException, ValidationException
		{
			for( ComponentType component : components )
				if( main ^ component.getVersion() > 1 )
					result.add( applyTagNumber( component ) );
		}

		private ComponentType applyTagNumber( ComponentType component ) throws ResolutionException, ValidationException
		{
			TagMethod method = selectTagMethod( component );
			TaggedTypeImpl subType =
					new TaggedTypeImpl( TagEncoding.context( tagNumber, method ), component.getComponentTypeRef() );
			subType.setNamespace( component.getNamespace() );
			tagNumber++;
			ComponentTypeImpl taggedComponent =
					new ComponentTypeImpl( component.getIndex(),
					                       component.getComponentName(),
					                       subType );
			taggedComponent.setVersion( component.getVersion() );
			taggedComponent.setOptional( component.isOptional() );
			taggedComponent.setDefaultValueRef( component.getDefaultValueRef() );
			taggedComponent.setNamespace( component.getNamespace() );
			taggedComponent.validate( scope );
			return taggedComponent;
		}

		@NotNull
		private TagMethod selectTagMethod( ComponentType component )
		{
			boolean isChoice = componentFamilyMap.get( component.getComponentName() ) == Family.CHOICE;
			boolean noEncodingAvailable = component.getEncoding( EncodingInstructions.TAG ) == null;
			return isChoice && noEncodingAvailable ? TagMethod.EXPLICIT : TagMethod.IMPLICIT;
		}

	}
}
