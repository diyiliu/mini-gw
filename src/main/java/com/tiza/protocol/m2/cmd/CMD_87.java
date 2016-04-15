package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.M2Header;
import com.tiza.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

        toDB(m2Header.getTerminalId(), position, status, param);
    }

    private Map parseParameter(byte[] content) {
        Map parameters = new HashMap<>();

        ByteBuf byteBuf = Unpooled.copiedBuffer(content);

        while (byteBuf.readableBytes() > 4) {
            int id = byteBuf.readUnsignedShort();
            int length = byteBuf.readUnsignedShort();
            if (byteBuf.readableBytes() < length) {
                break;
            }
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);

            parameters.put(id, bytes);
        }

        return parameters;
    }

}
