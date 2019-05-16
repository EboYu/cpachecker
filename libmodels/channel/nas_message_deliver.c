//TODO for NAS: start from the emm_as.c->_emm_as_data_req->_emm_as_encode/_emm_as_encrypt (message out) to build channel

//UE-Side

void UE_emm_as_recv(){//nas_message_decode
    nas_msg = uePullPlainNASMsgFromCache();
//    decoder_rc = 0;
}

void UE_emm_as_establish_cnf(){//nas_message_decode
    nas_msg = uePullPlainNASMsgFromCache();
//    decoder_rc = 0;
}

void UE_emm_as_data_ind(){//nas_message_decrypt, pull header
    header = uePullPlainNASHeaderFromCache();
//    bytes = 0;
}
//insert after if (as_msg.msgID > 0) {
void UE_emm_as_send(){
    uePushPlainASMsgIntoCache(as_msg);
}


//before nas_message_encode
void UE_emm_as_encode(){
    uePushPlainNASMsgIntoCache(*msg);
}


//MME-Side

void MME_emm_as_recv(){//nas_message_decode
    nas_msg = cnPullPlainNASMsgFromCache();
//    decoder_rc = 0;
}
void MME_emm_as_establish_req(){
    nas_msg = cnPullPlainNASMsgFromCache();
//    decoder_rc = 0;
}

void MME_emm_as_data_ind(){//nas_message_decrypt, pull header
    header = cnPullPlainNASHeaderFromCache();
//    bytes = 0;
}
//insert after if (as_msg.msgID > 0) {
void MME_emm_as_send(){
    cnPushPlainASMsgIntoCache(as_msg);
}


//before nas_message_encode
void MME_emm_as_encode(){
    cnPushPlainNASMsgIntoCache(*msg);
}
