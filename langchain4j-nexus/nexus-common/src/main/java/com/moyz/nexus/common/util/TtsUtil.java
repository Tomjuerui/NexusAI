package com.moyz.nexus.common.util;

import com.moyz.nexus.common.cosntant.NexusConstant;
import com.moyz.nexus.common.vo.TtsSetting;

public class TtsUtil {

    /**
     * 如果系统设置的语音合成器类型是后端合成，并且当前聊天设置的返回内容是音频，则表示将文本转语音
     *
     * @param ttsSetting        tts设置
     * @param answerContentType 内容类型
     * @return 是否要进行文本转语音
     */
    public static boolean needTts(TtsSetting ttsSetting, int answerContentType) {
        return NexusConstant.TtsConstant.SYNTHESIZER_SERVER.equals(ttsSetting.getSynthesizerSide()) && answerContentType == NexusConstant.ConversationConstant.ANSWER_CONTENT_TYPE_AUDIO;
    }
}
