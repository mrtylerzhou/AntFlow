package org.openoa.engine.bpmnconf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.openoa.base.util.SecurityUtils;
import org.openoa.engine.bpmnconf.confentity.BpmFlowruninfo;
import org.openoa.engine.bpmnconf.mapper.BpmFlowruninfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class BpmFlowruninfoServiceImpl extends ServiceImpl<BpmFlowruninfoMapper, BpmFlowruninfo> {
    protected static Map<String, ProcessDefinition> PROCESS_DEFINITION_CACHE = new HashMap<String, ProcessDefinition>();

    @Autowired
    private BpmFlowruninfoMapper mapper;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * create run info
     */
    public void createFlowRunInfo(String entryId, String processInstance) throws Exception {
        BpmFlowruninfo flowruninfo = new BpmFlowruninfo();
        flowruninfo.setEntitykey(entryId);
        flowruninfo.setCreateUserId( SecurityUtils.getLogInEmpIdSafe());
        flowruninfo.setRuninfoid(Long.parseLong(processInstance));
        flowruninfo.setEntityclass(SecurityUtils.getLogInEmpNameSafe());
        flowruninfo.setCreateactor(SecurityUtils.getLogInEmpNameSafe());
        flowruninfo.setCreatedate(new Date());
        mapper.insert(flowruninfo);
    }

    /**
     * create run info
     *
     * @param flowruninfo
     */
    public void createFlowRunInfo(BpmFlowruninfo flowruninfo) {
        mapper.insert(flowruninfo);
    }

    /**
     * get run info by id
     *
     * @param runInfoId
     * @return
     */
    public BpmFlowruninfo getFlowruninfo(Long runInfoId) {
        return mapper.getFlowruninfo(runInfoId);
    }

    /**
     * delete  run info
     */
    public void deleteFlowruninfo(Long id) {
        mapper.deleteById(id);
    }

    /**
     * get process definition
     *
     * @param processDefinitionId
     * @return
     */
    public ProcessDefinition getProcessDefinition(String processDefinitionId) {
        ProcessDefinition processDefinition = PROCESS_DEFINITION_CACHE
                .get(processDefinitionId);
        if (processDefinition == null) {
            processDefinition = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId).singleResult();
            PROCESS_DEFINITION_CACHE
                    .put(processDefinitionId, processDefinition);
        }
        return processDefinition;
    }
}
