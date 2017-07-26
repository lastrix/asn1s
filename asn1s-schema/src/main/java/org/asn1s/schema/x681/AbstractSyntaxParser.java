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

package org.asn1s.schema.x681;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Asn1Factory;
import org.asn1s.api.Ref;
import org.asn1s.api.UniversalType;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.type.ClassFieldType;
import org.asn1s.api.type.ClassType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.schema.Asn1ErrorListener;
import org.asn1s.schema.parser.Asn1Lexer;
import org.asn1s.schema.parser.Asn1Parser;
import org.asn1s.schema.x681.SyntaxObject.Kind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class AbstractSyntaxParser
{
	private static final Log log = LogFactory.getLog( AbstractSyntaxParser.class );

	private final ModuleResolver resolver;
	private final Asn1Factory factory;
	private final Module module;
	private final ClassType classType;
	private final GroupSyntaxObject root;
	private Map<String, Ref<?>> resultMap;
	private CommonTokenStream tokenStream;

	public AbstractSyntaxParser( ModuleResolver resolver, Asn1Factory factory, Module module, ClassType classType )
	{
		this.resolver = resolver;
		this.factory = factory;
		this.module = module;
		this.classType = classType;

		root = new GroupSyntaxObject();
		parseGroupItems( root, new LinkedList<>( classType.getSyntaxList() ), false );
	}

	public Map<String, Ref<?>> parse( String value ) throws Exception
	{
		try( Reader r = new StringReader( value ) )
		{
			TokenSource lexer = new Asn1Lexer( new ANTLRInputStream( r ) );
			tokenStream = new CommonTokenStream( lexer );
			resultMap = new HashMap<>();
			parseImpl();
			//noinspection ReturnOfCollectionOrArrayField
			return resultMap;
		} finally
		{
			tokenStream = null;
			resultMap = null;
		}
	}

	private void parseImpl()
	{
		parseTokens( root );
		Token token = tokenStream.LT( 1 );
		if( token != null && token.getType() != Recognizer.EOF )
			throw new IllegalArgumentException( "Unable to parse abstract syntax, token left: " + token );
	}

	private void parseTokens( GroupSyntaxObject root )
	{
		boolean start = true;
		List<SyntaxObject> objects = root.getObjects();
		for( SyntaxObject object : objects )
		{
			if( parseToken( start, object ) )
				break;
			start = false;
		}
	}

	private boolean parseToken( boolean start, SyntaxObject object )
	{
		if( object.getKind() == Kind.GROUP )
			parseTokens( (GroupSyntaxObject)object );
		else if( object.getKind() == Kind.KEYWORD )
			return parseKeywordToken( start, object );
		else if( object.getKind() == Kind.TYPE_FIELD )
			return parseTypeField( start, object );
		else if( object.getKind() == Kind.VALUE_FIELD )
			return parseValueField( start, object );
		return false;
	}

	private boolean parseValueField( boolean start, SyntaxObject object )
	{
		if( consumeValueField( object ) )
			return false;

		if( !start )
			throw new IllegalStateException( "Expected token does not found: " + object.getText() );
		return true;
	}

	private boolean parseTypeField( boolean start, SyntaxObject object )
	{
		if( consumeTypeField( object ) )
			return false;

		if( !start )
			throw new IllegalStateException( "Expected token does not found: " + object.getText() );
		return true;
	}

	private boolean parseKeywordToken( boolean start, SyntaxObject object )
	{
		Token token = tokenStream.LT( 1 );
		if( token.getText().equals( object.getText() ) )
		{
			tokenStream.consume();
			return false;
		}

		if( !start )
			throw new IllegalStateException( "Expected token does not found: " + object.getText() + ", got: " + token.getText() );
		return true;
	}

	private boolean consumeValueField( SyntaxObject object )
	{
		Token token = tokenStream.LT( 1 );
		ValueFactory valueFactory = factory.values();
		switch( token.getType() )
		{
			case Asn1Parser.Identifier:
				return new ReferenceParser( object, false ).tryParseReference( token );

			case Asn1Parser.OPEN_BRACE:
				return new BracedValueParser( object ).tryParseBraceValue();

			case Asn1Parser.NumberLiteral:
				registerFieldRef( object.getText(), valueFactory.integer( token.getText() ) );
				tokenStream.consume();
				return true;

			case Asn1Parser.RealLiteral:
				registerFieldRef( object.getText(), valueFactory.real( token.getText() ) );
				tokenStream.consume();
				return true;

			case Asn1Parser.HString:
				registerFieldRef( object.getText(), valueFactory.hString( token.getText() ) );
				tokenStream.consume();
				return true;

			case Asn1Parser.BString:
				registerFieldRef( object.getText(), valueFactory.bString( token.getText() ) );
				tokenStream.consume();
				return true;

			case Asn1Parser.CString:
				registerFieldRef( object.getText(), valueFactory.cString( token.getText() ) );
				tokenStream.consume();
				return true;

			default:
				return false;
		}
	}

	private boolean consumeTypeField( SyntaxObject object )
	{
		Token token = tokenStream.LT( 1 );
		switch( token.getType() )
		{
			case Asn1Parser.EMBEDDED:
				return expectNextAndRegister( Asn1Parser.PDV, object, UniversalType.EMBEDDED_PDV );

			case Asn1Parser.OCTET:
				return expectNextAndRegister( Asn1Parser.STRING, object, UniversalType.OCTET_STRING );

			case Asn1Parser.BIT:
				return expectNextAndRegister( Asn1Parser.STRING, object, UniversalType.BIT_STRING );

			case Asn1Parser.CHARACTER:
				return expectNextAndRegister( Asn1Parser.STRING, object, UniversalType.CHARACTER_STRING );

			case Asn1Parser.OBJECT:
				return expectNextAndRegister( Asn1Parser.IDENTIFIER, object, UniversalType.OBJECT_IDENTIFIER );

			case Asn1Parser.RestrictedString:
			case Asn1Parser.BOOLEAN:
			case Asn1Parser.DATE:
			case Asn1Parser.DATE_TIME:
			case Asn1Parser.DURATION:
			case Asn1Parser.EXTERNAL:
			case Asn1Parser.OID_IRI:
			case Asn1Parser.NULL:
			case Asn1Parser.REAL:
			case Asn1Parser.RELATIVE_OID_IRI:
			case Asn1Parser.RELATIVE_OID:
			case Asn1Parser.TIME:
			case Asn1Parser.TIME_OF_DAY:
			case Asn1Parser.INTEGER:
				registerFieldRef( object.getText(), UniversalType.forTypeName( token.getText() ).ref() );
				return true;

			case Asn1Parser.Identifier:
				return new ReferenceParser( object, true ).tryParseReference( token );

			default:
				throw new IllegalArgumentException( token.getText() );
		}
	}

	private boolean expectNextAndRegister( int tokenType, SyntaxObject object, UniversalType type )
	{
		Token nextToken = tokenStream.LT( 2 );
		if( nextToken.getType() != tokenType )
			throw new IllegalStateException( "Expected token does not found" );
		tokenStream.consume();
		tokenStream.consume();
		registerFieldRef( object.getText(), type.ref() );
		return true;
	}

	private void registerFieldRef( String name, Ref<?> ref )
	{
		if( resultMap.containsKey( name ) )
			throw new IllegalStateException( "Trying to redefine value for field: " + name );

		resultMap.put( name, ref );
	}

	private static void parseGroupItems( GroupSyntaxObject root, Queue<String> list, boolean expectCloseBracket )
	{
		while( !list.isEmpty() )
		{
			String item = list.poll();
			if( item.startsWith( "&" ) )
				root.addObject( new SimpleSyntaxObject( Character.isUpperCase( item.charAt( 1 ) ) ? Kind.TYPE_FIELD : Kind.VALUE_FIELD, item ) );
			else if( item.equals( "[" ) )
			{
				GroupSyntaxObject object = new GroupSyntaxObject();
				root.addObject( object );
				parseGroupItems( object, list, true );
			}
			else if( item.equals( "]" ) )
			{
				if( !expectCloseBracket )
					throw new IllegalStateException( "Unexpected close bracket!" );
				break;
			}
			else
			{
				root.addObject( new SimpleSyntaxObject( Kind.KEYWORD, item ) );
			}
		}
	}

	@SuppressWarnings( "NonStaticInnerClassInSecureContext" )
	private final class BracedValueParser
	{

		private final SyntaxObject object;
		private int level;
		private int position = 2;
		private final StringBuilder sb;

		private BracedValueParser( SyntaxObject object )
		{
			this.object = object;
			sb = new StringBuilder( "{ " );
		}

		private boolean tryParseBraceValue()
		{
			// consume all tokens up to }
			Token token = tokenStream.LT( position );
			while( token != null )
			{
				if( parseToken( token ) )
					return consumeBracedValue( sb );
				token = tokenStream.LT( position );
			}

			return false;
		}

		private boolean parseToken( Token token )
		{
			switch( token.getType() )
			{
				case Asn1Parser.CLOSE_BRACE:
					if( level == 0 )
						return true;
					level--;
					break;

				case Asn1Parser.OPEN_BRACE:
					level++;
					break;

				default:
			}

			sb.append( token.getText() ).append( ' ' );
			position++;
			return false;
		}

		private boolean consumeBracedValue( StringBuilder sb )
		{
			sb.append( '}' );
			if( parseBracedValue( sb.toString(), object ) )
			{
				while( position > 0 )
				{
					tokenStream.consume();
					position--;
				}
				return true;
			}
			return false;
		}

		private boolean parseBracedValue( String value, SyntaxObject object )
		{
			ClassFieldType field = classType.getField( object.getText() );
			assert field != null;
			boolean isOid = field.getFamily() == Family.OID;

			try( Reader r = new StringReader( value ) )
			{
				parseBracedValueImpl( object, isOid, r );
				return true;
			} catch( Exception e )
			{
				if( log.isDebugEnabled() )
					log.debug( "Unable to parse value: " + value, e );
				return false;
			}
		}

		private void parseBracedValueImpl( SyntaxObject object, boolean isOid, Reader r ) throws IOException
		{
			TokenSource lexer = new Asn1Lexer( new ANTLRInputStream( r ) );
			CommonTokenStream valueTokenStream = new CommonTokenStream( lexer );
			Asn1Parser parser = new Asn1Parser( valueTokenStream, resolver, factory );
			parser.addErrorListener( new Asn1ErrorListener() );
			parser.setBuildParseTree( false );
			parser.setModule( module );
			parser.getInterpreter().setPredictionMode( PredictionMode.SLL );

			if( isOid )
				registerFieldRef( object.getText(), parser.objectIdentifierValue().result );
			else
				registerFieldRef( object.getText(), parser.value().result );
		}
	}

	@SuppressWarnings( "NonStaticInnerClassInSecureContext" )
	private final class ReferenceParser
	{
		private final SyntaxObject object;
		private final boolean typeOrValueReference;

		private ReferenceParser( SyntaxObject object, boolean typeOrValueReference )
		{
			this.object = object;
			this.typeOrValueReference = typeOrValueReference;
		}

		private boolean tryParseReference( Token token )
		{
			if( !RefUtils.isValueRef( token.getText() ) && !RefUtils.isTypeRef( token.getText() ) )
				return false;

			Token nextToken = tokenStream.LT( 2 );
			if( nextToken != null && nextToken.getType() == Asn1Parser.DOT )
				return tryParseExternalReference( token );

			registerFieldRef( object.getText(), createReference( null, token ) );
			tokenStream.consume();
			return true;
		}

		private boolean tryParseExternalReference( Token token )
		{
			Token nextToken = tokenStream.LT( 3 );
			return nextToken != null
					&& nextToken.getType() == Asn1Parser.Identifier
					&& parseExternalReference( token, nextToken );
		}

		private boolean parseExternalReference( Token token, Token nextToken )
		{
			if( !RefUtils.isValueRef( token.getText() ) )
				return false;

			tokenStream.consume();
			tokenStream.consume();
			tokenStream.consume();
			registerFieldRef( object.getText(), createReference( token, nextToken ) );
			return true;
		}

		@NotNull
		private Ref<?> createReference( @Nullable Token moduleToken, @NotNull Token referenceToken )
		{
			if( RefUtils.isValueRef( referenceToken.getText() ) )
			{
				if( typeOrValueReference )
					throw new IllegalArgumentException( "Type reference expected" );
				return new ValueNameRef( referenceToken.getText(), moduleToken == null ? null : moduleToken.getText() );
			}

			if( !typeOrValueReference )
				throw new IllegalArgumentException( "Value reference expected" );
			return new TypeNameRef( referenceToken.getText(), moduleToken == null ? null : moduleToken.getText() );
		}
	}
}
