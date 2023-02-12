package com.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.common.CustomException;
import com.chen.domain.Category;
import com.chen.domain.Dish;
import com.chen.domain.Setmeal;
import com.chen.mapper.CategoryMapper;
import com.chen.service.CategoryService;
import com.chen.service.DishService;
import com.chen.service.SetmealService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {

    @Resource
    private DishService dishService;
    @Resource
    private SetmealService setmealService;
    /*
   * 根据id删除分类，删除前进行判断
   * */

    @Override
    public void remove(Long ids) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        int count = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联了菜品，如果关联了，抛出业务异常
        if (count > 1){
            //已经关联菜品，不能删除
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果关联了，抛出业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);
        int count1setmeal= setmealService.count();
        if (count1setmeal > 1){
            //已经关联套餐，不能删除
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //正常删除
        super.removeById(ids);
    }
}
