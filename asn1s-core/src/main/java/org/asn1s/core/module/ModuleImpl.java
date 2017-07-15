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

import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.value.x680.DefinedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class ModuleImpl extends AbstractModule
{
	private static final String DUMMY = "Dummy-Module";

	public static ModuleImpl newDummy( ModuleResolver resolver )
	{
		ModuleImpl module = new ModuleImpl( new ModuleReference( DUMMY ), resolver );
		module.setTagMethod( TagMethod.Automatic );
		return module;
	}

	public ModuleImpl( @NotNull ModuleReference name, @Nullable ModuleResolver resolver )
	{
		super( name, resolver );
	}

	@Override
	public Module getCoreModule()
	{
		return CoreModule.getInstance();
	}

	@Override
	protected void onValidate()
	{
		if( DUMMY.equals( getModuleName() ) && getModuleResolver() != null && getModuleResolver().getAllModules() != null )
		{
			for( Module module : getModuleResolver().getAllModules() )
			{
				Collection<String> exports = new HashSet<>();
				for( DefinedType type : module.getTypeResolver().getTypes() )
					exports.add( type.getName() );

				for( DefinedValue value : module.getValueResolver().getValues() )
					exports.add( value.getName() );

				getTypeResolver().addImports( module.getModuleReference(), exports );
				getValueResolver().addImports( module.getModuleReference(), exports );
			}
		}
	}
}
