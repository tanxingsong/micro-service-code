package com.ttcode.shiro.security;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.cas.CasFilter;
import org.apache.shiro.cas.CasToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author : xingsongtan@qq.com
 * @date : 14:17 2019/7/17
 */
public class MyCasFilter extends CasFilter {
    private static       Logger logger           = LoggerFactory.getLogger(MyCasFilter.class);
    private static final String TICKET_PARAMETER = "ticket";

    public MyCasFilter() {
    }

    @Override
    public AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        // 获取请求的ticket
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ticket = getRequestTicket(httpRequest);
        if (StringUtils.isEmpty(ticket)) {
            logger.debug("票证获取失败,票证为空！");
            return null;
        }
        return new CasToken(ticket);
    }

    /**
     * 拒绝除了option以外的所有请求
     **/
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        // 获取ticket，如果不存在，直接返回false
        String ticket = getRequestTicket((HttpServletRequest) request);
        if (StringUtils.isEmpty(ticket)) {
            return false;
        }
        return this.executeLogin(request, response);
    }

    /**
     * 获取请求的ticket
     */
    private String getRequestTicket(HttpServletRequest httpRequest) {
        // 从参数中获取ticket
        String ticket = httpRequest.getParameter(TICKET_PARAMETER);
        if (StringUtils.isEmpty(ticket)) {
            // 如果为空的话，则从header中获取参数
            ticket = httpRequest.getHeader(TICKET_PARAMETER);
        }
        return ticket;
    }

}
