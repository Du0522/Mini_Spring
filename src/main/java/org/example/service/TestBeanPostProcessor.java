package org.example.service;

import org.Spring.BeanPostProcessor;
import org.Spring.Component;

@Component
public class TestBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return null;
    }
}
