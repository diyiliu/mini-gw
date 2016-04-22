package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.M2Header;
import com.tiza.util.CommonUtil;
import com.tiza.util.bean.CanPackage;
import com.tiza.util.bean.NodeItem;
import com.tiza.util.cache.ICache;
import com.tiza.util.entity.CanInfo;
import com.tiza.util.entity.VehicleInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: CMD_87
 * Author: DIYILIU
 * Update: 2016-03-21 16:22
 */

@Service
public class CMD_87 extends M2DataProcess {

    public CMD_87() {
        this.cmdId = 0x87;
    }

    @Resource
    private ICache canCacheProvider;

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte[] positionArray = new byte[22];
        buf.readBytes(positionArray);

        Position position = renderPosition(positionArray);
        Status status = renderStatu(position.getStatus());

        byte[] paramArray = new byte[buf.readableBytes()];
        buf.readBytes(paramArray);

        Map<Integer, byte[]> parameters = parseParameter(paramArray);

        Parameter param = new Parameter();
        if (parameters.containsKey(0x01)) {
            long accTime = CommonUtil.bytesToLong(parameters.get(0x01));
            param.setAccTime(accTime);
        }
        if (parameters.containsKey(0x02)) {
            int gsmSignal = CommonUtil.getNoSin(parameters.get(0x02)[0]);
            param.setGsmSignal(gsmSignal);
        }
        if (parameters.containsKey(0x03)) {
            double voltage = CommonUtil.bytesToLong(parameters.get(0x03));
            param.setVoltage(voltage);
        }
        if (parameters.containsKey(0x04)) {
            int satellite = CommonUtil.getNoSin(parameters.get(0x04)[0]);
            param.setSatellite(satellite);
        }

        VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(m2Header.getTerminalId());
        CanInfo canInfo = (CanInfo) canCacheProvider.get(vehicleInfo.getSoftVersion());

        Map emptyValues = canInfo.getEmptyValues();
        if (canInfo != null && parameters.containsKey(canInfo.getModelCode())){
            byte[] bytes = parameters.get(canInfo.getModelCode());
            Map<String, CanPackage> canPackages = canInfo.getCanPackages();

            Map canValues = parseCan(bytes, canPackages, canInfo.getPidLength());
            emptyValues.putAll(canValues);
        }
        param.setCanValues(emptyValues);


        toDB(m2Header.getTerminalId(), position, status, param);
    }

    private Map parseParameter(byte[] content) {
        Map parameters = new HashMap<>();

        ByteBuf byteBuf = Unpooled.copiedBuffer(content);

        while (byteBuf.readableBytes() > 4) {
            int id = byteBuf.readUnsignedShort();
            int length = byteBuf.readUnsignedShort();
            if (byteBuf.readableBytes() < length) {
                logger.error("工况数据长度不足！");
                break;
            }
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);

            parameters.put(id, bytes);
        }

        return parameters;
    }

    private Map parseCan(byte[] bytes, Map<String, CanPackage> canPackages, int idLength){

        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        Map canValues = new HashedMap();
        while (buf.readableBytes() > idLength){
            byte[] idBytes = new byte[idLength];
            buf.readBytes(idBytes);

            String packageId = CommonUtil.bytesToStr(idBytes);
            if (!canPackages.containsKey(packageId)){
                logger.error("未配置的功能集[{}]", packageId);
                break;
            }

            CanPackage canPackage = canPackages.get(packageId);
            if (buf.readableBytes() < canPackage.getLength()){
                logger.error("功能集数据不足！");
                break;
            }
            byte[] content = new byte[canPackage.getLength()];
            buf.readBytes(content);

            Map values = parsePackage(content, canPackage.getItemList());
            canValues.putAll(values);
        }

        return canValues;
    }

    private Map parsePackage(byte[] content, List<NodeItem> nodeItems){

        Map packageValues = new HashMap<>(nodeItems.size());

        for (NodeItem item: nodeItems){
            try {
                packageValues.put(item.getField().toUpperCase(), parseItem(content, item));
            } catch (ScriptException e) {
                logger.error("解析表达式错误：", e);
            }
        }

        return packageValues;
    }

    private String parseItem(byte[] data, NodeItem item) throws ScriptException {

        String tVal;

        byte[] val = CommonUtil.byteToByte(data, item.getByteStart(), item.getByteLen(), item.getEndian());

        int tempVal = CommonUtil.byte2int(val);
        if (item.isOnlyByte()) {
            tVal = CommonUtil.parseExp(tempVal, item.getExpression(), item.getType());
        } else {
            int biteVal = CommonUtil.getBits(tempVal, item.getBitStart(), item.getBitLen());
            tVal = CommonUtil.parseExp(biteVal, item.getExpression(), item.getType());
        }

        return tVal;
    }
}
