package com.tiza.util.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * Description: IM2Sender
 * Author: DIYILIU
 * Update: 2016-03-29 15:53
 */

@WebService
public interface IM2Sender extends ISender{

    @WebMethod
    void locate(int id, String terminalId, int interval);
}
