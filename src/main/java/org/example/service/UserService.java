package org.example.service;

import org.Spring.Autowired;
import org.Spring.BeanNameAware;
import org.Spring.Component;
import org.Spring.Scope;

@Component("userService")
public class UserService implements BeanNameAware {
    @Autowired
    private OrderService orderService;

    private String beanName;

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
    }
}

