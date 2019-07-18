package com.ttcode.shiro.conf;

import com.ttcode.shiro.security.MyCasFilter;
import com.ttcode.shiro.security.MyCasRealm;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.cas.CasSubjectFactory;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro的配置文件
 *
 * @author Mark sunlightcs@gmail.com
 */
@Configuration
public class ShiroCasConfiguration {
    /**
     * 添加shiro的filter
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new DelegatingFilterProxy("shiroFilter"));
        filterRegistration.addInitParameter("targetFilterLifecycle", "true");
        filterRegistration.setEnabled(true);
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    /**
     * 保证了shiro内部lifecycle函数bean的执行
     */
    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 配置授权策略
     */
    @Bean(name = "authenticator")
    public ModularRealmAuthenticator modularRealmAuthenticator() {
        ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
        authenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        return authenticator;
    }

    @Bean(name = "casRealm")
    public MyCasRealm casRealm(@Value("${shiro.casServerUrlPrefix}") String casServerUrlPrefix,
                               @Value("${shiro.shiroServerUrlPrefix}") String shiroServerUrlPrefix,
                               @Value("${shiro.casFilterUrlPattern}") String casFilterUrlPattern) {
        MyCasRealm casRealm = new MyCasRealm();
        // 认证通过后的默认角色
        casRealm.setDefaultRoles("ROLE_USER");
        // cas服务端地址前缀
        casRealm.setCasServerUrlPrefix(casServerUrlPrefix);
        // 应用服务地址，用来接收cas服务端票证
        casRealm.setCasService(shiroServerUrlPrefix + casFilterUrlPattern);
        return casRealm;
    }

    /**
     * 配置安全管理器
     **/
    @Bean(name = "securityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(ModularRealmAuthenticator authenticator,
                                                               MyCasRealm casRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置授权策略,此步骤必须在设置realm的前面，不然会报错realm未配置
        securityManager.setAuthenticator(authenticator);
        securityManager.setSubjectFactory(new CasSubjectFactory());
        // 缓存管理器
        securityManager.setCacheManager(new MemoryConstrainedCacheManager());
        // 设置自定义验证策略
        securityManager.setRealm(casRealm);
        return securityManager;
    }


    /**
     * 配置登录过滤器
     */
    @Bean(name = "casFilter")
    public MyCasFilter casFilter(@Value("${shiro.loginUrl}") String loginUrl) {
        MyCasFilter casFilter = new MyCasFilter();
        casFilter.setName("casFilter");
        casFilter.setEnabled(true);
        casFilter.setFailureUrl(loginUrl);
        return casFilter;
    }

    /**
     * shiro 过滤器
     */
    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager,
                                                            MyCasFilter casFilter,
                                                            @Value("${shiro.logoutUrl}") String logoutUrl,
                                                            @Value("${shiro.loginUrl}") String loginUrl,
                                                            @Value("${shiro.casFilterUrlPattern}") String casFilterUrlPattern) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 设置登录地址
        shiroFilterFactoryBean.setLoginUrl(loginUrl);
        // 设置登录成功地址
        shiroFilterFactoryBean.setSuccessUrl("/");
        // 配置拦截地址
        Map<String, Filter> filters = new HashMap<>();
        filters.put("casFilter", casFilter);
        LogoutFilter logoutFilter = new LogoutFilter();
        // 配置登出地址
        logoutFilter.setRedirectUrl(logoutUrl);
        filters.put("logout", logoutFilter);
        shiroFilterFactoryBean.setFilters(filters);
        // 设置访问用户页面需要授权的操作
        loadShiroFilterChain(shiroFilterFactoryBean, casFilterUrlPattern);
        // 将设置的权限设置到shiroFilterFactoryBean
        return shiroFilterFactoryBean;
    }

    /**
     * 1、当我们第一次访问客户端时，先去cas进行认证，成功后会返回一个ticket
     * 2、返回的ticket地址在casRealm已经进行了配置，shiroServerUrlPrefix + casFilterUrlPattern
     * 3、即地址为/shiro-cas，对该地址进行casFilter拦截
     */
    private void loadShiroFilterChain(ShiroFilterFactoryBean shiroFilterFactoryBean, String casFilterUrlPattern) {
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        filterChainDefinitionMap.put(casFilterUrlPattern, "casFilter");
        filterChainDefinitionMap.put("/logout", "logout");
        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
    }

    /**
     * 开启Shiro的注解(如@RequiresPermissions)
     * 需借助SpringAOP扫描使用Shiro注解的类
     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator和AuthorizationAttributeSourceAdvisor)即可实现此功能
     *
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    /**
     * 开启aop注解支持
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

}