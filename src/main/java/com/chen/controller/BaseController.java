package com.chen.controller;


import com.chen.service.*;





import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/*
* 统一依赖注入
*
* */
@Controller
public class BaseController {

    //跳转默认页(主页)
    @RequestMapping("/")
    private void index(){
        response.setHeader("refresh", "0;url=front/page/login.html");
    }
    //跳转到后台
    @RequestMapping("/admin")
    private void ht(){
        response.setHeader("refresh", "0;url=backend/index.html");
    }

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;


    @Resource
    protected EmployeeService employeeService;

    @Resource
    protected CategoryService categoryService;

    @Resource
    protected DishService dishService;

    @Resource
    protected DishFlavorService dishFlavorService;

    @Resource
    protected SetmealDishService setmealDishService;

    @Resource
    protected SetmealService setmealService;


    @Resource
    protected UserService userService;

    @Resource
    protected ShoppingCartService shoppingCartService;

    @Resource
    protected OrdersService ordersService;








    //@ModelAttribute 会在此controller的每个方法执行前被执行 ，如果有返回值，则自动将该返回值加入到ModelMap中。
    @ModelAttribute
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession(true);
    }
}
