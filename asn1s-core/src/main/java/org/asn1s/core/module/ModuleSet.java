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

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.Disposable;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.EmptyModuleResolver;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.core.DefaultAsn1Factory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class ModuleSet implements ModuleResolver, Disposable, Iterable<Module>
{
	public ModuleSet()
	{
		resolver = new EmptyModuleResolver();
	}

	@SuppressWarnings( "unused" )
	public ModuleSet( ModuleResolver resolver )
	{
		this.resolver = resolver;
	}

	private final ModuleResolver resolver;
	private final Map<String, Module> moduleMap = new HashMap<>();

	@NotNull
	@Override
	public Module resolve( ModuleReference reference ) throws ResolutionException
	{
		Module module = moduleMap.get( reference.getName() );
		if( module != null )
			return module;

		module = resolver.resolve( reference );
		registerModule( module );
		return module;
	}

	@NotNull
	@Override
	public Iterator<Module> iterator()
	{
		return moduleMap.values().iterator();
	}

	@Override
	public void forEach( Consumer<? super Module> action )
	{
		moduleMap.values().forEach( action );
	}

	public boolean isEmpty()
	{
		return moduleMap.isEmpty();
	}

	@Override
	public Spliterator<Module> spliterator()
	{
		return moduleMap.values().spliterator();
	}

	@Override
	public void dispose()
	{
		moduleMap.values().forEach( Disposable:: dispose );
		moduleMap.clear();
	}

	@Override
	public void registerModule( Module module )
	{
		moduleMap.put( module.getModuleName(), module );
	}

	public void validate() throws ValidationException, ResolutionException
	{
		for( Module module : moduleMap.values() )
			module.validate();
	}

	@Override
	public Collection<Module> getAllModules()
	{
		return Collections.unmodifiableCollection( moduleMap.values() );
	}

	@Override
	public Asn1Factory createObjectFactory()
	{
		return new DefaultAsn1Factory( this );
	}
}
