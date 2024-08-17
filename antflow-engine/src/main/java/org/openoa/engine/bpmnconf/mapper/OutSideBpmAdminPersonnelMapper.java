package org.openoa.engine.bpmnconf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.openoa.engine.bpmnconf.confentity.OutSideBpmAdminPersonnel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutSideBpmAdminPersonnelMapper extends BaseMapper<OutSideBpmAdminPersonnel> {

    /**
     * get business party id list by employee id and filtering out the type
     *
     * @param employeeId
     * @return
     */
    List<Integer> getBusinessPartyIdByEmployeeId(@Param("employeeId") Integer employeeId, @Param("types") List<Integer> types);

}
