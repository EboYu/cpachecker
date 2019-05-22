cn_channel_message_t *CN_channel_message_cache=NULL;

void cnChannelMessageInit(){
    CN_channel_message_cache=ENB_malloc(sizeof(cn_channel_message_t));
}

void cnPushNASMSGIDIntoCache(uint16_t msgID){
    CN_channel_message_cache->nas_message.msgID = msgID;
}

void cnPushPlainNASEMMMsgIntoCache(EMM_msg msg){
	CN_channel_message_cache->nas_message.nas_message.plain.emm=msg;
}

void cnPushPlainNASMsgIntoCache(nas_message_t msg){
	CN_channel_message_cache->nas_message.nas_message=msg;
}

void cnPushPlainNASESMMsgIntoCache(ESM_msg msg){
	CN_channel_message_cache->nas_message.nas_message.plain.esm=msg;
}


void cnPushPlainASMsgIntoCache(as_message_t msg){
	CN_channel_message_cache->nas_message.as_message=msg;
}

EMM_msg cnPullPlainNASEMMMsgFromCache(){
	EMM_msg msg = CN_channel_message_cache->nas_message.nas_message.plain.emm;
	return msg;
}

ESM_msg cnPullPlainNASESMMsgFromCache(){
	ESM_msg msg = CN_channel_message_cache->nas_message.nas_message.plain.esm;
	return msg;
}

as_message_t cnPullPlainASMsgFromCache(){
	as_message_t msg = CN_channel_message_cache->nas_message.as_message;
	return msg;
}

nas_message_t cnPullPlainNASMsgFromCache(){
	nas_message_t msg = CN_channel_message_cache->nas_message.nas_message;
	return msg;
}

nas_message_security_header_t cnPullPlainNASHeaderFromCache(){
	nas_message_security_header_t msg = CN_channel_message_cache->nas_message.nas_message.header;
	return msg;
}

//void cnPushPlainULCCCHMessageIntoCache(LTE_UL_CCCH_Message_t msg){
//	CN_channel_message_cache->rrc_message.message.ul_ccch_msg = msg;
//}

void cnPushPlainDLCCCHMessageIntoCache(LTE_DL_CCCH_Message_t msg){
	CN_channel_message_cache->rrc_message.message.dl_ccch_msg = msg;
}

//void cnPushPlainULDCCHMessageIntoCache(LTE_UL_DCCH_Message_t msg){
//	CN_channel_message_cache->rrc_message.message.ul_dcch_msg = msg;
//}

void cnPushPlainDLDCCHMessageIntoCache(LTE_DL_DCCH_Message_t msg){
	CN_channel_message_cache->rrc_message.message.dl_dcch_msg = msg;
}

void cnPushPlainBCCHBCHMessageIntoCache(LTE_BCCH_BCH_Message_t msg){
	CN_channel_message_cache->rrc_message.message.bcch_bch_msg = msg;
}

void cnPushPlainBCCHDLSCHMessageIntoCache(LTE_BCCH_DL_SCH_Message_t msg){
	CN_channel_message_cache->rrc_message.message.bcch_dl_sch_msg = msg;
}

void cnPushPlainPCCHMessageIntoCache(LTE_PCCH_Message_t msg){
	CN_channel_message_cache->rrc_message.message.pcch_msg = msg;
}

void cnPushPlainMCCHMessageIntoCache(LTE_MCCH_Message_t msg){
	CN_channel_message_cache->rrc_message.message.mcch_msg = msg;
}


LTE_UL_CCCH_Message_t cnPullPlainULCCCHMessageFromCache(){
	LTE_UL_CCCH_Message_t msg = CN_channel_message_cache->rrc_message.message.ul_ccch_msg;
	return msg;
}

LTE_UE_EUTRA_Capability_t cnPullPlainUECapMsgFromCache{
    LTE_UE_EUTRA_Capability_t msg = (LTE_UE_EUTRA_Capability_t)(*ue_cap);
    return msg;
}

//LTE_DL_CCCH_Message_t cnPullPlainDLCCCHMessageIntoCache(){
//	LTE_DL_CCCH_Message_t msg = CN_channel_message_cache->rrc_message.message.dl_ccch_msg;
//	return msg;
//}


//LTE_DL_DCCH_Message_t cnPullPlainDLDCCHMessageIntoCache(){
//	return CN_channel_message_cache->rrc_message.message.dl_dcch_msg;
//}

//LTE_BCCH_BCH_Message_t cnPullPlainBCCHBCHMessageIntoCache(){
//	LTE_BCCH_BCH_Message_t msg = CN_channel_message_cache->rrc_message.message.bcch_bch_msg;
//	return msg;
//}
//
//LTE_BCCH_DL_SCH_Message_t cnPullPlainBCCHDLSCHMessageIntoCache(){
//	LTE_BCCH_DL_SCH_Message_t msg = CN_channel_message_cache->rrc_message.message.bcch_dl_sch_msg;
//	return msg;
//}
//
//LTE_PCCH_Message_t cnPullPlainPCCHMessageIntoCache(){
//	LTE_PCCH_Message_t msg = CN_channel_message_cache->rrc_message.message.pcch_msg;
//	return msg;
//}


LTE_UL_DCCH_Message_t cnPullPlainULDCCHMessageFromCache(){
	return CN_channel_message_cache->rrc_message.message.ul_dcch_msg;
}