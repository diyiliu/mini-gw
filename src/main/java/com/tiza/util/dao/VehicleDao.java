package com.tiza.util.dao;

import com.tiza.util.config.Constant;
import com.tiza.util.dao.base.BaseDao;
import com.tiza.util.entity.VehicleInfo;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.List;

/**
 * Description: VehicleDao
 * Author: DIYILIU
 * Update: 2016-03-25 15:37
 */

@Component
public class VehicleDao extends BaseDao {


    public List<VehicleInfo> selectVehicle() {
        String sql = Constant.getSQL("selectVehicle");

        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            VehicleInfo vehicleInfo = new VehicleInfo();
            vehicleInfo.setId(rs.getInt("Id"));
            vehicleInfo.setTerminalId(rs.getString("DeviceId"));
            vehicleInfo.setName(rs.getString("NAME"));
            vehicleInfo.setLicense(rs.getString("License"));
            vehicleInfo.setCreateTimeStr(rs.getString("CreateTime"));

            return vehicleInfo;
        });
    }
}
