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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.nulist.plugin.parser.CFGAST.*;
import static org.nulist.plugin.parser.CFGAST.dumpASTWITHClass;
import static org.nulist.plugin.parser.CFGNode.*;
import static org.nulist.plugin.parser.CFGParser.*;
import static org.nulist.plugin.util.CFGDumping.dumpCFG2Dot;
import static org.nulist.plugin.util.FileOperations.*;
import static org.nulist.plugin.util.ClassTool.*;
import static org.nulist.plugin.FunctionTest.*;
import static org.nulist.plugin.model.action.ITTIAbstract.*;
/**
 * @ClassName CFABuilder
 * @Description For a C file
 * @Author Yinbo Yu
 * @Date 2/27/19 4:18 PM
 * @Version 1.0
 **/
public class CFABuilder implements Serializable {

    public static final long serialVersionUID = 4040269724332602192L;
    public final LogManager logger;
    public final MachineModel machineModel;

    public final CFGTypeConverter typeConverter;
    public String projectName ="";
    public String projectPrefix="";

    public final List<Path> parsedFiles = new ArrayList<>();

    // Function name -> Function declaration
    public Map<String, CFunctionDeclaration> functionDeclarations;
    public NavigableMap<String, FunctionEntryNode> functions;
    protected NavigableMap<String, FunctionEntryNode> systemFunctions;
    protected List<Pair<ADeclaration, String>> globalVariableDeclarations ;
    //protected final Map<Integer, ADeclaration> globalVariableDeclarations = new HashMap<>();
    public SortedSetMultimap<String, CFANode> cfaNodes;
    public Map<String, CFGFunctionBuilder> cfgFunctionBuilderMap = new HashMap<>();
    public CFGHandleExpression expressionHandler;

    public CFABuilder(final LogManager pLogger, final MachineModel pMachineModel, String projectName) {
        logger = pLogger;
        machineModel = pMachineModel;
        this.projectName = projectName;
        if(projectName.equals(UE))
            projectPrefix = "UE_";
        else if(projectName.equals(ENB))
            projectPrefix = "ENB_";
        else if(projectName.equals(MME))
            projectPrefix = "MME_";

        typeConverter = new CFGTypeConverter(logger);

        functionDeclarations = new HashMap<>();

        functions = new TreeMap<>();
        systemFunctions = new TreeMap<>();
        cfaNodes = TreeMultimap.create();
        globalVariableDeclarations = new ArrayList<>();
        expressionHandler = new CFGHandleExpression(logger,"",projectName,typeConverter);

    }


    //TODO shall add all function declarations
    public List<Pair<ADeclaration, String>> getGlobalVariableDeclarations(){
        if(globalVariableDeclarations.isEmpty() ||
                globalVariableDeclarations.size()!= expressionHandler.globalDeclarations.size()){
            globalVariableDeclarations = new ArrayList<>();
            for(ADeclaration gvar:expressionHandler.globalDeclarations.values())
                globalVariableDeclarations.add(Pair.of(gvar,gvar.getName()));
        }
        return globalVariableDeclarations;
    }


    public void addNode(String funcName, CFANode nd) {
        cfaNodes.put(funcName, nd);
    }

    public void addParsedFile(Path filePath){
        parsedFiles.add(filePath);
    }

    public void basicBuild(compunit cu, String projectName)throws result{

        String pFileName = cu.normalized_name();
        // Iterate over all procedures in the compilation unit
        // procedure = function

        //expressionHandler.setGlobalVariableDeclarations(globalVariableDeclarations);
        /* create global variable and function declaration*/
        for (compunit_procedure_iterator proc_it = cu.procedures();
             !proc_it.at_end(); proc_it.advance()) {
            procedure proc = proc_it.current();
            buildGlobalDeclaration(proc, pFileName, cu.is_user());
        }

        parsedFiles.add(Paths.get(pFileName));
    }

    public void buildGlobalDeclaration(procedure proc, String pFileName, boolean userFile)throws result{
        if(proc.get_kind().equals(procedure_kind.getUSER_DEFINED())){//
            String funcName = proc.name();
            if((funcName.equals("main") && !isProjectMainFunction(pFileName,projectName)) ||
                    ignoredFunction(pFileName,funcName)||
                    cfgFunctionBuilderMap.containsKey(funcName)) //oai has inline functions and asn generated codes have several same functions
                return;

            //System.out.println(funcName);
            funcName=projectPrefix+funcName;
            CFGFunctionBuilder cfgFunctionBuilder =
                    new CFGFunctionBuilder(logger, typeConverter, proc,funcName,proc.name(), pFileName, this);
            // add function declaration
            CFunctionDeclaration functionDeclaration = cfgFunctionBuilder.handleFunctionDeclaration();

            functionDeclarations.put(funcName, functionDeclaration);
            expressionHandler.globalDeclarations.put(funcName.hashCode(), functionDeclaration);
            // handle the function definition
            CFunctionEntryNode en = cfgFunctionBuilder.handleFunctionDefinition();

            functions.put(funcName, en);
            cfgFunctionBuilderMap.put(funcName,cfgFunctionBuilder);
//            if(proc.get_kind().equals(procedure_kind.getUSER_DEFINED())){
//
//            }else
//                systemFunctions.put(funcName, en);

        }else if(proc.get_kind().equals(procedure_kind.getLIBRARY())){
            String funcName = proc.name();
            CFGFunctionBuilder cfgFunctionBuilder =
                    new CFGFunctionBuilder(logger, typeConverter, proc,funcName,proc.name(), pFileName, this);
            // add function declaration
            CFunctionDeclaration functionDeclaration = cfgFunctionBuilder.handleFunctionDeclaration();

            //functionDeclarations.put(funcName, functionDeclaration);

            expressionHandler.globalDeclarations.put(funcName.hashCode(), functionDeclaration);

        }else if(userFile && proc.get_kind().equals(procedure_kind.getFILE_INITIALIZATION())
                && proc.name().contains("Global_Initialization")){
            visitGlobalItem(proc,projectName);
        }
    }

    /**
     *@Description input is a C file
     *@Param [cu]
     *@return org.sosy_lab.cpachecker.cfa.ParseResult
     **/
    public void build(compunit cu) throws result {

        for (compunit_procedure_iterator proc_it = cu.procedures();
             !proc_it.at_end(); proc_it.advance()) {
            procedure proc = proc_it.current();
            if(proc.get_kind().equals(procedure_kind.getUSER_DEFINED())){
                String funcName = proc.name();
                if(!functionFilter(cu.name(),funcName)){
                    funcName = projectPrefix+funcName;
                    if(cfgFunctionBuilderMap.containsKey(funcName)){
                        System.out.println(funcName);
                        CFGFunctionBuilder cfgFunctionBuilder = cfgFunctionBuilderMap.get(funcName);
                        if(!cfgFunctionBuilder.isFinished){
                            boolean nofinish = notfinishFunctionBuild(cu.name(),funcName.replace(projectPrefix,""));
                            cfgFunctionBuilder.visitFunction(!nofinish);
                        }
                    }
                }
            }
        }
    }

    private boolean notfinishFunctionBuild(String fileaname, String functionName){
        if(projectName.equals(UE)){
            return  functionName.equals("fill_ue_capability")||
                    (fileaname.endsWith("asn1_msg.c")&& functionName.startsWith("do_") && !functionName.equals("do_MIB_SL")) ||
                    functionName.equals("rrc_ue_process_securityModeCommand")||
                    functionName.equals("rrc_ue_process_ueCapabilityEnquiry")||
                    functionName.equals("rrc_ue_decode_dcch")||
                    functionName.equals("rrc_ue_decode_ccch")||
                    functionName.equals("decode_BCCH_DLSCH_Message")||
                    functionName.equals("decode_PCCH_DLSCH_Message")||
                    functionName.equals("decode_MCCH_Message")||
                    functionName.equals("nas_message_encode") ||//EMM message
                    functionName.equals("esm_msg_encode") ||//ESM message
                    functionName.equals("nas_message_decode") ||
                    functionName.equals("nas_message_decrypt") ||
                    functionName.equals("_emm_as_send") ||
                    functionName.equals("_emm_as_encode") ||
                    functionName.equals("_emm_as_data_ind") ||
                    functionName.equals("_emm_as_establish_cnf") ||
                    functionName.equals("_emm_as_recv") ||
                    functionName.equals("uper_encode_to_buffer") ||
                    functionName.equals("uper_decode_complete") ||
                    functionName.equals("uper_decode");
        }else if(projectName.equals(MME)){
            return functionName.equals("nas_message_encode") ||//EMM message
                    functionName.equals("esm_msg_encode") ||//ESM message
                    functionName.equals("nas_message_decode") ||
                    functionName.equals("nas_message_decrypt") ||
                    functionName.equals("_emm_as_send") ||
                    functionName.equals("_emm_as_encode") ||
                    functionName.equals("_emm_as_data_ind") ||
                    functionName.equals("_emm_as_establish_req") ||
                    functionName.equals("_emm_as_recv") ||
                    functionName.equals("s1ap_generate_downlink_nas_transport");
        }else {//eNB
            return  (fileaname.endsWith("asn1_msg.c") && functionName.startsWith("do_") && !functionName.equals("do_MIB_SL")&& !functionName.contains("Handover")) ||
                    functionName.equals("mac_rrc_data_req") ||
                    functionName.equals("rrc_eNB_decode_ccch")||
                    functionName.equals("rrc_eNB_decode_dcch") ||
                    functionName.equals("uper_encode_to_buffer") ||
                    functionName.equals("uper_decode_complete") ||
                    functionName.equals("uper_decode") ||
                    functionName.equals("s1ap_eNB_handle_nas_first_req")||
                    functionName.equals("s1ap_eNB_nas_uplink")||
                    functionName.equals("s1ap_eNB_nas_non_delivery_ind");
        }
    }


    private boolean ignoredFunction(String filename, String funcName){
        return funcName.equals("cmpint") ||
                funcName.equals("ASN__STACK_OVERFLOW_CHECK") ||
                funcName.equals("rrc_control_socket_init") ||
                funcName.contains("ASN_DEBUG")||
                funcName.contains("_ASN_STACK_OVERFLOW_CHECK")||
                funcName.startsWith("dump_") ||
                funcName.startsWith("memb_")||
                funcName.equals("init_UE_stub_single_thread")||
                funcName.contains("_constraint")||
                (projectName.equals(UE)&& filename.endsWith("asn1_msg.c") &&
                        (funcName.equals("do_MIB")||
                                funcName.equals("do_MIB_SL")||
                                funcName.equals("do_SIB1")||
                                funcName.equals("do_SIB23")||
                                funcName.equals("do_RRCConnectionSetup")||
                                funcName.equals("do_RRCConnectionSetup_BR")||
                                funcName.equals("do_RRCConnectionReconfiguration_BR")||
                                funcName.equals("do_RRCConnectionReconfiguration")||
                                funcName.equals("do_RRCConnectionReestablishment")||
                                funcName.equals("do_RRCConnectionReestablishmentReject")||
                                funcName.equals("do_RRCConnectionReject")||
                                funcName.equals("do_RRCConnectionRelease")||
                                funcName.equals("do_MBSFNAreaConfig")||
                                funcName.equals("do_DLInformationTransfer")||
                                funcName.equals("do_Paging")||
                                funcName.equals("do_UECapabilityEnquiry")||
                                funcName.equals("do_HandoverPreparation")||
                                funcName.equals("do_HandoverCommand")||
                                funcName.equals("do_SecurityModeCommand")))||
                ((filename.endsWith("lte-uesoftmodem.c")||filename.endsWith("lte-softmodem.c")) &&
                        !funcName.equals("main") &&
                        !funcName.equals("set_default_frame_parms") &&
                        !funcName.equals("get_options") )||
                (filename.endsWith("lte-ue.c") &&
                        !funcName.equals("init_UE") &&
                        !funcName.equals("init_ue_vars"))||
                (projectName.equals(ENB)&& filename.endsWith("asn1_msg.c") &&
                        (funcName.equals("do_RRCConnectionRequest")||
                                funcName.equals("do_SidelinkUEInformation")||
                                funcName.equals("do_RRCConnectionSetupComplete")||
                                funcName.equals("do_RRCConnectionSetup_BR")||
                                funcName.equals("do_RRCConnectionReconfiguration_BR")||
                                funcName.equals("do_RRCConnectionReconfigurationComplete")||
                                funcName.equals("do_MeasurementReport")||
                                funcName.equals("fill_ue_capability")||
                                funcName.equals("do_ULInformationTransfer")))||
                funcName.equals("mainOld")||
                funcName.equals("init_UE_threads")||
                funcName.equals("init_thread")||
                funcName.equals("init_UE_stub")||
                funcName.equals("UE_thread_synch")||
                funcName.equals("UE_thread")||
                funcName.equals("xer__print2s")||
                funcName.equals("xer_sprint")||
                funcName.startsWith("asn_")||
                (funcName.startsWith("decode_") && !(filename.endsWith("rrc_UE.c")))||
                funcName.startsWith("nas_message_decode")||
                funcName.endsWith("test")||
                funcName.equals("rrc_ue_task")||
                funcName.equals("nas_ue_task")||
                funcName.equals("rrc_enb_task")||
                funcName.equals("L1_thread")||
                funcName.equals("L1_thread_tx")||
                funcName.equals("rrc_enb_process_itti_msg")||
                funcName.equals("s1ap_eNB_process_itti_msg")||
                funcName.equals("s1ap_eNB_task")||
                funcName.equals("nas_intertask_interface")||
                funcName.equals("mme_app_thread")||
                funcName.equals("s1ap_mme_thread")||
                (projectName.equals(MME) && filename.contains("CMakeFiles/r10.5"))||
                (filename.contains("openair2/LAYER2/MAC/main_ue.c") && !(funcName.equals("mac_top_init_ue")||funcName.equals("l2_init_ue")))||
                (filename.contains("openair2/LAYER2/MAC/main.c") && (funcName.equals("init_slice_info")||funcName.equals("rlc_mac_init_global_param")));

    }

    private boolean functionFilter(String filename, String functionName){
        return isITTITaskProcessFunction(functionName)||
                functionName.equals("create_tasks_ue")||//start itti tasks in ue
                functionName.equals("create_tasks")||
                (filename.contains("openair2/LAYER2/MAC/main_ue.c") && !(functionName.equals("mac_top_init_ue")||functionName.equals("l2_init_ue")))||
                (filename.contains("openair2/LAYER2/MAC/main.c") && (functionName.equals("init_slice_info")||functionName.equals("rlc_mac_init_global_param")));//start itti tasks in enb
    }

    /**
     *@Description global variables and their initialization
     * in Codesurfer, all global variables are initialized in the following procedures:
     *                            Global_Initialization_0==> no initialization
     *                            Global_Initialization_1==> static initialization
     *@Param [global_initialization, pFileName]
     *@return void
     **/
    private void visitGlobalItem(procedure global_initialization, String projectName) throws result {


        point_set pointSet = global_initialization.points();
        List<String> variableList = new ArrayList<>();
        for(point_set_iterator point_it = pointSet.cbegin();
            !point_it.at_end(); point_it.advance()){
            //point p = point_it.current();
            point node =  point_it.current();

            //f
            if(isVariable_Initialization(node)|| isExpression(node)){

                try {
                    if(!fileFilter(node.file_line().get_first().name(), projectName))
                        continue;
                }catch (result r){
                    System.out.println(node.get_procedure().get_compunit().name()+":"+node.toString());
                    throw new RuntimeException(r);
                }
                ast un_ast = node.get_ast(ast_family.getC_UNNORMALIZED());
                symbol variableSym = un_ast.get(ast_ordinal.getUC_ABS_LOC()).as_symbol();
                String variableName =variableSym.name();

                String pFileName = node.file_line().get_first().name();
                FileLocation fileLocation = getLocation(node, pFileName);

                // Support static and other storage classes
                CStorageClass storageClass= getStorageClass(un_ast);
                String normalizedName = projectPrefix+variableName;
                if (storageClass == CStorageClass.STATIC) {
                    //file static
                    normalizedName = "static__"+normalizedName;
                    storageClass = CStorageClass.AUTO;
                }

                if(variableList.contains(normalizedName) && node.get_ast(ast_family.getC_NORMALIZED()).is_a(ast_class.getNC_BLOCKASSIGN())){
                    continue;
                }

                if(expressionHandler.globalDeclarations.containsKey(normalizedName.hashCode()))
                    continue;

                //global variable is initialized static
                ast init = un_ast.get(ast_ordinal.getUC_STATIC_INIT()).as_ast();

                CType varType = typeConverter.getCType(un_ast.get(ast_ordinal.getBASE_TYPE()).as_ast(), expressionHandler);
                variableList.add(normalizedName);

                // void (*funA)(int)=&myFun;
//                if(typeConverter.isFunctionPointerType(varType)){
//                    printASTField(un_ast);
//                    //varType = typeConverter.convertCFuntionType(varType, variableName, fileLocation);
//                }


                CInitializer initializer = null;
                if(init.is_a(ast_class.getUC_STATIC_INITIALIZER())){
                    //System.out.println(un_ast.toString());
                    if(variableName.equals("mme_app_desc")){
                        ast no_ast = node.get_ast(ast_family.getC_NORMALIZED());
                        ast orginal = no_ast.get(ast_ordinal.getNC_ORIGINAL()).as_ast();
                        initializer = expressionHandler.getInitializerFromOriginal(orginal.children().get(1).as_ast(),varType,fileLocation);
                    } else if(un_ast.pretty_print().equals("mcc_mnc_list")){//can only get error from codesurfer CFG
                        //read from txt
                        initializer = expressionHandler.getInitializerFromTXT(varType,fileLocation);
                    }else{
                        initializer = expressionHandler.getInitializerFromUC(init,varType,fileLocation);
                    }
                }

                CSimpleDeclaration newDecl =
                        new CVariableDeclaration(
                                fileLocation,
                                true,
                                storageClass,
                                varType,
                                normalizedName,
                                variableName,
                                normalizedName,
                                initializer);

                expressionHandler.globalDeclarations.put(normalizedName.hashCode(),(ADeclaration) newDecl);
            }
        }
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


}
