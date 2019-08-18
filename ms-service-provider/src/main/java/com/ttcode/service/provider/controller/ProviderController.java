package com.ttcode.service.provider.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : xingsongtan@qq.com
 * @date : 19:14 2019/7/28
 */
@RestController
@RequestMapping("/test")
public class ProviderController {
    private static Logger LOGGER = LoggerFactory.getLogger(ProviderController.class);

    @GetMapping("{info}")
    public String welcome(@PathVariable("info") String info) {
        LOGGER.info("welcome ~ " + info);
        return "service-provider received info : " + info;
    }
}
