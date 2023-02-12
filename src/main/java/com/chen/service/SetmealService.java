package com.chen.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.domain.Setmeal;
import com.chen.dto.DishDto;
import com.chen.dto.SetmealDto;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {

    /*
    * 新增菜品
    * 同时保存菜品和套餐的关联关系
    * */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 根据套餐id修改售卖状态
     * @param status
     * @param ids
     */
    void updateSetmealStatusById(Integer status,List<Long> ids);

    /**
     * 回显套餐数据：根据套餐id查询套餐
     * @return
     */
    SetmealDto getDate(Long id);


}
