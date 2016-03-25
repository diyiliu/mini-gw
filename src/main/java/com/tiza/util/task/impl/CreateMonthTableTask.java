package com.tiza.util.task.impl;

import com.tiza.util.DateUtil;
import com.tiza.util.config.Constant;
import com.tiza.util.entity.TableSchema;
import com.tiza.util.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Description: CreateMonthTableTask
 * Author: DIYILIU
 * Update: 2016-03-24 17:13
 */
public class CreateMonthTableTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static int NEXT_TO = 1;

    @Resource
    JdbcTemplate jdbcTemplate;

    @Override
    public void execute() {
        logger.info("创建月表...");
        List<TableSchema> schemaList = Constant.DBInfo.DB_CLOUD_MONTH_TABLES;

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i <= NEXT_TO; i++) {
            calendar.add(Calendar.MONTH, i);
            Date date = calendar.getTime();
            String monthTable = DateUtil.dateToString(date, "%1$tY%1$tm");
            for (TableSchema schema : schemaList) {

                // 创建月表
                if (!existTable(schema, monthTable)) {
                    createTable(schema, monthTable);
                }
                // 创建索引
                if (!existIndex(schema, monthTable)) {
                    createIndex(schema, monthTable);
                }
            }
        }
    }

    private void createTable(TableSchema schema, String monthTable) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("CREATE TABLE ").append(schema.getTableName()).append(monthTable).append(" (")
                .append(schema.getTableContent())
                .append(" )");

        logger.info("创建月表：{}, SQL[{}]", schema.getTableName() + monthTable, strBuilder.toString());

        jdbcTemplate.execute(strBuilder.toString());
    }

    private void createIndex(TableSchema schema, String monthTable) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("CREATE INDEX ").append(schema.getIndexName())
                .append(" ON ").append(schema.getTableName()).append(monthTable).append("(").append(schema.getIndexContent()).append(")");

        logger.info("创建索引：{}, 表名[{}], SQL[{}]", schema.getIndexName(), schema.getTableName() + monthTable, strBuilder.toString());

        jdbcTemplate.batchUpdate(strBuilder.toString());
    }

    private boolean existTable(TableSchema schema, String monthTable) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("SELECT ")
                .append(" `TABLE_NAME`  ")
                .append("FROM ")
                .append("information_schema.`TABLES` t ")
                .append("WHERE t.`TABLE_SCHEMA` = '").append(Constant.DBInfo.DB_CLOUD_USER).append("'")
                .append(" AND t.`TABLE_NAME` = '").append(schema.getTableName()).append(monthTable).append("'");

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(strBuilder.toString());

        if (rowSet.next()) {

            return true;
        }

        return false;
    }

    private boolean existIndex(TableSchema schema, String monthTable) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("SELECT DISTINCT")
                .append(" `INDEX_NAME`  ")
                .append("FROM ")
                .append("information_schema.statistics s ")
                .append("WHERE s.`INDEX_SCHEMA` = '").append(Constant.DBInfo.DB_CLOUD_USER).append("'")
                .append(" AND s.`TABLE_NAME` = '").append(schema.getTableName()).append(monthTable).append("'")
                .append(" AND s.`INDEX_NAME` = '").append(schema.getIndexName()).append("'");

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(strBuilder.toString());

        if (rowSet.next()) {

            return true;
        }

        return false;
    }
}
