package com.moyz.nexus.common.workflow;

import com.moyz.nexus.common.entity.WorkflowComponent;
import com.moyz.nexus.common.entity.WorkflowNode;
import com.moyz.nexus.common.workflow.node.AbstractWfNode;
import com.moyz.nexus.common.workflow.node.EndNode;
import com.moyz.nexus.common.workflow.node.answer.LLMAnswerNode;
import com.moyz.nexus.common.workflow.node.classifier.ClassifierNode;
import com.moyz.nexus.common.workflow.node.openaiimage.OpenAiImageNode;
import com.moyz.nexus.common.workflow.node.documentextractor.DocumentExtractorNode;
import com.moyz.nexus.common.workflow.node.faqextractor.FaqExtractorNode;
import com.moyz.nexus.common.workflow.node.google.GoogleNode;
import com.moyz.nexus.common.workflow.node.httprequest.HttpRequestNode;
import com.moyz.nexus.common.workflow.node.humanfeedback.HumanFeedbackNode;
import com.moyz.nexus.common.workflow.node.keywordextractor.KeywordExtractorNode;
import com.moyz.nexus.common.workflow.node.knowledgeretrieval.KnowledgeRetrievalNode;
import com.moyz.nexus.common.workflow.node.mailsender.MailSendNode;
import com.moyz.nexus.common.workflow.node.start.StartNode;
import com.moyz.nexus.common.workflow.node.switcher.SwitcherNode;
import com.moyz.nexus.common.workflow.node.template.TemplateNode;
import com.moyz.nexus.common.workflow.node.tongyiwanx.TongyiwanxNode;

public class WfNodeFactory {
    public static AbstractWfNode create(WorkflowComponent wfComponent, WorkflowNode nodeDefinition, WfState wfState, WfNodeState nodeState) {
        AbstractWfNode wfNode = null;
        switch (WfComponentNameEnum.getByName(wfComponent.getName())) {
            case START:
                wfNode = new StartNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case LLM_ANSWER:
                wfNode = new LLMAnswerNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case CLASSIFIER:
                wfNode = new ClassifierNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case SWITCHER:
                wfNode = new SwitcherNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case TEMPLATE:
                wfNode = new TemplateNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case KEYWORD_EXTRACTOR:
                wfNode = new KeywordExtractorNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case DOCUMENT_EXTRACTOR:
                wfNode = new DocumentExtractorNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case FAQ_EXTRACTOR:
                wfNode = new FaqExtractorNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case KNOWLEDGE_RETRIEVER:
                wfNode = new KnowledgeRetrievalNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case GOOGLE_SEARCH:
                wfNode = new GoogleNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case OPENAI_IMAGE:
                wfNode = new OpenAiImageNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case TONGYI_WANX:
                wfNode = new TongyiwanxNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case HUMAN_FEEDBACK:
                wfNode = new HumanFeedbackNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case MAIL_SEND:
                wfNode = new MailSendNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case HTTP_REQUEST:
                wfNode = new HttpRequestNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            case END:
                wfNode = new EndNode(wfComponent, nodeDefinition, wfState, nodeState);
                break;
            default:
        }
        return wfNode;
    }
}
