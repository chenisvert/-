package com.chen.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.common.BaseContext;
import com.chen.common.R;
import com.chen.domain.Employee;
import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController extends BaseController {


    /***
     *
     * 员工登录
     * @Author chen
     * @Date  23:05
     * @Param request
     * @Return employee
     * @Since version-11

     */
    @RequestMapping("/login")
    public R<Employee> login(@RequestBody Employee employee){

        //提交的密码md5 处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据提交的用户名查数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //// 需要注意用户名是唯一的，所以需要使用getOne
        Employee emp = employeeService.getOne(queryWrapper);

        //没有查询用户名到返回登录失败
        if (emp == null){
            return R.error("登录失败");
        }
        //密码对比
        if (!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        //查看员工禁用状态
        if (emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //登录成功，将员工id存入Session并返回登录成功结果
        session.setAttribute("employee",emp.getId());
        //将登录id填入ThreadLocal
        BaseContext.setCurrentId(emp.getId());
        return R.success(emp);
    }

    /*
    * 后台员工退出
    * */
    @RequestMapping("/logout")
    public R<String> logout(){
        //删除session
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");

    }

/*
* 增加员工
* */
    @PostMapping
    public R<String> save(@RequestBody Employee employee){

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录的用户id
        Long empId =(Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功!");
    }


    /*
    * 员工分页信息查询
    * */
    @RequestMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {} ,pageSize = {} , name = {}",page,pageSize,name);

        //构建分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构建条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        /*
        * StringUtils.isNotEmpty(name) 判断不为空 执行 Employee::getName,name
        * */
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询(不需要接收返回值，已经赋好值了 pageInfo)
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /*
    * 修改员工信息
    * */

    @PutMapping
    public R<String> update(@RequestBody Employee employee){
        log.info(employee.toString());
        //获取当前登录用户的id
        Long empId = (Long)request.getSession().getAttribute("employee");

        //设置员工信息修改的时间
//        employee.setUpdateTime(LocalDateTime.now());
        //哪个用户进行的修改
//        employee.setUpdateUser(empId);

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }


    /***
     *
     * 根据id查询信息
     * @Author chen
     * @Date  16:16
     * @Param
     * @Return
     * @Since version-11

     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询信息  id = {}",id);
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }



}
