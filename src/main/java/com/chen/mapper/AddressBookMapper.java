package com.chen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.chen.domain.AddressBook;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {

}
