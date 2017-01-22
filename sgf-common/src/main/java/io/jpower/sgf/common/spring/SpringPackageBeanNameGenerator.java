package io.jpower.sgf.common.spring;

import java.beans.Introspector;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * @author zheng.sun
 */
public class SpringPackageBeanNameGenerator extends AnnotationBeanNameGenerator {

    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {
        return Introspector.decapitalize(definition.getBeanClassName());
    }

}
