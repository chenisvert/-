package com.chen.controller;

import com.chen.common.R;
import com.chen.domain.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController extends BaseController {


    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {

        log.info("订单信息:" + orders.toString());

        ordersService.submit(orders);
        return R.success("已成功下单!");

    }
}