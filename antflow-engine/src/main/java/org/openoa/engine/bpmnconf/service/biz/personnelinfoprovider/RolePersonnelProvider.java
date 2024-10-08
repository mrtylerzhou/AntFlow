package org.openoa.engine.bpmnconf.service.biz.personnelinfoprovider;

import lombok.extern.slf4j.Slf4j;
import org.openoa.base.exception.JiMuBizException;
import org.openoa.base.service.empinfoprovider.BpmnRoleInfoProvider;
import org.openoa.base.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author TylerZhou
 * @Date 2024/7/18 6:42
 * @Version 1.0
 */
@Slf4j
@Component
public class RolePersonnelProvider extends AbstractNodeAssigneeVoProvider{
    @Autowired
    private BpmnRoleInfoProvider roleInfoProvider;;
    @Override
    public List<BpmnNodeParamsAssigneeVo> getAssigneeList(BpmnNodeVo bpmnNodeVo, BpmnStartConditionsVo startConditionsVo) {
        BpmnNodePropertysVo propertysVo = bpmnNodeVo.getProperty();
        if (propertysVo==null || CollectionUtils.isEmpty(propertysVo.getRoleIds())) {
            throw new JiMuBizException("指定角色找人条件不全，无法找人！");
        }
        if(bpmnNodeVo.getIsOutSideProcess()!=null&&bpmnNodeVo.getIsOutSideProcess().equals(1)){
            List<BaseIdTranStruVo> emplList = bpmnNodeVo.getProperty().getEmplList();
            if(CollectionUtils.isEmpty(emplList)){
                throw new JiMuBizException("thirdy party process role node has no employee info");
            }
            return  super.provideAssigneeList(bpmnNodeVo,null);
        }
        List<String> roleIds = propertysVo.getRoleIds();
        Map<String, String> roleEmployeeInfo = roleInfoProvider.provideRoleEmployeeInfo(roleIds);
        if(CollectionUtils.isEmpty(roleEmployeeInfo)){
            log.warn("can not find specified roles info via roleIds:{}",roleIds);
            throw new JiMuBizException("can not find specified roles info via roleIds");
        }
        ArrayList<String> userIds = new ArrayList<>(roleEmployeeInfo.keySet());
        return  super.provideAssigneeList(bpmnNodeVo,userIds);
    }
}
