package com.chen.controller;

import ch.qos.logback.core.util.TimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.common.BaseContext;
import com.chen.common.R;
import com.chen.domain.User;


import com.chen.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController extends BaseController{




    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) throws MessagingException {
        //获取邮箱
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            userService.sendMsg(phone,"【瑞吉外卖】 登录验证码","您的验证码："+code+"<br>"+"有效期为5分钟");

            //需要将生成的验证码保存到Session
//            session.setAttribute(phone,code);

            //生成的验证码放入redis缓存中。有效期5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("验证码发送成功");
        }

        return R.error("发送失败");
    }



    @PostMapping("/login")
    public R<User> login(@RequestBody Map map){

        log.info(map.toString());
        //获取邮箱
        String email = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //获取session保存的验证码
//        Object codeSession = session.getAttribute(email);
        Object codeSession = redisTemplate.opsForValue().get(email);


        if (codeSession != null &&  codeSession.equals(code)){
            //判断当前邮箱是否为新用户，如果是就注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,email);
            User user = userService.getOne(queryWrapper);
            if (user ==null){
                 user = new User();
                 user.setPhone(email);
                 user.setStatus(1);
                 userService.save(user);
            }
            session.setAttribute("user", user.getId());
            redisTemplate.delete(email);
            return   R.success(user);
    }

        return R.error("登录失败");
    }

    /**
     * 用户退出
     *
     * @param
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout() {
        request.getSession().removeAttribute("user");
        return R.success("安全退出成功！");
    }

}
