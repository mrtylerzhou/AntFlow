package org.openoa.engine.bpmnconf.common;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.openoa.base.constant.StringConstants;
import org.openoa.base.constant.enums.ProcessJurisdictionEnum;
import org.openoa.base.constant.enums.ProcessNoticeEnum;
import org.openoa.base.constant.enums.ProcessStateEnum;
import org.openoa.base.entity.BpmBusinessProcess;
import org.openoa.base.entity.Employee;
import org.openoa.base.exception.JiMuBizException;
import org.openoa.base.util.SecurityUtils;
import org.openoa.base.vo.ProcessRecordInfoVo;
import org.openoa.engine.bpmnconf.confentity.BpmProcessForward;
import org.openoa.engine.bpmnconf.service.impl.BpmProcessForwardServiceImpl;
import org.openoa.engine.bpmnconf.service.impl.EmployeeServiceImpl;
import org.openoa.engine.vo.ProcessInforVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author AntFlow
 * @since 0.5
 */
@Slf4j
@Component
public class ProcessBusinessContans extends ProcessServiceFactory {
    @Autowired
    private BpmProcessForwardServiceImpl processForwardService;


    @Autowired
    private HistoryService historyService;
    @Autowired
    private EmployeeServiceImpl employeeService;






    /**
     * query process record info
     */
    public ProcessRecordInfoVo processInfo(BpmBusinessProcess bpmBusinessProcess) {
        ProcessRecordInfoVo processInfoVo = new ProcessRecordInfoVo();
        if (!ObjectUtils.isEmpty(bpmBusinessProcess)) {
            //check permissions
            if (!this.showProcessData(bpmBusinessProcess.getBusinessNumber())) {
                throw new JiMuBizException("00", "current user has no access right！");
            }
            //set task state
            processInfoVo.setTaskState(ProcessStateEnum.getDescByCode(bpmBusinessProcess.getProcessState()));
            //process's verify info
            processInfoVo.setVerifyInfoList(verifyInfoService.verifyInfoList(bpmBusinessProcess));
            //set process desc
            processInfoVo.setProcessTitle(bpmBusinessProcess.getDescription());

            Employee employee = employeeService.qryLiteEmployeeInfoById(bpmBusinessProcess.getCreateUser());
            processInfoVo.setEmployee(employee);
            processInfoVo.setCreateTime(bpmBusinessProcess.getCreateTime());
            //set start userId
            processInfoVo.setStartUserId(bpmBusinessProcess.getCreateUser());
            //set process Number
            processInfoVo.setProcessNumber(bpmBusinessProcess.getBusinessNumber());
            String processInstanceId = taskMgmtMapper.findByBusinessId(bpmBusinessProcess.getEntryId());
            //modify forward data
            processForwardService.updateProcessForward(BpmProcessForward.builder()
                    .processInstanceId(processInstanceId)
                    .forwardUserId(SecurityUtils.getLogInEmpIdStr())
                    .build());
            //modify notice
            userMessageService.readNode(processInstanceId);
            List<Task> list = taskService.createTaskQuery().processInstanceId(bpmBusinessProcess.getProcInstId()).taskAssignee(SecurityUtils.getLogInEmpId().toString()).list();
            if (!ObjectUtils.isEmpty(list)) {
                processInfoVo.setTaskId(list.get(0).getId());
                processInfoVo.setNodeId(list.get(0).getTaskDefinitionKey());
            }
        }
        return processInfoVo;
    }


    /**
     * delete process
     */
    public void deleteProcessInstance(String processInstanceId) {
        runtimeService.deleteProcessInstance(processInstanceId, "process ending");
    }



    /**
     * verify current logged in user's data right
     *
     * @param processCode that is process number
     * @return
     */
    public boolean showProcessData(String processCode) {

        BpmBusinessProcess bpmBusinessProcess = bpmBusinessProcessService.getBpmBusinessProcess(processCode);
        //monitor,view,process's admin,super admin,historical approvers and forward user
        if (!ObjectUtils.isEmpty(bpmBusinessProcess)) {
            List<HistoricTaskInstance> taskInstanceList = historyService.createHistoricTaskInstanceQuery().processInstanceId(bpmBusinessProcess.getProcInstId()).list();
            List<String> list = taskInstanceList.stream().filter(s -> !ObjectUtils.isEmpty(s)).map(HistoricTaskInstance::getAssignee).collect(Collectors.toList());
            if (list.contains(SecurityUtils.getLogInEmpIdStr())) {
                return true;
            }
            //todo redesign
            return true;
        }
        return true;
    }

    /**
     * app route url build
     *
     * @param processKey process key
     * @param fromCode   form code
     * @return
     * @throws UnsupportedEncodingException
     */
    public String applyRoute(String processKey, String fromCode, boolean isOutSide) {
        String roure = "";
        try {
            String inParameter = URLEncoder.encode("{\"processKey\":\"" + processKey + "\",\"formCode\":\"" + fromCode + "\"}", "UTF-8");
            roure = "oaapp://oa.app/page?param={\"type\":\"H5\",\"pageName\":\"{申请类型}\"," +
                    "\"pageURL\":\"/{申请类型}/{路由}/{工作流入参}\"}";
            if (isOutSide) {
                fromCode = "OUTSIDE_WMA";
            }
            String routePath = "apply";


            roure = StringUtils.replaceEach(roure,
                    new String[]{"{申请类型}", "{路由}", "{工作流入参}"},
                    new String[]{fromCode, routePath, inParameter});
            roure = URLEncoder.encode(roure, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return roure;
    }
    /***
     * pc route
     */
    public String pcApplyRoute(String processKey, String formCode, String processNumber, Integer type, boolean isOutside) {

        //if it is an outside process,then return outside url
        if (isOutside) {//todo third party url,to be redesigned
            return StringUtils.join("/user/workflow/detail/third-party/", formCode, "/", processNumber);
        }

        String pcUrl;
        if (type.equals(ProcessJurisdictionEnum.VIEW_TYPE.getCode())) {
            pcUrl = "/user/workflow/detail/" + formCode + "/" + processKey + "/" + processNumber;
        } else if (type.equals(ProcessJurisdictionEnum.CREATE_TYPE.getCode())) {
            pcUrl = "/user/workflow/Upcoming/check/" + formCode + "/" + processKey + "/" + processNumber;
        } else {
           //todo
            pcUrl = "/user/workflow/Upcoming/apply/" + formCode + "/" + processKey + "/" + processNumber;
        }
        return pcUrl;
    }
    /**
     * get route
     *
     * @param type    ProcessNoticeEnum
     * @param inforVo
     * @return
     */
    public String getRoute(Integer type, ProcessInforVo inforVo, boolean isOutside) {
        String url = "";
        //email
        if (type.equals(ProcessNoticeEnum.EMAIL_TYPE.getCode())) {
            url = this.pcApplyRoute(inforVo.getProcessinessKey(), inforVo.getFormCode(), inforVo.getBusinessNumber(), inforVo.getType(), isOutside);
        } else {
            url = this.detailRoute(inforVo.getFormCode(), inforVo.getBusinessNumber(), inforVo.getType(), isOutside);
        }
        return url;
    }

    /**
     * app approve/view route url
     *
     * @param formCode      form code
     * @param processNumber process number
     * @param type          type(1：view，2：operate)
     * @return
     * @throws UnsupportedEncodingException
     */
    public String detailRoute(String formCode, String processNumber, Integer type, boolean isOutSide) {
        String detail = "";
        String appUrl = "";
        if (type.equals(ProcessJurisdictionEnum.CONTROL_TYPE.getCode())) {
            type = ProcessJurisdictionEnum.CREATE_TYPE.getCode();
            appUrl = "apply";

        } else {
            appUrl = "approval";

            if (!StringUtils.isEmpty(processNumber)) {
                BpmBusinessProcess bpmBusinessProcess = bpmBusinessProcessService.getBpmBusinessProcess(processNumber);
                appUrl = appUrl.concat("_");
                if(bpmBusinessProcess.getVersion()!=null){
                    appUrl=appUrl.concat(bpmBusinessProcess.getVersion().toString());
                }
            }
        }

        try {
            String inParameter = URLEncoder.encode("{\"formCode\":\"" + formCode + "\",\"processNumber\":\"" + processNumber + "\",\"type\":" + type + "}", "UTF-8");
            //todo to be resesigned
            detail = "";
            if (isOutSide) {
                formCode = "OUTSIDE_WMA";
            }
            detail = StringUtils.replaceEach(detail,
                    new String[]{"{申请类型}", "{路由}", "{工作流入参}"},
                    new String[]{formCode, appUrl, inParameter});
            detail = URLEncoder.encode(detail, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return detail;
    }
    /**
     * check current app version true for current version false for old version,not implemented yet at the moment
     * @return
     */
    public boolean checkAppVersionByCurrentUser() {
        //todo to be implemented
        return false;
    }
}
