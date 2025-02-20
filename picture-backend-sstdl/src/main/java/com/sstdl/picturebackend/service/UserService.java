package com.sstdl.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sstdl.picturebackend.model.dto.user.UserQueryRequest;
import com.sstdl.picturebackend.model.entity.User;
import com.sstdl.picturebackend.model.vo.LoginUserVO;
import com.sstdl.picturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author WSH
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-02-16 14:25:17
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户脱敏信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户脱敏信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 构造分页查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取用户列表
     */
    boolean isAdmin(User user);

}
