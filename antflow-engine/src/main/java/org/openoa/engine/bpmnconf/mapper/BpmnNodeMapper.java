package org.openoa.engine.bpmnconf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.openoa.engine.bpmnconf.confentity.BpmnNode;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface BpmnNodeMapper extends BaseMapper<BpmnNode> {

    List<BpmnNode> customNode(String processKey);

}