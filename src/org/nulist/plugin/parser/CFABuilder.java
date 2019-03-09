package org.nulist.plugin.parser;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.grammatech.cs.*;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.ParseResult;
import org.sosy_lab.cpachecker.cfa.ast.ADeclaration;
import org.sosy_lab.cpachecker.cfa.ast.FileLocation;
import org.sosy_lab.cpachecker.cfa.ast.c.*;
import org.sosy_lab.cpachecker.cfa.model.BlankEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.model.FunctionExitNode;
import org.sosy_lab.cpachecker.cfa.model.c.CFunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.model.c.CLabelNode;
import org.sosy_lab.cpachecker.cfa.parser.Scope;
import org.sosy_lab.cpachecker.cfa.types.MachineModel;
import org.sosy_lab.cpachecker.cfa.types.c.*;
import org.sosy_lab.cpachecker.util.Pair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

import static org.nulist.plugin.parser.CFGAST.isConstantAggregateZero;
import static org.nulist.plugin.parser.CFGAST.isConstantArrayOrVector;
import static org.nulist.plugin.parser.CFGNode.*;
import static org.nulist.plugin.util.ClassTool.getUnsignedInt;
import static org.nulist.plugin.util.FileOperations.*;
import static org.sosy_lab.cpachecker.cfa.CFACreationUtils.isReachableNode;
import static org.sosy_lab.cpachecker.cfa.types.c.CFunctionType.NO_ARGS_VOID_FUNCTION;

/**
 * @ClassName CFABuilder
 * @Description For a C file
 * @Author Yinbo Yu
 * @Date 2/27/19 4:18 PM
 * @Version 1.0
 **/
public class CFABuilder {
    private final LogManager logger;
    private final MachineModel machineModel;

    private final CFGTypeConverter typeConverter;


    private final List<Path> parsedFiles = new ArrayList<>();

    private GlobalScope fileScope = new GlobalScope();
    private Scope artificialScope;

    // Function name -> Function declaration
    protected Map<String, CFunctionDeclaration> functionDeclarations;
    protected NavigableMap<String, CFunctionEntryNode> functions;
    //protected List<Pair<ADeclaration, String>> globalVariableDeclarations;
    protected final Map<Integer, CSimpleDeclaration> globalVariableDeclarations = new HashMap<>();
    protected SortedSetMultimap<String, CFANode> cfaNodes;


    public CFABuilder(final LogManager pLogger, final MachineModel pMachineModel) {
        logger = pLogger;
        machineModel = pMachineModel;

        typeConverter = new CFGTypeConverter(machineModel,logger);

        functionDeclarations = new HashMap<>();

        functions = new TreeMap<>();
        cfaNodes = TreeMultimap.create();
    }

    protected void addNode(String funcName, CFANode nd) {
        cfaNodes.put(funcName, nd);
    }

    /**
     *@Description input is a C file TODO
     *@Param [cu]
     *@return org.sosy_lab.cpachecker.cfa.ParseResult
     **/
    public ParseResult build(compunit cu) throws result {
        String pFileName = cu.normalized_name();

        // Iterate over all procedures in the compilation unit
        // procedure = function

        /* create global variable declaration*/
        declareGlobalVariables(cu, pFileName);


        for (compunit_procedure_iterator proc_it = cu.procedures();
             !proc_it.at_end(); proc_it.advance()) {
            procedure proc = proc_it.current();

            //only focus on the function defined by user
            if(proc.get_kind().equals(procedure_kind.getUSER_DEFINED())){

                String funcName = proc.name();
                CFGFunctionBuilder cfgFunctionBuilder =
                        new CFGFunctionBuilder(logger,machineModel, typeConverter,  proc,funcName, pFileName, this);
                // add function declaration
                functionDeclarations.put(funcName,cfgFunctionBuilder.handleFunctionDeclaration());

                // handle the function definition
                CFunctionEntryNode en = cfgFunctionBuilder.handleFunctionDefinition();
                addNode(funcName, en);
                addNode(funcName, en.getExitNode());
                //

                cfgFunctionBuilder.visitFunction();
                cfaNodes.put(pFileName, en);
                functions.put(funcName, en);
            }
        }

        parsedFiles.add(Paths.get(pFileName));

        return  null;
    }



    /**
     *@Description all global and file static variables are defined in File_Initialization-
     *              Global_Initialization_0 (no initializer) and Global_Initialization_1 (static initializer)
     *              all symbols of global and file static variables also can be obtained by compunit.global_symbols
     *                 and figured out by symbol.is_gobal() or symbol.is_file_static() (or directly is_user())
     *@Param [compunit, pFileName]
     *@return void
     **/
    private void declareGlobalVariables(compunit compunit, final String pFileName) throws result{

        for (compunit_procedure_iterator proc_it = compunit.procedures();
             !proc_it.at_end(); proc_it.advance()) {
            procedure proc = proc_it.current();
            if(proc.get_kind().equals(procedure_kind.getFILE_INITIALIZATION())
                && proc.name().contains("Global_Initialization")){
                visitGlobalItem(proc, pFileName);
            }
        }
        
    }



    private void visitGlobalItem(procedure global_initialization, final String pFileName) throws result {

        point_set pointSet = global_initialization.points();
        for(point_set_iterator point_it = pointSet.cbegin();
            !point_it.at_end();point_it.advance()){
            //point p = point_it.current();
            CFGNode node = (CFGNode) point_it.current();
            CInitializer initializer = null;

            //another way of checking the type of initializer
            /*ast a = p.get_ast(ast_family.getC_UNNORMALIZED());
            a.get(ast_ordinal.getUC_STATIC_INIT()).as_ast().get_class().equals(ast_class.getUC_NO_INITIALIZER());
            a.get(ast_ordinal.getUC_STATIC_INIT()).as_ast().get_class().equals(ast_class.getUC_STATIC_INITIALIZER());
            a.get(ast_ordinal.getUC_STATIC_INIT()).as_ast().get_class().equals(ast_class.getUC_ZERO_INITIALIZER());*/

            if(node.isVariable_Initialization()||node.isExpression()){
                FileLocation fileLocation = getLocation(node,pFileName);
                CFGAST no_ast = (CFGAST) node.get_ast(ast_family.getC_NORMALIZED());
                CFGAST un_ast = (CFGAST) node.get_ast(ast_family.getC_UNNORMALIZED());
                // for example: int i=0;
                // in nc_ast: children {i, 0}
                //            attributes {is_initialization: true, type: int}
                // has initialization
                if(no_ast.get(ast_ordinal.getNC_IS_INITIALIZATION()).as_boolean()){
                    ast_field type = no_ast.get(ast_ordinal.getNC_TYPE());
                    CFGAST type_ast = (CFGAST)type.as_ast();

                    initializer = new CInitializerExpression(fileLocation,
                            (CExpression) getConstant(node, fileLocation));

                    /*if(isConstantArrayOrVector(type) || type_ast.isStructType() || type_ast.isEnumType()){
                        initializer = getConstantAggregateInitializer(no_ast, fileLocation, pFileName);
                    } else if (isConstantAggregateZero(type)) {
                        CType expressionType = typeConverter.getCType(initializerRaw.typeOf());
                        initializer = getZeroInitializer(initializerRaw, expressionType, pFileName);
                    } else {

                    }*/
                }else {//// Declaration without initialization
                    initializer = null;
                }


                String assignedVar = no_ast.children().get(0).as_ast().pretty_print();//the 1st child field store the variable

                // Support static and other storage classes

                CStorageClass storageClass= un_ast.getStorageClass();

                CType varType = typeConverter.getCType((CFGAST) no_ast.get(ast_ordinal.getNC_TYPE()).as_ast());

                // We handle alloca not like malloc, which returns a pointer, but as a general
                // variable declaration. Consider that here by using the allocated type, not the
                // pointer of that type alloca returns.
//            if (pItem.isAllocaInst()) {
//                varType = typeConverter.getCType(pItem.getAllocatedType());
//            }

                if ( varType instanceof CPointerType) {
                    varType = ((CPointerType) varType).getType();
                }


                CSimpleDeclaration newDecl =
                        new CVariableDeclaration(
                                fileLocation,
                                true,
                                storageClass,
                                varType,
                                assignedVar,
                                assignedVar,
                                getQualifiedName(assignedVar, fileLocation.getFileName()),
                                initializer);

                globalVariableDeclarations.put(assignedVar.hashCode(),newDecl);
            }

        }
    }



    /**
     *@Description handle the aggregate initialization (normalized ast), e.g., int array[5]={1,2,3,4,5};
     *@Param [no_ast, pFileName]
     *@return org.sosy_lab.cpachecker.cfa.ast.c.CInitializer
     **/
    private CInitializer getConstantAggregateInitializer(ast no_ast,
            final FileLocation fileLoc, final String pFileName) throws result {

        //ast no_ast = initialPoint.get_ast(ast_family.getC_NORMALIZED());
        ast_field value = no_ast.children().get(1);
        ast_field_vector elements = value.as_ast().children();

        int length = (int)elements.size();
        List<CInitializer> elementInitializers = new ArrayList<>(length);
        for(int i=0;i<length;i++){
            ast_field element = elements.get(i);
            CInitializer elementInitializer;
            ast_field elementType = element.as_ast().get(ast_ordinal.getBASE_TYPE());
            CFGAST elementType_ast = (CFGAST) elementType.as_ast();
            if(isConstantArrayOrVector(elementType) || elementType_ast.isStructType() || elementType_ast.isEnumType()){
                elementInitializer = getConstantAggregateInitializer(element.as_ast(), fileLoc, pFileName);
            }else if(isConstantAggregateZero(elementType)){
                elementInitializer =
                        getZeroInitializer(element.as_ast(), typeConverter.getCType(elementType_ast), fileLoc,pFileName);
            } else {
                elementInitializer = null;
                        //new CInitializerExpression(
                        //        fileLoc, (CExpression) getConstant((CFGAST)element.as_ast(), pFileName));
            }
            elementInitializers.add(elementInitializer);
        }

        CInitializerList aggregateInitializer =
                new CInitializerList(fileLoc, elementInitializers);
        return aggregateInitializer;
    }


    private CInitializer getZeroInitializer(
            final ast init_ast, final CType pExpectedType, final FileLocation fileLoc, final String pFileName) throws result {

        CInitializer init;
        CType canonicalType = pExpectedType.getCanonicalType();
        if (canonicalType instanceof CArrayType) {
            int length = ((CArrayType) canonicalType).getLengthAsInt().getAsInt();
            CType elementType = ((CArrayType) canonicalType).getType().getCanonicalType();
            CInitializer zeroInitializer = getZeroInitializer(init_ast, elementType, fileLoc,pFileName);
            List<CInitializer> initializers = Collections.nCopies(length, zeroInitializer);
            init = new CInitializerList(fileLoc, initializers);
        } else if (canonicalType instanceof CCompositeType) {

            List<CCompositeType.CCompositeTypeMemberDeclaration> members = ((CCompositeType) canonicalType).getMembers();
            List<CInitializer> initializers = new ArrayList<>(members.size());
            for (CCompositeType.CCompositeTypeMemberDeclaration m : members) {
                CType memberType = m.getType();
                CInitializer memberInit = getZeroInitializer(init_ast, memberType, fileLoc, pFileName);
                initializers.add(memberInit);
            }
            init = new CInitializerList(fileLoc, initializers);

        } else {
            CExpression zeroExpression;
            if (canonicalType instanceof CSimpleType) {
                CBasicType basicType = ((CSimpleType) canonicalType).getType();
                if (basicType == CBasicType.FLOAT || basicType == CBasicType.DOUBLE) {
                    // use expected type for float, not canonical
                    zeroExpression = new CFloatLiteralExpression(fileLoc, pExpectedType, BigDecimal.ZERO);
                } else {
                    zeroExpression = CIntegerLiteralExpression.ZERO;
                }
            } else {
                // use expected type for cast, not canonical
                zeroExpression = new CCastExpression(fileLoc, pExpectedType, CIntegerLiteralExpression.ZERO);
            }
            init = new CInitializerExpression(fileLoc, zeroExpression);
        }

        return init;
    }

    private CRightHandSide getConstant(final CFGNode exprNode, final FileLocation fileLoc, final String pFileName, CFGTypeConverter typeConverter)
            throws result {
        ast no_ast =exprNode.get_ast(ast_family.getC_NORMALIZED());
        //ast value_ast = no_ast.children().get(1).as_ast();

        CType expectedType = typeConverter.getCType((CFGAST) no_ast.get(ast_ordinal.getBASE_TYPE()).as_ast());
        return getConstant(exprNode, expectedType, fileLoc, pFileName);
    }

    private CRightHandSide getConstant(final CFGNode exprNode, CType pExpectedType, final FileLocation fileLoc, final String pFileName)
            throws result {

        CFGAST no_ast =(CFGAST) exprNode.get_ast(ast_family.getC_NORMALIZED());
        CFGAST value_ast =(CFGAST) no_ast.children().get(1).as_ast();
        ast_field type = no_ast.get(ast_ordinal.getBASE_TYPE());

        if(type.as_ast().pretty_print().equals("int"))//const int
        {
            int constantValue = value_ast.get(ast_ordinal.getBASE_VALUE()).as_int32();
            return new CIntegerLiteralExpression(fileLoc, pExpectedType, BigInteger.valueOf(constantValue));
        }else if(value_ast.isNullPointer())//null pointer: e.g., p = 0; p= NULL;
        {
            return new CPointerExpression(fileLoc,pExpectedType,getNull(fileLoc,pExpectedType));
        }else if(value_ast.isConstantExpression()){
            return getExpression(exprNode,pExpectedType,pFileName);
        }else if(value_ast.isUndef()){//TODO
            CType constantType = typeConverter.getCType((CFGAST) type.as_ast());
            String undefName = "__VERIFIER_undef_" + constantType.toString().replace(' ', '_');
            CSimpleDeclaration undefDecl =
                    new CVariableDeclaration(
                            fileLoc,
                            true,
                            CStorageClass.AUTO,
                            pExpectedType,
                            undefName,
                            undefName,
                            undefName,
                            null);
            CExpression undefExpression = new CIdExpression(fileLoc, undefDecl);
            return undefExpression;
        } else if(isFunction(value_ast)){
            String value = value_ast.get(ast_ordinal.getBASE_NAME()).as_str();
            String functionName = value.substring(0,value.indexOf("$result"));
            CFunctionDeclaration funcDecl = functionDeclarations.get(functionName);
            CType functionType = funcDecl.getType();

            CIdExpression funcId = new CIdExpression(fileLoc, funcDecl);
            if (pointerOf(pExpectedType, functionType)) {
                return new CUnaryExpression(fileLoc, pExpectedType, funcId, CUnaryExpression.UnaryOperator.AMPER);
            } else {
                return funcId;
            }
        }
        else if (exprNode.isGlobalConstant() && exprNode.isGlobalVariable()) {
            return getAssignedIdExpression(exprNode, pExpectedType, fileLoc);
        } else {
            throw new UnsupportedOperationException("CFG parsing does not support constant " + exprNode.characters());
        }
    }
    /**
     * Returns the id expression to an already declared variable. Returns it as a cast, if necessary
     * to match the expected type.
     */
    private CExpression getAssignedIdExpression(
            final CFGNode node, final CType pExpectedType, final FileLocation fileLocation) throws result{
        logger.log(Level.FINE, "Getting var declaration for point");

        /*globalVariableDeclarations.get()
        CSimpleDeclaration assignedVarDeclaration = variableDeclarations.get(getUnsignedInt(node.hashCode()));

        String assignedVarName = assignedVarDeclaration.getName();
        CType expressionType = assignedVarDeclaration.getType().getCanonicalType();
        CIdExpression idExpression =
                new CIdExpression(
                        fileLocation, expressionType, assignedVarName, assignedVarDeclaration);

        if (expressionType.canBeAssignedFrom(pExpectedType)) {
            return idExpression;

        } else if (pointerOf(pExpectedType, expressionType)) {
            CType typePointingTo = ((CPointerType) pExpectedType).getType().getCanonicalType();
            if (expressionType.canBeAssignedFrom(typePointingTo)
                    || expressionType.equals(typePointingTo)) {
                return new CUnaryExpression(
                        fileLocation, pExpectedType, idExpression, CUnaryExpression.UnaryOperator.AMPER);
            } else {
                throw new AssertionError("Unhandled type structure");
            }
        } else if (expressionType instanceof CPointerType) {
            return new CPointerExpression(fileLocation, pExpectedType, idExpression);
        } else {
            throw new AssertionError("Unhandled types structure");
        }*/
        return null;
    }

    /**
     *@Description TODO
     *@Param [point, pFunctionName, pInitializer, pFileName]
     *@return org.sosy_lab.cpachecker.cfa.ast.c.CSimpleDeclaration
     **/
    private CDeclaration getAssignedVarDeclaration(
            final CFGNode node, CInitializer pInitializer, final FileLocation fileLocation) throws result {


        final long itemId = node.id();


        CFGAST nc_ast = (CFGAST) node.get_ast(ast_family.getC_NORMALIZED());
        CFGAST un_ast = (CFGAST) node.get_ast(ast_family.getC_UNNORMALIZED());

        String assignedVar = nc_ast.children().get(0).as_ast().pretty_print();//the 1st child field store the variable

        final boolean isGlobal = node.declared_symbol().is_global();
        // Support static and other storage classes

        CStorageClass storageClass= un_ast.getStorageClass();

        CType varType = typeConverter.getCType((CFGAST) nc_ast.get(ast_ordinal.getNC_TYPE()).as_ast());

        // We handle alloca not like malloc, which returns a pointer, but as a general
        // variable declaration. Consider that here by using the allocated type, not the
        // pointer of that type alloca returns.
//            if (pItem.isAllocaInst()) {
//                varType = typeConverter.getCType(pItem.getAllocatedType());
//            }

        if (isGlobal && varType instanceof CPointerType) {
            varType = ((CPointerType) varType).getType();
        }


        CSimpleDeclaration newDecl =
                new CVariableDeclaration(
                        fileLocation,
                        isGlobal,
                        storageClass,
                        varType,
                        assignedVar,
                        assignedVar,
                        getQualifiedName(assignedVar, fileLocation.getFileName()),
                        pInitializer);
        return (CDeclaration) newDecl;
    }

    /**
     *@Description handle the aggregate initialization (normalized ast), e.g., int array[5]={1,2,3,4,5};
     *@Param [no_ast, pFileName]
     *@return org.sosy_lab.cpachecker.cfa.ast.c.CInitializer
     **/
    private CInitializer getConstantAggregateInitializer(ast no_ast,
                                                         final FileLocation fileLoc) throws result {

        //ast no_ast = initialPoint.get_ast(ast_family.getC_NORMALIZED());
        ast_field value = no_ast.children().get(1);
        ast_field_vector elements = value.as_ast().children();

        int length = (int)elements.size();
        List<CInitializer> elementInitializers = new ArrayList<>(length);
        for(int i=0;i<length;i++){
            ast_field element = elements.get(i);
            CInitializer elementInitializer;
            ast_field elementType = element.as_ast().get(ast_ordinal.getBASE_TYPE());
            CFGAST elementType_ast = (CFGAST) elementType.as_ast();
            if(isConstantArrayOrVector(elementType) || elementType_ast.isStructType() || elementType_ast.isEnumType()){
                elementInitializer = getConstantAggregateInitializer(element.as_ast(), fileLoc);
            }else if(isConstantAggregateZero(elementType)){
                elementInitializer =
                        getZeroInitializer(element.as_ast(), typeConverter.getCType(elementType_ast), fileLoc);
            } else {
                elementInitializer = null;
                        //new CInitializerExpression(
                        //        fileLoc, (CExpression) getConstant((CFGAST)element.as_ast()));
            }
            elementInitializers.add(elementInitializer);
        }

        CInitializerList aggregateInitializer =
                new CInitializerList(fileLoc, elementInitializers);
        return aggregateInitializer;
    }


    private CInitializer getZeroInitializer(
            final ast init_ast, final CType pExpectedType, final FileLocation fileLoc) throws result {

        CInitializer init;
        CType canonicalType = pExpectedType.getCanonicalType();
        if (canonicalType instanceof CArrayType) {
            int length = ((CArrayType) canonicalType).getLengthAsInt().getAsInt();
            CType elementType = ((CArrayType) canonicalType).getType().getCanonicalType();
            CInitializer zeroInitializer = getZeroInitializer(init_ast, elementType, fileLoc);
            List<CInitializer> initializers = Collections.nCopies(length, zeroInitializer);
            init = new CInitializerList(fileLoc, initializers);
        } else if (canonicalType instanceof CCompositeType) {

            List<CCompositeType.CCompositeTypeMemberDeclaration> members = ((CCompositeType) canonicalType).getMembers();
            List<CInitializer> initializers = new ArrayList<>(members.size());
            for (CCompositeType.CCompositeTypeMemberDeclaration m : members) {
                CType memberType = m.getType();
                CInitializer memberInit = getZeroInitializer(init_ast, memberType, fileLoc);
                initializers.add(memberInit);
            }
            init = new CInitializerList(fileLoc, initializers);

        } else {
            CExpression zeroExpression;
            if (canonicalType instanceof CSimpleType) {
                CBasicType basicType = ((CSimpleType) canonicalType).getType();
                if (basicType == CBasicType.FLOAT || basicType == CBasicType.DOUBLE) {
                    // use expected type for float, not canonical
                    zeroExpression = new CFloatLiteralExpression(fileLoc, pExpectedType, BigDecimal.ZERO);
                } else {
                    zeroExpression = CIntegerLiteralExpression.ZERO;
                }
            } else {
                // use expected type for cast, not canonical
                zeroExpression = new CCastExpression(fileLoc, pExpectedType, CIntegerLiteralExpression.ZERO);
            }
            init = new CInitializerExpression(fileLoc, zeroExpression);
        }

        return init;
    }

    private CRightHandSide getConstant(final CFGNode exprNode, final FileLocation fileLoc)
            throws result {
        ast no_ast =exprNode.get_ast(ast_family.getC_NORMALIZED());
        //ast value_ast = no_ast.children().get(1).as_ast();

        CType expectedType = typeConverter.getCType((CFGAST) no_ast.get(ast_ordinal.getBASE_TYPE()).as_ast());
        return getConstant(exprNode, expectedType, fileLoc);
    }

    private CRightHandSide getConstant(final CFGNode exprNode, CType pExpectedType, final FileLocation fileLoc)
            throws result {

        CFGAST no_ast =(CFGAST) exprNode.get_ast(ast_family.getC_NORMALIZED());
        CFGAST value_ast =(CFGAST) no_ast.children().get(1).as_ast();
        ast_field type = no_ast.get(ast_ordinal.getBASE_TYPE());

        if(type.as_ast().pretty_print().equals("int"))//const int
        {
            int constantValue = value_ast.get(ast_ordinal.getBASE_VALUE()).as_int32();
            return new CIntegerLiteralExpression(fileLoc, pExpectedType, BigInteger.valueOf(constantValue));
        }else if(value_ast.isNullPointer())//null pointer: e.g., p = 0; p= NULL;
        {
            return new CPointerExpression(fileLoc,pExpectedType,getNull(fileLoc,pExpectedType));
        }else if(value_ast.isConstantExpression()){
            return getExpression(exprNode,pExpectedType, fileLoc.getFileName());
        }else if(value_ast.isUndef()){//TODO
            CType constantType = typeConverter.getCType((CFGAST) type.as_ast());
            String undefName = "__VERIFIER_undef_" + constantType.toString().replace(' ', '_');
            CSimpleDeclaration undefDecl =
                    new CVariableDeclaration(
                            fileLoc,
                            true,
                            CStorageClass.AUTO,
                            pExpectedType,
                            undefName,
                            undefName,
                            undefName,
                            null);
            CExpression undefExpression = new CIdExpression(fileLoc, undefDecl);
            return undefExpression;
        } else if(isFunction(value_ast)){
            String value = value_ast.get(ast_ordinal.getBASE_NAME()).as_str();
            String functionName = value.substring(0,value.indexOf("$result"));
            CFunctionDeclaration funcDecl = functionDeclarations.get(functionName);
            CType functionType = funcDecl.getType();

            CIdExpression funcId = new CIdExpression(fileLoc, funcDecl);
            if (pointerOf(pExpectedType, functionType)) {
                return new CUnaryExpression(fileLoc, pExpectedType, funcId, CUnaryExpression.UnaryOperator.AMPER);
            } else {
                return funcId;
            }
        }
        else if (exprNode.isGlobalConstant() && exprNode.isGlobalVariable()) {
            return getAssignedIdExpression(exprNode, pExpectedType, fileLoc);
        } else {
            throw new UnsupportedOperationException("CFG parsing does not support constant " + exprNode.characters());
        }
    }




    private boolean isFunction(ast value_ast) throws result{
//        ast no_ast=point.get_ast(ast_family.getC_NORMALIZED());
//        ast_field value_ast = no_ast.children().get(1);
//        try {
//
//            String value = value_ast.get(ast_ordinal.getBASE_NAME()).as_str();
//            if(value.contains("$result")){
//                String functionName = value.substring(0,value.indexOf("$result"));
//                if(functionDeclarations.containsKey(functionName))
//                    return true;
//            }
//            return false;
//        }catch (result r){
//            return false;
//        }
        try {
            symbol value_symbol = value_ast.get(ast_ordinal.getBASE_ABS_LOC()).as_symbol();
            if(value_symbol.get_kind().equals(symbol_kind.getRESULT())){
                String value = value_ast.get(ast_ordinal.getBASE_NAME()).as_str();
                String functionName = value.substring(0,value.indexOf("$result"));
                if(functionDeclarations.containsKey(functionName))
                    return true;
            }
            return false;
        }catch (result r){
            return false;
        }
    }



    


    private CExpression getExpression(
            final CFGNode exprNode, final CType pExpectedType, final String pFileName)
            throws result {
        CFGAST un_ast = (CFGAST) exprNode.get_ast(ast_family.getC_UNNORMALIZED());
        CFGAST no_ast = (CFGAST) exprNode.get_ast(ast_family.getC_NORMALIZED());
        FileLocation fileLoc = getLocation(exprNode,pFileName);
        if (no_ast.isConstantExpression()) {
            return createFromOpCode(exprNode, pFileName);
        } else if (no_ast.isConstant() && !exprNode.isGlobalVariable()) {
            return (CExpression) getConstant(exprNode, pExpectedType, fileLoc, pFileName);
        } else {
            return getAssignedIdExpression(exprNode, pExpectedType, fileLoc);
        }
    }


    private CExpression createFromOpCode(
            final CFGNode exprNode, final String pFileName) throws result {

        ast nc_ast = exprNode.get_ast(ast_family.getC_NORMALIZED());
        ast_class operand = nc_ast.children().get(1).as_ast().get_class();

        if(operand.is_subclass_of(ast_class.getNC_ABSTRACT_ARITHMETIC())||
                operand.is_subclass_of(ast_class.getNC_ABSTRACT_BITWISE())){
            return createFromArithmeticOp(exprNode, operand, pFileName);
        }else if(operand.equals(ast_class.getNC_STRUCTORUNIONREF())){
            return createGetElementDotExp(exprNode, pFileName);
        }else if(operand.equals(ast_class.getNC_POINTEREXPR())){
            return null;
            //return new CCastExpression(getLocation(exprNode, pFileName), typeConverter.getCType(pItem
             //       .typeOf()), getExpression(exprNode.getOperand(0), typeConverter.getCType(pItem
              //      .getOperand(0).typeOf()), pFileName));
        }else {
            throw  new UnsupportedOperationException(operand.name());
        }
    }

    /**
     *@Description TODO
     *@Param [varInitPoint, pFileName]
     *@return org.sosy_lab.cpachecker.cfa.ast.c.CExpression
     **/
    private CExpression createGetElementDotExp(final CFGNode varInitNode, final String pFileName)
            throws result {
        ast nc_ast = varInitNode.get_ast(ast_family.getC_NORMALIZED());
        ast_class operand = nc_ast.children().get(1).as_ast().get_class();

        FileLocation fileLocation = getLocation(varInitNode, pFileName);

        return null;
    }


    /**
     *@Description TODO
     *@Param [varInitPoint, operand, pFileName]
     *@return org.sosy_lab.cpachecker.cfa.ast.c.CExpression
     **/
    private CExpression createFromArithmeticOp(
            final CFGNode varInitNode, final ast_class operand, final String pFileName) throws result {

        CFGAST nc_ast = (CFGAST) varInitNode.get_ast(ast_family.getC_NORMALIZED());

        final CType expressionType = typeConverter.getCType((CFGAST) nc_ast.children().get(0).as_ast());

        // TODO: Currently we only support flat expressions, no nested ones. Make this work
        // in the future.

        ast_class operand1 = varInitNode.get_ast(ast_family.getC_NORMALIZED()).get_class(); // First operand
        logger.log(Level.FINE, "Getting id expression for operand 1");
        //CType op1type = typeConverter.getCType();
        CExpression operand1Exp = null;//getExpression(operand1, op1type, pFileName);

        ast_class operand2 = operand; // Second operand
        //CType op2type = typeConverter.getCType();
        logger.log(Level.FINE, "Getting id expression for operand 2");
        CExpression operand2Exp = null;//getExpression(operand2, op2type, pFileName);

        CBinaryExpression.BinaryOperator operation;

        if(operand.equals(ast_class.getNC_ADDEXPR()))
            operation = CBinaryExpression.BinaryOperator.PLUS;
        else if(operand.equals(ast_class.getNC_SUBEXPR()))
            operation = CBinaryExpression.BinaryOperator.MINUS;
        else if(operand.equals(ast_class.getNC_MULEXPR()))
            operation = CBinaryExpression.BinaryOperator.MULTIPLY;
        else if(operand.equals(ast_class.getNC_DIVEXPR()))
            operation = CBinaryExpression.BinaryOperator.DIVIDE;
        else if(operand.equals(ast_class.getNC_MODEXPR()))
            operation = CBinaryExpression.BinaryOperator.MODULO;
        else if(operand.equals(ast_class.getNC_RIGHTASSIGN()))
            operation = CBinaryExpression.BinaryOperator.SHIFT_RIGHT;
        else if(operand.equals(ast_class.getNC_LEFTASSIGN()))
            operation = CBinaryExpression.BinaryOperator.SHIFT_LEFT;
        else if(operand.equals(ast_class.getNC_ANDASSIGN()))
            operation = CBinaryExpression.BinaryOperator.BINARY_AND;
        else if(operand.equals(ast_class.getNC_ORASSIGN()))
            operation = CBinaryExpression.BinaryOperator.BINARY_OR;
        else if(operand.equals(ast_class.getNC_XORASSIGN()))
            operation = CBinaryExpression.BinaryOperator.BINARY_XOR;
        else
            throw new AssertionError("Unhandled operation " + operand.name());


        return new CBinaryExpression(
                getLocation(varInitNode, pFileName),
                expressionType,
                expressionType,
                operand1Exp,
                operand2Exp,
                operation);
    }


    /**
     * Returns whether the first param is a pointer of the type of the second parameter.<br>
     * Examples:
     *
     * <ul>
     *   <li>pointerOf(*int, int) -> true
     *   <li>pointerOf(**int, *int) -> true
     *   <li>pointerOf(int, int*) -> false
     *   <li>pointerOf(int, int) -> false
     * </ul>
     */
    public static boolean pointerOf(CType pPotentialPointer, CType pPotentialPointee) {
        if (pPotentialPointer instanceof CPointerType) {
            return ((CPointerType) pPotentialPointer)
                    .getType()
                    .getCanonicalType()
                    .equals(pPotentialPointee.getCanonicalType());
        } else {
            return false;
        }
    }


    /**
     *@Description the expression of a null pointer. In c, a null pointer means *p=0
     *@Param [pLocation, pType]
     *@return org.sosy_lab.cpachecker.cfa.ast.c.CExpression
     **/
    private CExpression getNull(final FileLocation pLocation, final CType pType) {
        return new CIntegerLiteralExpression(pLocation, pType, BigInteger.ZERO);
    }



}
