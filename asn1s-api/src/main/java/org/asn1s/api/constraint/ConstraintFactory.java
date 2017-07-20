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

package org.asn1s.api.constraint;

import org.asn1s.api.Ref;
import org.asn1s.api.type.RelationItem;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ConstraintFactory
{
	@NotNull
	ConstraintTemplate elementSetSpecs( @Nullable ConstraintTemplate setSpec, boolean extensible, @Nullable ConstraintTemplate additionalSetSpec );

	@NotNull
	ConstraintTemplate elementSetSpec( @NotNull List<ConstraintTemplate> unions );

	@NotNull
	ConstraintTemplate elementSetSpec( @NotNull ConstraintTemplate exclusion );

	@NotNull
	ConstraintTemplate union( @NotNull List<ConstraintTemplate> intersections );

	@NotNull
	ConstraintTemplate elements( @NotNull ConstraintTemplate elements, @Nullable ConstraintTemplate exclusion );

	@NotNull
	ConstraintTemplate value( @NotNull Ref<Value> valueRef );

	@NotNull
	ConstraintTemplate valueRange( @Nullable Ref<Value> min, boolean minLt, @Nullable Ref<Value> max, boolean maxGt );

	@NotNull
	ConstraintTemplate permittedAlphabet( @NotNull ConstraintTemplate template );

	@NotNull
	ConstraintTemplate type( @NotNull Ref<Type> typeRef );

	@NotNull
	ConstraintTemplate pattern( @NotNull Ref<Value> valueRef );

	@NotNull
	ConstraintTemplate settings( @NotNull String settings );

	@NotNull
	ConstraintTemplate size( @NotNull ConstraintTemplate template );

	@NotNull
	ConstraintTemplate innerType( @NotNull ConstraintTemplate component );

	@NotNull
	ConstraintTemplate innerTypes( @NotNull List<ConstraintTemplate> components, boolean partial );

	@NotNull
	ConstraintTemplate component( @NotNull String componentName, @Nullable ConstraintTemplate template, @NotNull Presence presence );

	@NotNull
	ConstraintTemplate containedSubtype( @NotNull Ref<Type> typeRef, boolean includes );

	@NotNull
	ConstraintTemplate valuesFromSet( @NotNull Ref<Type> setRef );

	@NotNull
	ConstraintTemplate tableConstraint( @NotNull Ref<Type> objectSet, @Nullable List<RelationItem> relationItems );
}
