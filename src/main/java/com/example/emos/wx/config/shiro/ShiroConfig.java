package com.example.emos.wx.config.shiro;

import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    /**
     * 配置 SecurityManager
     */
    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(OAuth2Realm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        securityManager.setRememberMeManager(null); // 禁用 RememberMe 功能
        return securityManager;
    }

    /**
     * 配置 Shiro Filter
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(DefaultWebSecurityManager securityManager,
                                            OAuth2Filter oAuth2Filter) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);

        // 自定义 Filter
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("oauth2", oAuth2Filter);
        factoryBean.setFilters(filterMap);

        // 拦截器链配置
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        filterRuleMap.put("/webjars/**", "anon");           // Webjars 资源不拦截
        filterRuleMap.put("/druid/**", "anon");             // Druid 监控页面不拦截
        filterRuleMap.put("/app/**", "anon");               // APP 接口不拦截
        filterRuleMap.put("/v2/api-docs", "anon");          // Swagger API 不拦截
        filterRuleMap.put("/swagger/**", "anon");           // Swagger 资源不拦截
        filterRuleMap.put("/swagger-ui.html**", "anon");    // Swagger UI 不拦截
        filterRuleMap.put("/doc.html", "anon");             // Knife4j 文档不拦截
        filterRuleMap.put("/swagger-resources/**", "anon"); // Swagger 资源不拦截
        filterRuleMap.put("/captcha.jpg", "anon");          // 验证码接口不拦截
        filterRuleMap.put("/user/login", "anon");           // 登录接口不拦截
        filterRuleMap.put("/user/register", "anon");        // 注册接口不拦截
        filterRuleMap.put("/test/**", "anon");              // 测试接口不拦截
        filterRuleMap.put("/**", "oauth2");
        filterRuleMap.put("/meeting/recieveNotify", "anon");
        // 其他所有接口都需要认证

        factoryBean.setFilterChainDefinitionMap(filterRuleMap);

        return factoryBean;
    }

    /**
     * 开启 Shiro 注解支持
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
            DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * Shiro 生命周期处理器
     */
    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 对 Realm 进行代理，处理认证和授权注解
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
}
