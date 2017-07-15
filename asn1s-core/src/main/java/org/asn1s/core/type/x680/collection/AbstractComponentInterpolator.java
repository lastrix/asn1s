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
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.CollectionTypeExtensionGroup;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.core.type.TaggedTypeImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

abstract class AbstractComponentInterpolator
{
	private final Scope scope;
	private final AbstractCollectionType type;
	private final boolean applyAutomaticTags;
	private final Map<String, Family> componentFamilyMap = new HashMap<>();


	AbstractComponentInterpolator( Scope scope, AbstractCollectionType type )
	{
		this.scope = scope;
		this.type = type;
		applyAutomaticTags = type.isAutomaticTags() && mayUseAutomaticTags( type );
	}

	private static boolean mayUseAutomaticTags( @NotNull AbstractCollectionType collectionType )
	{
		for( Type type : collectionType.getComponents() )
		{
			if( type instanceof ComponentType && ( (ComponentType)type ).isExplicitlyTagged() )
				return false;
		}
		for( Type type : collectionType.getComponentsLast() )
		{
			if( type instanceof ComponentType && ( (ComponentType)type ).isExplicitlyTagged() )
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
		assertValidity();

		List<ComponentType> components = buildComponentTypeList();

		if( isApplyAutomaticTags() )
			components = applyAutomaticTags( components );

		for( ComponentType component : components )
			component.validate( getScope() );

		assertTagAmbiguity( components );
		return components;
	}

	private void assertValidity() throws ValidationException
	{
		assertNames();
		assertExtensions();
	}

	private void assertNames() throws ValidationException
	{
		Collection<String> names = new HashSet<>();
		for( Type item : getType().getComponents() )
			assertName( item, names );

		for( Type item : getType().getExtensions() )
			assertName( item, names );

		for( Type item : getType().getComponentsLast() )
			assertName( item, names );
	}

	private static void assertName( Type item, Collection<String> names ) throws ValidationException
	{
		if( item instanceof ComponentType )
		{
			String componentName = ( (ComponentType)item ).getComponentName();
			if( names.contains( componentName ) )
				throw new ValidationException( "ComponentType with name '" + componentName + "' already exist" );
			names.add( componentName );
		}
		else if( item instanceof CollectionTypeExtensionGroup )
		{
			for( ComponentType componentType : ( (CollectionTypeExtensionGroup)item ).getComponents() )
				assertName( componentType, names );
		}
		else if( item instanceof ComponentsFromType )
			for( ComponentType componentType : ( (ComponentsFromType)item ).getComponents() )
				assertName( componentType, names );
	}

	private void assertExtensions() throws ValidationException
	{
		boolean allHasVersion = true;
		int prevVersion = 1;
		int checked = 0;
		for( Type extension : getType().getExtensions() )
		{
			if( extension instanceof ComponentType || extension instanceof ComponentsFromType )
			{
				allHasVersion = false;
			}
			else if( extension instanceof CollectionTypeExtensionGroup )
			{
				int groupVersion = ( (CollectionTypeExtensionGroup)extension ).getVersion();
				if( groupVersion == -1 )
				{
					if( checked > 0 && allHasVersion )
						throw new ValidationException( "All extension groups must have version or no one." );
					allHasVersion = false;
				}

				if( allHasVersion )
				{
					if( prevVersion >= groupVersion )
						throw new ValidationException( "Extension group version is greater than previous" );

					prevVersion = groupVersion;
				}
			}
			checked++;
		}
	}

	@NotNull
	private List<ComponentType> applyAutomaticTags( @NotNull Collection<ComponentType> components )
	{
		int tagNumber = 0;
		List<ComponentType> result = new ArrayList<>( components.size() );
		for( ComponentType component : components )
		{
			if( component.getVersion() == 1 )
			{
				result.add( applyTagNumber( tagNumber, component ) );
				tagNumber++;
			}
		}
		for( ComponentType component : components )
		{
			if( component.getVersion() > 1 )
			{
				result.add( applyTagNumber( tagNumber, component ) );
				tagNumber++;
			}
		}
		result.sort( Comparator.comparingInt( ComponentType:: getIndex ) );
		return result;
	}

	private ComponentType applyTagNumber( int tagNumber, ComponentType component )
	{
		TagMethod method =
				componentFamilyMap.get( component.getComponentName() ) == Family.Choice && component.getEncoding( EncodingInstructions.Tag ) == null
						? TagMethod.Explicit
						: TagMethod.Implicit;
		TaggedTypeImpl subType = new TaggedTypeImpl( TagEncoding.context( tagNumber, method ), component.getComponentTypeRef() );
		return new ComponentTypeImpl( component.getIndex(),
		                              component.getVersion(),
		                              component.getComponentName(),
		                              subType,
		                              component.isOptional(),
		                              component.getDefaultValueRef() );
	}

	@NotNull
	private List<ComponentType> buildComponentTypeList() throws ValidationException
	{
		Collection<ComponentType> components = new ArrayList<>();
		for( Type source : getType().getComponents() )
			resolveComponentTypeSource( components, source, 1 );

		Collection<ComponentType> componentsLast = new ArrayList<>();
		for( Type source : getType().getComponentsLast() )
			resolveComponentTypeSource( componentsLast, source, 1 );

		int version = 1;
		Collection<ComponentType> extensions = new ArrayList<>();
		for( Type source : getType().getExtensions() )
		{
			version++;
			resolveComponentTypeSource( extensions, source, version );
		}

		List<ComponentType> result = new ArrayList<>();
		int index = 0;
		for( ComponentType component : components )
		{
			addComponentType( result, component, component.getVersion(), index );
			index++;
		}
		for( ComponentType extension : extensions )
		{
			addComponentType( result, extension, extension.getVersion(), index );
			index++;
		}
		for( ComponentType component : componentsLast )
		{
			addComponentType( result, component, component.getVersion(), index );
			index++;
		}
		return result;
	}

	private boolean isApplyAutomaticTags()
	{
		return applyAutomaticTags;
	}

	private void resolveComponentTypeSource( Collection<ComponentType> list, Type source, int version ) throws ValidationException
	{
		if( source instanceof ComponentType )
			addComponentType( list, (ComponentType)source, version, -1 );
		else if( source instanceof ComponentsFromType )
		{
			for( ComponentType componentType : ( (ComponentsFromType)source ).getComponents() )
				addComponentType( list, componentType, version, -1 );
		}
		else if( source instanceof CollectionTypeExtensionGroup )
		{
			int groupVersion = ( (CollectionTypeExtensionGroup)source ).getVersion();
			if( groupVersion != -1 )
			{
				if( groupVersion < version )
					throw new ValidationException( "Version must be greater than previous group" );
				version = groupVersion;
			}
			for( ComponentType componentType : ( (CollectionTypeExtensionGroup)source ).getComponents() )
				addComponentType( list, componentType, version, -1 );
		}
		else
			throw new IllegalStateException( "Unable to use type: " + source );
	}

	private void addComponentType( Collection<ComponentType> list, ComponentType source, int version, int index )
	{
		if( source.getState() == State.Done )
			componentFamilyMap.put( source.getComponentName(), source.getFamily() );

		if( source.getVersion() == version && index != -1 && source.getIndex() == index )
			list.add( source );
		else
		{
			ComponentType componentType =
					new ComponentTypeImpl( index == -1 ? source.getIndex() : index,
					                       version,
					                       source.getComponentName(),
					                       source.getComponentTypeRef(),
					                       source.isOptional(),
					                       source.getDefaultValueRef() );
			list.add( componentType );
		}
	}

	static void assertTags( NamedType component, Iterable<ComponentType> list ) throws ValidationException
	{
		if( component.getFamily() == Family.Choice && component.getEncoding( EncodingInstructions.Tag ) == null )
		{
			for( NamedType namedType : component.getNamedTypes() )
				assertTags( namedType, list );
		}
		else
		{
			TagEncoding encoding = (TagEncoding)component.getEncoding( EncodingInstructions.Tag );
			assertTagsImpl( component.getName(), encoding.getTagClass(), encoding.getTagNumber(), list );
		}
	}

	private static void assertTagsImpl( String name, TagClass tagClass, int tagNumber, Iterable<? extends NamedType> list ) throws ValidationException
	{
		for( NamedType component : list )
		{
			IEncoding enc = component.getEncoding( EncodingInstructions.Tag );
			if( enc == null )
			{
				if( component.getFamily() == Family.Choice )
					assertTagsImpl( name, tagClass, tagNumber, component.getNamedTypes() );

				throw new IllegalStateException();
			}
			else
			{
				TagEncoding encoding = (TagEncoding)enc;
				if( tagClass == encoding.getTagClass() && tagNumber == encoding.getTagNumber() )
					throw new ValidationException( "Duplicate tag detected for component '" + name + "' and '" + component.getName() + '\'' );
			}
		}
	}
}
