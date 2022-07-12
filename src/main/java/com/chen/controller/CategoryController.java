package com.chen.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.common.R;
import com.chen.domain.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController extends BaseController {

    /***
     *
     * 新增分类
     * @Author chen
     * @Date  16:43
     * @Param
     * @Return
     * @Since version-11

     */
    @PostMapping
    public R<String> save(@RequestBody Category category){

        log.info("category = {}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /*
    * 分页
    * */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        Page<Category> pageInfo = new Page<>(page,pageSize);
        log.info("page = {} , pageSize = {}",page,pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        //进行分页查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

/*
* 删除分类
* */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除id 为 id = {}",ids);
        categoryService.remove(ids);

        return R.success("分类信息删除成功");
    }
    //根据id修改分类信息
    @PutMapping
    public R<String> updata(@RequestBody Category category){
        log.info("修改分类信息 ：{}",category);
        categoryService.updateById(category);
        return R.success("分类信息修改成功");
    }
    /*
    * 根据条件查询分类数据
    * */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条(优先使用 .orderByAsc(Category::getSort))
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //返回一个list集合 (传入条件构造器)
        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }



}
