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
import org.asn1s.api.module.TypeResolver;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeName;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.util.RefUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class TypeResolverImpl implements TypeResolver, Disposable
{
	TypeResolverImpl( String ourModuleName, ModuleResolver resolver )
	{
		this.ourModuleName = ourModuleName;
		this.resolver = resolver;
	}

	private final String ourModuleName;
	private final ModuleResolver resolver;
	private final Map<String, DefinedType> typeMap = new LinkedHashMap<>();
	private final Map<ModuleReference, Map<String, TypeNameRef>> importedTypeMap = new HashMap<>();
	private final Map<String, TypeNameRef> referencedTypes = new HashMap<>();

	@Override
	public void add( @NotNull DefinedType type )
	{
		typeMap.put( type.getName(), type );
	}

	@Override
	public void addImports( @NotNull ModuleReference moduleReference, @NotNull Collection<String> symbols )
	{
		Map<String, TypeNameRef> typeReferenceMap = importedTypeMap.computeIfAbsent( moduleReference, e -> new HashMap<>() );
		symbols.stream()
				.filter( RefUtils:: isTypeRef )
				.map( e -> new TypeNameRef( e, moduleReference.getName() ) )
				.forEach( e -> typeReferenceMap.put( e.getName(), e ) );
	}

	@Override
	public DefinedType getType( @NotNull String name )
	{
		return typeMap.get( name );
	}

	@NotNull
	@Override
	public Ref<Type> getTypeRef( @NotNull String ref, @Nullable String module )
	{
		for( Map<String, TypeNameRef> map : importedTypeMap.values() )
			for( TypeNameRef reference : map.values() )
				if( ref.equals( reference.getName() ) )
					return reference;

		String fullTypeName = module == null ? ref : module + '.' + ref;
		return referencedTypes.computeIfAbsent( fullTypeName, e -> new TypeNameRef( new TypeName( ref, module ) ) );
	}

	@Override
	@NotNull
	public Collection<DefinedType> getTypes()
	{
		return Collections.unmodifiableCollection( typeMap.values() );
	}

	@Override
	public void dispose()
	{
		typeMap.clear();
		referencedTypes.clear();
		importedTypeMap.clear();
	}

	@Override
	@NotNull
	public Type resolve( @NotNull TypeName typeName ) throws ResolutionException
	{
		if( typeName.getModuleName() == null || ourModuleName.equals( typeName.getModuleName() ) )
		{
			DefinedType type = typeMap.get( typeName.getName() );
			if( type != null )
				return type;

			if( typeName.getModuleName() != null )
				throw new ResolutionException( "Unable to find type: " + typeName );
		}

		if( typeName.getModuleName() == null )
			return resolveTypeByImports( typeName );

		return resolveTypeFromModule( typeName );
	}

	@NotNull
	private Type resolveTypeByImports( @NotNull TypeName typeName ) throws ResolutionException
	{
		for( Map<String, TypeNameRef> map : importedTypeMap.values() )
		{
			TypeNameRef ref = map.get( typeName.getName() );
			if( ref != null )
				return ref.resolve( resolver.resolve( ref.getModuleName() ).createScope() );
		}
		throw new ResolutionException( "Unable to find type in imports: " + typeName );
	}

	private Type resolveTypeFromModule( @NotNull TypeName typeName ) throws ResolutionException
	{
		ModuleReference moduleReference = new ModuleReference( typeName.getModuleName() );
		Map<String, TypeNameRef> map = importedTypeMap.computeIfAbsent( moduleReference, e -> new HashMap<>() );
		TypeNameRef ref = map.get( typeName.getName() );
		if( ref != null )
			return ref.resolve( resolver.resolve( typeName.getModuleName() ).createScope() );

		//final chance = manual search trough modules
		Module module = resolver.resolve( moduleReference );
		return module.getTypeResolver().resolve( typeName );
	}

	public void validate( Scope scope ) throws ResolutionException, ValidationException
	{
		for( DefinedType type : typeMap.values() )
			if( !type.isAbstract() )
				type.validate( scope );
	}
}
