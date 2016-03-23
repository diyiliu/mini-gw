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

        for (int i = 0; i < 5; i++) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (i == 5 || i == 0) {
                DBPClient.sendSQL("insert into user(id, name, age)values(" + i + "tom" + i + "," + (10 + i) + ")");
            }else {
                DBPClient.sendSQL("insert into user(id, name, age)values(" + i + ", tom" + i + "," + (10 + i) + ")");
            }


//            if (i == 5 || i == 10) {
//                DBPClient.sendSQL("insert into user(id, name, age)values(" + i + ", tom" + i + "," + (10 + i) + ")");
//            } else {
//                DBPClient.sendSQL("insert into user(id, name, age)values(" + i + ", 'tom" + i + "'," + (10 + i) + ")");
//            }
        }

    }
}
