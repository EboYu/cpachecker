ue_channel_message_t *UE_channel_message_cache= NULL;
LTE_UE_EUTRA_Capability_t *ue_cap=NULL;

void ueChannelMessageInit(){
    UE_channel_message_cache=UE_malloc(sizeof(ue_channel_message_t));
}

void uePushNASMSGIDIntoCache(uint16_t msgID){
    UE_channel_message_cache->nas_message.msgID = msgID;
}

void uePushPlainNASEMMMsgIntoCache(EMM_msg msg){
	UE_channel_message_cache->nas_message.nas_message.plain.emm=msg;
}


void uePushPlainNASESMMsgIntoCache(ESM_msg msg){
	UE_channel_message_cache->nas_message.nas_message.plain.esm=msg;
}

void uePushPlainNASMsgIntoCache(nas_message_t msg){
	UE_channel_message_cache->nas_message.nas_message=msg;
}

void uePushPlainASMsgIntoCache(as_message_t msg){
	UE_channel_message_cache->nas_message.as_message=msg;
}

EMM_msg uePullPlainNASEMMMsgFromCache(){
	EMM_msg msg = UE_channel_message_cache->nas_message.nas_message.plain.emm;
	return msg;
}

ESM_msg uePullPlainNASESMMsgFromCache(){
	ESM_msg msg = UE_channel_message_cache->nas_message.nas_message.plain.esm;
	return msg;
}

as_message_t uePullPlainASMsgFromCache(){
	as_message_t msg = UE_channel_message_cache->nas_message.as_message;
	return msg;
}

nas_message_t uePullPlainNASMsgFromCache(){
	nas_message_t msg = UE_channel_message_cache->nas_message.nas_message;
	return msg;
}

nas_message_security_header_t uePullPlainNASHeaderFromCache(){
	nas_message_security_header_t msg = UE_channel_message_cache->nas_message.nas_message.header;
	return msg;
}

void uePushPlainULCCCHMessageIntoCache(LTE_UL_CCCH_Message_t msg){
	UE_channel_message_cache->rrc_message.message.ul_ccch_msg = msg;
}
//
//void uePushPlainDLCCCHMessageIntoCache(LTE_DL_CCCH_Message_t msg){
//	UE_channel_message_cache->rrc_message.message.dl_ccch_msg = msg;
//}

void uePushPlainULDCCHMessageIntoCache(LTE_UL_DCCH_Message_t msg){
	UE_channel_message_cache->rrc_message.message.ul_dcch_msg = msg;
}

void uePushPlainUECapMessageIntoCache(LTE_UE_EUTRA_Capability_t msg){
    ue_cap=UE_malloc(sizeof(LTE_UE_EUTRA_Capability_t));
	*ue_cap = msg;
}

//void uePushPlainDLDCCHMessageIntoCache(LTE_DL_DCCH_Message_t msg){
//	UE_channel_message_cache->rrc_message.message.dl_dcch_msg = msg;
//}

//void uePushPlainBCCHBCHMessageIntoCache(LTE_BCCH_BCH_Message_t msg){
//	UE_channel_message_cache->rrc_message.message.bcch_bch_msg = msg;
//}

//void uePushPlainBCCHDLSCHMessageIntoCache(LTE_BCCH_DL_SCH_Message_t msg){
//	UE_channel_message_cache->rrc_message.message.bcch_dl_sch_msg = msg;
//}

//void uePushPlainPCCHMessageIntoCache(LTE_PCCH_Message_t msg){
//	UE_channel_message_cache->rrc_message.message.pcch_msg = msg;
//}

//LTE_UL_CCCH_Message_t uePullPlainULCCCHMessageIntoCache(){
//	LTE_UL_CCCH_Message_t msg = UE_channel_message_cache->rrc_message.message.ul_ccch_msg;
//	return msg;
//}

LTE_DL_CCCH_Message_t uePullPlainDLCCCHMessagefromCache(){
	LTE_DL_CCCH_Message_t msg = UE_channel_message_cache->rrc_message.message.dl_ccch_msg;
	return msg;
}

//LTE_UL_DCCH_Message_t uePullPlainULDCCHMessagefromCache(){
//	return UE_channel_message_cache->rrc_message.message.ul_dcch_msg;
//}

LTE_DL_DCCH_Message_t uePullPlainDLDCCHMessageFromCache(){
	return UE_channel_message_cache->rrc_message.message.dl_dcch_msg;
}

LTE_BCCH_BCH_Message_t uePullPlainBCCHBCHMessageFromCache(){
	LTE_BCCH_BCH_Message_t msg = UE_channel_message_cache->rrc_message.message.bcch_bch_msg;
	return msg;
}

LTE_BCCH_DL_SCH_Message_t uePullPlainBCCHDLSCHMessageFromCache(){
	LTE_BCCH_DL_SCH_Message_t msg = UE_channel_message_cache->rrc_message.message.bcch_dl_sch_msg;
	return msg;
}

LTE_PCCH_Message_t uePullPlainPCCHMessageFromCache(){
	LTE_PCCH_Message_t msg = UE_channel_message_cache->rrc_message.message.pcch_msg;
	return msg;
}

LTE_MCCH_Message_t uePullPlainMCCHMessageFromCache(){
	LTE_MCCH_Message_t msg = UE_channel_message_cache->rrc_message.message.mcch_msg;
	return msg;
}
