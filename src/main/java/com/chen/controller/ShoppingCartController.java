package com.chen.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chen.common.BaseContext;
import com.chen.common.R;
import com.chen.domain.ShoppingCart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController extends BaseController{



    @PostMapping("/add")
    public R<ShoppingCart>  add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据 ：{}",shoppingCart);
        //设置用户id 指定当前是那个用户的购物车 BaseContext（拦截器里set了）这里还有点小bug
//        Long currentId = BaseContext.getCurrentId();
        Long currentId = (Long) session.getAttribute("user");
        shoppingCart.setUserId(currentId);
        // 查询当前菜品或套餐是否 在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 根据登录用户的 userId去ShoppingCart表中查询该用户的购物车数据
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        // 先判断添加进购物车的是菜品
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        //  如果购物车中 已经存在该菜品或套餐，其数量+1，不存在，就将该购物车数据保存到数据库中
        if (cartServiceOne != null) {
            //已经存在于购物车
            Integer number = cartServiceOne.getNumber(); //获取原先的数量
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //不存在于购物车（添加到购物车）
            shoppingCart.setNumber(1); //设置默认数据1
            // 设置创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart); //添加
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);

    }

    // 在购物车中删减订单
    @PostMapping("/sub")
    public R<String> subToCart(@RequestBody ShoppingCart shoppingCart) {
//        // 清除缓存
//        cleanCache();
        log.info("购物车中的数据:{}" + shoppingCart.toString());

        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 查询当前菜品或套餐是否 在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 根据登录用户的 userId去ShoppingCart表中查询该用户的购物车数据
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        // 添加进购物车的是菜品，且 购物车中已经添加过 该菜品
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }


        ShoppingCart oneCart = shoppingCartService.getOne(queryWrapper);
        //  如果购物车中 已经存在该菜品或套餐
        if (oneCart != null) {
            Integer number = oneCart.getNumber();
            // 如果数量大于 0，其数量 -1， 否则清除
            if (number != 0) {
                oneCart.setNumber(number - 1);
                shoppingCartService.updateById(oneCart);
            } else {
                shoppingCartService.remove(queryWrapper);
            }
        }
        return R.success("成功删减订单!");
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

}