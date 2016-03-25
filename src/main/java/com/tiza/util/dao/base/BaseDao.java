package com.tiza.util.dao.base;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Description: BaseDao
 * Author: DIYILIU
 * Update: 2016-03-25 15:36
 */

@Component
public abstract class BaseDao {

    @Resource
    protected JdbcTemplate jdbcTemplate;
}
