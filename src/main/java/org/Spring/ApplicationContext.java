package org.Spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>(); //单例池
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public ApplicationContext(Class configClass) {
        this.configClass = configClass;

        //解析配置
        scan(configClass);

        //依次为扫描到的所有类构建实例
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition); // 单例Bean
                singletonObjects.put(beanName, bean);
            }
        }


    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            //调用该类的无参构造函数创建类的一个实例
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 依赖注入
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

            //Aware回调
            if (instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            //初始化前自定义处理
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            //初始化
            if (instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }

            //初始化后自定义处理
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String scanPackage = componentScanAnnotation.value();
        scanPackage = scanPackage.replace('.', '/');

        //扫描路径
        // Bootstrap--->jre/lib
        // Ext--------->jre/ext/lib
        // App--------->classpath---->
        ClassLoader classLoader = ApplicationContext.class.getClassLoader(); //app
        URL resource = classLoader.getResource(scanPackage);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath();
                if (fileName.endsWith(".class")) {
                    String className = fileName.substring(fileName.indexOf("org"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        // 表示当前这个类是一个Bean
                        if (clazz.isAnnotationPresent(Component.class)) {

                            //实现了BeanPostProcessor的bean
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }


                            // 解析类--->BeanDefinition
                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }

                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            //判断是单例bean，还是原型bean
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = singletonObjects.get(beanName);
                return bean;
            } else {
                Object bean = createBean(beanName, beanDefinition);
                return bean;
            }
        } else {
            // 不存在对应的Bean
            throw new NullPointerException();
        }
    }
}
