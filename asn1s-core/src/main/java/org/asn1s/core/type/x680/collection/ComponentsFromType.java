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
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractType;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class ComponentsFromType extends AbstractType
{
	ComponentsFromType( @NotNull Ref<Type> ref, Family requiredFamily )
	{
		this.ref = ref;
		this.requiredFamily = requiredFamily;

		if( ref instanceof Type )
			type = (Type)ref;
	}

	private List<ComponentType> components;
	private Ref<Type> ref;
	private final Family requiredFamily;
	private Type type;

	@NotNull
	@Override
	public Type getSibling()
	{
		return type;
	}

	@Override
	public boolean hasSibling()
	{
		return type != null;
	}

	public List<ComponentType> getComponents()
	{
		return Collections.unmodifiableList( components );
	}

	@Override
	public boolean equals( Object obj )
	{
		return obj == this || obj instanceof ComponentsFromType && toString().equals( obj.toString() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public String toString()
	{
		return "COMPONENTS FROM " + ref;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( type == null )
			type = ref.resolve( scope );

		components = new ArrayList<>();
		resolveComponents( scope, type );
	}

	private void resolveComponents( Scope scope, Type type ) throws ValidationException, ResolutionException
	{
		type.validate( scope );
		Family family = type.getFamily();
		if( family != Family.SEQUENCE && family != Family.SET )
			throw new ValidationException( "ComponentsFromType should point to: " + requiredFamily );

		while( !( type instanceof CollectionType ) )
		{
			assert type != null;
			type = type.getSibling();
		}

		resolveToComponentTypes( scope, getRawComponents( (CollectionType)type ) );
	}

	private void resolveToComponentTypes( Scope scope, Iterable<Type> rawComponents ) throws ValidationException, ResolutionException
	{
		for( Type component : rawComponents )
		{
			if( component instanceof ComponentType )
				components.add( (ComponentType)component );
			else if( component instanceof ExtensionAdditionGroupType )
				throw new IllegalStateException( "No extension addition group allowed" );
			else if( component instanceof ComponentsFromType )
			{
				assert component.hasSibling();
				resolveComponents( scope, component.getSibling() );
			}
			else
				throw new UnsupportedOperationException( component.getClass().getTypeName() );
		}
	}

	private static Iterable<Type> getRawComponents( CollectionType type )
	{
		List<Type> primary = type.getComponents( Kind.PRIMARY );
		List<Type> secondary = type.getComponents( Kind.SECONDARY );
		Collection<Type> list = new ArrayList<>( primary.size() + secondary.size() );
		list.addAll( primary );
		list.addAll( secondary );
		return list;
	}

	@NotNull
	@Override
	public Type copy()
	{
		Ref<Type> sub = Objects.equals( ref, type ) ? type.copy() : ref;
		return new ComponentsFromType( sub, requiredFamily );
	}

	@Override
	protected void onDispose()
	{
		ref = null;
		type = null;
		components.clear();
		components = null;
	}
}
