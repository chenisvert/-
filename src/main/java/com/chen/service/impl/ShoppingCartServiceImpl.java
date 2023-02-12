package com.chen.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.domain.ShoppingCart;
import com.chen.mapper.ShoppingCartMapper;
import com.chen.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}
