package org.openoa.engine.bpmnconf.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.openoa.base.dto.PageDto;
import org.openoa.base.util.PageUtils;
import org.openoa.base.util.SecurityUtils;
import org.openoa.base.vo.*;
import org.openoa.engine.bpmnconf.confentity.BpmnConf;
import org.openoa.engine.bpmnconf.confentity.UserEntrust;
import org.openoa.engine.bpmnconf.mapper.UserEntrustMapper;
import org.openoa.base.exception.JiMuBizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class UserEntrustServiceImpl extends ServiceImpl<UserEntrustMapper, UserEntrust> {


    @Autowired
    private UserEntrustMapper mapper;

    //获get current login employee's entrust list
    public List<Entrust> getEntrustList() {
        return mapper.getEntrustListNew( SecurityUtils.getLogInEmpIdSafe().intValue());
    }
    public UserEntrust getEntrustDetail(Integer id) {
        return this.getBaseMapper().selectById(id);
    }
    public ResultAndPage<Entrust> getEntrustPageList(PageDto pageDto, Entrust vo, Integer type) {
        if (type == 1){
            vo.setReceiverId(SecurityUtils.getLogInEmpIdSafe().intValue());
        }
        Page<Entrust> page = PageUtils.getPageByPageDto(pageDto);
        List<Entrust> resultData = this.getBaseMapper().getEntrustPageList(page, vo.getReceiverId());
        if (resultData==null) {
            return PageUtils.getResultAndPage(page);
        }
        page.setRecords(resultData);
        return PageUtils.getResultAndPage(page);
    }

    //batch save or update entrust list
    @Transactional
    public void updateEntrustList(DataVo dataVo) {
        for (IdsVo idsVo : dataVo.getIds()) {
            UserEntrust userEntrust = new UserEntrust();
            userEntrust.setUpdateUser(SecurityUtils.getLogInEmpNameSafe());
            userEntrust.setBeginTime(dataVo.getBeginTime());
            userEntrust.setEndTime(dataVo.getEndTime());
            userEntrust.setReceiverId(dataVo.getReceiverId());
            userEntrust.setReceiverName(dataVo.getReceiverName());
            //userEntrust.setSender( SecurityUtils.getLogInEmpIdSafe().intValue());
            userEntrust.setSender(dataVo.getSender());
            if (idsVo.getId() != null && idsVo.getId() > 0) {
                //插入
                UserEntrust userEntrustCheck = mapper.selectById(idsVo.getId());
                if (userEntrustCheck == null) {
                    throw new JiMuBizException("300001", "更新的记录不存在");
                }
                userEntrust.setId(idsVo.getId());
                userEntrust.setPowerId(userEntrustCheck.getPowerId());
                userEntrust.setCreateUser(SecurityUtils.getLogInEmpNameSafe());
                //mapper.update(userEntrust);
            } else if (idsVo.getPowerId() != null) {
                if (userEntrust.getReceiverId() == null) {
                    throw new JiMuBizException("300002", "请选择委托对象");
                }
                //更新
                userEntrust.setCreateUser(SecurityUtils.getLogInEmpNameSafe());
                userEntrust.setPowerId(idsVo.getPowerId());
                mapper.insert(userEntrust);
            }
        }
    }


    public Integer getEntrustEmployee(Integer employeeId, String powerId) {
        if (ObjectUtils.isEmpty(employeeId) || ObjectUtils.isEmpty(powerId)) {
            return employeeId;
        }

        Integer result =  SecurityUtils.getLogInEmpIdSafe().intValue();
        if (result.intValue() != employeeId.intValue()) {

            return result;
        }
        result = this.getEntrustEmployeeOnly(employeeId, powerId);
        return result;
    }

    /**
     * get one's entryst employee,if has no then return himself
     *
     * @param employeeId
     * @param powerId    formid
     * @return
     */
    public Integer getEntrustEmployeeOnly(Integer employeeId, String powerId) {
        if (ObjectUtils.isEmpty(employeeId) || ObjectUtils.isEmpty(powerId)) {
            return employeeId;
        }
        QueryWrapper<UserEntrust> wrapper = new QueryWrapper<>();
        wrapper.eq("power_id", powerId).eq("sender", employeeId);
        List<UserEntrust> list = this.mapper.selectList(wrapper);
        //todo
        return employeeId;
    }


}
