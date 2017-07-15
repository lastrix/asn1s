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

import org.asn1s.api.Module;
import org.asn1s.api.ModuleReference;
import org.asn1s.api.Ref;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.type.CollectionType.Kind;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.DefinedValue;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TypeFactory
{
	@NotNull
	Module module( @NotNull ModuleReference moduleReference );

	@NotNull
	Module dummyModule();

	void setModule( @Nullable Module module );

	//////////////////////////////// Types /////////////////////////////////////////////////////////////////////////////
	@NotNull
	TemplateParameter templateParameter( int index, @NotNull Ref<?> ref, @Nullable Ref<Type> governor );

	@NotNull
	DefinedType define( @NotNull String name, @NotNull Ref<Type> typeRef, @Nullable Collection<TemplateParameter> parameters );

	@NotNull
	DefinedValue define( @NotNull String name, @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef, @Nullable Collection<TemplateParameter> parameters );

	@NotNull
	Ref<Type> builtin( @NotNull String typeName );

	@NotNull
	Type builtin( @NotNull String typeName, @NotNull Collection<NamedValue> namedValues );

	@NotNull
	Type constrained( @NotNull ConstraintTemplate constraintTemplate, @NotNull Ref<Type> typeRef );

	@NotNull
	Type tagged( @NotNull IEncoding encoding, @NotNull Ref<Type> typeRef );

	IEncoding tagEncoding( @NotNull TagMethod method, @NotNull TagClass tagClass, @NotNull Ref<Value> tagNumberRef );

	@NotNull
	Type selectionType( @NotNull String componentName, @NotNull Ref<Type> typeRef );

	@NotNull
	Enumerated enumerated();

	@NotNull
	CollectionType collection( @NotNull Kind collectionKind );

	@NotNull
	CollectionTypeExtensionGroup extensionGroup( @NotNull Family requiredFamily );

	@NotNull
	Value valueTemplateInstance( @NotNull Ref<Value> ref, @NotNull List<Ref<?>> arguments );

	@NotNull
	Type typeTemplateInstance( @NotNull Ref<Type> ref, @NotNull List<Ref<?>> arguments );

	//////////////////////////////// Classes ///////////////////////////////////////////////////////////////////////////

	@NotNull
	Type instanceOf( @NotNull Ref<Type> classTypeRef );

	@NotNull
	ClassType classType();

	@NotNull
	ClassFieldType typeClassField( @NotNull String name, boolean optional, @Nullable Ref<Type> defaultTypeRef );

	@NotNull
	ClassFieldType fixedTypeValueField( @NotNull String name, @NotNull Ref<Type> typeRef, boolean unique, boolean optional, @Nullable Ref<Value> defaultValue );

	@NotNull
	ClassFieldType variableTypeValueField( @NotNull String name, @NotNull String fieldName, boolean optional, @Nullable Ref<Value> defaultValue );

	@NotNull
	ClassFieldType fixedTypeValueSetField( @NotNull String name, @NotNull Ref<Type> typeRef, boolean optional, @Nullable ConstraintTemplate defaultElementSetSpecs );

	@NotNull
	ClassFieldType variableTypeValueSetField( @NotNull String name, @NotNull String fieldName, boolean optional, @Nullable ConstraintTemplate defaultElementSetSpecs );

	@NotNull
	Ref<Value> valueFromObjectRef( @NotNull Ref<?> source, @Nullable String path, @NotNull String name );

	@NotNull
	Ref<Type> valueSetFromObjectRef( @NotNull Ref<?> source, @Nullable String path, @NotNull String name );

	@NotNull
	Ref<Type> typeFromObjectRef( @NotNull Ref<?> source, @Nullable List<String> path, @NotNull String name );

	@NotNull
	ConstraintTemplate objectSetElements( @NotNull Ref<?> source );

	@NotNull
	Value object( @NotNull Map<String, Ref<?>> map );
}
