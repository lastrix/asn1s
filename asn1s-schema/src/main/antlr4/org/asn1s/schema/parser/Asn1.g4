grammar Asn1;

@header {
import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.*;
import org.asn1s.api.constraint.*;
import org.asn1s.api.encoding.*;
import org.asn1s.api.encoding.tag.*;
import org.asn1s.api.module.*;
import org.asn1s.api.type.*;
import org.asn1s.api.util.*;
import org.asn1s.api.value.*;
import org.asn1s.api.value.x680.*;
import org.asn1s.api.value.x681.*;
import org.asn1s.schema.x681.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
}

@parser::members {
    public Asn1Parser(TokenStream input, ModuleResolver resolver, ObjectFactory factory) {
        this(input);
        this.resolver = resolver;
        this.factory = factory;
    }

    public Asn1Parser(TokenStream input, ModuleResolver resolver, ObjectFactory factory, ClassType classType) {
        this(input);
        this.resolver = resolver;
        this.factory = factory;
        this.classType = classType;
    }

	private ModuleResolver resolver = new EmptyModuleResolver();
	private ObjectFactory factory;
	private ClassType classType;

	private String tokens2string( @NotNull Token start, @NotNull Token stop )
	{
        StringBuilder sb = new StringBuilder(  );
        sb.append( start.getText() );
        for(int i = start.getTokenIndex()+1; i <= stop.getTokenIndex(); i++ )
            sb.append( ' ' ).append( _input.get( i ).getText() );

        return sb.toString();
	}

	boolean isSimpleString()
	{
		String text = _input.LT( 1 ).getText();
		int idx = text.indexOf( '"' );
		int length = text.length();
		if( idx > 0 && idx < length - 1 )
			return false;

		for( int i = 1; i < length - 1; i++ )
		{
			char c = text.charAt( i );
			//noinspection MagicNumber
			if( c >= 32 && c <= 126 || c == '\r' || c == '\n' || c == '\t' )
				continue;
			return false;
		}
		return true;
	}

	boolean isTokenFollows( String text, int count )
	{
		int previous = -1;
		for( int i = 0; i < count; i++ )
		{
			Token token = _input.LT( i + 1 );
			if( !token.getText().equals( text ) )
				return false;

			if( previous != -1 && previous + 1 != token.getCharPositionInLine() )
				return false;

			previous = token.getCharPositionInLine();
		}
		return true;
	}

    public static boolean isObjectClassReference( String name )
    {
        int size = name.length();
        for( int i = 0; i < size; i++ )
        {
            char c = name.charAt( i );
            if( c != '-' && !Character.isDigit( c ) && !Character.isUpperCase( c ) )
                return false;
        }
        return true;
    }

	////////////////////////////////////// Reference management ////////////////////////////////////////////////////////
	public Ref<Type> getTypeRef( String name, String moduleName )
	{
		RefUtils.assertTypeRef( name );
		if( moduleName != null )
			RefUtils.assertTypeRef( moduleName );

		return getModule().getTypeResolver().getTypeRef( name, moduleName );
	}

	public Ref<Value> getValueRef( String name, String moduleName )
	{
		RefUtils.assertValueRef( name );
		if( moduleName != null )
			RefUtils.assertTypeRef( moduleName );

		return getModule().getValueResolver().getValueRef( name, moduleName );
	}

	////////////////////////////////////// Module setup ////////////////////////////////////////////////////////////////
	Module setupModule(ModuleReference name)
	{
		if( module != null )
			throw new IllegalStateException( "Current module is not null" );

		module = name == null ? factory.dummyModule() : factory.module(name);
		return module;
	}

	void notifyModuleComplete(boolean registerModule)
	{
		if( module == null )
			throw new IllegalStateException();

        if ( registerModule )
		    resolver.registerModule( module );
		module = null;
		factory.setModule(null);
	}

	Module getModule()
	{
		return module;
	}

    public void setModule(Module module)
    {
        this.module = module;
    }

	private Module module;
}

startStmt returns [List<Module> result]
    @init { $result = new ArrayList<>(); }
    :   (moduleDefinition { $result.add($moduleDefinition.result); })*
    ;

pduStmt returns [Module result]
    @init  { $result = setupModule(null); }
    @after {  notifyModuleComplete(false);}
    :
        (
            valueAssignment
        //|   value     { $result = (Value)$value.result; }
        )+
    ;

//typeStmt returns [Type result]
//    :   { setupModule(null); }
//        typeAssignment { $result = $typeAssignment.result; }
//        { notifyModuleComplete(false); }
//    ;

// X.680, p 13.1
moduleDefinition returns [Module result]
    :
        moduleReference
        { $result = setupModule($moduleReference.result); }
        DEFINITIONS
        moduleEncodingInstructions?
        moduleTaggingMethod?
        moduleExtensibilityImplied?
        ASSIGN BEGIN
        moduleExports?
        moduleImports?
        definition*
        END
        { notifyModuleComplete(true); }
    ;

moduleEncodingInstructions
    :   // Encoding instructions, module default is Tag
        encodingReference INSTRUCTIONS
        //TODO:{ getModule().setDefaultEncodingInstructions($encodingReference.text); }
    ;

moduleTaggingMethod
    :   // Tagging method, module default is Unknown
        taggingMethod TAGS
        { getModule().setTagMethod($taggingMethod.result); }
    ;

moduleExtensibilityImplied
    :   // if types are extensible, module default is false
        EXTENSIBILITY IMPLIED
        { getModule().setAllTypesExtensible(true); }
    ;

moduleExports
    :   EXPORTS
        (   ALL { getModule().setExports(null); }
        |   symbolList { getModule().setExports( $symbolList.result ); }
        )?
        SEMI
    ;

moduleImports
    :   IMPORTS
        (
           symbolList FROM globalModuleReference
           {
                getModule().getTypeResolver().addImports($globalModuleReference.result, $symbolList.result );
                getModule().getValueResolver().addImports($globalModuleReference.result, $symbolList.result );
           }
        )*
        SEMI
    ;

moduleReference returns [ModuleReference result]
    :   typeReference
        { $result = new ModuleReference($typeReference.text); }
        (
            OPEN_BRACE (oidComponent )* CLOSE_BRACE
            iriValueString?
        )?
    ;

globalModuleReference returns [ModuleReference result]
	:	typeReference
        (
            OPEN_BRACE ~CLOSE_BRACE* CLOSE_BRACE
        |   definedValue
        )?
        { $result = new ModuleReference($typeReference.text); }
	;


oidComponent
	:
		identifier ( OPEN_PAREN number CLOSE_PAREN)?
	|   number
	;

symbolList returns [List<String> result]
    @init{ $result = new ArrayList<>(); }
	:
		symbol
		{ $result.add($symbol.result); }
		(
		    COMMA symbol
		    { $result.add($symbol.result); }
		)*
	;

symbol returns [String result]
	:   reference (OPEN_BRACE CLOSE_BRACE)? { $result = $reference.text; }
	//|   parameterizedReference // TODO: parameterizedReference
	;

definition
    :   typeAssignment
    |   objectOrValueAssignment
    //|   objectAssignment
    |   objectSetAssignment
    |   valueSetTypeAssignment
    |   objectClassAssignment
    //|   xmlValueAssignment TODO: xmlValueAssignment
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Types ///////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// X.680, p 16.1
typeAssignment returns [DefinedType result]
	:
	    typeReference
	    params = templateTypeParameterList?
		ASSIGN type
		{ $result = factory.define($typeReference.text, $type.result, $params.ctx == null ? null : $params.result); }
	;

// X.680, p17.1
type returns [Ref<Type> result]
    :
    (   taggedType              { $result = $taggedType.result; }
    |   specialBuiltinType      { $result = $specialBuiltinType.result; }
	|   builtinType             { $result = factory.builtin($builtinType.text);  }
	|   enumeratedType          { $result = $enumeratedType.result; }
	|   instanceOfType          { $result = $instanceOfType.result; }
	|   objectClassFieldType    { $result = $objectClassFieldType.result; }
	|   collectionType          { $result = $collectionType.result; }
	|   collectionOfType        { $result = $collectionOfType.result; }
	|   choiceType              { $result = $choiceType.result; }
	|   referencedType          { $result = $referencedType.result; }
	)
	(
	    constraint
	    {
	        if ( $constraint.result != null )
    	        $result = factory.constrained($constraint.result, $result);
	    }
	)?
    ;

// X.680, p 31
// X.680, p 31.2
// X.680, p 31.3
taggedType returns [Type result]
    locals
    [
        EncodingInstructions instructions = EncodingInstructions.Tag,
        TagClass typeTagClass = TagClass.ContextSpecific,
        TagMethod typeTagMethod = TagMethod.Unknown,
        Ref<Value> tagNumberRef = null
    ]
	:   OPEN_BRACKET
		(
			encodingReference COLON
			{
			    $instructions = EncodingInstructions.find($encodingReference.text);
			    if ( $instructions != EncodingInstructions.Tag )
			      throw new UnsupportedOperationException();
			}
		)?
	    (   tagClass { $typeTagClass = $tagClass.result; } )?

	    (
	        integerValue
	        { $tagNumberRef = $integerValue.result; }

	    |   definedValue
	        { $tagNumberRef = $definedValue.result; }
	    )

		CLOSE_BRACKET

        (
            IMPLICIT
            { $typeTagMethod = TagMethod.Implicit; }

        |   EXPLICIT
            { $typeTagMethod = TagMethod.Explicit; }
        )?

        type
        {
            $result =
                factory.tagged( factory.tagEncoding( $typeTagMethod, $typeTagClass, $tagNumberRef ), $type.result );
        }
	;

builtinType
	:
	// X.680, p 18
	    BOOLEAN
    // X.680, p 38.4.1
	|   DATE
    // X.680, p 38.4.3
	|   DATE_TIME
    // X.680, p 38.4.4
	|   DURATION
    // X.680, p 36
	|   EMBEDDED PDV
    // X.680, p 37
	|   EXTERNAL
    // X.680, p 34
	|   OID_IRI
    // X.680, p 24
	|   NULL
    // X.680, p 23
	|   OCTET STRING
    // X.680, p 21
	|   REAL
    // X.680, p 35
	|   RELATIVE_OID_IRI
    // X.680, p 33
	|   RELATIVE_OID
    // X.680, p 38.1.1
	|   TIME
    // X.680, p 38.4.2
	|   TIME_OF_DAY
    // X.680, p 32.1
	|   OBJECT IDENTIFIER
    // X.680 p. 22
    |   BIT STRING

    // X.680, p 19
	|   INTEGER

    // X.680, p 40; X.680, p 44.1,  X.680, p 41
	|   RestrictedString
    |   CHARACTER STRING
	;

specialBuiltinType returns[Ref<Type> result]
	:   // X.680 p. 22
	    specialBuiltinTypeNames
	    OPEN_BRACE integerTypeValueList CLOSE_BRACE
	    { $result = factory.builtin($specialBuiltinTypeNames.text, $integerTypeValueList.result); }
    ;

specialBuiltinTypeNames
    :   // X.680 p. 22
        BIT STRING

        // X.680, p 19
    |   INTEGER
    ;

instanceOfType returns [Ref<Type> result]
    :   INSTANCE OF definedObjectClass { $result = factory.instanceOf($definedObjectClass.result); }
    ;

// X.681, p 14.1
objectClassFieldType returns [Ref<Type> result]
    :   typeFromObject { $result = $typeFromObject.result; }
    ;

// X.680, p 17.3
referencedType returns [Ref<Type> result]
	:   definedType         { $result = $definedType.result; }
	|   UsefullType         { $result = factory.builtin($UsefullType.text); }
	|   selectionType       { $result = $selectionType.result; }
	|   typeFromObject      { $result = $typeFromObject.result; }
	|   valueSetFromObjects { $result = $valueSetFromObjects.result; }
	;

// X.680, p 14.1
definedType returns [Ref<Type> result]
	:   // X.680, p 14.6
	    (mr = typeReference DOT)? tr = typeReference args=actualTemplateTypeParameterList?
        {
            if ( $args.ctx == null )
                $result = getTypeRef($tr.text, $mr.text);
            else
                $result = factory.typeTemplateInstance( getTypeRef($tr.text, $mr.text), $args.result );
        }
	;

// X.680, p 30.1
selectionType returns [Type result]
	:   valueReference LT type
	    { $result = factory.selectionType( $valueReference.text, $type.result ); }
	;

// X.680, p 20.1
enumeratedType returns [Enumerated result]
    @init { $result = factory.enumerated(); }
	:   ENUMERATED
		OPEN_BRACE
	    enumValueList[$result, Enumerated.ItemKind.Primary]
		(
			COMMA ELLIPSIS
			{ $result.setExtensible(true); }
			exceptionSpec?
			(
			    COMMA
				enumValueList[$result, Enumerated.ItemKind.Extension]
			)?
		)?
		CLOSE_BRACE
	;

// X.680, p 25.1
// X.680, p 27.1
collectionType returns [CollectionType result]
    :
        (
            SEQUENCE { $result = factory.collection(Type.Family.Sequence); }
        |   SET      { $result = factory.collection(Type.Family.Set); }
        )
        OPEN_BRACE
        (
            ELLIPSIS
            exceptionSpec?
            ( COMMA collectionExtensionComponents[$result] )?
            (
                COMMA ELLIPSIS
                COMMA collectionComponentTypeList[$result, ComponentType.Kind.Secondary]
            |   COMMA ELLIPSIS { $result.setExtensible(true); }
            )?
        |   collectionComponentTypeList[$result, ComponentType.Kind.Primary]
            (
                COMMA ELLIPSIS exceptionSpec?
                ( COMMA collectionExtensionComponents[$result] )?
                (
                    COMMA ELLIPSIS
                    COMMA collectionComponentTypeList[$result, ComponentType.Kind.Secondary]
                |   COMMA ELLIPSIS { $result.setExtensible(true); }
                )?
            |   COMMA ELLIPSIS { $result.setExtensible(true); }
            )?
        )?
        CLOSE_BRACE
    ;

// X.680, p 26.1
// X.680, p 28.1
collectionOfType returns [Ref<Type> result]
    locals [
        CollectionOfType actualType,
        ConstraintTemplate constraintTemplate = null,
        String componentName = ComponentType.DUMMY ]
    :
    (
        SEQUENCE { $actualType = (CollectionOfType)factory.collectionOf(Type.Family.SequenceOf); }
    |   SET      { $actualType = (CollectionOfType)factory.collectionOf(Type.Family.SetOf); }
    )
    { $result = $actualType; }
    (
        (   constraint { $constraintTemplate = $constraint.result; }
        |   sizeConstraint  { $constraintTemplate = $sizeConstraint.result; }
        )
        { $result = factory.constrained($constraintTemplate, $actualType); }
    )?
    OF
    (valueReference { $componentName = $valueReference.text; } )?
    type
    { $actualType.setComponent( $componentName, $type.result ); }
    ;

// X.680, p 29
choiceType returns [CollectionType result]
    @init { $result = factory.collection(Type.Family.Choice); }
	:   CHOICE
		OPEN_BRACE
		choiceComponentTypeList[$result]
		(
            COMMA ELLIPSIS { $result.setExtensible(true); }
            exceptionSpec?
			( COMMA choiceExtensionComponentTypeList[$result] )?
			( COMMA ELLIPSIS )?
		)?
		CLOSE_BRACE
	;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Misc ////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
templateTypeParameterList returns [List<TemplateParameter> result]
    @init { $result = new ArrayList<>(); }
    :
        OPEN_BRACE
        templateTypeParameter[$result]
        ( COMMA templateTypeParameter[$result] )*
        CLOSE_BRACE
    ;

templateTypeParameter[List<TemplateParameter> paramList]
    :
        templateTypeParameterGovernor?
        Identifier
        { $paramList.add(factory.templateParameter( paramList.size(), $Identifier.getText(), $templateTypeParameterGovernor.governor )); }
    ;

templateTypeParameterGovernor returns [Ref<Type> governor]
    :   (
            type                { $governor = $type.result; }
        |   definedObjectClass  { $governor = $definedObjectClass.result; }
        )
        COLON
    ;

actualTemplateTypeParameterList returns[List<Ref<?>> result]
    @init { $result = new ArrayList<>(); }
    :   OPEN_BRACE
        (
            actualTemplateTypeParameter[$result]
            ( COMMA actualTemplateTypeParameter[$result] )*
        )?
        CLOSE_BRACE
    ;

actualTemplateTypeParameter[List<Ref<?>> result]
    :   definedType          { $result.add($definedType.result); }
    |   OPEN_BRACE definedType CLOSE_BRACE { $result.add($definedType.result); }
    |   definedValue         { $result.add($definedValue.result); }
    |   builtinScalarValue   { $result.add($builtinScalarValue.result); }
    ;

integerTypeValueList returns [List<NamedValue> result]
    @init{ $result = new ArrayList<>(); }
    :   integerTypeValue[$result]
        ( COMMA integerTypeValue[$result] )*
    ;

integerTypeValue[List<NamedValue> valueList]
    :   valueReference OPEN_PAREN number CLOSE_PAREN
        { $valueList.add( factory.named($valueReference.text, factory.integer($number.text)) ); }

    |   valueReference OPEN_PAREN definedValue CLOSE_PAREN
        { $valueList.add( factory.named($valueReference.text, $definedValue.result) ); }
    ;

enumValueList[Enumerated enumeration, Enumerated.ItemKind itemKind ]
    :
        enumValue[$enumeration, $itemKind]
        ( COMMA enumValue[$enumeration, $itemKind] )*
    ;

enumValue[Enumerated enumeration, Enumerated.ItemKind itemKind ]
    :
        valueReference OPEN_PAREN integerValue CLOSE_PAREN
        { $enumeration.addItem($itemKind, $valueReference.text, $integerValue.result); }
    |   valueReference OPEN_PAREN definedValue CLOSE_PAREN
        { $enumeration.addItem($itemKind, $valueReference.text, $definedValue.result); }
    |   valueReference
        { $enumeration.addItem($itemKind, $valueReference.text, null); }
    ;

exceptionSpec
	:
	    EXCLAMATION
	    (
	        number
	    |   definedValue
	    |   type COLON value
	    )
	;

collectionComponentTypeList[ComponentTypeConsumer consumer, ComponentType.Kind componentKind]
    :   collectionComponentType[$consumer, $componentKind]
        ( COMMA collectionComponentType[$consumer, $componentKind] )*
    ;

collectionExtensionComponents[ComponentTypeConsumer consumer]
    :   collectionExtensionComponent[$consumer]
        ( COMMA collectionExtensionComponent[$consumer] )*
    ;

collectionExtensionComponent[ComponentTypeConsumer consumer]
    :   collectionComponentType[$consumer, ComponentType.Kind.Extension]
    |   collectionExtensionAdditionGroup[$consumer]
    ;

collectionExtensionAdditionGroup[ComponentTypeConsumer consumer]
    :   { isTokenFollows("[", 2) }? OPEN_BRACKET OPEN_BRACKET
        { CollectionTypeExtensionGroup extGroup = factory.extensionGroup(((Type)consumer).getFamily()); }
        ( number COLON { extGroup.setVersion(Integer.parseInt($number.text)); } )?
        collectionComponentType[extGroup, ComponentType.Kind.Extension]
        ( COMMA collectionComponentType[extGroup, ComponentType.Kind.Extension])*
        { isTokenFollows("]", 2) }? CLOSE_BRACKET CLOSE_BRACKET
        { $consumer.addExtensionGroup( extGroup ); }
    ;

collectionComponentType[ComponentTypeConsumer consumer, ComponentType.Kind componentKind]
    locals[ boolean optional = false, Ref<Value> defaultValueRef = null ]
    :
        valueReference type
        (
            OPTIONAL { $optional = true; }
        |   DEFAULT value { $defaultValueRef = $value.result; }
        )?
        { consumer.addComponent(componentKind, $valueReference.text, $type.result, $optional, $defaultValueRef); }

    |   COMPONENTS OF type
        { $consumer.addComponentsFromType($componentKind, $type.result); }
    ;

choiceComponentTypeList[ComponentTypeConsumer consumer]
    :   choiceComponentType[$consumer, ComponentType.Kind.Primary]
        ( COMMA choiceComponentType[$consumer, ComponentType.Kind.Primary] )*
    ;

// X.680, p 29.1
choiceExtensionComponentTypeList[ComponentTypeConsumer consumer]
    :   choiceExtensionComponentType[$consumer]
        ( COMMA choiceExtensionComponentType[$consumer] )*
    ;

choiceComponentType[ComponentTypeConsumer target, ComponentType.Kind componentKind]
    :
        valueReference type
        { $target.addComponent(componentKind, $valueReference.text, $type.result, false, null ); }
    ;

// X.680, p 29.1
choiceExtensionComponentType[ComponentTypeConsumer consumer]
    :   choiceComponentType[$consumer, ComponentType.Kind.Extension]
    |   choiceExtensionAdditionGroup[$consumer]
    ;

// X.680, p 29.1
choiceExtensionAdditionGroup[ComponentTypeConsumer consumer]
    :   { isTokenFollows("[", 2) }? OPEN_BRACKET OPEN_BRACKET
        { CollectionTypeExtensionGroup extGroup = factory.extensionGroup(((Type)consumer).getFamily()); }
        ( number COLON { extGroup.setVersion(Integer.parseInt($number.text)); } )?
        choiceComponentType[extGroup, ComponentType.Kind.Extension]
        ( COMMA choiceComponentType[extGroup, ComponentType.Kind.Extension] )*
        { isTokenFollows("]", 2) }? CLOSE_BRACKET CLOSE_BRACKET
        { $consumer.addExtensionGroup( extGroup ); }
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Values //////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// X.680, p 16.2
valueAssignment returns [DefinedValue result]
	:
	    valueReference
        params=templateTypeParameterList?
	    OBJECT IDENTIFIER ASSIGN objectIdentifierValue
	    { $result = factory.define( $valueReference.text, factory.builtin("OBJECT IDENTIFIER" ), $objectIdentifierValue.result, $params.ctx == null ? null : $params.result ); }

	|   valueReference
        params=templateTypeParameterList?
	    type ASSIGN value
	    { $result = factory.define( $valueReference.text, $type.result, $value.result, $params.ctx == null ? null : $params.result ); }
	;

objectOrValueAssignment returns [DefinedValue result]
	:
    valueReference params=templateTypeParameterList?
    (
	    OBJECT IDENTIFIER ASSIGN objectIdentifierValue
	    { $result = factory.define( $valueReference.text, factory.builtin("OBJECT IDENTIFIER" ), $objectIdentifierValue.result, $params.ctx == null ? null : $params.result ); }

	|   definedObjectClass ASSIGN object
	    { $result = factory.define( $valueReference.text, $definedObjectClass.result, $object.result, $params.ctx == null ? null : $params.result ); }

	|   type ASSIGN value
	    { $result = factory.define( $valueReference.text, $type.result, $value.result, $params.ctx == null ? null : $params.result ); }
	)
	;

valueSetTypeAssignment returns[DefinedType result]
	:
	    typeReference
	    templateTypeParameterList?
        type ASSIGN valueSet
        {
            Type constrained = factory.constrained($valueSet.result, $type.result);
            $result = factory.define(
                                $typeReference.text,
                                constrained,
                                $templateTypeParameterList.ctx == null ? null : $templateTypeParameterList.result);
        }
	;

// X.680, p 17.7
value returns [Ref<Value> result]
	:   builtinScalarValue      { $result = $builtinScalarValue.result; }
	|   builtinValue            { $result = $builtinValue.result; }

    // X.680, p 17.11 referenced values
	|   definedValue        { $result = $definedValue.result; }
	|   valueFromObject
	|   objectClassFieldValue   { $result = $objectClassFieldValue.result; }
	;

objectClassFieldValue returns [Value result]
    :   type COLON value { $result = factory.openTypeValue($type.result, $value.result); }
    ;

// X.680, p 16.7
valueSet returns [ConstraintTemplate result]
    :   OPEN_BRACE
        elementSetSpecs
        CLOSE_BRACE
        { $result = $elementSetSpecs.result; }
    ;

builtinValue returns [Value result]
	:
	// X.680, p 23
        namedValueCollection    { $result = $namedValueCollection.result; }
    |   valueCollection         { $result = $valueCollection.result; }
	|   choiceValue             { $result = $choiceValue.result; }
	|   objectIdentifierValue   { $result = $objectIdentifierValue.result; }
	//|   CONTAINING value        { /*$result = new ContainingValue( $value.result );*/ }// TODO: CONTAINING value
	;

builtinScalarValue returns [Value result]
    :   CString                 { $result = factory.cString($CString.text); }
    |   HString                 { $result = factory.hString($HString.text); }
    |   BString                 { $result = factory.bString($BString.text); }
    |   integerValue            { $result = $integerValue.result; }
    // X.680, p 12.9
    |   RealLiteral             { $result = factory.real($RealLiteral.text); }
    |   SpecialRealValue        { $result = factory.real($SpecialRealValue.text); }
    |   TRUE                    { $result = BooleanValue.TRUE; }
    |   FALSE                   { $result = BooleanValue.FALSE; }
	|   NULL                    { $result = NullValue.INSTANCE; }
    ;

definedValue returns [Ref<Value> result]
	:   // X.680, p 14
	    // X.680, p 14.6
	    (mr=typeReference DOT)? valueReference args=actualTemplateTypeParameterList?
	    {
	        if ( $args.ctx == null )
	            $result = getValueRef($valueReference.text, $mr.text);
	        else
	            $result = factory.valueTemplateInstance(getValueRef($valueReference.text, $mr.text), $args.ctx == null ? null : $args.result);
	    }
	;

// X.680, p 12.8
integerValue returns [Value result]
    :   NumberLiteral     { $result = factory.integer( $NumberLiteral.text ); }
    ;

choiceValue returns [Value result]
	:   valueReference COLON value
	    { $result = factory.named($valueReference.text, $value.result); }
	;

valueCollection returns [ValueCollection result]
    @init { $result = factory.collection(false); }
    :
        OPEN_BRACE
        (
            value
            { $result.add($value.result); }
            (
                COMMA value
                { $result.add($value.result); }
            )*
        )?
        CLOSE_BRACE
    ;

namedValueCollection returns [ValueCollection result]
    @init { $result = factory.collection(true); }
    :
        OPEN_BRACE
        (
            valueReference value
            { $result.addNamed($valueReference.text, $value.result); }
            (
                COMMA valueReference value
                { $result.addNamed($valueReference.text, $value.result); }
            )*
        )?
        CLOSE_BRACE
    ;

objectIdentifierValue returns [Value result]
    locals [ List<Ref<Value>> values = new ArrayList<>(); ]
    @after { $result = factory.objectIdentifier($values); }
    :
        OPEN_BRACE
        (objectIdentifierValueItem { $values.add($objectIdentifierValueItem.result); } )*
        CLOSE_BRACE
    ;

objectIdentifierValueItem returns [Ref<Value> result]
    :   integerValue { $result = $integerValue.result; }
    |   valueReference OPEN_PAREN integerValue CLOSE_PAREN { $result = factory.named($valueReference.text, $integerValue.result); }
    |   definedValue { $result = $definedValue.result; }
    ;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Constraints /////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// X.680, p 49.6
constraint returns [ConstraintTemplate result]
    :   OPEN_PAREN
        (   generalConstraint { $result = $generalConstraint.result; }
        |   elementSetSpecs   { $result = $elementSetSpecs.result; }
        )
        CLOSE_PAREN
    ;

generalConstraint returns [ConstraintTemplate result]
    :   userDefinedConstraint { $result = $userDefinedConstraint.result; }
    |   tableConstraint { $result = $tableConstraint.result; }
    |   contentsConstraint { $result = $contentsConstraint.result; }
    ;

// X.682, p 9.1
userDefinedConstraint returns [ConstraintTemplate result]
    :   CONSTRAINED BY OPEN_BRACE braceConsumer CLOSE_BRACE
        { $result = null; }
        // TODO: not implemented
    ;

braceConsumer
	:
		OPEN_BRACE ( ~(OPEN_BRACE|CLOSE_BRACE) | braceConsumer )* CLOSE_BRACE
	;

// X.682, p 10.3
tableConstraint returns[ConstraintTemplate result]
	:
	    OPEN_BRACE definedObjectSet CLOSE_BRACE
	    (OPEN_BRACE relationItemList CLOSE_BRACE)?
	    { $result = factory.tableConstraint($definedObjectSet.result, $relationItemList.ctx == null ? null : $relationItemList.result); }
	;

relationItemList returns[List<RelationItem> result]
    @init { $result = new ArrayList<>(); }
    @after { if ( $result.isEmpty() ) $result = null; }
    :
        relationItem { $result.add($relationItem.result); }
        (COMMA relationItem { $result.add($relationItem.result); })*
    ;

relationItem returns[RelationItem result]
    locals[ int level = 0, List<String> path = new ArrayList<>() ]
    :   AT
        (DOT { $level++; } )*
        (identifier DOT { $path.add($identifier.text); } )*
        identifier
        { $result = new RelationItem($identifier.text, $path, $level); }
    ;

// X.682, p 11.1
contentsConstraint returns [ConstraintTemplate result ]
    @init { $result = null; }
	:   CONTAINING type (ENCODED BY value)?
	    { } // TODO: impl
	|   ENCODED BY value
        { } // TODO: impl
	;

elementSetSpecs returns [ConstraintTemplate result]
    locals[ boolean extensible = false; ]
	:
	root=elementSetSpec
	(
	    COMMA ELLIPSIS { $extensible = true; }
	    ( COMMA ext = elementSetSpec )?
	)?
	{ $result = factory.elementSetSpecs($root.result, $extensible, $ext.ctx == null ? null : $ext.result); }
	| ELLIPSIS
	{ $result = factory.elementSetSpecs(null, true, null); }
	;

objectSetElementSpecs returns [ConstraintTemplate result]
    locals[ boolean extensible = false; ]
    :
        root = objectSetElementSpec
        (
            COMMA ELLIPSIS { $extensible = true; }
            ( COMMA ext = objectSetElementSpec )?
        )?
	{ $result = factory.elementSetSpecs($root.result, $extensible, $ext.ctx == null ? null : $ext.result); }
    |   ELLIPSIS
	{ $result = factory.elementSetSpecs(null, true, null); }
    ;

objectSetElementSpec returns [ConstraintTemplate result]
    locals [List<ConstraintTemplate> list]
    @init { $list = new ArrayList<>(); }
    :   objectSetElementSpecItem { $list.add($objectSetElementSpecItem.result); }
        ( ( UNION| OR ) objectSetElementSpecItem { $list.add($objectSetElementSpecItem.result); })*
        { $result = factory.elementSetSpec($list); }
    ;

objectSetElementSpecItem returns [ConstraintTemplate result]
    :   object { $result = factory.value( $object.result ); }
    |   definedObjectSet { $result = factory.type($definedObjectSet.result); }
    ;

elementSetSpec returns [ConstraintTemplate result]
	:
	    unions { $result = factory.elementSetSpec($unions.result); }
	|   ALL EXCEPT elements { $result = factory.elementSetSpec($elements.result); }
	;


exclusions returns [ConstraintTemplate result]
    :   EXCEPT elements { $result = $elements.result; }
    ;

unions returns [List<ConstraintTemplate> result]
    @init { $result = new ArrayList<>(); }
	:
	    union { $result.add($union.result); }
	    (( UNION | OR ) union { $result.add($union.result); } )*
    ;

union returns [ConstraintTemplate result]
	:   intersections
	    { $result = factory.union($intersections.result); }
	;

intersections returns [List<ConstraintTemplate> result]
    @init { $result = new ArrayList<>(); }
	:   intersectionItem  { $result.add( $intersectionItem.result ); }
	    ( (INTERSECTION | CARET ) intersectionItem { $result.add( $intersectionItem.result ); } )*
	;

intersectionItem returns [ConstraintTemplate result]
    :   elements exclusions?
        { $result = factory.elements($elements.result, $exclusions.ctx == null ? null : $exclusions.result); }
    ;

elements returns [ConstraintTemplate result]
    :   subtypeElements                         { $result = $subtypeElements.result; }
    |   objectSetElements                       { $result = $objectSetElements.result; }
    |   OPEN_PAREN elementSetSpecs CLOSE_PAREN  { $result = $elementSetSpecs.result; }
    ;

// X.680, p 51.1
subtypeElements returns [ConstraintTemplate result]
    :
        OPEN_BRACE definedType CLOSE_BRACE
                                { $result = factory.valuesFromSet($definedType.result); }
    // X.680, p 51.2 Single value
    |   value                   { $result = factory.value( $value.result ); }
    |   containedSubtype        { $result = $containedSubtype.result; }
    |   valueRange              { $result = $valueRange.result; }
    // X.680, p 51.7
    |   FROM elementSetSpecs    { $result = factory.permittedAlphabet( $elementSetSpecs.result ); }
    |   sizeConstraint          { $result = $sizeConstraint.result; }
    // X.680, p 51.6
    |   type                    { $result = factory.type( $type.result ); }
    |   innerTypeConstraints    { $result = $innerTypeConstraints.result; }
    // X.680, p 51.9
    |   PATTERN value           { $result = factory.pattern($value.result); }
    // X.680, p 51.10
    |   SETTINGS simpleString   { $result = factory.settings($simpleString.text); }
    //TODO:|   durationRange
    //TODO:|   timePointRange
    //TODO:|   recurrenceRange
    ;

objectSetElements returns [ ConstraintTemplate result]
    :   object                  { $result = factory.objectSetElements($object.result); }
    |   definedObjectSet        { $result = factory.objectSetElements($definedObjectSet.result); }
    |   objectSetFromObjects    { $result = factory.objectSetElements($objectSetFromObjects.result); }
    ;

// X.680, p 51.3
containedSubtype returns [ConstraintTemplate result]
    locals [Ref<Type> contained = null;]
    :   { boolean includes = false; }
        (   NULL { $contained = factory.builtin("NULL"); }
        |   INCLUDES {includes = true;} type { $contained = $type.result; }
        )
        { $result = factory.containedSubtype( $contained, includes ); }
    ;

// X.680, p 51.4
valueRange returns [ConstraintTemplate result]
    locals [
        Ref<Value> minValue = null,
        boolean minValueLt = false,
        Ref<Value> maxValue = null,
        boolean maxValueGt = false;
    ]
    :
        (
            value { $minValue = $value.result; }
        |   MIN
        )
        (LT { $minValueLt = true; } )?
        RANGE
        (LT { $maxValueGt = true; } )?
        (
            value  { $maxValue = $value.result; }
        |   MAX
        )
        { $result = factory.valueRange($minValue, $minValueLt, $maxValue, $maxValueGt); }
    ;

// X.680, p 51.5
sizeConstraint returns[ConstraintTemplate result]
    :   SIZE constraint         { $result = factory.size( $constraint.result ); }
    ;

innerTypeConstraints returns [ConstraintTemplate result]
    :   WITH
        (
            COMPONENT constraint
            { $result = factory.innerType( $constraint.result ); }

        |   COMPONENTS
            { List<ConstraintTemplate> list = new ArrayList<>(); boolean partial = false; }
            OPEN_BRACE
            (
                componentConstraintList[list, Presence.Present]

            |   { partial = true; }
                ELLIPSIS COMMA componentConstraintList[list, Presence.Optional]
            )
            CLOSE_BRACE
            { $result = factory.innerTypes(list, partial); }
        )
    ;

componentConstraintList[List<ConstraintTemplate> list, Presence presence]
    :   componentConstraint[$presence] { $list.add($componentConstraint.result); }
        (COMMA componentConstraint[$presence] { $list.add($componentConstraint.result); } )*
    ;

// X.680, p 51.8.8; X.680, p 51.8.5
componentConstraint[Presence defaultPresence] returns [ConstraintTemplate result]
    locals [ Presence presence = defaultPresence; ]
    :   identifier
        constraint?
        (
            PRESENT { $presence = Presence.Present; }
        |   ABSENT { $presence = Presence.Absent; }
        |   OPTIONAL { $presence = Presence.Optional; }
        )?
        { $result = factory.component( $identifier.text, $constraint.ctx == null ? null : $constraint.result, $presence ); }
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Class ///////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// X.681, p 9
objectClassAssignment returns [DefinedType result]
    locals [Ref<Type> assigned]
    :
    objectClassReference
    params=templateTypeParameterList?
    ASSIGN (definedObjectClass {$assigned = $definedObjectClass.result; } | objectClass {$assigned = $objectClass.result; })
    { $result = factory.define( $objectClassReference.text, $assigned, $params.ctx == null ? null : $params.result); }
    ;

objectClass returns [ClassType result]
    @init { $result = factory.classType(); }
    :   CLASS
        OPEN_BRACE
        fieldSpec[$result]
        (COMMA fieldSpec[$result])*
        CLOSE_BRACE
        (WITH SYNTAX OPEN_BRACE syntaxList CLOSE_BRACE { $result.setSyntaxList($syntaxList.result); })?
    ;

syntaxList returns[List<String> result]
    @init { $result = new ArrayList<>(); }
    :   tokenOrGroupSpec[$result]+
    ;

tokenOrGroupSpec[List<String> list]
    :
        OPEN_BRACKET { list.add("["); }
        tokenOrGroupSpec[$list]+
        CLOSE_BRACKET  { list.add("]"); }

    |   (requiredToken { list.add($requiredToken.text); } )+
    ;

requiredToken
    :   FieldIdentifier | word | COMMA
    ;

fieldSpec[ClassType classType]
    :   typeFieldSpec { $classType.add($typeFieldSpec.result); }
//    |   objectFieldSpec { $classType.add($objectFieldSpec.result); }
//    |   objectSetFieldSpec { $classType.add($objectSetFieldSpec.result); }
    |   fixedTypeValueFieldSpec { $classType.add($fixedTypeValueFieldSpec.result); }
    |   variableTypeValueFieldSpec { $classType.add($variableTypeValueFieldSpec.result); }
    |   fixedTypeValueSetFieldSpec { $classType.add($fixedTypeValueSetFieldSpec.result); }
    |   variableTypeValueSetFieldSpec { $classType.add($variableTypeValueSetFieldSpec.result); }
    ;

typeFieldSpec returns [ClassFieldType result]
    locals [boolean optional = false, Ref<Type> defaultType = null;]
    :   typeFieldReference
        (OPTIONAL { $optional = true; } | DEFAULT type { $defaultType = $type.result; } )?
        { $result = factory.typeClassField($typeFieldReference.text, $optional, $defaultType); }
    ;

fixedTypeValueFieldSpec returns [ClassFieldType result]
    locals [boolean optional = false, boolean unique = false, Ref<Value> defaultValue = null;]
    :   valueFieldReference type
        (   UNIQUE { $unique = true; } (OPTIONAL { $optional = true; })?
        |   (OPTIONAL { $optional = true; } | DEFAULT value { $defaultValue = $value.result; } )
        )?
        { $result = factory.fixedTypeValueField($valueFieldReference.text, $type.result, $unique, $optional, $defaultValue); }
    ;

variableTypeValueFieldSpec returns [ClassFieldType result]
    locals [boolean optional = false,  Ref<Value> defaultValue = null;]
    :   valueFieldReference
        fieldName
        (OPTIONAL { $optional = true; } | DEFAULT value { $defaultValue = $value.result; } )?
        { $result = factory.variableTypeValueField($valueFieldReference.text, $fieldName.text, $optional, $defaultValue); }
    ;

fixedTypeValueSetFieldSpec returns [ClassFieldType result]
    locals [boolean optional = false, ConstraintTemplate valueSetType = null;]
    :   valueSetFieldReference
        type
        (OPTIONAL { $optional = true; } | DEFAULT valueSet { $valueSetType = $valueSet.result; } )?
        { $result = factory.fixedTypeValueSetField( $valueSetFieldReference.text, $type.result, $optional, $valueSetType ); }
    ;

variableTypeValueSetFieldSpec returns [ClassFieldType result]
    locals [boolean optional = false, ConstraintTemplate valueSetType = null;]
    :   valueSetFieldReference
        fieldName
        (OPTIONAL { $optional = true; } | DEFAULT valueSet { $valueSetType = $valueSet.result; } )?
        { $result = factory.variableTypeValueSetField( $valueSetFieldReference.text, $fieldName.text, $optional, $valueSetType ); }
    ;

objectFieldSpec returns [ClassFieldType result]
    locals [boolean optional = false, Ref<Value> defaultValue = null;]
    :   objectFieldReference
        definedObjectClass
        (OPTIONAL { $optional = true; } | DEFAULT object { $defaultValue = $object.result; } )?
        { $result = factory.fixedTypeValueField($objectFieldReference.text, $definedObjectClass.result, false, $optional, $defaultValue); }
    ;

objectSetFieldSpec returns [ClassFieldType result]
    locals [boolean optional = false, ConstraintTemplate valueSetType = null;]
    :   objectSetFieldReference
        definedObjectClass
        (OPTIONAL { $optional = true; } | DEFAULT objectSet { $valueSetType = $objectSet.result; } )?
        { $result = factory.fixedTypeValueSetField( $objectSetFieldReference.text, $definedObjectClass.result, $optional, $valueSetType ); }
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Object //////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// X.681, p 11.1
objectAssignment returns [DefinedValue result]
    :
	    valueReference
        params=templateTypeParameterList?
	    definedObjectClass ASSIGN object
	    { $result = factory.define( $valueReference.text, $definedObjectClass.result, $object.result, $params.ctx == null ? null : $params.result ); }
    ;

// X.681, p 12.1
objectSetAssignment returns[DefinedType result]
    :
	    typeReference
	    params=templateTypeParameterList?
        definedObjectClass ASSIGN objectSet
        {
            Type constrained = factory.constrained($objectSet.result, $definedObjectClass.result);
            $result = factory.define(
                                $typeReference.text,
                                constrained,
                                $params.ctx == null ? null : $params.result);
        }
    ;

definedObjectClass returns [Ref<Type> result]
    :   definedObjectClassRef { $result = $definedObjectClassRef.result; }
    |   TYPE_IDENTIFIER { $result = factory.builtin("TYPE-IDENTIFIER"); }
    |   ABSTRACT_SYNTAX { $result = factory.builtin("ABSTRACT SYNTAX"); }
    ;

definedObjectClassRef returns [Ref<Type> result]
    :   (mr=typeReference DOT)? ref=objectClassReference args=actualTemplateTypeParameterList?
        {
            if ( $args.ctx == null )
                $result = getTypeRef($ref.text, $mr.text);
            else
                $result = $result = factory.typeTemplateInstance( getTypeRef($ref.text, $mr.text), $args.result );
        }
    ;

// X.681, p 11.3
object returns [Ref<Value> result]
    :   definedObject       { $result = $definedObject.result; }
    |   objectDefn          { $result = $objectDefn.result; }
    |   objectFromObject    { $result = $objectFromObject.result; }
    ;

definedObject returns [Ref<Value> result]
    :   definedValue { $result = $definedValue.result; }
    ;

objectSet returns [ConstraintTemplate result]
    :
        OPEN_BRACE objectSetElementSpecs CLOSE_BRACE { $result = $objectSetElementSpecs.result; }
    |   valueSet { $result = $valueSet.result; }
    ;

definedObjectSet returns [Ref<Type> result]
    :   definedType { $result = $definedType.result; }
    ;

////////////////////////////////////// Object Misc /////////////////////////////////////////////////////////////////////
valueFromObject returns [Ref<Value> result]
    :   referencedObjects (DOT fieldName)? DOT valueFieldReference
        { $result = factory.valueFromObjectRef($referencedObjects.result, $fieldName.ctx == null ? null : $fieldName.text, $valueFieldReference.text); }
    ;

// X.681, p 15.1
objectFromObject returns [Ref<Value> result]
    :   valueFromObject { $result = $valueFromObject.result; }
    ;

objectSetFromObjects returns [Ref<Type> result]
    :  valueSetFromObjects { $result = $valueSetFromObjects.result; }
    ;

valueSetFromObjects returns [Ref<Type> result]
    :   referencedObjects (DOT fieldName)* DOT typeFieldReference
        { $result = factory.valueSetFromObjectRef($referencedObjects.result, $fieldName.ctx == null ? null : $fieldName.text, $typeFieldReference.text); }
    ;

typeFromObject returns [Ref<Type> result]
    locals[ List<String> pathParts = new ArrayList<>(); ]
    :   referencedObjects
        (DOT FieldIdentifier { $pathParts.add( $FieldIdentifier.text ); })*
        DOT FieldIdentifier
        { $result = factory.typeFromObjectRef($referencedObjects.result, $pathParts, $FieldIdentifier.text); }
    ;

referencedObjects returns [ Ref<?> result ]
    :   definedObject       { $result = $definedObject.result; }
    |   definedObjectSet    { $result = $definedObjectSet.result; }
    ;

// Object definition
objectDefn returns [Ref<Value> result]
    :
        OPEN_BRACE fieldSettings CLOSE_BRACE { $result = factory.object($fieldSettings.result); }
        // TODO: some garbage may pass thru here
    |   braceConsumer { $result = new AbstractSyntaxObjectRef(tokens2string($braceConsumer.start, $braceConsumer.stop)); }
    ;

fieldSettings returns [Map<String, Ref<?>> result]
    @init { $result = new HashMap<>(); }
    :
    (
        fieldSetting[$result]
        (COMMA fieldSetting[$result])*
    )?
    ;

fieldSetting [Map<String, Ref<?>> fieldMap]
    :   FieldIdentifier setting
        {
            if ( $fieldMap.containsKey($FieldIdentifier.text) )
                throw new IllegalStateException( "Duplicate field: " + $FieldIdentifier.text);
            $fieldMap.put($FieldIdentifier.text, $setting.result);
        }
    ;

setting returns [Ref<?> result]
    :   type        { $result = $type.result; }
    |   value       { $result = $value.result; }
    |   valueSet    { $result = $valueSet.result; }
    |   object      { $result = $object.result; }
    |   objectSet   { $result = $objectSet.result; }
    ;

definedFieldSetting
    :   definedSyntaxToken+
    ;

definedSyntaxToken returns [Object result]
    :
        FieldIdentifier { $result = $FieldIdentifier.text; }
    |   Identifier      { $result = $Identifier.text; }
    |   COMMA           { $result = ","; }
    ;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                    //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                    //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////// Abstract Syntax Parser ///////////////////////////////////////////////////////////////////
abstractSyntaxWord
    :   objectClassReference
    |   COMMA
    |   ABSENT
    |   ABSTRACT_SYNTAX
    |   ALL
    |   APPLICATION
    |   AUTOMATIC
    |   BEGIN
    |   BY
    |   CLASS
    |   COMPONENT
    |   COMPONENTS
    |   CONSTRAINED
    |   CONTAINING
    |   DEFAULT
    |   DEFINITIONS
    |   ENCODED
    |   ENCODING_CONTROL
    |   EXCEPT
    |   EXPLICIT
    |   EXPORTS
    |   EXTENSIBILITY
    |   FROM
    |   IDENTIFIER
    |   IMPLICIT
    |   IMPLIED
    |   IMPORTS
    |   INCLUDES
    |   INSTRUCTIONS
    |   MAX
    |   MIN
    |   OF
    |   OID_IRI
    |   OPTIONAL
    |   PATTERN
    |   PDV
    |   PRESENT
    |   PRIVATE
    |   RELATIVE_OID_IRI
    |   SETTINGS
    |   SIZE
    |   STRING
    |   SYNTAX
    |   TAGS
    |   TYPE_IDENTIFIER
    |   UNIQUE
    |   UNIVERSAL
    |   WITH
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
reference
	:   typeReference
	|   valueReference
	|   objectClassReference
	;

taggingMethod returns[ TagMethod result ]
    :   AUTOMATIC { $result = TagMethod.Automatic; }
    |   EXPLICIT  { $result = TagMethod.Explicit; }
    |   IMPLICIT  { $result = TagMethod.Implicit; }
    ;

tagClass returns[TagClass result]
    :   APPLICATION { $result = TagClass.Application; }
    |   UNIVERSAL   { $result = TagClass.Universal; }
    |   PRIVATE     { $result = TagClass.Private; }
    ;

fieldName
    :   FieldIdentifier (DOT FieldIdentifier)*
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// ASN.1 Lexical items, X.680 p.12

// X.680, p 12.2
typeReference
	:   { RefUtils.isTypeRef( _input.LT( 1 ).getText() ) }? Identifier
	;

// X.681, p 7.1
objectClassReference
    :   { isObjectClassReference(_input.LT( 1 ).getText()) }?
        Identifier
    ;

//objectReference
//    :   valueReference
//    ;
//
//objectSetReference
//    :   typeReference
//    ;

// X.680, p 12.3
identifier
	:   { RefUtils.isValueRef( _input.LT( 1 ).getText() ) }?
	    Identifier
	;

// X.680, p 12.4
valueReference
	:   { RefUtils.isValueRef( _input.LT( 1 ).getText() ) }? Identifier
	;

// X.680, p 12.8
number
	:   NumberLiteral
	;

// X.680, p 12.14
cstring
	:   CString
	;
// X.680, p 12.16
simpleString
	:   { isSimpleString() }? cstring
	;


iriValueString
	:   { RefUtils.isIriValue( _input.LT( 1 ).getText() ) }?
	    cstring
	;

// X.680, p 12.25
encodingReference
	:   { StringUtils.isAllUpperCase(_input.LT( 1 ).getText()) }?
		Identifier
	;

typeFieldReference
    :   { RefUtils.isTypeRef( _input.LT( 1 ).getText().substring( 1 ) ) }?
        FieldIdentifier
    ;

valueFieldReference
    :   { RefUtils.isValueRef( _input.LT( 1 ).getText().substring( 1 ) ) }?
        FieldIdentifier
    ;

valueSetFieldReference
    :   typeFieldReference
    ;

objectFieldReference
    :   valueFieldReference
    ;

objectSetFieldReference
    :   typeFieldReference
    ;

word
    :   Identifier
    ;

/////////////////////////////////////////////// LEXER //////////////////////////////////////////////////////////////////

// X.680, p 21
SpecialRealValue    :   'PLUS-INFINITY' | 'MINUS-INFINITY' | 'NOT-A-NUMBER';
// X.680, p.45.1
UsefullType         :   'GeneralizedTime' | 'UTCTime' | 'ObjectDescriptor';
RestrictedString    :   'BMPString' | 'GeneralString' | 'GraphicString' | 'IA5String' | 'ISO646String' | 'NumericString'
                        | 'PrintableString' | 'TeletexString' | 'T61String' | 'UniversalString' | 'UTF8String'
                        | 'VideotexString' | 'VisibleString';

// Reserved keywords X.680, p. 12.38
FALSE               : 'FALSE';
TRUE                : 'TRUE';
ABSENT              : 'ABSENT';
ABSTRACT_SYNTAX     : 'ABSTRACT-SYNTAX';
ALL                 : 'ALL';
APPLICATION         : 'APPLICATION';
AUTOMATIC           : 'AUTOMATIC';
BEGIN               : 'BEGIN';
BIT                 : 'BIT';
BOOLEAN             : 'BOOLEAN';
BY                  : 'BY';
CHARACTER           : 'CHARACTER';
CHOICE              : 'CHOICE';
CLASS               : 'CLASS';
COMPONENT           : 'COMPONENT';
COMPONENTS          : 'COMPONENTS';
CONSTRAINED         : 'CONSTRAINED';
CONTAINING          : 'CONTAINING';
DATE                : 'DATE';
DATE_TIME           : 'DATE-TIME';
DEFAULT             : 'DEFAULT';
DEFINITIONS         : 'DEFINITIONS';
DURATION            : 'DURATION';
EMBEDDED            : 'EMBEDDED';
ENCODED             : 'ENCODED';
ENCODING_CONTROL    : 'ENCODING-CONTROL';
END                 : 'END';
ENUMERATED          : 'ENUMERATED';
EXCEPT              : 'EXCEPT';
EXPLICIT            : 'EXPLICIT';
EXPORTS             : 'EXPORTS';
EXTENSIBILITY       : 'EXTENSIBILITY';
EXTERNAL            : 'EXTERNAL';
FROM                : 'FROM';
IDENTIFIER          : 'IDENTIFIER';
IMPLICIT            : 'IMPLICIT';
IMPLIED             : 'IMPLIED';
IMPORTS             : 'IMPORTS';
INCLUDES            : 'INCLUDES';
INSTANCE            : 'INSTANCE';
INSTRUCTIONS        : 'INSTRUCTIONS';
INTEGER             : 'INTEGER';
INTERSECTION        : 'INTERSECTION';
MAX                 : 'MAX';
MIN                 : 'MIN';
NULL                : 'NULL';
OBJECT              : 'OBJECT';
OCTET               : 'OCTET';
OF                  : 'OF';
OID_IRI             : 'OID-IRI';
OPTIONAL            : 'OPTIONAL';
PATTERN             : 'PATTERN';
PDV                 : 'PDV';
PRESENT             : 'PRESENT';
PRIVATE             : 'PRIVATE';
REAL                : 'REAL';
RELATIVE_OID        : 'RELATIVE-OID';
RELATIVE_OID_IRI    : 'RELATIVE-OID-IRI';
SEQUENCE            : 'SEQUENCE';
SET                 : 'SET';
SETTINGS            : 'SETTINGS';
SIZE                : 'SIZE';
STRING              : 'STRING';
SYNTAX              : 'SYNTAX';
TAGS                : 'TAGS';
TIME                : 'TIME';
TIME_OF_DAY         : 'TIME-OF-DAY';
TYPE_IDENTIFIER     : 'TYPE-IDENTIFIER';
UNION               : 'UNION';
UNIQUE              : 'UNIQUE';
UNIVERSAL           : 'UNIVERSAL';
WITH                : 'WITH';

// X.680, p 12.20
ASSIGN              : '::=';
// X.680, p 12.22
ELLIPSIS            : '...';
// X.680, p 12.21
RANGE               : '..';

// X.680, p 12.23
//LEFT_VER_BRACKETS   : '[[';
// X.680, p 12.24
//RIGHT_VER_BRACKETS  : ']]';

// X.680, p 12.37
OPEN_BRACE          : '{';
CLOSE_BRACE         : '}';
LT                  : '<';
GT                  : '>';
COMMA               : ',';
DOT                 : '.';
SLASH               : '/';
OPEN_PAREN          : '(';
CLOSE_PAREN         : ')';
OPEN_BRACKET        : '[';
CLOSE_BRACKET       : ']';
HYPEN               : '-' | '\u2011';
COLON               : ':';
EQUALS              : '=';
QUOT                : '"';
APOST               : '\'';
SEMI                : ';';
AT                  : '@';
OR                  : '|';
EXCLAMATION         : '!';
CARET               : '^';
AMP                 : '&';


FieldIdentifier  :   AMP [A-Za-z] (HYPEN? [A-Za-z0-9] )*;

// X.680, pp 12.2 12.3 12.4 12.5
Identifier		    :	[A-Za-z] (HYPEN? [A-Za-z0-9] )*;

NumberLiteral		:	Sign? Number;

RealLiteral	        :	Sign? Number (DOT Digits)? (Exponent Sign? Number)?
                    |   Sign? Number DOT Digits? Exponent Sign? Number;

BString             : '\'' [01 ]* '\'B';
HString             : '\'' [A-F0-9 ]* '\'H';
CString             : '"' (~["]| '""' )* '"'
                    |  '\'' (~[']| '\'\'' )* '\'';

fragment
Number              : '0' | (NonZeroDigit Digits*);

fragment
Digits              : Digit+;

fragment
Digit               : '0' | NonZeroDigit;

fragment
NonZeroDigit        : [1-9];

fragment
Sign                : [+-];

fragment
Exponent      : [eE];

fragment
Letter              : SmallLetter | HighLetter;

fragment
HighLetter          : [A-Z];

fragment
SmallLetter         : [a-z];


// X.680, p 12.6
MULTI_LINE_COMMENT  : '/*' .*? '*/' -> skip;
SINGLE_LINE_COMMENT : '--'  ( ~[\-] '-' | ~[\-\r\n])* ('\r'?'\n' | '--' ) -> skip;

WS                  :  [ \t\r\n\u000C]+ -> skip;
