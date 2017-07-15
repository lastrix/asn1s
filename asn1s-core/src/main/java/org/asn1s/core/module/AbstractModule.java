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

package org.asn1s.core.module;

import org.asn1s.api.*;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeName;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueName;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x680.DefinedValue;
import org.asn1s.core.scope.ModuleScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

abstract class AbstractModule implements Module
{
	AbstractModule( @NotNull ModuleReference name, @Nullable ModuleResolver resolver )
	{
		this.name = name;
		this.resolver = resolver == null ? new ModuleSet() : resolver;
	}

	private final ModuleReference name;
	private final ModuleResolver resolver;
	private final Map<String, DefinedType> typeMap = new LinkedHashMap<>();
	private final Map<ModuleReference, Map<String, TypeNameRef>> importedTypeMap = new HashMap<>();
	private final Map<String, TypeNameRef> referencedTypes = new HashMap<>();
	private final Map<String, DefinedValue> valueMap = new LinkedHashMap<>();
	private final Map<ModuleReference, Map<String, ValueNameRef>> importedValueMap = new HashMap<>();
	private final Map<String, ValueNameRef> referencedValues = new HashMap<>();
	private final Collection<Disposable> disposables = new ArrayList<>();

	@Nullable
	@Override
	public ModuleResolver getModuleResolver()
	{
		return resolver;
	}

	@NotNull
	@Override
	public ModuleReference getModuleReference()
	{
		return name;
	}

	@NotNull
	@Override
	public String getModuleName()
	{
		return name.getName();
	}

	@Override
	public void addImports( ModuleReference moduleReference, Iterable<String> symbols )
	{
		Map<String, TypeNameRef> typeReferenceMap = importedTypeMap.computeIfAbsent( moduleReference, e -> new HashMap<>() );
		Map<String, ValueNameRef> valueReferenceMap = importedValueMap.computeIfAbsent( moduleReference, e -> new HashMap<>() );
		for( String symbol : symbols )
		{
			if( RefUtils.isTypeRef( symbol ) )
				typeReferenceMap.put( symbol, new TypeNameRef( new TypeName( symbol, moduleReference.getName() ) ) );
			else
				valueReferenceMap.put( symbol, new ValueNameRef( new ValueName( symbol, moduleReference.getName() ) ) );
		}
	}

	public Map<String, List<String>> getImportsAsSymbolsFromModule()
	{
		Map<String, List<String>> result = new HashMap<>();
		for( Map<String, TypeNameRef> map : importedTypeMap.values() )
			for( TypeNameRef reference : map.values() )
				result.computeIfAbsent( reference.getModuleName(), e -> new ArrayList<>() ).add( reference.getName() );

		for( Map<String, ValueNameRef> map : importedValueMap.values() )
			for( ValueNameRef reference : map.values() )
				result.computeIfAbsent( reference.getModuleName(), e -> new ArrayList<>() ).add( reference.getName() );

		for( List<String> list : result.values() )
			Collections.sort( list );
		return result;
	}

	@Override
	public Scope createScope()
	{
		return new ModuleScope( this );
	}

	/////////////////////////////// Attributes /////////////////////////////////////////////////////////////////////////
	private TagMethod tagMethod = TagMethod.Unknown;
	private boolean allTypesExtensible;
	private Collection<String> exports = new ArrayList<>();

	@Override
	@NotNull
	public final TagMethod getTagMethod()
	{
		return tagMethod;
	}

	@Override
	public final void setTagMethod( TagMethod tagMethod )
	{
		this.tagMethod = tagMethod;
	}

	@Override
	public boolean isAllTypesExtensible()
	{
		return allTypesExtensible;
	}

	@Override
	public void setAllTypesExtensible( @SuppressWarnings( "SameParameterValue" ) boolean flag )
	{
		allTypesExtensible = flag;
	}

	@Override
	public boolean hasExports()
	{
		return exports != null && !exports.isEmpty();
	}

	@Override
	public boolean isExportAll()
	{
		return exports == null;
	}

	@Override
	public Collection<String> getExports()
	{
		return exports == null ? Collections.emptyList() : Collections.unmodifiableCollection( exports );
	}

	@Override
	public void setExports( Collection<String> exports )
	{
		this.exports = exports == null ? null : new ArrayList<>( exports );
	}

	/////////////////////////////// Type operations ////////////////////////////////////////////////////////////////////
	@NotNull
	@Override
	public Collection<DefinedType> getTypes()
	{
		return Collections.unmodifiableCollection( typeMap.values() );
	}

	@Nullable
	@Override
	public DefinedType getType( String name )
	{
		return typeMap.get( name );
	}

	@NotNull
	@Override
	public Ref<Type> getTypeRef( @NotNull String ref, @Nullable String module )
	{
//		if( StringUtils.isBlank( moduleName ) )
//			moduleName = getModuleName();
		for( Map<String, TypeNameRef> map : importedTypeMap.values() )
			for( TypeNameRef reference : map.values() )
				if( ref.equals( reference.getName() ) )
					return reference;

		String fullTypeName = module == null ? ref : module + '.' + ref;
		return referencedTypes.computeIfAbsent( fullTypeName, e -> new TypeNameRef( new TypeName( ref, module ) ) );
	}

	@Override
	public void addType( @NotNull DefinedType type )
	{
		typeMap.put( type.getName(), type );
		addDisposable( type );
	}

	@NotNull
	@Override
	public Type resolveType( @NotNull TypeName typeName ) throws ResolutionException
	{
		String moduleName = typeName.getModuleName();
		if( moduleName == null || getModuleName().equals( moduleName ) )
		{
			DefinedType type = typeMap.get( typeName.getName() );
			if( type != null )
				return type;

			if( moduleName != null )
				throw new ResolutionException( "Unable to find type: " + typeName );
		}


		if( moduleName == null )
		{
			for( Map<String, TypeNameRef> map : importedTypeMap.values() )
			{
				TypeNameRef ref = map.get( typeName.getName() );
				if( ref != null )
					return ref.resolve( resolveModuleOrDie( ref.getModuleName() ) );
			}
		}
		else
		{
			ModuleReference moduleReference = new ModuleReference( moduleName );
			Map<String, TypeNameRef> map = importedTypeMap.computeIfAbsent( moduleReference, e -> new HashMap<>() );
			TypeNameRef ref = map.get( typeName.getName() );
			if( ref != null )
				return ref.resolve( resolveModuleOrDie( ref.getModuleName() ) );

			//final chance = manual search trough modules
			Module module = resolver.resolve( moduleReference );
			return module.resolveType( typeName );
		}
		throw new ResolutionException( "Unable to find type in imports: " + typeName );
	}

	/////////////////////////////// Value operations ///////////////////////////////////////////////////////////////////

	@NotNull
	@Override
	public Collection<DefinedValue> getValues()
	{
		return Collections.unmodifiableCollection( valueMap.values() );
	}

	@Nullable
	@Override
	public DefinedValue getValue( String name )
	{
		return valueMap.get( name );
	}

	@NotNull
	@Override
	public Ref<Value> getValueRef( @NotNull String ref, @Nullable String module )
	{
//		if( StringUtils.isBlank( moduleName ) )
//			moduleName = getModuleName();
		// lets check imports first
		for( Map<String, ValueNameRef> map : importedValueMap.values() )
			for( ValueNameRef valueReference : map.values() )
				if( ref.equals( valueReference.getName() ) )
					return valueReference;

		String fullTypeName = module == null ? ref : module + '.' + ref;
		return referencedValues.computeIfAbsent( fullTypeName, e -> new ValueNameRef( new ValueName( ref, module ) ) );
	}

	@Override
	public void addValue( @NotNull DefinedValue value )
	{
		valueMap.put( value.getName(), value );
		addDisposable( value );
	}

	@Override
	public Value resolveValue( @NotNull ValueName valueName ) throws ResolutionException
	{
		String moduleName = valueName.getModuleName();
		if( moduleName == null || getModuleName().equals( moduleName ) )
		{
			DefinedValue value = valueMap.get( valueName.getName() );
			if( value != null )
				return value;

			if( moduleName != null )
				throw new ResolutionException( "Unable to find value: " + valueName );
		}

		if( moduleName == null )
		{

			for( Map<String, ValueNameRef> map : importedValueMap.values() )
			{
				ValueNameRef ref = map.get( valueName.getName() );
				if( ref != null )
					return ref.resolve( resolveModuleOrDie( ref.getModuleName() ) );
			}
		}
		else
		{
			ModuleReference moduleReference = new ModuleReference( moduleName );
			Map<String, ValueNameRef> map = importedValueMap.computeIfAbsent( moduleReference, e -> new HashMap<>() );
			ValueNameRef ref = map.get( valueName.getName() );
			if( ref != null )
				return ref.resolve( resolveModuleOrDie( ref.getModuleName() ) );
		}
		throw new ResolutionException( "Unable to resolve value: " + valueName );
	}

	/////////////////////////////// Misc operations ////////////////////////////////////////////////////////////////////
	@NotNull
	private Scope resolveModuleOrDie( String moduleName ) throws ResolutionException
	{
		if( getModuleName().equals( moduleName ) )
			throw new ResolutionException( "TypeReference must be from other module" );

		return resolver.resolve( moduleName ).createScope();
	}

	@Override
	public void dispose()
	{
		disposables.forEach( Disposable:: dispose );
		disposables.clear();
		typeMap.clear();
		valueMap.clear();
		referencedTypes.clear();
		referencedValues.clear();
		importedTypeMap.clear();
		importedValueMap.clear();
	}

	@Override
	public void addDisposable( Disposable disposable )
	{
		disposables.add( disposable );
	}

	@Override
	public final void validate( boolean types, boolean values ) throws ValidationException, ResolutionException
	{
		onValidate();

		Scope scope = createScope();
		if( types )
			for( DefinedValue value : valueMap.values() )
				value.validate( scope );

		if( values )
			for( DefinedType type : typeMap.values() )
				if( !( type instanceof Template ) )
					type.validate( scope );
	}

	protected abstract void onValidate() throws ValidationException, ResolutionException;
}
