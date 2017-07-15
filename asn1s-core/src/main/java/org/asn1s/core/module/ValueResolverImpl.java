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

import org.asn1s.api.Disposable;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.module.ValueResolver;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueName;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x680.DefinedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class ValueResolverImpl implements ValueResolver, Disposable
{
	ValueResolverImpl( String ourModuleName, ModuleResolver resolver )
	{
		this.ourModuleName = ourModuleName;
		this.resolver = resolver;
	}

	private final String ourModuleName;
	private final ModuleResolver resolver;
	private final Map<String, DefinedValue> valueMap = new LinkedHashMap<>();
	private final Map<ModuleReference, Map<String, ValueNameRef>> importedValueMap = new HashMap<>();
	private final Map<String, ValueNameRef> referencedValues = new HashMap<>();

	@Override
	public void add( @NotNull DefinedValue value )
	{
		valueMap.put( value.getName(), value );
	}

	@Override
	public void addImports( @NotNull ModuleReference moduleReference, @NotNull Collection<String> symbols )
	{
		Map<String, ValueNameRef> valueReferenceMap = importedValueMap.computeIfAbsent( moduleReference, e -> new HashMap<>() );
		symbols.stream()
				.filter( RefUtils:: isValueRef )
				.map( e -> new ValueNameRef( e, moduleReference.getName() ) )
				.forEach( e -> valueReferenceMap.put( e.getName(), e ) );
	}

	@Override
	@Nullable
	public DefinedValue getValue( String name )
	{
		return valueMap.get( name );
	}

	@Override
	@NotNull
	public Ref<Value> getValueRef( @NotNull String ref, @Nullable String module )
	{
		// lets check imports first
		for( Map<String, ValueNameRef> map : importedValueMap.values() )
			for( ValueNameRef valueReference : map.values() )
				if( ref.equals( valueReference.getName() ) )
					return valueReference;

		String fullTypeName = module == null ? ref : module + '.' + ref;
		return referencedValues.computeIfAbsent( fullTypeName, e -> new ValueNameRef( new ValueName( ref, module ) ) );
	}

	@NotNull
	@Override
	public Collection<DefinedValue> getValues()
	{
		return Collections.unmodifiableCollection( valueMap.values() );
	}

	@Override
	@NotNull
	public Value resolve( @NotNull ValueName valueName ) throws ResolutionException
	{
		if( valueName.getModuleName() == null || ourModuleName.equals( valueName.getModuleName() ) )
		{
			DefinedValue value = valueMap.get( valueName.getName() );
			if( value != null )
				return value;

			if( valueName.getModuleName() != null )
				throw new ResolutionException( "Unable to find value: " + valueName );
		}

		if( valueName.getModuleName() == null )
			return resolveValueByImports( valueName );

		return resolveValueFromModule( valueName );
	}

	@NotNull
	private Value resolveValueByImports( ValueName valueName ) throws ResolutionException
	{
		for( Map<String, ValueNameRef> map : importedValueMap.values() )
		{
			ValueNameRef ref = map.get( valueName.getName() );
			if( ref != null )
				return ref.resolve( resolver.resolve( ref.getModuleName() ).createScope() );
		}
		throw new ResolutionException( "Unable to resolve value: " + valueName );
	}

	private Value resolveValueFromModule( ValueName valueName ) throws ResolutionException
	{
		ModuleReference moduleReference = new ModuleReference( valueName.getModuleName() );
		Map<String, ValueNameRef> map = importedValueMap.computeIfAbsent( moduleReference, e -> new HashMap<>() );
		ValueNameRef ref = map.get( valueName.getName() );
		if( ref != null )
			return ref.resolve( resolver.resolve( valueName.getModuleName() ).createScope() );
		//final chance = manual search trough modules
		Module module = resolver.resolve( moduleReference );
		return module.getValueResolver().resolve( valueName );
	}

	@Override
	public void dispose()
	{
		valueMap.clear();
		referencedValues.clear();
		importedValueMap.clear();
	}

	void validate( Scope scope ) throws ResolutionException, ValidationException
	{
		for( DefinedValue value : valueMap.values() )
			value.validate( scope );
	}
}
