package org.Spring;

public interface BeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception;
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception;
}
