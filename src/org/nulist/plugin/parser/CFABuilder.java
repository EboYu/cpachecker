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
                funcName.equals("RCconfig_L1")||
                funcName.equals("RCconfig_S1")||
                funcName.equals("rrc_enb_process_itti_msg")||
                funcName.equals("s1ap_eNB_process_itti_msg")||
                funcName.equals("s1ap_eNB_task")||
                funcName.equals("nas_intertask_interface")||
                funcName.equals("mme_app_thread")||
                funcName.equals("s1ap_mme_thread")||
                funcName.equals("RCconfig_X2")||
                funcName.equals("RCconfig_flexran")||
                funcName.equals("RCconfig_gtpu")||
                funcName.equals("RCconfig_macrlc")||
                funcName.equals("binary_search_float")||
                funcName.equals("binary_search_int")||
                funcName.equals("check_handovers")||
                funcName.equals("check_permitted_alphabet_1")||
                funcName.equals("config_dedicated")||
                funcName.equals("do_HandoverCommand")||
                funcName.equals("do_MIB_SL")||
                funcName.equals("eNB_app_register")||
                funcName.equals("eNB_app_register_x2")||
                funcName.equals("eNB_app_task")||
                funcName.equals("eNB_top")||
                funcName.equals("flexran_rrc_eNB_generate_defaultRRCConnectionReconfiguration")||
                funcName.equals("free_transport")||
                funcName.equals("from_earfcn")||
                funcName.equals("get_adjacent_cell_mod_id")||
                funcName.equals("get_softmodem_optmask")||
                funcName.equals("get_thread_parallel_conf")||
                funcName.equals("get_thread_worker_conf")||
                funcName.equals("get_uldl_offset")||
                funcName.equals("handle_reconfiguration")||
                funcName.equals("init_UE_list")||
                funcName.equals("init_eNB_afterRU")||
                funcName.equals("init_eNB_proc")||
                funcName.equals("init_transport")||
                funcName.equals("kill_eNB_proc")||
                funcName.equals("mac_UE_get_rrc_status")||
                funcName.equals("mac_eNB_get_rrc_status")||
                funcName.equals("mac_eNB_rrc_ul_failure")||
                funcName.equals("mac_eNB_rrc_ul_in_sync")||
                funcName.equals("mac_eNB_rrc_uplane_failure")||
                funcName.equals("mac_rrc_data_ind")||
                funcName.equals("mac_rrc_data_ind_ue")||
                funcName.equals("mac_rrc_data_req")||
                funcName.equals("mac_rrc_data_req_ue")||
                funcName.equals("mac_top_cleanup")||
                funcName.equals("mac_top_init_eNB")||
                funcName.equals("mac_ue_ccch_success_ind")||
                funcName.equals("print_opp_meas")||
                funcName.equals("rdtsc_oai")||
                funcName.equals("release_UE_in_freeList")||
                funcName.equals("remove_UE_from_freelist")||
                funcName.equals("reset_meas")||
                funcName.equals("reset_opp_meas")||
                funcName.equals("rrc_data_ind")||
                funcName.equals("rrc_data_ind_ue")||
                funcName.equals("rrc_data_req_ue")||
                funcName.equals("rrc_eNB_configure_rbs_handover")||
                funcName.equals("rrc_eNB_free_UE")||
                funcName.equals("rrc_eNB_generate_HO_RRCConnectionReconfiguration")||
                funcName.equals("rrc_eNB_generate_RRCConnectionReconfiguration_SCell")||
                funcName.equals("rrc_eNB_reconfigure_DRBs")||
                funcName.equals("rrc_eNB_remove_ue_context")||
                funcName.equals("rrc_eNB_send_S1AP_UE_CONTEXT_RELEASE_REQ")||
                funcName.equals("rrc_in_sync_ind")||
                funcName.equals("rrc_out_of_sync_ind")||
                funcName.equals("rrc_rx_tx")||
                funcName.equals("rrc_top_cleanup_eNB")||
                funcName.equals("rrc_ue_tree_s_RB_NFIND")||
                funcName.equals("rrc_ue_tree_s_RB_PREV")||
                funcName.equals("rxtx")||
                funcName.equals("s1ap_direction2String")||
                funcName.equals("s1ap_eNB_get_MME_from_instance")||
                funcName.equals("s1ap_eNB_handle_overload_start")||
                funcName.equals("s1ap_eNB_handle_overload_stop")||
                funcName.equals("s1ap_eNB_init")||
                funcName.equals("s1ap_eNB_itti_send_sctp_close_association")||
                funcName.equals("s1ap_eNB_prepare_internal_data")||
                funcName.equals("s1ap_eNB_remove_mme_desc")||
                funcName.equals("s1ap_handle_criticality")||
                funcName.equals("s1ap_mme_map_RB_REMOVE")||
                funcName.equals("s1ap_mme_map_RB_REMOVE_COLOR")||
                funcName.equals("s1ap_ue_map_RB_MINMAX")||
                funcName.equals("s1ap_ue_map_RB_NEXT")||
                funcName.equals("select_Bearers_SubjectToStatusTransfer_ItemExtIEs_S1AP_criticality_type")||
                funcName.equals("select_Bearers_SubjectToStatusTransfer_ItemExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_Bearers_SubjectToStatusTransfer_ItemIEs_S1AP_criticality_type")||
                funcName.equals("select_Bearers_SubjectToStatusTransfer_ItemIEs_S1AP_value_type")||
                funcName.equals("select_CellTrafficTraceIEs_S1AP_criticality_type")||
                funcName.equals("select_CellTrafficTraceIEs_S1AP_value_type")||
                funcName.equals("select_ConnectionEstablishmentIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_ConnectionEstablishmentIndicationIEs_S1AP_value_type")||
                funcName.equals("select_DeactivateTraceIEs_S1AP_criticality_type")||
                funcName.equals("select_DeactivateTraceIEs_S1AP_value_type")||
                funcName.equals("select_DownlinkNASTransport_IEs_S1AP_criticality_type")||
                funcName.equals("select_DownlinkNASTransport_IEs_S1AP_value_type")||
                funcName.equals("select_DownlinkNonUEAssociatedLPPaTransport_IEs_S1AP_criticality_type")||
                funcName.equals("select_DownlinkNonUEAssociatedLPPaTransport_IEs_S1AP_value_type")||
                funcName.equals("select_DownlinkS1cdma2000tunnellingIEs_S1AP_criticality_type")||
                funcName.equals("select_DownlinkS1cdma2000tunnellingIEs_S1AP_value_type")||
                funcName.equals("select_DownlinkUEAssociatedLPPaTransport_IEs_S1AP_criticality_type")||
                funcName.equals("select_DownlinkUEAssociatedLPPaTransport_IEs_S1AP_value_type")||
                funcName.equals("select_ENBCPRelocationIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_ENBCPRelocationIndicationIEs_S1AP_value_type")||
                funcName.equals("select_ENBConfigurationTransferIEs_S1AP_criticality_type")||
                funcName.equals("select_ENBConfigurationTransferIEs_S1AP_value_type")||
                funcName.equals("select_ENBConfigurationUpdateAcknowledgeIEs_S1AP_criticality_type")||
                funcName.equals("select_ENBConfigurationUpdateAcknowledgeIEs_S1AP_value_type")||
                funcName.equals("select_ENBConfigurationUpdateFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_ENBConfigurationUpdateFailureIEs_S1AP_value_type")||
                funcName.equals("select_ENBConfigurationUpdateIEs_S1AP_criticality_type")||
                funcName.equals("select_ENBConfigurationUpdateIEs_S1AP_value_type")||
                funcName.equals("select_ENBDirectInformationTransferIEs_S1AP_criticality_type")||
                funcName.equals("select_ENBDirectInformationTransferIEs_S1AP_value_type")||
                funcName.equals("select_ENBStatusTransferIEs_S1AP_criticality_type")||
                funcName.equals("select_ENBStatusTransferIEs_S1AP_value_type")||
                funcName.equals("select_E_RABAdmittedItemIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABAdmittedItemIEs_S1AP_value_type")||
                funcName.equals("select_E_RABDataForwardingItemIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABDataForwardingItemIEs_S1AP_value_type")||
                funcName.equals("select_E_RABFailedToResumeItemResumeReqIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABFailedToResumeItemResumeReqIEs_S1AP_value_type")||
                funcName.equals("select_E_RABFailedToResumeItemResumeResIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABFailedToResumeItemResumeResIEs_S1AP_value_type")||
                funcName.equals("select_E_RABFailedtoSetupItemHOReqAckIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABFailedtoSetupItemHOReqAckIEs_S1AP_value_type")||
                funcName.equals("select_E_RABInformationListIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABInformationListIEs_S1AP_value_type")||
                funcName.equals("select_E_RABItemIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABItemIEs_S1AP_value_type")||
                funcName.equals("select_E_RABModificationConfirmIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABModificationConfirmIEs_S1AP_value_type")||
                funcName.equals("select_E_RABModificationIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABModificationIndicationIEs_S1AP_value_type")||
                funcName.equals("select_E_RABModifyItemBearerModConfIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABModifyItemBearerModConfIEs_S1AP_value_type")||
                funcName.equals("select_E_RABModifyItemBearerModResIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABModifyItemBearerModResIEs_S1AP_value_type")||
                funcName.equals("select_E_RABModifyRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABModifyRequestIEs_S1AP_value_type")||
                funcName.equals("select_E_RABModifyResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABModifyResponseIEs_S1AP_value_type")||
                funcName.equals("select_E_RABNotToBeModifiedItemBearerModIndIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABNotToBeModifiedItemBearerModIndIEs_S1AP_value_type")||
                funcName.equals("select_E_RABReleaseCommandIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABReleaseCommandIEs_S1AP_value_type")||
                funcName.equals("select_E_RABReleaseIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABReleaseIndicationIEs_S1AP_value_type")||
                funcName.equals("select_E_RABReleaseItemBearerRelCompIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABReleaseItemBearerRelCompIEs_S1AP_value_type")||
                funcName.equals("select_E_RABReleaseResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABReleaseResponseIEs_S1AP_value_type")||
                funcName.equals("select_E_RABSetupItemBearerSUResIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABSetupItemBearerSUResIEs_S1AP_value_type")||
                funcName.equals("select_E_RABSetupItemCtxtSUResIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABSetupItemCtxtSUResIEs_S1AP_value_type")||
                funcName.equals("select_E_RABSetupRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABSetupRequestIEs_S1AP_value_type")||
                funcName.equals("select_E_RABSetupResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABSetupResponseIEs_S1AP_value_type")||
                funcName.equals("select_E_RABToBeModifiedItemBearerModIndIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeModifiedItemBearerModIndIEs_S1AP_value_type")||
                funcName.equals("select_E_RABToBeModifiedItemBearerModReqIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeModifiedItemBearerModReqIEs_S1AP_value_type")||
                funcName.equals("select_E_RABToBeModifyItemBearerModReqExtIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeModifyItemBearerModReqExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_E_RABToBeSetupItemBearerSUReqExtIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeSetupItemBearerSUReqExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_E_RABToBeSetupItemBearerSUReqIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeSetupItemBearerSUReqIEs_S1AP_value_type")||
                funcName.equals("select_E_RABToBeSetupItemCtxtSUReqExtIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeSetupItemCtxtSUReqExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_E_RABToBeSetupItemCtxtSUReqIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeSetupItemCtxtSUReqIEs_S1AP_value_type")||
                funcName.equals("select_E_RABToBeSetupItemHOReqIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeSetupItemHOReqIEs_S1AP_value_type")||
                funcName.equals("select_E_RABToBeSetupItemHOReq_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeSetupItemHOReq_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_E_RABToBeSwitchedDLItemIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeSwitchedDLItemIEs_S1AP_value_type")||
                funcName.equals("select_E_RABToBeSwitchedULItemIEs_S1AP_criticality_type")||
                funcName.equals("select_E_RABToBeSwitchedULItemIEs_S1AP_value_type")||
                funcName.equals("select_ErrorIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_ErrorIndicationIEs_S1AP_value_type")||
                funcName.equals("select_HandoverCancelAcknowledgeIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverCancelAcknowledgeIEs_S1AP_value_type")||
                funcName.equals("select_HandoverCancelIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverCancelIEs_S1AP_value_type")||
                funcName.equals("select_HandoverCommandIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverCommandIEs_S1AP_value_type")||
                funcName.equals("select_HandoverFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverFailureIEs_S1AP_value_type")||
                funcName.equals("select_HandoverNotifyIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverNotifyIEs_S1AP_value_type")||
                funcName.equals("select_HandoverPreparationFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverPreparationFailureIEs_S1AP_value_type")||
                funcName.equals("select_HandoverRequestAcknowledgeIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverRequestAcknowledgeIEs_S1AP_value_type")||
                funcName.equals("select_HandoverRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverRequestIEs_S1AP_value_type")||
                funcName.equals("select_HandoverRequiredIEs_S1AP_criticality_type")||
                funcName.equals("select_HandoverRequiredIEs_S1AP_value_type")||
                funcName.equals("select_ImmediateMDT_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_ImmediateMDT_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_InitialContextSetupFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_InitialContextSetupFailureIEs_S1AP_value_type")||
                funcName.equals("select_InitialContextSetupRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_InitialContextSetupRequestIEs_S1AP_value_type")||
                funcName.equals("select_InitialContextSetupResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_InitialContextSetupResponseIEs_S1AP_value_type")||
                funcName.equals("select_InitialUEMessage_IEs_S1AP_criticality_type")||
                funcName.equals("select_InitialUEMessage_IEs_S1AP_value_type")||
                funcName.equals("select_InitiatingMessage_S1AP_criticality_type")||
                funcName.equals("select_InitiatingMessage_S1AP_value_type")||
                funcName.equals("select_KillRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_KillRequestIEs_S1AP_value_type")||
                funcName.equals("select_KillResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_KillResponseIEs_S1AP_value_type")||
                funcName.equals("select_LastVisitedEUTRANCellInformation_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_LastVisitedEUTRANCellInformation_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_LocationReportIEs_S1AP_criticality_type")||
                funcName.equals("select_LocationReportIEs_S1AP_value_type")||
                funcName.equals("select_LocationReportingControlIEs_S1AP_criticality_type")||
                funcName.equals("select_LocationReportingControlIEs_S1AP_value_type")||
                funcName.equals("select_LocationReportingFailureIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_LocationReportingFailureIndicationIEs_S1AP_value_type")||
                funcName.equals("select_MDTMode_ExtensionIE_S1AP_criticality_type")||
                funcName.equals("select_MDTMode_ExtensionIE_S1AP_value_type")||
                funcName.equals("select_MDT_Configuration_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_MDT_Configuration_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_MMECPRelocationIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_MMECPRelocationIndicationIEs_S1AP_value_type")||
                funcName.equals("select_MMEConfigurationTransferIEs_S1AP_criticality_type")||
                funcName.equals("select_MMEConfigurationTransferIEs_S1AP_value_type")||
                funcName.equals("select_MMEConfigurationUpdateAcknowledgeIEs_S1AP_criticality_type")||
                funcName.equals("select_MMEConfigurationUpdateAcknowledgeIEs_S1AP_value_type")||
                funcName.equals("select_MMEConfigurationUpdateFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_MMEConfigurationUpdateFailureIEs_S1AP_value_type")||
                funcName.equals("select_MMEConfigurationUpdateIEs_S1AP_criticality_type")||
                funcName.equals("select_MMEConfigurationUpdateIEs_S1AP_value_type")||
                funcName.equals("select_MMEDirectInformationTransferIEs_S1AP_criticality_type")||
                funcName.equals("select_MMEDirectInformationTransferIEs_S1AP_value_type")||
                funcName.equals("select_MMEStatusTransferIEs_S1AP_criticality_type")||
                funcName.equals("select_MMEStatusTransferIEs_S1AP_value_type")||
                funcName.equals("select_NASDeliveryIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_NASDeliveryIndicationIEs_S1AP_value_type")||
                funcName.equals("select_NASNonDeliveryIndication_IEs_S1AP_criticality_type")||
                funcName.equals("select_NASNonDeliveryIndication_IEs_S1AP_value_type")||
                funcName.equals("select_OverloadStartIEs_S1AP_criticality_type")||
                funcName.equals("select_OverloadStartIEs_S1AP_value_type")||
                funcName.equals("select_OverloadStopIEs_S1AP_criticality_type")||
                funcName.equals("select_OverloadStopIEs_S1AP_value_type")||
                funcName.equals("select_PWSFailureIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_PWSFailureIndicationIEs_S1AP_value_type")||
                funcName.equals("select_PWSRestartIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_PWSRestartIndicationIEs_S1AP_value_type")||
                funcName.equals("select_PagingIEs_S1AP_criticality_type")||
                funcName.equals("select_PagingIEs_S1AP_value_type")||
                funcName.equals("select_PathSwitchRequestAcknowledgeIEs_S1AP_criticality_type")||
                funcName.equals("select_PathSwitchRequestAcknowledgeIEs_S1AP_value_type")||
                funcName.equals("select_PathSwitchRequestFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_PathSwitchRequestFailureIEs_S1AP_value_type")||
                funcName.equals("select_PathSwitchRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_PathSwitchRequestIEs_S1AP_value_type")||
                funcName.equals("select_ProSeAuthorized_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_ProSeAuthorized_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_RecommendedCellItemIEs_S1AP_criticality_type")||
                funcName.equals("select_RecommendedCellItemIEs_S1AP_value_type")||
                funcName.equals("select_RecommendedENBItemIEs_S1AP_criticality_type")||
                funcName.equals("select_RecommendedENBItemIEs_S1AP_value_type")||
                funcName.equals("select_RerouteNASRequest_IEs_S1AP_criticality_type")||
                funcName.equals("select_RerouteNASRequest_IEs_S1AP_value_type")||
                funcName.equals("select_ResetAcknowledgeIEs_S1AP_criticality_type")||
                funcName.equals("select_ResetAcknowledgeIEs_S1AP_value_type")||
                funcName.equals("select_ResetIEs_S1AP_criticality_type")||
                funcName.equals("select_ResetIEs_S1AP_value_type")||
                funcName.equals("select_RetrieveUEInformationIEs_S1AP_criticality_type")||
                funcName.equals("select_RetrieveUEInformationIEs_S1AP_value_type")||
                funcName.equals("select_S1SetupFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_S1SetupFailureIEs_S1AP_value_type")||
                funcName.equals("select_S1SetupRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_S1SetupRequestIEs_S1AP_value_type")||
                funcName.equals("select_S1SetupResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_S1SetupResponseIEs_S1AP_value_type")||
                funcName.equals("select_SONConfigurationTransfer_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_SONConfigurationTransfer_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_SONInformationReply_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_SONInformationReply_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_SONInformation_ExtensionIE_S1AP_criticality_type")||
                funcName.equals("select_SONInformation_ExtensionIE_S1AP_value_type")||
                funcName.equals("select_SourceeNB_ToTargeteNB_TransparentContainer_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_SourceeNB_ToTargeteNB_TransparentContainer_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_SuccessfulOutcome_S1AP_criticality_type")||
                funcName.equals("select_SuccessfulOutcome_S1AP_value_type")||
                funcName.equals("select_SupportedTAs_Item_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_SupportedTAs_Item_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_TAIItemIEs_S1AP_criticality_type")||
                funcName.equals("select_TAIItemIEs_S1AP_value_type")||
                funcName.equals("select_TimeSynchronisationInfo_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_TimeSynchronisationInfo_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_TraceActivation_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_TraceActivation_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("select_TraceFailureIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_TraceFailureIndicationIEs_S1AP_value_type")||
                funcName.equals("select_TraceStartIEs_S1AP_criticality_type")||
                funcName.equals("select_TraceStartIEs_S1AP_value_type")||
                funcName.equals("select_UECapabilityInfoIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_UECapabilityInfoIndicationIEs_S1AP_value_type")||
                funcName.equals("select_UEContextModificationConfirmIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextModificationConfirmIEs_S1AP_value_type")||
                funcName.equals("select_UEContextModificationFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextModificationFailureIEs_S1AP_value_type")||
                funcName.equals("select_UEContextModificationIndicationIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextModificationIndicationIEs_S1AP_value_type")||
                funcName.equals("select_UEContextModificationRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextModificationRequestIEs_S1AP_value_type")||
                funcName.equals("select_UEContextModificationResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextModificationResponseIEs_S1AP_value_type")||
                funcName.equals("select_UEContextReleaseCommand_IEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextReleaseCommand_IEs_S1AP_value_type")||
                funcName.equals("select_UEContextReleaseComplete_IEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextReleaseComplete_IEs_S1AP_value_type")||
                funcName.equals("select_UEContextReleaseRequest_IEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextReleaseRequest_IEs_S1AP_value_type")||
                funcName.equals("select_UEContextResumeFailureIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextResumeFailureIEs_S1AP_value_type")||
                funcName.equals("select_UEContextResumeRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextResumeRequestIEs_S1AP_value_type")||
                funcName.equals("select_UEContextResumeResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextResumeResponseIEs_S1AP_value_type")||
                funcName.equals("select_UEContextSuspendRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextSuspendRequestIEs_S1AP_value_type")||
                funcName.equals("select_UEContextSuspendResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_UEContextSuspendResponseIEs_S1AP_value_type")||
                funcName.equals("select_UEInformationTransferIEs_S1AP_criticality_type")||
                funcName.equals("select_UEInformationTransferIEs_S1AP_value_type")||
                funcName.equals("select_UERadioCapabilityMatchRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_UERadioCapabilityMatchRequestIEs_S1AP_value_type")||
                funcName.equals("select_UERadioCapabilityMatchResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_UERadioCapabilityMatchResponseIEs_S1AP_value_type")||
                funcName.equals("select_UE_associatedLogicalS1_ConnectionItemResAck_S1AP_criticality_type")||
                funcName.equals("select_UE_associatedLogicalS1_ConnectionItemResAck_S1AP_value_type")||
                funcName.equals("select_UE_associatedLogicalS1_ConnectionItemRes_S1AP_criticality_type")||
                funcName.equals("select_UE_associatedLogicalS1_ConnectionItemRes_S1AP_value_type")||
                funcName.equals("select_UnsuccessfulOutcome_S1AP_criticality_type")||
                funcName.equals("select_UnsuccessfulOutcome_S1AP_value_type")||
                funcName.equals("select_UplinkNASTransport_IEs_S1AP_criticality_type")||
                funcName.equals("select_UplinkNASTransport_IEs_S1AP_value_type")||
                funcName.equals("select_UplinkNonUEAssociatedLPPaTransport_IEs_S1AP_criticality_type")||
                funcName.equals("select_UplinkNonUEAssociatedLPPaTransport_IEs_S1AP_value_type")||
                funcName.equals("select_UplinkS1cdma2000tunnellingIEs_S1AP_criticality_type")||
                funcName.equals("select_UplinkS1cdma2000tunnellingIEs_S1AP_value_type")||
                funcName.equals("select_UplinkUEAssociatedLPPaTransport_IEs_S1AP_criticality_type")||
                funcName.equals("select_UplinkUEAssociatedLPPaTransport_IEs_S1AP_value_type")||
                funcName.equals("select_WriteReplaceWarningRequestIEs_S1AP_criticality_type")||
                funcName.equals("select_WriteReplaceWarningRequestIEs_S1AP_value_type")||
                funcName.equals("select_WriteReplaceWarningResponseIEs_S1AP_criticality_type")||
                funcName.equals("select_WriteReplaceWarningResponseIEs_S1AP_value_type")||
                funcName.equals("select_X2TNLConfigurationInfo_ExtIEs_S1AP_criticality_type")||
                funcName.equals("select_X2TNLConfigurationInfo_ExtIEs_S1AP_extensionValue_type")||
                funcName.equals("set_default_frame_parms")||
                funcName.equals("start_meas")||
                funcName.equals("stop_eNB")||
                funcName.equals("stop_meas")||
                funcName.equals("to_earfcn_UL")||
                funcName.equals("uid_linear_allocator_free")||
                funcName.equals("wakeup_prach_eNB")||
                funcName.equals("wakeup_prach_eNB_br")||
                funcName.equals("wakeup_rxtx")||
                funcName.equals("wakeup_tx")||
                funcName.equals("wakeup_txfh")||
                funcName.equals("BearerPool_RB_NEXT")||
                funcName.equals("_emm_attach_security")||
                funcName.equals("_nas_message_decrypt")||
                funcName.equals("_nas_message_plain_decode")||
                funcName.equals("_nas_message_protected_decode")||
                funcName.equals("_nas_new_esm_procedures")||
                funcName.equals("_pdn_disconnect_get_pid")||
                funcName.equals("construct_s1ap_mme_full_reset_req")||
                funcName.equals("emm_as_initialize")||
                funcName.equals("emm_attach_security")||
                funcName.equals("emm_context_dump")||
                funcName.equals("emm_ctx_clear_attribute_valid")||
                funcName.equals("emm_ctx_clear_auth_vector")||
                funcName.equals("emm_ctx_get_new_ue_id")||
                funcName.equals("emm_ctx_set_imei")||
                funcName.equals("emm_ctx_set_imeisv")||
                funcName.equals("emm_ctx_set_imsi")||
                funcName.equals("emm_ctx_set_lvr_tai")||
                funcName.equals("emm_ctx_set_valid_guti")||
                funcName.equals("emm_ctx_set_valid_old_guti")||
                funcName.equals("emm_data_context_add_imsi")||
                funcName.equals("emm_data_context_add_old_guti")||
                funcName.equals("emm_data_context_remove_mobile_ids")||
                funcName.equals("emm_esm_initialize")||
                funcName.equals("emm_fsm_get_state_str")||
                funcName.equals("emm_fsm_initialize")||
                funcName.equals("emm_msg_decode")||
                funcName.equals("emm_msg_decode_header")||
                funcName.equals("emm_recv_tracking_area_update_req_type_normal")||
                funcName.equals("emm_reg_initialize")||
                funcName.equals("emm_sap_initialize")||
                funcName.equals("encode_attach_request")||
                funcName.equals("encode_detach_type")||
                funcName.equals("encode_eps_attach_result")||
                funcName.equals("encode_eps_attach_type")||
                funcName.equals("encode_eps_update_result")||
                funcName.equals("encode_eps_update_type")||
                funcName.equals("encode_linked_eps_bearer_identity")||
                funcName.equals("encode_message_type")||
                funcName.equals("encode_pdn_type")||
                funcName.equals("encode_request_type")||
                funcName.equals("encode_security_header_type")||
                funcName.equals("encode_service_type")||
                funcName.equals("encode_u8_additional_update_result")||
                funcName.equals("encode_u8_csfb_response")||
                funcName.equals("encode_u8_eps_attach_type")||
                funcName.equals("encode_u8_esm_information_transfer_flag")||
                funcName.equals("encode_u8_guti_type")||
                funcName.equals("encode_u8_radio_priority")||
                funcName.equals("encode_u8_ue_radio_capability_information_update_needed")||
                funcName.equals("eps_qos_bit_rate_ext_value")||
                funcName.equals("eps_qos_bit_rate_value")||
                funcName.equals("esm_data_get_ipv4_addr")||
                funcName.equals("esm_data_get_ipv4v6_addr")||
                funcName.equals("esm_data_get_ipv6_addr")||
                funcName.equals("esm_proc_dedicated_eps_bearer_context_request")||
                funcName.equals("esm_proc_status")||
                funcName.equals("esm_sap_initialize")||
                funcName.equals("file_bname_init")||
                funcName.equals("find_mnc_length")||
                funcName.equals("free_esm_bearer_context")||
                funcName.equals("get_esm_transaction_procedure")||
                funcName.equals("get_nas_common_procedure_guti_realloc")||
                funcName.equals("get_nas_con_mngt_procedure_service_request")||
                funcName.equals("hashtable_apply_callback_on_elements")||
                funcName.equals("hashtable_create")||
                funcName.equals("hashtable_destroy")||
                funcName.equals("hashtable_dump_content")||
                funcName.equals("hashtable_free")||
                funcName.equals("hashtable_get")||
                funcName.equals("hashtable_init")||
                funcName.equals("hashtable_insert")||
                funcName.equals("hashtable_is_key_exists")||
                funcName.equals("hashtable_rc_code2string")||
                funcName.equals("hashtable_remove")||
                funcName.equals("hashtable_resize")||
                funcName.equals("hashtable_ts_get_elements")||
                funcName.equals("hashtable_ts_get_keys")||
                funcName.equals("hashtable_ts_resize")||
                funcName.equals("hashtable_uint64_apply_callback_on_elements")||
                funcName.equals("hashtable_uint64_create")||
                funcName.equals("hashtable_uint64_destroy")||
                funcName.equals("hashtable_uint64_dump_content")||
                funcName.equals("hashtable_uint64_free")||
                funcName.equals("hashtable_uint64_get")||
                funcName.equals("hashtable_uint64_init")||
                funcName.equals("hashtable_uint64_insert")||
                funcName.equals("hashtable_uint64_is_key_exists")||
                funcName.equals("hashtable_uint64_remove")||
                funcName.equals("hashtable_uint64_resize")||
                funcName.equals("hashtable_uint64_ts_apply_callback_on_elements")||
                funcName.equals("hashtable_uint64_ts_free")||
                funcName.equals("hashtable_uint64_ts_get_elements")||
                funcName.equals("hashtable_uint64_ts_get_keys")||
                funcName.equals("hashtable_uint64_ts_insert")||
                funcName.equals("hashtable_uint64_ts_is_key_exists")||
                funcName.equals("hashtable_uint64_ts_remove")||
                funcName.equals("hashtable_uint64_ts_resize")||
                funcName.equals("is_nas_attach_complete_received")||
                funcName.equals("is_nas_common_procedure_guti_realloc_running")||
                funcName.equals("is_nas_specific_procedure_detach_running")||
                funcName.equals("is_nas_tau_complete_received")||
                funcName.equals("lowerlayer_establish")||
                funcName.equals("mme_api_subscribe")||
                funcName.equals("mme_app_bearer_context_delete")||
                funcName.equals("mme_app_convert_imsi_to_imsi_mme")||
                funcName.equals("mme_app_copy_imsi")||
                funcName.equals("mme_app_delete_s10_procedures")||
                funcName.equals("mme_app_delete_s11_procedures")||
                funcName.equals("mme_app_dump_protocol_configuration_options")||
                funcName.equals("mme_app_dump_ue_contexts")||
                funcName.equals("mme_app_free_bearer_context")||
                funcName.equals("mme_app_free_pdn_connection")||
                funcName.equals("mme_app_imsi_compare")||
                funcName.equals("mme_app_imsi_to_string")||
                funcName.equals("mme_app_imsi_to_u64")||
                funcName.equals("mme_app_is_imsi_empty")||
                funcName.equals("mme_app_string_to_imsi")||
                funcName.equals("mme_app_trigger_mme_initiated_dedicated_bearer_deactivation_procedure")||
                funcName.equals("mme_ue_context_notified_new_ue_s1ap_id_association")||
                funcName.equals("nas_itti_establish_rej")||
                funcName.equals("nas_message_decrypt")||
                funcName.equals("nas_message_decrypt_abstract")||
                funcName.equals("nas_message_header_decode")||
                funcName.equals("nas_new_service_request_procedure")||
                funcName.equals("obj_hashtable_dump_content")||
                funcName.equals("obj_hashtable_free")||
                funcName.equals("obj_hashtable_get_keys")||
                funcName.equals("obj_hashtable_is_key_exists")||
                funcName.equals("obj_hashtable_no_free_key_callback")||
                funcName.equals("obj_hashtable_remove")||
                funcName.equals("obj_hashtable_resize")||
                funcName.equals("obj_hashtable_ts_dump_content")||
                funcName.equals("obj_hashtable_ts_free")||
                funcName.equals("obj_hashtable_ts_get")||
                funcName.equals("obj_hashtable_ts_get_keys")||
                funcName.equals("obj_hashtable_ts_is_key_exists")||
                funcName.equals("obj_hashtable_ts_resize")||
                funcName.equals("obj_hashtable_uint64_destroy")||
                funcName.equals("obj_hashtable_uint64_dump_content")||
                funcName.equals("obj_hashtable_uint64_free")||
                funcName.equals("obj_hashtable_uint64_get")||
                funcName.equals("obj_hashtable_uint64_get_keys")||
                funcName.equals("obj_hashtable_uint64_insert")||
                funcName.equals("obj_hashtable_uint64_is_key_exists")||
                funcName.equals("obj_hashtable_uint64_remove")||
                funcName.equals("obj_hashtable_uint64_resize")||
                funcName.equals("obj_hashtable_uint64_ts_free")||
                funcName.equals("obj_hashtable_uint64_ts_get_keys")||
                funcName.equals("obj_hashtable_uint64_ts_insert")||
                funcName.equals("obj_hashtable_uint64_ts_is_key_exists")||
                funcName.equals("obj_hashtable_uint64_ts_remove")||
                funcName.equals("obj_hashtable_uint64_ts_resize")||
                funcName.equals("s1ap_generate_bearer_to_forward")||
                funcName.equals("s1ap_handle_criticality")||
                funcName.equals("s1ap_handle_timer_expiry")||
                funcName.equals("s1ap_is_s11_sgw_teid_in_list")||
                funcName.equals("s1ap_mme_handle_enb_status_transfer")||
                funcName.equals("s1ap_mme_handle_erab_setup_response")||
                funcName.equals("s1ap_mme_handle_error_ind_message")||
                funcName.equals("s1ap_mme_handle_handover_resource_allocation_failure")||
                funcName.equals("s1ap_mme_timer_map_compare_id")||
                funcName.equals("s1ap_send_enb_deregistered_ind")||
                funcName.equals("s1ap_timer_insert")||
                funcName.equals("s1ap_timer_map_RB_FIND")||
                funcName.equals("s1ap_timer_map_RB_INSERT")||
                funcName.equals("s1ap_timer_map_RB_INSERT_COLOR")||
                funcName.equals("s1ap_timer_map_RB_MINMAX")||
                funcName.equals("s1ap_timer_map_RB_NEXT")||
                funcName.equals("s1ap_timer_map_RB_REMOVE")||
                funcName.equals("s1ap_timer_map_RB_REMOVE_COLOR")||
                funcName.equals("s1ap_timer_remove_ue")||
                funcName.equals("s1ap_ue_compare_by_enb_ue_s1ap_id_cb")||
                funcName.equals("update_mme_app_stats_default_bearer_add")||
                funcName.equals("update_mme_app_stats_s1u_bearer_add")||
                funcName.equals("ComputeOPc")||
                funcName.equals("IdleMode_get_nb_plmns")||
                funcName.equals("IdleMode_get_rplmn_index")||
                funcName.equals("SIB2SoundingPresent")||
                funcName.equals("SIB2defaultPagingCycle")||
                funcName.equals("SIB2mac_ContentionResolutionTimer")||
                funcName.equals("SIB2modificationPeriodCoeff")||
                funcName.equals("SIB2nB")||
                funcName.equals("SIB2numberOfRA_Preambles")||
                funcName.equals("SIB2powerRampingStep")||
                funcName.equals("SIB2preambleInitialReceivedTargetPower")||
                funcName.equals("SIB2preambleTransMax")||
                funcName.equals("SIB2ra_ResponseWindowSize")||
                funcName.equals("SIBallowed")||
                funcName.equals("SIBbarred")||
                funcName.equals("SIBreserved")||
                funcName.equals("_emm_msg_decode_header")||
                funcName.equals("_luhn")||
                funcName.equals("_nas_message_decrypt")||
                funcName.equals("_nas_message_header_decode")||
                funcName.equals("_nas_message_plain_decode")||
                funcName.equals("_nas_message_protected_decode")||
                funcName.equals("_nas_timer_db_init")||
                funcName.equals("arfcn_to_freq")||
                funcName.equals("as_message_decode")||
                funcName.equals("as_message_encode")||
                funcName.equals("as_message_send")||
                funcName.equals("binary_search_float")||
                funcName.equals("binary_search_int")||
                funcName.equals("check_trigger_meas_event")||
                funcName.equals("decode_SL_Discovery_Message")||
                funcName.equals("dup_octet_string")||
                funcName.equals("emm_attach_type2str")||
                funcName.equals("emm_detach_type2str")||
                funcName.equals("emm_esm_primitive2str")||
                funcName.equals("emm_fsm_event2str")||
                funcName.equals("emm_fsm_status2str")||
                funcName.equals("emm_identity_type2str")||
                funcName.equals("emm_main_cleanup")||
                funcName.equals("emm_main_get_registered_plmn")||
                funcName.equals("emm_msg_decode")||
                funcName.equals("emmas_to_str")||
                funcName.equals("encode_detach_type")||
                funcName.equals("encode_eps_attach_result")||
                funcName.equals("encode_eps_attach_type")||
                funcName.equals("encode_eps_bearer_identity")||
                funcName.equals("encode_eps_update_result")||
                funcName.equals("encode_eps_update_type")||
                funcName.equals("encode_identity_type_2")||
                funcName.equals("encode_linked_eps_bearer_identity")||
                funcName.equals("encode_message_type")||
                funcName.equals("encode_pdn_type")||
                funcName.equals("encode_procedure_transaction_identity")||
                funcName.equals("encode_protocol_discriminator")||
                funcName.equals("encode_request_type")||
                funcName.equals("encode_security_header_type")||
                funcName.equals("encode_service_type")||
                funcName.equals("encode_u8_additional_update_result")||
                funcName.equals("encode_u8_ciphering_key_sequence_number")||
                funcName.equals("encode_u8_csfb_response")||
                funcName.equals("encode_u8_esm_information_transfer_flag")||
                funcName.equals("encode_u8_guti_type")||
                funcName.equals("encode_u8_imeisv_request")||
                funcName.equals("encode_u8_radio_priority")||
                funcName.equals("encode_u8_tmsi_status")||
                funcName.equals("encode_u8_ue_radio_capability_information_update_needed")||
                funcName.equals("esm_data_get_ipv4_addr")||
                funcName.equals("esm_data_get_ipv4v6_addr")||
                funcName.equals("esm_data_get_ipv6_addr")||
                funcName.equals("esm_ebr_state2str")||
                funcName.equals("esm_main_cleanup")||
                funcName.equals("esm_pt_get_status")||
                funcName.equals("esm_pt_state2str")||
                funcName.equals("esm_sap_initialize")||
                funcName.equals("esm_sap_primitive2str")||
                funcName.equals("fill_SLSS")||
                funcName.equals("find_user_from_fd")||
                funcName.equals("free_octet_string")||
                funcName.equals("freq_to_arfcn10")||
                funcName.equals("gen_emm_data")||
                funcName.equals("gen_network_record_from_conf")||
                funcName.equals("gen_user_data")||
                funcName.equals("get_adjacent_cell_id")||
                funcName.equals("get_config_from_file")||
                funcName.equals("get_msin_parity")||
                funcName.equals("get_plmn_index")||
                funcName.equals("get_softmodem_optmask")||
                funcName.equals("init_SL_preconfig")||
                funcName.equals("mac_get_rrc_status")||
                funcName.equals("mac_rrc_data_ind_ue")||
                funcName.equals("mac_rrc_data_req_ue")||
                funcName.equals("mac_ue_ccch_success_ind")||
                funcName.equals("make_plmn_from_conf")||
                funcName.equals("nas_itti_protected_msg")||
                funcName.equals("nas_message_decrypt")||
                funcName.equals("nas_network_cleanup")||
                funcName.equals("nas_network_initialize")||
                funcName.equals("nas_network_process_data")||
                funcName.equals("nas_proc_cleanup")||
                funcName.equals("nas_proc_disable_s1_mode")||
                funcName.equals("nas_proc_establish_rej")||
                funcName.equals("nas_proc_get_eps")||
                funcName.equals("nas_timer_handle_signal_expiry")||
                funcName.equals("nas_timer_init")||
                funcName.equals("nas_ue_process_events")||
                funcName.equals("network_api_close")||
                funcName.equals("network_api_decode_data")||
                funcName.equals("network_api_encode_data")||
                funcName.equals("network_api_get_data")||
                funcName.equals("network_api_get_fd")||
                funcName.equals("network_api_initialize")||
                funcName.equals("network_api_read_data")||
                funcName.equals("network_api_send_data")||
                funcName.equals("parse_Xplmn")||
                funcName.equals("parse_config_file")||
                funcName.equals("parse_plmn_param")||
                funcName.equals("parse_plmns")||
                funcName.equals("parse_ue_user_data")||
                funcName.equals("parse_user_plmns_conf")||
                funcName.equals("rrc_control_socket_thread_fct")||
                funcName.equals("rrc_data_ind")||
                funcName.equals("rrc_data_ind_ue")||
                funcName.equals("rrc_detach_from_eNB")||
                funcName.equals("rrc_in_sync_ind")||
                funcName.equals("rrc_init_global_param")||
                funcName.equals("rrc_out_of_sync_ind")||
                funcName.equals("rrc_rx_tx_ue")||
                funcName.equals("rrc_t310_expiration")||
                funcName.equals("rrc_top_cleanup_ue")||
                funcName.equals("rrc_ue_generate_MeasurementReport")||
                funcName.equals("tlv_decode_perror")||
                funcName.equals("tlv_encode_perror")||
                funcName.equals("ue_meas_filtering")||
                funcName.equals("ue_measurement_report_triggering")||
                funcName.equals("user_api_close")||
                funcName.equals("user_plmns_free")||
                funcName.equals("usim_api_write")||
                funcName.equals("write_emm_data")||
                funcName.equals("write_user_data")||
                funcName.equals("eNB_thread_prach")||
                funcName.equals("eNB_thread_prach_br")||
                funcName.equals("process_stats_thread")||
                funcName.equals("release_thread")||
                funcName.equals("wait_on_condition")||
                funcName.equals("wait_sync")||
                funcName.equals("bearer_state2string")||
                funcName.equals("emm_main_initialize")||
                funcName.equals("esm_ebr_initialize")||
                funcName.equals("esm_ebr_state2string")||
                funcName.equals("esm_main_initialize")||
                funcName.equals("mme_api_get_emm_config")||
                funcName.equals("mme_api_get_esm_config")||
                funcName.equals("mme_app_dump_bearer_context")||
                funcName.equals("mme_app_dump_pdn_context")||
                funcName.equals("mme_app_dump_ue_context")||
                funcName.equals("nas_proc_initialize")||
                funcName.equals("nas_timer_cleanup")||
                funcName.equals("obj_hashtable_ts_create")||
                funcName.equals("obj_hashtable_ts_destroy")||
                funcName.equals("obj_hashtable_ts_init")||
                funcName.equals("s1ap_enb_find_ue_by_s11_sgw_teid_cb")||
                funcName.equals("_esm_ebr_context_check_identifiers")||
                funcName.equals("_esm_ebr_context_check_precedence")||
                funcName.equals("encode_ms_network_feature_support")||
                funcName.equals("encode_voice_domain_preference_and_ue_usage_setting")||
                funcName.equals("esm_ebr_context_check_tft")||
                funcName.equals("esm_ebr_context_get_pid")||
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
