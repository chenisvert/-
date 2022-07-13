package com.chen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.domain.OrderDetail;

import java.util.List;

public interface OrderDetailService extends IService<OrderDetail> {

    List<OrderDetail> getOrderDetailsByOrderId(Long orderId);
}