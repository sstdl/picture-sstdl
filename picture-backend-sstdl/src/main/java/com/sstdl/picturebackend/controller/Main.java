package com.sstdl.picturebackend.controller;

import com.sstdl.picturebackend.common.BaseResponse;
import com.sstdl.picturebackend.utils.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SSTDL
 * @description
 */
@RequestMapping("/main")
@RestController
public class Main {
    @GetMapping("/hello")
    public BaseResponse<String> hello(){
        return ResultUtils.success("hello world");
    }
}
