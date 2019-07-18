package com.ttcode.shiro.controller;

import com.ttcode.shiro.service.AuthService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : xingsongtan@qq.com
 * @date : 15:47 2019/7/18
 */
@RestController
public class AuthController {
    @Autowired
    AuthService authService;

    @GetMapping("/test")
    public String test() {
        return "请求成功！" + authService.getUsername();
    }

    @GetMapping("/testPermission")
    @RequiresPermissions("sys:dept:list")
    public String testPermission() {
        return "权限请求成功！" + authService.getUsername();
    }

    @GetMapping("/testPermissionFail")
    @RequiresPermissions("sys:dept:fail")
    public String testPermissionFail() {
        return "权限请求失败！" + authService.getUsername();
    }
}
