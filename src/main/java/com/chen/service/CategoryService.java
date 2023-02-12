package com.chen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.domain.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long ids);
}
