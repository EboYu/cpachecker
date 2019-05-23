cn_channel_message_t *Adv_cn_channel_message_cache=NULL;
ue_channel_message_t *Adv_ue_channel_message_cache= NULL;

void adversaryMessageInit(){
    Adv_cn_channel_message_cache=ENB_malloc(sizeof(cn_channel_message_t));
    Adv_ue_channel_message_cache=UE_malloc(sizeof(ue_channel_message_t));
}

void messageListenInstrumentation(){

}