package org.openoa.engine.bpmnconf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.openoa.base.util.SecurityUtils;
import org.openoa.engine.bpmnconf.confentity.BpmnNodeSignUpConf;
import org.openoa.engine.bpmnconf.mapper.BpmnNodeSignUpConfMapper;
import org.openoa.base.vo.BpmnNodeVo;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;

@Service
public class BpmnNodeSignUpConfServiceImpl extends ServiceImpl<BpmnNodeSignUpConfMapper, BpmnNodeSignUpConf> {

    public void editSignUpConf(BpmnNodeVo bpmnNodeVo, Long bpmnNodeId) {
        if (ObjectUtils.isEmpty(bpmnNodeVo.getIsSignUp()) || bpmnNodeVo.getIsSignUp() != 1) {
            return;
        }

        BpmnNodeSignUpConf bpmnNodeSignUpConf = BpmnNodeSignUpConf
                .builder()
                .bpmnNodeId(bpmnNodeId)
                .afterSignUpWay(bpmnNodeVo.getProperty().getAfterSignUpWay())
                .signUpType(bpmnNodeVo.getProperty().getSignUpType())
                .createUser(SecurityUtils.getLogInEmpNameSafe())
                .createTime(new Date())
                .build();
        this.getBaseMapper().insert(bpmnNodeSignUpConf);
    }
}