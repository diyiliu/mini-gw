package com.tiza;

import com.tiza.util.SpringUtil;
import com.tiza.util.client.impl.DBPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: Main
 * Author: DIYILIU
 * Update: 2016-03-15 14:48
 */

public class Main {

    public static void main(String[] args) {

        SpringUtil.init();

        for (int i = 0; i < 3; i++) {

//            if (i % 30 == 10) {
//                DBPClient.sendSQL("insert into user(id, name, age)values(" + i + ", tom" + i + "," + (10 + i) + ")");
//            } else {
//                DBPClient.sendSQL("insert into user(id, name, age)values(" + i + ", 'tom" + i + "'," + (10 + i) + ")");
//            }


            if (i % 2 == 0){
                DBPClient.sendSQL("update user set age=" + i + "where id=" + i);
            }else {
                DBPClient.sendSQL("update user set age=" + i + " where id=" + i);
            }

            //DBPClient.sendSQL("update user set age=" + (i + 100) + " where id=" + i/10);

        }

    }
}
