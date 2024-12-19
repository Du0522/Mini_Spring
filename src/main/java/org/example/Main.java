package org.example;

import org.Spring.ApplicationContext;
import org.example.service.UserService;

public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
    }
}