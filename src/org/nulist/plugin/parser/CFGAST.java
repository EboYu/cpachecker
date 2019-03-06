/**
 * @ClassName CFGAST
 * @Description TODO
 * @Author Yinbo Yu
 * @Date 3/6/19 11:58 AM
 * @Version 1.0
 **/
package org.nulist.plugin.parser;

import com.grammatech.cs.*;

public class CFGAST extends ast {

    public CFGAST(long cPtr, boolean cMemoryOwn){
        super(cPtr, cMemoryOwn);
    }

    /**
     *@Description check if the type of a given variable (p) is constant array or vector
     *@Param [type]  ast_field = p's normalized ast' type (getNC_TYPE)
     *@return boolean
     **/
    public static boolean isConstantArrayOrVector(ast_field type){
        try {
            String typeName = type.as_ast().pretty_print();

            if(typeName.startsWith("const")){
                ast_class ac = type.as_ast().get_class();
                if(ac.equals(ast_class.getUC_POINTER())||
                        ac.equals(ast_class.getUC_ARRAY())
                        ||ac.equals(ast_class.getUC_VECTOR_TYPE())
                ){
                    return true;
                }
            }
            return false;
        }catch (result r){
            return false;
        }
    }

    public boolean isStructType(){
        try {
            return this.get_class().equals(ast_class.getUC_STRUCT()) ||
                    this.get(ast_ordinal.getBASE_TYPE()).as_ast().get_class().equals(ast_class.getUC_STRUCT());
        }catch (result r){
            return false;
        }

    }

    public boolean isEnumType(){
        try {
            return this.get_class().equals(ast_class.getUC_ENUM()) ||
                    this.get(ast_ordinal.getBASE_TYPE()).as_ast().get_class().equals(ast_class.getUC_ENUM());
        }catch (result r){
            return false;
        }
    }



    public static boolean isConstantAggregateZero(ast_field type){

        return false;
    }
    /**
     *@Description check if a pointer is null, using its normalized ast
     *@Param []
     *@return boolean
     **/
    public boolean isNullPointer(){
        try {
            ast_field type = this.get(ast_ordinal.getNC_TYPE());//normalized type
            if(type.as_ast().get_class().equals(ast_class.getUC_POINTER())){
                if(this.children().get(1).as_int32()==0){
                    return true;
                }
            }
            return false;
        }catch (result r){
            return false;
        }
    }

    /**
     *@Description check if the assigned value is constant expression, e.g., right child has the type of c:exprs
     *@Param []
     *@return boolean
     **/
    public boolean isConstantExpression(){
        try {
            //normalizedVarInitAST.children().get(1).as_ast().name() = c:exprs
            return this.children().get(1).as_ast().equals(ast_class.getUC_EXPR_CONSTANT());
        }catch (result r){
            return false;
        }
    }

    /**
     *@Description check if a value is a global constant, using NORMALIZED ast
     *@Param []
     *@return boolean
     **/
    public boolean isGlobalConstant(){
        try {
            ast value_ast = this.children().get(1).as_ast();
            if(value_ast.get(ast_ordinal.getBASE_ABS_LOC()).as_symbol().is_global()){//global value
                if(value_ast.get(ast_ordinal.getBASE_TYPE()).as_ast().pretty_print().startsWith("const"))//const value
                    return true;
            }
            return false;
        }catch (result r){
            return false;
        }

    }

    /**
     *@Description check if a value is a constant, include local and global
     *@Param [normalizedVarInitAST]
     *@return boolean
     **/
    public boolean isConstant(){
        try {
            ast value_ast = this.children().get(1).as_ast();
            return value_ast.get(ast_ordinal.getBASE_TYPE()).as_ast().pretty_print().startsWith("const");
        }catch (result r){
            return false;
        }
    }

    public CFGAST getStructType() throws result{
        if(this.get_class().equals(ast_class.getUC_STRUCT())){
            return this;
        }else {//for typedef struct
            return (CFGAST) this.get(ast_ordinal.getBASE_TYPE()).as_ast();
        }
    }

    //ast normalizedVarInitAST
    public boolean isUndef(){
        return false;
    }
}
