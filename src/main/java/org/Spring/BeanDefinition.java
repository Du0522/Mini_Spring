package org.Spring;

public class BeanDefinition {

    private Class clazz; //bean的类型
    private String scope; //bean的作用域

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
