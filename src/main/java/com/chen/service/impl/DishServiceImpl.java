package com.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.common.CustomException;
import com.chen.domain.Dish;
import com.chen.domain.DishFlavor;
import com.chen.dto.DishDto;
import com.chen.mapper.DishMapper;
import com.chen.service.DishFlavorService;
import com.chen.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {


    @Resource
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存基本菜品到dish表 (this.save 调用父类的save方法)
        this.save(dishDto);

        Long dishId = dishDto.getId();
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //将菜品id放入集合
        //item为遍历的每一项 (.collect(Collectors.toList()); 是map再转 list)
        flavors = flavors.stream().map(item ->{
             item.setDishId(dishId);
             return item;
        }).collect(Collectors.toList());

        //保存口味数据到dish_flavor表 (saveBatch 保存集合 dishDto.getFlavors()是一个list列表)
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     *套餐批量删除和单个删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {

        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //先查询该菜品是否在售卖，如果是则抛出业务异常
        queryWrapper.in(ids!=null,Dish::getId,ids);
        List<Dish> list = this.list(queryWrapper);
        for (Dish dish : list) {
            Integer status = dish.getStatus();
            //如果不是在售卖,则可以删除
            if (status == 0){
                this.removeById(dish.getId());
            }else {
                //此时应该回滚,因为可能前面的删除了，但是后面的是正在售卖
                throw new CustomException("删除菜品中有正在售卖菜品,无法全部删除");
            }
        }

    }



    /*
    * 根据id查询 菜品信息和对应的口味信息
    *
    * */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品的基本信息 ，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //由于返回对象为Dto 进行对象拷贝
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor 表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        //查询
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }
}
