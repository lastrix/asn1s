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

package org.asn1s.core.type;

import org.asn1s.api.BuiltinClass;
import org.asn1s.api.Ref;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.UniversalType;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.type.*;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.DefinedValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x681.ObjectValue;
import org.asn1s.core.TemplateParameterImpl;
import org.asn1s.core.module.ModuleImpl;
import org.asn1s.core.module.ModuleSet;
import org.asn1s.core.type.x680.EnumeratedType;
import org.asn1s.core.type.x680.IntegerType;
import org.asn1s.core.type.x680.collection.*;
import org.asn1s.core.type.x680.string.BitStringType;
import org.asn1s.core.type.x681.*;
import org.asn1s.core.value.DefinedValueImpl;
import org.asn1s.core.value.DefinedValueTemplateImpl;
import org.asn1s.core.value.TemplateValueInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CoreTypeFactory implements TypeFactory
{
	public CoreTypeFactory()
	{
		this( null );
	}

	public CoreTypeFactory( @Nullable ModuleResolver resolver )
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
		DefinedValueImpl value = parameters == null
				? new DefinedValueImpl( module, name )
				: new DefinedValueTemplateImpl( module, name, parameters );
		value.setTypeRef( typeRef );
		value.setValueRef( valueRef );
		module.getValueResolver().add( value );
		return value;
	}

	@NotNull
	@Override
	public Ref<Type> builtin( @NotNull String typeName )
	{
		if( "TYPE-IDENTIFIER".equals( typeName ) )
			return BuiltinClass.TYPE_IDENTIFIER.ref();

		return UniversalType.forTypeName( typeName ).ref();
	}

	@NotNull
	@Override
	public Type builtin( @NotNull String typeName, @NotNull Collection<NamedValue> namedValues )
	{
		switch( UniversalType.forTypeName( typeName ) )
		{
			case INTEGER:
				return new IntegerType( namedValues );

			case BIT_STRING:
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
		if( tagNumberRef instanceof Value && ( (Value)tagNumberRef ).getKind() == Kind.INTEGER && ( (Value)tagNumberRef ).toIntegerValue().isInt() )
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
	public CollectionType collection( @NotNull Family collectionFamily )
	{
		boolean automaticTags = module.getTagMethod() == TagMethod.AUTOMATIC;
		switch( collectionFamily )
		{
			case CHOICE:
				return new ChoiceType( automaticTags );

			case SEQUENCE:
				return new SequenceType( automaticTags );

			case SET:
				return new SetType( automaticTags );

			default:
				throw new IllegalArgumentException( collectionFamily.name() );
		}
	}

	@NotNull
	@Override
	public CollectionOfType collectionOf( @NotNull Family collectionFamily )
	{
		switch( collectionFamily )
		{
			case SEQUENCE_OF:
				return new SequenceOfType();

			case SET_OF:
				return new SetOfType();

			default:
				throw new IllegalArgumentException( collectionFamily.name() );
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
	public Value object( @NotNull Map<String, Ref<?>> map )
	{
		return new ObjectValue( map );
	}
}
