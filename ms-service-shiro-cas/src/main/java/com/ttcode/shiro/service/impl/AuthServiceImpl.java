package com.ttcode.shiro.service.impl;

import com.ttcode.shiro.service.AuthService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

/**
 * @author : xingsongtan@qq.com
 * @date : 15:47 2019/7/18
 */
@Service
public class AuthServiceImpl implements AuthService {
    /**
     * 获取用户名
     */
    @Override
    public String getUsername() {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null || subject.getPrincipals() == null) {
            return null;
        }
        return (String) subject.getPrincipals().getPrimaryPrincipal();
    }
}
