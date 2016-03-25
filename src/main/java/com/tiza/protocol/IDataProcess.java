package com.tiza.protocol;

import com.tiza.protocol.model.header.Header;

/**
 * Description: IDataProcess
 * Author: DIYILIU
 * Update: 2016-03-21 9:55
 */
public interface IDataProcess {

    Header dealHeader(byte[] bytes);

    void parse(byte[] content, Header header);

    byte[] pack(String id, Header header);
}
