package com.tiza.util.config;

import com.tiza.util.entity.TableSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Constant
 * Author: DIYILIU
 * Update: 2016-03-24 15:58
 */
public class Constant {

    public enum DBInfo {
        ;
        public final static String DB_CLOUD_USER = "cloudgps";
        public final static String DB_CLOUD_RAWDATA = "gpsrawdata";


        public final static List<TableSchema> DB_CLOUD_MONTH_TABLES = new ArrayList() {{

            this.add(new TableSchema(
                    "gpsrawdata",
                    " `Id` int(10) unsigned NOT NULL AUTO_INCREMENT," +
                            "  `DeviceId` bigint(20) NOT NULL COMMENT 'GPS设备编号'," +
                            "  `ReceiveTime` datetime NOT NULL COMMENT '数据接收时间'," +
                            "  `DataFlow` bit(1) DEFAULT NULL COMMENT '数据流向(0上行;1下行;)'," +
                            "  `Instruction` varchar(8) DEFAULT NULL COMMENT '指令'," +
                            "  `RawData` text COMMENT '原始数据'," +
                            "  PRIMARY KEY (`Id`)",
                    "IX_GpsRawData",
                    "`DeviceId`,`ReceiveTime`,`DataFlow`,`Instruction`"));

            this.add(new TableSchema(
                    "vehicletrack",
                    "`Id` int(11) NOT NULL AUTO_INCREMENT," +
                            "  `VehicleId` int(11) DEFAULT NULL," +
                            "  `Lat` decimal(9,6) DEFAULT NULL COMMENT 'GPS终端上传纬度'," +
                            "  `Lng` decimal(9,6) DEFAULT NULL COMMENT 'GPS终端上传经度'," +
                            "  `EncryptLat` decimal(9,6) DEFAULT NULL COMMENT '加密纬度'," +
                            "  `EncryptLng` decimal(9,6) DEFAULT NULL COMMENT '加密经度'," +
                            "  `Speed` tinyint(4) DEFAULT NULL COMMENT '速度(KM/H)'," +
                            "  `Direction` smallint(6) DEFAULT NULL COMMENT '方向'," +
                            "  `GpsTime` datetime DEFAULT NULL COMMENT 'GPS时间'," +
                            "  `SystemTime` datetime DEFAULT NULL COMMENT '系统时间'," +
                            "  `AccStatus` bit(1) DEFAULT NULL COMMENT 'Acc状态(0 Acc关;1 Acc开)'," +
                            "  `LocationStatus` bit(1) DEFAULT NULL COMMENT 'GPS定位状态(0 GPS未定位;1 GPS已定位)'," +
                            "  `AccOnHours` decimal(8,2) DEFAULT NULL COMMENT '终端累计工作时间'," +
                            "  `GsmSignal` tinyint(4) DEFAULT NULL COMMENT 'GSM信号强度(0-32)'," +
                            "  `GpsStatellite` tinyint(4) DEFAULT NULL COMMENT 'GPS卫星数'," +
                            "  `PowerOff` bit(1) DEFAULT NULL COMMENT '断电报警'," +
                            "  `LowVoltage` bit(1) DEFAULT NULL COMMENT '低电压报警'," +
                            "  `GpsModule` bit(1) DEFAULT NULL COMMENT 'GPS模块发故障报警'," +
                            "  `GpsAntenna` bit(1) DEFAULT NULL COMMENT 'GPS天线报警'," +
                            "  PRIMARY KEY (`Id`)",
                    "IX_VehicleTrack_Vehicle",
                    "`VehicleId`,`GpsTime`,`Id`"));
        }};
    }
}
