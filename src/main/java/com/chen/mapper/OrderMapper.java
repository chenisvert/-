package com.chen.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.domain.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

}