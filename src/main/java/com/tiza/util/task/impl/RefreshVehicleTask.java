package com.tiza.util.task.impl;

import com.tiza.util.cache.ICache;
import com.tiza.util.dao.VehicleDao;
import com.tiza.util.entity.VehicleInfo;
import com.tiza.util.task.ITask;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description: RefreshVehicleTask
 * Author: DIYILIU
 * Update: 2016-03-25 14:31
 */


public class RefreshVehicleTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache vehicleCacheProvider;

    @Resource
    private VehicleDao vehicleDao;

    @Override
    public void execute() {
        logger.info("刷新车辆列表...");

        List<VehicleInfo> vehicleInfos = vehicleDao.selectVehicle();

        refresh(vehicleInfos, vehicleCacheProvider);
    }


    private void refresh(List<VehicleInfo> vehicleInfos, ICache vehicleCache) {

        if (vehicleInfos == null || vehicleInfos.size() < 1){
            logger.warn("无车辆信息！");
            return;
        }

        Set oldKeys = vehicleCache.getKeys();
        Set tempKeys = new HashSet<>(vehicleInfos.size());

        for (VehicleInfo vehicle : vehicleInfos) {
            vehicleCache.put(vehicle.getTerminalId(), vehicle);
            tempKeys.add(vehicle.getTerminalId());
        }

        Collection subKeys = CollectionUtils.subtract(oldKeys, tempKeys);

        subKeys.forEach(key -> vehicleCache.remove(key));
    }
}
