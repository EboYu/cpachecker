
void do_MIB(){
    cnPushPlainBCCHBCHMessageIntoCache(*mib);
}
void do_SIB1(){
    cnPushPlainBCCHDLSCHMessageIntoCache(*bcch_message);
}
void do_SIB23(){
    cnPushPlainBCCHDLSCHMessageIntoCache(*bcch_message);
}
void do_RRCConnectionSetup(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void do_RRCConnectionSetup_BR(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void do_RRCConnectionReconfiguration_BR(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}
void do_RRCConnectionReconfiguration(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}
void do_RRCConnectionReestablishment(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void do_RRCConnectionReestablishmentReject(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void do_RRCConnectionReject(){
    cnPushPlainDLCCCHMessageIntoCache(*dl_ccch_msg);
}
void do_RRCConnectionRelease(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}

void do_MBSFNAreaConfig(){
    cnPushPlainMCCHMessageIntoCache(*mcch_message);
}

void do_DLInformationTransfer(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}
void do_Paging(){
    cnPushPlainPCCHMessageIntoCache(*pcch_msg);
}

void do_UECapabilityEnquiry(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}
void do_SecurityModeCommand(){
    cnPushPlainDLDCCHMessageIntoCache(*dl_dcch_msg);
}

void rrc_eNB_decode_ccch(){
    *ul_ccch_msg = cnPullPlainULCCCHMessageFromCache();
}

void rrc_eNB_decode_dcch(){
    *ul_dcch_msg = cnPullPlainULDCCHMessageFromCache();
    *(ue_context_p->ue_context.UE_Capability) = cnPullPlainUECapMsgFromCache();
}

void do_RRCConnectionRequest(){
    uePushPlainULCCCHMessageIntoCache(*ul_ccch_msg);
}
void fill_ue_capability(){
    uePushPlainUECapMessageIntoCache();
}

void do_SidelinkUEInformation(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void do_RRCConnectionSetupComplete(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void do_RRCConnectionReconfigurationComplete(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void do_ULInformationTransfer(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void do_MeasurementReport(){
   uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}
void fill_ue_capability(){
    uePushPlainUECapMessageIntoCache(*UE_EUTRA_Capability);
}
void rrc_ue_process_securityModeCommand(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}

void rrc_ue_process_ueCapabilityEnquiry(){
    uePushPlainULDCCHMessageIntoCache(*ul_dcch_msg);
}

void rrc_ue_decode_dcch(){//uper_decode
    *dl_dcch_msg = uePullPlainDLDCCHMessageFromCache();
}
void rrc_ue_decode_ccch(){//uper_decode
    *dl_ccch_msg = uePullPlainDLCCCHMessagefromCache();
}

void decode_BCCH_DLSCH_Message(){//uper_decode_complete
    *bcch_message = uePullPlainBCCHDLSCHMessageFromCache();
}

void decode_PCCH_DLSCH_Message(){//uper_decode_complete
    *pcch_message= uePullPlainPCCHMessageFromCache();
}
void decode_MCCH_Message(){//uper_decode_complete
    *mcch = uePullPlainMCCHMessageFromCache();
}

