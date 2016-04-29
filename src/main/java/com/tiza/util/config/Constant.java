package com.tiza.util.config;

import com.tiza.util.entity.TableSchema;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: Constant
 * Author: DIYILIU
 * Update: 2016-03-24 15:58
 */
public final class Constant {

    public enum Protocol {
        ;
        public final static int M2_REPEAT_COUNT = 2;
        public final static int M2_REPEAT_TIME = 8;
        public final static int MOBILE_REPEAT_COUNT = 2;
        public final static int MOBILE_REPEAT_TIME = 10;
    }


    public enum DBInfo {
        ;
        public final static String DB_CLOUD_USER = "cloudgps";
        public final static String DB_CLOUD_INSTRUCTION = "gpsinstruction";
        public final static String DB_CLOUD_RAWDATA = "gpsrawdata";
        public final static String DB_CLOUD_VEHICLEGPSINFO = "vehiclegpsinfo";
        public final static String DB_CLOUD_VEHICLETRACK = "vehicletrack";
        public final static String DB_CLOUD_VEHICLEWORKPARAM= "vehicleworkparam";

        public final static List<TableSchema> DB_CLOUD_MONTH_TABLES = new ArrayList() {
            {

                this.add(new TableSchema(
                        "gpsrawdata",
                        " `Id` int(10) unsigned NOT NULL AUTO_INCREMENT," +
                                "  `DeviceId` bigint(20) NOT NULL COMMENT 'GPS设备编号'," +
                                "  `ReceiveTime` datetime NOT NULL COMMENT '数据接收时间'," +
                                "  `DataFlow` tinyint DEFAULT NULL COMMENT '数据流向(0上行;1下行;)'," +
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
                                "  `GpsVoltage` decimal(4,2) DEFAULT NULL COMMENT 'GPS电压值'," +
                                "  `GpsStatellite` tinyint(4) DEFAULT NULL COMMENT 'GPS卫星数'," +
                                "  `PowerOff` bit(1) DEFAULT NULL COMMENT '断电报警'," +
                                "  `LowVoltage` bit(1) DEFAULT NULL COMMENT '低电压报警'," +
                                "  `GpsModule` bit(1) DEFAULT NULL COMMENT 'GPS模块发故障报警'," +
                                "  `GpsAntenna` bit(1) DEFAULT NULL COMMENT 'GPS天线报警'," +
                                "  PRIMARY KEY (`Id`)",
                        "IX_VehicleTrack_Vehicle",
                        "`VehicleId`,`GpsTime`,`Id`"));

                this.add(new TableSchema(
                        "vehicleworkparam",
                        " `Id` int(10) unsigned NOT NULL AUTO_INCREMENT," +
                                "  `VehicleId` int(11) NOT NULL," +
                                "  `WorkDataTime` datetime NOT NULL," +
                                "  `SystemTime` datetime NOT NULL," +
                                "  `EncryptLat` decimal(9,6) NOT NULL," +
                                "  `EncryptLng` decimal(9,6) NOT NULL," +
                                "  `Speed` tinyint(4) DEFAULT NULL," +
                                "  `AccStatus` bit(1) DEFAULT NULL," +
                                "  `AccOnHours` decimal(8,2) DEFAULT NULL," +
                                "  `Bit1` tinyint(4) DEFAULT NULL," +
                                "  `Bit2` tinyint(4) DEFAULT NULL," +
                                "  `Bit3` tinyint(4) DEFAULT NULL," +
                                "  `Bit4` tinyint(4) DEFAULT NULL," +
                                "  `Bit5` tinyint(4) DEFAULT NULL," +
                                "  `Bit6` tinyint(4) DEFAULT NULL," +
                                "  `Bit7` tinyint(4) DEFAULT NULL," +
                                "  `Bit8` tinyint(4) DEFAULT NULL," +
                                "  `Bit9` tinyint(4) DEFAULT NULL," +
                                "  `Bit10` tinyint(4) DEFAULT NULL," +
                                "  `Bit11` tinyint(4) DEFAULT NULL," +
                                "  `Bit12` tinyint(4) DEFAULT NULL," +
                                "  `Bit13` tinyint(4) DEFAULT NULL," +
                                "  `Bit14` tinyint(4) DEFAULT NULL," +
                                "  `Bit15` tinyint(4) DEFAULT NULL," +
                                "  `Byte1` tinyint(4) DEFAULT NULL," +
                                "  `Byte2` tinyint(4) DEFAULT NULL," +
                                "  `Byte3` tinyint(4) DEFAULT NULL," +
                                "  `Byte4` tinyint(4) DEFAULT NULL," +
                                "  `Byte5` tinyint(4) DEFAULT NULL," +
                                "  `Byte6` tinyint(4) DEFAULT NULL," +
                                "  `Byte7` tinyint(4) DEFAULT NULL," +
                                "  `Byte8` tinyint(4) DEFAULT NULL," +
                                "  `Byte9` tinyint(4) DEFAULT NULL," +
                                "  `Byte10` tinyint(4) DEFAULT NULL," +
                                "  `Integer1` int(11) DEFAULT NULL," +
                                "  `Integer2` int(11) DEFAULT NULL," +
                                "  `Integer3` int(11) DEFAULT NULL," +
                                "  `Integer4` int(11) DEFAULT NULL," +
                                "  `Integer5` int(11) DEFAULT NULL," +
                                "  `Integer6` int(11) DEFAULT NULL," +
                                "  `Integer7` int(11) DEFAULT NULL," +
                                "  `Integer8` int(11) DEFAULT NULL," +
                                "  `Integer9` int(11) DEFAULT NULL," +
                                "  `Integer10` int(11) DEFAULT NULL," +
                                "  `Decimal1` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal2` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal3` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal4` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal5` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal6` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal7` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal8` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal9` decimal(10,4) DEFAULT NULL," +
                                "  `Decimal10` decimal(10,4) DEFAULT NULL," +
                                "  PRIMARY KEY (`Id`)",
                        "IX_VehicleWorkParam",
                        "`VehicleId`,`GpsTime`"
                ));
            }
        };
    }

    public void init() {
        initSqlCache();

    }

    private final static String SQL_FILE = "sql.xml";

    private static Map<String, String> sqlCache = new HashMap<>();

    public static String getSQL(String sqlId) {
        return sqlCache.get(sqlId);
    }

    public void initSqlCache() {
        sqlCache.clear();

        InputStream is = null;
        try {
            is = new ClassPathResource(SQL_FILE).getInputStream();
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(is);

            List<Node> sqlList = document.selectNodes("root/sql");
            for (Node sqlNode : sqlList) {
                String id = sqlNode.valueOf("@id");
                String content = sqlNode.getText().trim();
                sqlCache.put(id, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
