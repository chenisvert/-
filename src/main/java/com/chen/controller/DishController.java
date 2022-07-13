package com.chen.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.common.R;
import com.chen.domain.Category;
import com.chen.domain.Dish;
import com.chen.domain.DishFlavor;
import com.chen.dto.DishDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController extends BaseController {


    /*
    * 新增菜品
    * */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);
//        //清理所有菜品的缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        //清理某个分页下面的菜品缓存数据
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);


        return R.success("修改菜品成功");
    }


    /*
    *
    * 根据条件查询对应的菜品数据
    * */
//
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        //判断是否为null
//        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//        //查询状态为1 的（起售）
//        queryWrapper.eq(Dish::getStatus ,1);
//        //添加排序条件(先根据sort 排序)
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//        return R.success(list);
//    }


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        // 使用缓存进行代码优化
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 首先要从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        // 如果缓存中存在则直接返回
        if (null != dishDtoList) {
            log.info("【log】:菜品命中缓存！");
            return R.success(dishDtoList);
        }
        log.info("dish:{}", dish);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(dish.getName()), Dish::getName, dish.getName());
        queryWrapper.eq(null != dish.getCategoryId(), Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 列出所有的Dish
        List<Dish> dishes = dishService.list(queryWrapper);

        dishDtoList = dishes.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, item.getId());

            dishDto.setFlavors(dishFlavorService.list(wrapper));
            return dishDto;
        }).collect(Collectors.toList());
        // 如果不存在，就查数据库，然后存到redis中 60分钟后过期
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }




    @GetMapping("/page")
    private R<Page> page(int page,int pageSize,String name){

        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //根据name来进行模糊查询 （不为空就进行）
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件（根据修改时间排序）
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝(从pageInfo 拷贝到 dishDtoPage) 忽略 records属性不拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();


        List<DishDto> list =  records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            //如果能根据id查到对应的分类就查，否则直接返回
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;

        }).collect(Collectors.toList());


        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /*
    * 根据id查询 菜品信息和对应的口味信息
    * */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /*以下为新增功能*/

    /**
     * 对菜品批量或者是单个 进行停售或者是起售
     * @return
     */
    @PostMapping("/status/{status}")
//这个参数这里一定记得加注解才能获取到参数，否则这里非常容易出问题
    public R<String> status(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        //log.info("status:{}",status);
        //log.info("ids:{}",ids);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(ids !=null,Dish::getId,ids);
        //根据数据进行批量查询
        List<Dish> list = dishService.list(queryWrapper);

        for (Dish dish : list) {
            if (dish != null){
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }
        return R.success("售卖状态修改成功");
    }

    /**
     * 套餐批量删除和单个删除
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        //删除菜品  这里的删除是逻辑删除
        dishService.deleteByIds(ids);
        //删除菜品对应的口味  也是逻辑删除
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);
        return R.success("菜品删除成功");
    }
}
