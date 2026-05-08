package com.moyz.nexus.common.workflow.node.mailsender;

import com.moyz.nexus.common.entity.WorkflowComponent;
import com.moyz.nexus.common.entity.WorkflowNode;
import com.moyz.nexus.common.exception.BaseException;
import com.moyz.nexus.common.helper.NexusMailSender;
import com.moyz.nexus.common.util.AesUtil;
import com.moyz.nexus.common.util.SpringUtil;
import com.moyz.nexus.common.vo.CustomMailInfo;
import com.moyz.nexus.common.workflow.NodeProcessResult;
import com.moyz.nexus.common.workflow.WfNodeState;
import com.moyz.nexus.common.workflow.WfState;
import com.moyz.nexus.common.workflow.WorkflowUtil;
import com.moyz.nexus.common.workflow.data.NodeIOData;
import com.moyz.nexus.common.workflow.node.AbstractWfNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.moyz.nexus.common.cosntant.NexusConstant.WorkflowConstant.DEFAULT_OUTPUT_PARAM_NAME;
import static com.moyz.nexus.common.cosntant.NexusConstant.WorkflowConstant.MAIL_SENDER_TYPE_CUSTOM;
import static com.moyz.nexus.common.enums.ErrorEnum.*;

@Slf4j
public class MailSendNode extends AbstractWfNode {

    public MailSendNode(WorkflowComponent wfComponent, WorkflowNode node, WfState wfState, WfNodeState nodeState) {
        super(wfComponent, node, wfState, nodeState);
    }

    @Override
    protected NodeProcessResult onProcess() {
        MailSendNodeConfig nodeConfig = checkAndGetConfig(MailSendNodeConfig.class);
        int senderType = nodeConfig.getSenderType();
        String subject = WorkflowUtil.renderTemplate(nodeConfig.getSubject(), state.getInputs());
        String content = WorkflowUtil.renderTemplate(nodeConfig.getContent(), state.getInputs());
        String toMails = WorkflowUtil.renderTemplate(nodeConfig.getToMails(), state.getInputs());
        if (StringUtils.isBlank(toMails)) {
            log.warn("邮件发送节点的配置错误1,{}", state.getUuid());
            throw new BaseException(A_MAIL_RECEIVER_EMPTY);
        }
        toMails = String.join(",", filterValidMails(toMails));
        if (StringUtils.isBlank(toMails)) {
            log.warn("邮件发送节点的配置错误2,{}", state.getUuid());
            throw new BaseException(A_MAIL_RECEIVER_EMPTY);
        }
        String ccMails = StringUtils.defaultString(nodeConfig.getCcMails(), "");
        if (StringUtils.isNotBlank(ccMails)) {
            String cmails = WorkflowUtil.renderTemplate(nodeConfig.getCcMails(), state.getInputs());
            ccMails = String.join(",", filterValidMails(cmails));
        }
        if (senderType == MAIL_SENDER_TYPE_CUSTOM) {
            MailSendNodeConfig.SenderInfo senderInfo = nodeConfig.getSender();
            if (senderInfo == null) {
                log.warn("邮件发送节点的配置错误3,{}", state.getUuid());
                throw new BaseException(A_MAIL_SENDER_EMPTY);
            }
            if (StringUtils.isAnyBlank(senderInfo.getName(), senderInfo.getMail(), senderInfo.getPassword())) {
                log.warn("邮件发送节点的配置错误4,{}", state.getUuid());
                throw new BaseException(A_MAIL_SENDER_CONFIG_ERROR);
            }
            NexusMailSender NexusMailSender = SpringUtil.getBean(NexusMailSender.class);
            CustomMailInfo customMailInfo = new CustomMailInfo();
            setSmtpInfo(customMailInfo, nodeConfig.getSmtp());
            setSenderInfo(customMailInfo, senderInfo);
            setCustomMailInfo(customMailInfo, subject, content, toMails, ccMails);
            NexusMailSender.customSend(customMailInfo);
        } else {
            NexusMailSender NexusMailSender = SpringUtil.getBean(NexusMailSender.class);
            NexusMailSender.send(subject, content, toMails, ccMails);
        }
        NodeIOData output = NodeIOData.createByText(DEFAULT_OUTPUT_PARAM_NAME, "", "邮件发送成�?);
        return NodeProcessResult.builder().content(List.of(output)).build();
    }

    private void setCustomMailInfo(CustomMailInfo customMailInfo, String subject, String content, String toMails, String ccMails) {
        customMailInfo.setToMails(toMails);
        customMailInfo.setCcMails(ccMails);
        customMailInfo.setSubject(subject);
        customMailInfo.setContent(content);
    }

    private void setSmtpInfo(CustomMailInfo customMailInfo, MailSendNodeConfig.SmtpInfo smtpInfo) {
        customMailInfo.setHost(smtpInfo.getHost());
        customMailInfo.setPort(smtpInfo.getPort());
    }

    private void setSenderInfo(CustomMailInfo customMailInfo, MailSendNodeConfig.SenderInfo senderInfo) {
        customMailInfo.setSenderName(senderInfo.getName());
        customMailInfo.setSenderMail(senderInfo.getMail());
        String password = senderInfo.getPassword();
        String decrypt = AesUtil.decrypt(password);
        customMailInfo.setSenderPassword(decrypt);
    }

    private List<String> filterValidMails(String mails) {
        List<String> validMails = new ArrayList<>();
        String[] mailArray = mails.split(",");
        for (String mail : mailArray) {
            if (checkMail(mail)) {
                validMails.add(mail);
            } else {
                log.warn("邮箱地址无效，忽�?{}", mail);
            }
        }
        return validMails;
    }

    private boolean checkMail(String mail) {
        return Pattern.compile("^(.+)@(\\S+)$").matcher(mail).matches();
    }
}
