package com.tiza;

import com.tiza.util.SpringUtil;
import com.tiza.util.client.impl.DBPClient;

/**
 * Description: Main
 * Author: DIYILIU
 * Update: 2016-03-15 14:48
 */

public class Main {


    public static void main(String[] args) {

        SpringUtil.init();

        for (int i = 0; i < 10; i++){

            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

           // DBPClient.sendSQL("hello abc: " + i);
        }

    }
}