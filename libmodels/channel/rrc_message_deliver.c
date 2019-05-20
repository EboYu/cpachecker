
void ENB_do_MIB(){
    cnPushPlainBCCHBCHMessageIntoCache(*mib);
}
void ENB_do_SIB1(){
    cnPushPlainBCCHDLSCHMessageIntoCache(*bcch_message);
}
void ENB_do_SIB23(){
    cnPushPlainBCCHDLSCHMessageIntoCache(*bcch_message);
}
void ENB_do_RRCConnectionSetup(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void ENB_do_RRCConnectionSetup_BR(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void ENB_do_RRCConnectionReconfiguration_BR(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}
void ENB_do_RRCConnectionReconfiguration(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}
void ENB_do_RRCConnectionReestablishment(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void ENB_do_RRCConnectionReestablishmentReject(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void ENB_do_RRCConnectionReject(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void ENB_do_RRCConnectionRelease(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}

void ENB_do_MBSFNAreaConfig(){
    cnPushPlainMCCHMessageIntoCache(*mcch_message);
}

void ENB_do_DLInformationTransfer(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}
void ENB_do_Paging(){
    cnPushPlainPCCHMessageIntoCache(*pcch_msg);
}

void ENB_do_UECapabilityEnquiry(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}
void ENB_do_SecurityModeCommand(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}

void ENB_rrc_eNB_decode_ccch(){
    *ul_ccch_msg = cnPullPlainULCCCHMessageFromCache();
}

void ENB_rrc_eNB_decode_dcch(){
    *ul_dcch_msg = cnPullPlainULDCCHMessageFromCache();
    *(ue_context_p->ue_context.UE_Capability) = cnPullPlainUECapMsgFromCache();
}

void UE_do_RRCConnectionRequest(){
    uePushPlainULCCCHMessageIntoCache(*ul_ccch_msg);
}
void UE_fill_ue_capability(){
    uePushPlainUECapMessageIntoCache(*UE_EUTRA_Capability);
}

void UE_do_SidelinkUEInformation(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void UE_do_RRCConnectionSetupComplete(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void UE_do_RRCConnectionReconfigurationComplete(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void UE_do_ULInformationTransfer(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void UE_do_MeasurementReport(){
   uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}

void UE_rrc_ue_process_securityModeCommand(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}

void UE_rrc_ue_process_ueCapabilityEnquiry(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}

void UE_rrc_ue_decode_dcch(){//uper_decode
    *dl_dcch_msg = uePullPlainDLDCCHMessageFromCache();
}
void UE_rrc_ue_decode_ccch(){//uper_decode
    *dl_ccch_msg = uePullPlainDLCCCHMessagefromCache();
}

void UE_decode_BCCH_DLSCH_Message(){//uper_decode_complete
    *bcch_message = uePullPlainBCCHDLSCHMessageFromCache();
}

void UE_decode_PCCH_DLSCH_Message(){//uper_decode_complete
    *pcch_message= uePullPlainPCCHMessageFromCache();
}
void UE_decode_MCCH_Message(){//uper_decode_complete
    *mcch = uePullPlainMCCHMessageFromCache();
}

