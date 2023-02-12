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
import java.util.concurrent.TimeUnit;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController extends BaseController{

    private final   String key = "shopping_";


    @PostMapping("/add")
    public R<ShoppingCart> addToCart(@RequestBody ShoppingCart shoppingCart) {
        // 清除缓存
        cleanCache();
        log.info("购物车中的数据:{}" + shoppingCart.toString());

        //设置用户id,指定当前是哪个用户的 购物车数据
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 查询当前菜品或套餐是否 在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 根据登录用户的 userId去ShoppingCart表中查询该用户的购物车数据
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        // 先判断添加进购物车的是菜品
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }


        ShoppingCart oneCart = shoppingCartService.getOne(queryWrapper);
        //  如果购物车中 已经存在该菜品或套餐，其数量+1，不存在，就将该购物车数据保存到数据库中
        if (oneCart != null) {
            Integer number = oneCart.getNumber();
            oneCart.setNumber(number + 1);
            shoppingCartService.updateById(oneCart);
        } else {
            shoppingCart.setNumber(1);
            // 设置创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            oneCart = shoppingCart;
        }
        return R.success(oneCart);
    }

    // 在购物车中删减订单
    @PostMapping("/sub")
    public R<String> subToCart(@RequestBody ShoppingCart shoppingCart) {
        // 清除缓存
        cleanCache();
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

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        // 缓存的购物车key
        String key = "shopping_" + BaseContext.getCurrentId();
        // 先查询redis中是否存在
        List<ShoppingCart> shoppingCarts = (List<ShoppingCart>) redisTemplate.opsForValue().get(key);
        // 如果缓存中无数据，那么就查询数据库
        if (null == shoppingCarts) {
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

            // 最晚下单的 菜品或套餐在购物车中最先展示
            queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
            shoppingCarts = shoppingCartService.list(queryWrapper);
            // 存储到redis中
            redisTemplate.opsForValue().set(key, shoppingCarts, 60, TimeUnit.MINUTES);
        }
        return R.success(shoppingCarts);
    }

    @DeleteMapping("/clean")
    public R<String> cleanCart() {
        // 调用清除缓存方法，清除缓存
        cleanCache();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // DELETE FROM shopping_cart WHERE (user_id = ?)
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);
        return R.success("成功清空购物车！");
    }

    /**
     * 清除redis缓存
     */
    public void cleanCache() {
        // BaseContext.getCurrentId();拿出当前登录的用户id 清除对应的缓存
        String key = "shopping_" + BaseContext.getCurrentId();
        redisTemplate.delete(key);
    }


}

