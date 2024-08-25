package org.openoa.engine.bpmnconf.service.biz;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.openoa.base.dto.PageDto;
import org.openoa.base.entity.UserMessage;
import org.openoa.base.entity.UserMessageStatus;
import org.openoa.base.service.empinfoprovider.BpmnEmployeeInfoProviderService;
import org.openoa.base.util.MailUtils;
import org.openoa.base.util.PageUtils;
import org.openoa.base.util.SecurityUtils;
import org.openoa.base.vo.BaseMsgInfo;
import org.openoa.base.vo.MailInfo;
import org.openoa.base.vo.MessageInfo;
import org.openoa.base.vo.ResultAndPage;
import org.openoa.engine.bpmnconf.mapper.UserMessageMapper;
import org.openoa.engine.bpmnconf.service.impl.UserMessageServiceImpl;
import org.openoa.engine.bpmnconf.service.impl.UserMessageStatusServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageServiceImpl {

    @Autowired
    private BpmnEmployeeInfoProviderService bpmnEmployeeInfoProviderService;

    @Autowired
    private UserMessageServiceImpl userMessageService;

    @Autowired
    private UserMessageStatusServiceImpl userMessageStatusService;
    @Autowired
    private MailUtils mailUtils;

    /**
     * send single email
     *
     * @param mailInfo
     * @param userId
     */
    @Async
    public void sendMail(MailInfo mailInfo, Long userId) {

        UserMessageStatus userMessageStatus = getUserMessageStatus(userId);

        //如果员工没有消息配置数据则默认发送邮件
        //if user has no message config,the default to send email
        if (userMessageStatus==null) {
            mailUtils.doSendMail(mailInfo);
            return;
        }

        if (Boolean.TRUE.equals(userMessageStatus.getMailStatus())) {
            mailUtils.doSendMail(mailInfo);
        }
    }

    /**
     * send email in batch
     *
     * @param map
     */
    @Async
    public void sendMailBath(Map<Long, MailInfo> map) {
        List<MailInfo> mailInfos = Lists.newArrayList();

        for (Map.Entry<Long, MailInfo> entry : map.entrySet()) {

            UserMessageStatus userMessageStatus = getUserMessageStatus(entry.getKey());


            if (userMessageStatus==null) {
                mailInfos.add(entry.getValue());
                continue;
            }

            if (Boolean.TRUE.equals(userMessageStatus.getMailStatus())) {
                mailInfos.add(entry.getValue());
            }
        }
        if (!CollectionUtils.isEmpty(mailInfos)) {
            mailUtils.doSendMailBath(mailInfos);
        }
    }

    /**
     * send single sms
     *
     * @param messageInfo
     * @param userId
     */
    @Async
    public void sendSms(MessageInfo messageInfo, Long userId) {

        UserMessageStatus userMessageStatus = getUserMessageStatus(userId);
      //todo
    }

    /**
     * send sms in batch
     *
     * @param map
     */
    @Async
    public void sendSmsBath(Map<Long, MessageInfo> map) {
        List<MessageInfo> messageInfos = Lists.newArrayList();

        for (Map.Entry<Long, MessageInfo> entry : map.entrySet()) {

            UserMessageStatus userMessageStatus = getUserMessageStatus(entry.getKey());

            //todo
        }
    }

    /**
     * push single app notification
     *
     * @param baseMsgInfo
     * @param userId
     */
    @Async
    public void sendAppPush(BaseMsgInfo baseMsgInfo, Long userId) {
        doSendAppPush(baseMsgInfo, userId);
    }

    /**
     * push app notification in batch
     *
     * @param map
     */
    @Async
    public void sendAppPushBath(Map<Long, BaseMsgInfo> map) {
        for (Map.Entry<Long, BaseMsgInfo> entry : map.entrySet()) {
            doSendAppPush(entry.getValue(), entry.getKey());
        }
    }

    /**
     * insert user message
     *
     * @param userMessage
     */
    @Async
    public void insertUserMessage(UserMessage userMessage) {

        if (ObjectUtils.isEmpty(userMessage.getUserId())) {
            return;
        }

        userMessage.setCreateUser(SecurityUtils.getLogInEmpNameSafe());
        userMessage.setCreateTime(new Date());
        userMessage.setUpdateUser(SecurityUtils.getLogInEmpNameSafe());
        userMessage.setUpdateTime(new Date());
        userMessageService.insertMessage(userMessage);
    }

    /**
     * insert user message in batch
     *
     * @param userMessages
     */
    @Async
    public void insertUserMessageBath(List<UserMessage> userMessages) {

        for (UserMessage userMessage : userMessages) {
            if (ObjectUtils.isEmpty(userMessage.getUserId())) {
                continue;
            }

            userMessage.setCreateUser(SecurityUtils.getLogInEmpNameSafe());
            userMessage.setCreateTime(new Date());
            userMessage.setUpdateUser(SecurityUtils.getLogInEmpNameSafe());
            userMessage.setUpdateTime(new Date());
        }

        userMessageService.saveBatch(userMessages);
    }

    /**
     * do send app push
     *
     * @param baseMsgInfo
     * @param userId
     */
    private void doSendAppPush(BaseMsgInfo baseMsgInfo, Long userId) {

        baseMsgInfo.setUsername(getUsernameByUserId(userId));
        UserMessageStatus userMessageStatus = getUserMessageStatus(userId);

        //todo
    }

    /**
     * get user message config
     *
     * @param userId
     * @return
     */
    private UserMessageStatus getUserMessageStatus(Long userId) {
        return userMessageStatusService.getBaseMapper().selectOne(new QueryWrapper<UserMessageStatus>().eq("user_id", userId));
    }

    /**
     * get user name by id
     *
     * @param userId
     * @return
     */
    private String getUsernameByUserId(Long userId) {
        Map<String, String> employeeInfo = bpmnEmployeeInfoProviderService.provideEmployeeInfo(Lists.newArrayList(String.valueOf(userId)));
        return employeeInfo.get(String.valueOf(userId));
    }
}
