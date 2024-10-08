package org.openoa.engine.factory;

import com.alibaba.fastjson.JSON;

import org.openoa.base.exception.JiMuBizException;
import org.openoa.base.interf.ActivitiService;
import org.openoa.base.interf.FormOperationAdaptor;
import org.openoa.base.vo.BusinessDataVo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Classname FormFactory
 * @Description TODO
 * @Date 2021-11-09 6:58
 * @Created by AntOffice
 */
@Component
public class FormFactory implements ApplicationContextAware {
    @Autowired
    private IAdaptorFactory adaptorFactory;
    private ApplicationContext applicationContext;

    public FormOperationAdaptor getFormAdaptor(String formCode){
        return getFormAdaptor(BusinessDataVo.builder().formCode(formCode).build());
    }
    public FormOperationAdaptor getFormAdaptor(BusinessDataVo vo) {
        if (ObjectUtils.isEmpty(vo)) {
            return null;
        }
        ActivitiService activitiService = adaptorFactory.getActivitiService(vo);
        if (ObjectUtils.isEmpty(activitiService)) {
            throw new JiMuBizException("form code does not have a processing bean！");
        }
        return (FormOperationAdaptor) activitiService;
    }

    /**
     *
     * @param params the request body string
     * @param formCode if caller can't provide,pass null
     * @return
     */
    public BusinessDataVo dataFormConversion(String params,String formCode) {

        if(formCode==null){
            BusinessDataVo vo = JSON.parseObject(params, BusinessDataVo.class);
            formCode=vo.getFormCode();
            if(vo.getIsOutSideAccessProc()){
                return vo;
            }
        }

        Object bean = applicationContext.getBean(formCode);
        if (ObjectUtils.isEmpty(bean)) {
            throw new JiMuBizException("can not get the processing bean by form code:{}!"+formCode);
        }
        return JSON.parseObject(params, (Type) getFormTClass(formCode));

    }

    private Class<?> getFormTClass(String key) {
        FormOperationAdaptor bean = getFormAdaptor(BusinessDataVo.builder().formCode(key).build());
        if (!ObjectUtils.isEmpty(bean)) {
            Type[] interfacesTypes = bean.getClass().getGenericInterfaces();
            ParameterizedType p = (ParameterizedType) interfacesTypes[0];
            Class<?> cls = (Class) p.getActualTypeArguments()[0];
            if (!ObjectUtils.isEmpty(cls)) {
                return cls;
            }
        }
        throw new JiMuBizException("该表单未关联业务实现类或未关联实现类泛型！");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
