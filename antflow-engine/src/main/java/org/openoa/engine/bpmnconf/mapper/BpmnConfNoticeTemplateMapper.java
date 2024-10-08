package org.openoa.engine.bpmnconf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.openoa.engine.bpmnconf.confentity.BpmnConfNoticeTemplate;
import org.springframework.stereotype.Repository;

/**
 * @Classname BpmnConfNoticeTemplateMapper
 * @Date 2021-11-06 8:01
 * @Created by AntOffice
 */
@Mapper
public interface BpmnConfNoticeTemplateMapper extends BaseMapper<BpmnConfNoticeTemplate> {

}
