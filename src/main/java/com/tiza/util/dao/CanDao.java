package com.tiza.util.dao;

import com.tiza.util.config.Constant;
import com.tiza.util.dao.base.BaseDao;
import com.tiza.util.entity.CanInfo;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.List;

/**
 * Description: CanDao
 * Author: DIYILIU
 * Update: 2016-04-21 11:31
 */

@Component
public class CanDao extends BaseDao {

    public List<CanInfo> selectCan() {

        String sql = Constant.getSQL("selectCan");
        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {

            String softVersion = rs.getString("SoftVersion");
            String softName = rs.getString("SoftName");
            String modelCode = rs.getString("ModelCode");
            String functionXml = rs.getString("DcsFunctionSet");

            return new CanInfo(softVersion, softName, modelCode, functionXml);
        });
    }
}
