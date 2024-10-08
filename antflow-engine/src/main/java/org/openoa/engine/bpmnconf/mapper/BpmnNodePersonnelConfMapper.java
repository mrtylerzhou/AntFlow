package org.openoa.engine.bpmnconf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.openoa.engine.bpmnconf.confentity.BpmnNodePersonnelConf;
import org.springframework.stereotype.Repository;

/**
 * @Author JimuOffice
 * @Description node personnel conf
 * @Param
 * @return
 * @Version 0.0.1
 */
@Mapper
public interface BpmnNodePersonnelConfMapper extends BaseMapper<BpmnNodePersonnelConf> {
}
