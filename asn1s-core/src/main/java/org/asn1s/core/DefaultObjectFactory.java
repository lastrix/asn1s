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

package org.asn1s.core;

import org.asn1s.api.*;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.type.*;
import org.asn1s.api.type.CollectionType.Kind;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.DefinedValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x681.ObjectValue;
import org.asn1s.core.constraint.template.*;
import org.asn1s.core.module.ModuleImpl;
import org.asn1s.core.module.ModuleSet;
import org.asn1s.core.type.*;
import org.asn1s.core.type.x680.EnumeratedType;
import org.asn1s.core.type.x680.IntegerType;
import org.asn1s.core.type.x680.collection.*;
import org.asn1s.core.type.x680.string.BitStringType;
import org.asn1s.core.type.x681.*;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.core.value.DefinedValueImpl;
import org.asn1s.core.value.DefinedValueTemplateImpl;
import org.asn1s.core.value.TemplateValueInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DefaultObjectFactory extends CoreValueFactory implements ObjectFactory
{
	public DefaultObjectFactory()
	{
		this( null );
	}

	public DefaultObjectFactory( @Nullable ModuleResolver resolver )
	{
		this.resolver = resolver == null ? new ModuleSet() : resolver;
	}

	private final ModuleResolver resolver;
	private Module module;

	@NotNull
	@Override
	public Module module( @NotNull ModuleReference moduleReference )
	{
		module = new ModuleImpl( moduleReference, resolver );
		return module;
	}

	@NotNull
	@Override
	public Module dummyModule()
	{
		module = ModuleImpl.newDummy( resolver );
		return module;
	}

	@Override
	public void setModule( Module module )
	{
		this.module = module;
	}

	@NotNull
	@Override
	public TemplateParameter templateParameter( int index, @NotNull String reference, @Nullable Ref<Type> governor )
	{
		Ref<?> ref = RefUtils.isTypeRef( reference )
				? new TypeNameRef( reference, null )
				: new ValueNameRef( reference, null );

		return new TemplateParameterImpl( index, ref, governor );
	}

	@NotNull
	@Override
	public DefinedType define( @NotNull String name, @NotNull Ref<Type> typeRef, @Nullable Collection<TemplateParameter> parameters )
	{
		DefinedType type = parameters == null
				? new DefinedTypeImpl( module, name, typeRef )
				: new DefinedTypeTemplate( module, name, typeRef, parameters );
		module.getTypeResolver().add( type );
		return type;
	}

	@NotNull
	@Override
	public DefinedValue define( @NotNull String name, @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef, @Nullable Collection<TemplateParameter> parameters )
	{
		DefinedValue value = parameters == null
				? new DefinedValueImpl( module, name, typeRef, valueRef )
				: new DefinedValueTemplateImpl( module, name, typeRef, valueRef, parameters );
		module.getValueResolver().add( value );
		return value;
	}

	@NotNull
	@Override
	public Ref<Type> builtin( @NotNull String typeName )
	{
		if( "TYPE-IDENTIFIER".equals( typeName ) )
			return BuiltinClass.TypeIdentifier.ref();

		return UniversalType.forTypeName( typeName ).ref();
	}

	@NotNull
	@Override
	public Type builtin( @NotNull String typeName, @NotNull Collection<NamedValue> namedValues )
	{
		switch( UniversalType.forTypeName( typeName ) )
		{
			case Integer:
				return new IntegerType( namedValues );

			case BitString:
				return new BitStringType( namedValues );

			default:
				throw new IllegalArgumentException( typeName );
		}
	}

	@NotNull
	@Override
	public Type constrained( @NotNull ConstraintTemplate constraintTemplate, @NotNull Ref<Type> typeRef )
	{
		return new ConstrainedType( constraintTemplate, typeRef );
	}

	@NotNull
	@Override
	public Type tagged( @NotNull IEncoding encoding, @NotNull Ref<Type> typeRef )
	{
		return new TaggedTypeImpl( encoding, typeRef );
	}

	@Override
	public IEncoding tagEncoding( @NotNull TagMethod method, @NotNull TagClass tagClass, @NotNull Ref<Value> tagNumberRef )
	{
		if( tagNumberRef instanceof Value && ( (Value)tagNumberRef ).getKind() == Value.Kind.Integer && ( (Value)tagNumberRef ).toIntegerValue().isInt() )
			return TagEncoding.create( module.getTagMethod(), method, tagClass, ( (Value)tagNumberRef ).toIntegerValue().asInt() );
		return TagEncoding.create( module.getTagMethod(), method, tagClass, tagNumberRef );
	}

	@NotNull
	@Override
	public Type selectionType( @NotNull String componentName, @NotNull Ref<Type> typeRef )
	{
		return new SelectionType( componentName, typeRef );
	}

	@NotNull
	@Override
	public Enumerated enumerated()
	{
		return new EnumeratedType();
	}

	@NotNull
	@Override
	public CollectionType collection( @NotNull Kind collectionKind )
	{
		boolean automaticTags = module.getTagMethod() == TagMethod.Automatic;
		switch( collectionKind )
		{
			case Choice:
				return new ChoiceType( automaticTags );

			case Sequence:
				return new SequenceType( automaticTags );

			case SequenceOf:
				return new SequenceOfType();

			case Set:
				return new SetType( automaticTags );

			case SetOf:
				return new SetOfType();

			default:
				throw new IllegalArgumentException( collectionKind.name() );
		}
	}

	@NotNull
	@Override
	public CollectionTypeExtensionGroup extensionGroup( @NotNull Family requiredFamily )
	{
		return new ExtensionAdditionGroupType( requiredFamily );
	}

	@NotNull
	@Override
	public Value valueTemplateInstance( @NotNull Ref<Value> ref, @NotNull List<Ref<?>> arguments )
	{
		return new TemplateValueInstance( ref, arguments );
	}

	@NotNull
	@Override
	public Type typeTemplateInstance( @NotNull Ref<Type> ref, @NotNull List<Ref<?>> arguments )
	{
		return new TemplateTypeInstance( ref, arguments );
	}

	////////////////////////////////////// Classes /////////////////////////////////////////////////////////////////////
	@NotNull
	@Override
	public Type instanceOf( @NotNull Ref<Type> classTypeRef )
	{
		return new InstanceOfType( classTypeRef );
	}

	@NotNull
	@Override
	public ClassType classType()
	{
		return new ClassTypeImpl();
	}

	@NotNull
	@Override
	public ClassFieldType typeClassField( @NotNull String name, boolean optional, @Nullable Ref<Type> defaultTypeRef )
	{
		return new TypeFieldType( name, optional, defaultTypeRef );
	}

	@NotNull
	@Override
	public ClassFieldType fixedTypeValueField( @NotNull String name, @NotNull Ref<Type> typeRef, boolean unique, boolean optional, @Nullable Ref<Value> defaultValue )
	{
		return new FixedValueFieldType( name, typeRef, unique, optional, defaultValue );
	}

	@NotNull
	@Override
	public ClassFieldType variableTypeValueField( @NotNull String name, @NotNull String fieldName, boolean optional, @Nullable Ref<Value> defaultValue )
	{
		// TODO:
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public ClassFieldType fixedTypeValueSetField( @NotNull String name, @NotNull Ref<Type> typeRef, boolean optional, @Nullable ConstraintTemplate defaultElementSetSpecs )
	{
		return new FixedValueSetFieldType( name, typeRef, optional, defaultElementSetSpecs );
	}

	@NotNull
	@Override
	public ClassFieldType variableTypeValueSetField( @NotNull String name, @NotNull String fieldName, boolean optional, @Nullable ConstraintTemplate defaultElementSetSpecs )
	{
		// TODO:
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Ref<Value> valueFromObjectRef( @NotNull Ref<?> source, @Nullable String path, @NotNull String name )
	{
		// TODO:
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Ref<Type> valueSetFromObjectRef( @NotNull Ref<?> source, @Nullable String path, @NotNull String name )
	{
		// TODO:
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Ref<Type> typeFromObjectRef( @NotNull Ref<?> source, @Nullable List<String> path, @NotNull String name )
	{
		return new ClassFieldFromUnknownSourceRef( source, path, name );
	}

	@NotNull
	@Override
	public ConstraintTemplate objectSetElements( @NotNull Ref<?> source )
	{
		// TODO:
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Value object( @NotNull Map<String, Ref<?>> map )
	{
		return new ObjectValue( map );
	}

	///////////////////////////////////// Constraints //////////////////////////////////////////////////////////////////
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
