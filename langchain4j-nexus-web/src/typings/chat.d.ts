declare namespace Chat {

	interface ChatMessage {
		uuid: string | '',
		contentType: number // 2: text, 3: audio
		createTime: string
		thinkingContent: string //思考过�?		remark: string
		audioUuid: string
		audioUrl: string
		audioDuration: number // in seconds
		messageRole?: number
		children: ChatMessage[] //AI回复的消�?		aiModelPlatform?: string
		attachmentUrls: string[]
		isRefMemoryEmbedding: boolean //是否引用记忆向量
		isRefEmbedding: boolean //是否是引用知识库向量
		isRefGraph: boolean //是否是引用知识库图谱

		//Frontend only
		inversion?: boolean
		error?: boolean
		thinking?: boolean //是否正在思�?		loading?: boolean
		audioPlayState: AudioPlayState
		state?: Map<string, string> //消息状态描�?	}

	interface ConversationPreset {
		id: string
		uuid: string
		title: string
		remark: string
		aiSystemMessage: string

		used: boolean
	}

	interface ConvToPresetRel {
		id: string
		uuid: string
		userConvId: string
		presetConvId: string
	}

	//会话关联的知识库信息
	interface ConvKnowledge {
		id: string
		uuid: string
		title: string
		isMine: boolean
		isPublic: boolean
		kbInfo: KnowledgeBase.Info
		isEnable: boolean //该知识库是否可用
	}

	interface ConfigVoice {
		param_name: string // 用于API请求的参数名�?		model: string
		platform: string
	}

	interface AudioConfig {
		voice: ConfigVoice
	}

	interface Conversation {
		title: string
		uuid: string
		remark: string
		aiSystemMessage: string
		understandContextEnable: boolean
		loadedAll: boolean
		loadedFirstPageMsg: boolean
		minMsgUuid?: string | ''
		mcpIds: string[]
		kbIds: string[] // 关联的知识库ID
		convKnowledgeList: ConvKnowledge[] //关联的知识库包装信息
		answerContentType: number // 1: auto, 2: text, 3: audio
		isAutoplayAnswer: boolean //聊天时音频类型的响应内容是否自动播放
		isEnableThinking: boolean //是否启用思考过�?		isEnableWebSearch: boolean //是否启用网络搜索
		audioConfig: AudioConfig //语音配置
	}

	interface ConvWithMessages {
		uuid: string
		data: ChatMessage[]
	}

	interface ChatState {
		active: string
		usingContext: boolean
		conversations: Conversation[]
		chats: ConvWithMessages[]
		loadingMsgs: Set<string>
		presetConvs: ConversationPreset[]
		msgToMemoryRef: Map<string, MemoryEmbedding[]>
		msgToEmbeddingRef: Map<string, KnowledgeBase.QaRecordEmbeddingRef[]>
    msgToGraphRef: Map<string, KnowledgeBase.QaRecordGraphRef>
    loadingGraphRef: Map<string, boolean>
	}

	interface ConversationRequest {
		prompt: string,
		conversationUuid?: string
		parentMessageId?: string
	}

	interface ConversationResponse {
		text: string
	}

	interface AudioInfo {
		url: string
		uuid: string
		duration: number // in seconds
	}

	interface MetaData {
		question: {
			tokens: number,
			uuid: string
		},
		answer: {
			tokens: number,
			uuid: string
		},
		audioInfo: AudioInfo
	}

	interface ConvMsgListResp {
		minMsgUuid: string
		msgList: Chat.ChatMessage[]
	}

	interface ConversationsResp {
		total: number
		records: Chat.Conversation[]
	}

	interface Prompt {
		renderKey: string
		renderValue: string
		id: number
		act: string
		prompt: string
	}

	interface DrawState {
		loadingUuid: string
		loading: boolean
		myDraws: Draw[] //倒序，队尾的为最新数�?	}


	interface Draw {
		id?: number
		uuid: string
		prompt: string
		aiModelName: string
		originalImageUuid?: string
		originalImageUrl: string
		maskImageUuid?: string
		maskImageUrl: string
		interactingMethod: number
		processStatus: number   //1:processing,2:fail,3:success
		processStatusRemark: string

		aiModelPlatform: string
		//绘图成功后生成的图片
		imageUuids: string[]
		imageUrls: string[]
		createTime: string
		isPublic: boolean
		isStar: boolean
		starCount: number
		userUuid: string
		userName: string
		dynamicParams: any
	}

	interface DrawListResp {
		minId: number
		draws: Draw[]
	}

	interface DrawComment {
		uuid: string
		userUuid: string
		userName: string
		drawUuid: string
		remark: string
		createTime: string
	}

	interface DrawCommentsResp {
		records: Chat.DrawComment[]
		total: number
		current: number
	}

	interface GalleryState {
		loadingUuid: string
		loading: boolean
		publicDraws: Draw[]
		myStarDraws: Draw[]
	}

	 interface MemoryEmbedding {
    embeddingId: string
    text: string
  }
}
