package org.nulist.plugin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.google.common.base.Optional;
import com.grammatech.cs.*;
import org.nulist.plugin.parser.CFABuilder;
import org.nulist.plugin.parser.CFGFunctionBuilder;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.ast.FileLocation;
import org.sosy_lab.cpachecker.cfa.ast.c.*;
import org.sosy_lab.cpachecker.cfa.model.*;
import org.sosy_lab.cpachecker.cfa.model.c.CAssumeEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CDeclarationEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CFunctionEntryNode;
import org.sosy_lab.cpachecker.cfa.model.c.CStatementEdge;
import org.sosy_lab.cpachecker.cfa.types.MachineModel;
import org.sosy_lab.cpachecker.cfa.types.c.*;

import static org.nulist.plugin.util.ClassTool.*;
import static org.nulist.plugin.util.FileOperations.getLocation;
import static org.nulist.plugin.model.action.ITTIAbstract.*;
import static org.nulist.plugin.parser.CFGParser.*;
/**
 * @ClassName ITTIModelAbstract
 * @Description model itti as a synchronous model in a component
 * @Author Yinbo Yu
 * @Date 4/15/19 11:01 AM
 * @Version 1.0
 **/
public class ITTIModelAbstract {
    private final String functionName = "itti_send_msg_to_task";
    private final LogManager logger;
    private final MachineModel machineModel;
    public CFABuilder cfaBuilder;
    public String projectName;
    public compunit itti_cu;

    public ITTIModelAbstract(CFABuilder builder, String projectName, compunit cu, LogManager logger, MachineModel machineModel){
        cfaBuilder = builder;
        this.projectName = projectName;
        itti_cu = cu;
        this.logger = logger;
        this.machineModel = machineModel;
    }

    /*
     * @Description //copy the original function and inset msg as its input parameter
     * @Param [functionDeclaration]
     * @return void
     **/
    public CFunctionDeclaration functionExtension(CFunctionDeclaration functionDeclaration, CFGFunctionBuilder builder){

        CType msgType = cfaBuilder.typeConverter.typeCache.get("MessageDef".hashCode());
        if(msgType==null){
            throw new RuntimeException("No type of MessageDef");
        }

        CPointerType pointerType = new CPointerType(false,false,msgType);

        FileLocation fileLocation = functionDeclaration.getFileLocation();

        CFunctionType originalFuncType = functionDeclaration.getType();
        CType returnType = originalFuncType.getReturnType();
        List<CType> paramTypes = new ArrayList<>();
        paramTypes.add(pointerType);
        CFunctionType newFuncType = new CFunctionType(returnType,paramTypes, originalFuncType.takesVarArgs());

        List<CParameterDeclaration> originalParams = functionDeclaration.getParameters();
        CParameterDeclaration parameterDeclaration = new CParameterDeclaration(originalParams.get(0).getFileLocation(),pointerType, "msg_p");
        builder.expressionHandler.variableDeclarations.put("msg_p".hashCode(),parameterDeclaration);
        List<CParameterDeclaration> newParams = new ArrayList<>();
        newParams.add(parameterDeclaration);

        return new CFunctionDeclaration(fileLocation,newFuncType,functionDeclaration.getName()+extendSuffix,newParams);
    }



}
