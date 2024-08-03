package cn.katool.util.classes;
import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import javax.validation.constraints.NotNull;
@Component
public class SpringContextUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    public static Boolean contain(String beanName){
        return applicationContext.containsBean(beanName);
    }
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }
    /**
     * 通过名称获取 Bean
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }
    /**
     * 通过 class 获取 Bean
     *
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
    /**
     * 通过名称和类型获取 Bean
     *
     * @param beanName
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }
    public static void regBean(String beanName, Object bean) {
        if (SpringContextUtils.contain(beanName)) {
            unregBean(beanName);
        }
        SpringUtil.registerBean(beanName, bean);
    }
    public static void unregBean(String beanName){
        if (!SpringContextUtils.contain(beanName)) {
            return;
        }
        SpringUtil.unregisterBean(beanName);
    }
}