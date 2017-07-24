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

package org.asn1s.core.constraint;

import org.asn1s.api.Ref;
import org.asn1s.api.constraint.ConstraintFactory;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.type.RelationItem;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.core.constraint.template.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CoreConstraintFactory implements ConstraintFactory
{
	@NotNull
	@Override
	public ConstraintTemplate objectSetElements( @NotNull Ref<?> source )
	{
		// TODO:
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public ConstraintTemplate elementSetSpecs( @Nullable ConstraintTemplate setSpec, boolean extensible, @Nullable ConstraintTemplate additionalSetSpec )
	{
		return new ElementSetSpecsTemplate( setSpec, extensible, additionalSetSpec );
	}

	@NotNull
	@Override
	public ConstraintTemplate elementSetSpec( @NotNull List<ConstraintTemplate> unions )
	{
		return new ElementSetSpecTemplate( unions );
	}

	@NotNull
	@Override
	public ConstraintTemplate elementSetSpec( @NotNull ConstraintTemplate exclusion )
	{
		return new ElementSetSpecTemplate( exclusion );
	}

	@NotNull
	@Override
	public ConstraintTemplate union( @NotNull List<ConstraintTemplate> intersections )
	{
		return new UnionTemplate( intersections );
	}

	@NotNull
	@Override
	public ConstraintTemplate elements( @NotNull ConstraintTemplate elements, @Nullable ConstraintTemplate exclusion )
	{
		return new ElementsTemplate( elements, exclusion );
	}

	@NotNull
	@Override
	public ConstraintTemplate value( @NotNull Ref<Value> valueRef )
	{
		return new ValueConstraintTemplate( valueRef );
	}

	@NotNull
	@Override
	public ConstraintTemplate valueRange( @Nullable Ref<Value> min, boolean minLt, @Nullable Ref<Value> max, boolean maxGt )
	{
		return new ValueRangeConstraintTemplate( min, minLt, max, maxGt );
	}

	@NotNull
	@Override
	public ConstraintTemplate permittedAlphabet( @NotNull ConstraintTemplate template )
	{
		return new PermittedAlphabetConstraintTemplate( template );
	}

	@NotNull
	@Override
	public ConstraintTemplate type( @NotNull Ref<Type> typeRef )
	{
		return new TypeConstraintTemplate( typeRef );
	}

	@NotNull
	@Override
	public ConstraintTemplate pattern( @NotNull Ref<Value> valueRef )
	{
		return new PatternConstraintTemplate( valueRef );
	}

	@NotNull
	@Override
	public ConstraintTemplate settings( @NotNull String settings )
	{
		return new SettingsConstraintTemplate( settings );
	}

	@NotNull
	@Override
	public ConstraintTemplate size( @NotNull ConstraintTemplate template )
	{
		return new SizeConstraintTemplate( template );
	}

	@NotNull
	@Override
	public ConstraintTemplate innerType( @NotNull ConstraintTemplate component )
	{
		return new InnerTypeConstraintTemplate( component );
	}

	@NotNull
	@Override
	public ConstraintTemplate innerTypes( @NotNull List<ConstraintTemplate> components, boolean partial )
	{
		return new InnerTypesConstraintTemplate( components, partial );
	}

	@NotNull
	@Override
	public ConstraintTemplate component( @NotNull String componentName, @Nullable ConstraintTemplate template, @Nullable Presence presence )
	{
		return new ComponentConstraintTemplate( componentName, template, presence );
	}

	@NotNull
	@Override
	public ConstraintTemplate containedSubtype( @NotNull Ref<Type> typeRef, boolean includes )
	{
		return new ContainedSubtypeConstraintTemplate( typeRef, includes );
	}

	@NotNull
	@Override
	public ConstraintTemplate valuesFromSet( @NotNull Ref<Type> setRef )
	{
		return new ValuesFromSetConstraintTemplate( setRef );
	}

	@NotNull
	@Override
	public ConstraintTemplate tableConstraint( @NotNull Ref<Type> objectSet, @Nullable List<RelationItem> relationItems )
	{
		return new TableConstraintTemplate( objectSet, relationItems );
	}

}
