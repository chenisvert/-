package com.chen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.domain.Dish;
import com.chen.dto.DishDto;

import java.util.List;

public interface DishService extends IService<Dish> {


    //新增菜品，并插入菜品对应的口味数据，需要操作两张表 dish,dish_flavor
    public void saveWithFlavor(DishDto dishDto);
    //根据id查询 菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);
    //更新菜品信息，同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
    //根据传过来的id批量或者是单个的删除菜品
    void deleteByIds(List<Long> ids);
}
