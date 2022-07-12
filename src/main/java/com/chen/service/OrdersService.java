package com.chen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.domain.Orders;

import java.util.List;

public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);


}