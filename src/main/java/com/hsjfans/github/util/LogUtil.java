package com.hsjfans.github.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author hsjfans[hsjfans.scholar@gmail.com]
 */
public class LogUtil {
    private static final  Logger logger = Logger.getLogger("com.hsjfans.github.api-doc");

   static  {
        logger.setLevel(Level.WARNING);
    }

    public static  void  info(String msg){
        logger.info(msg);
    }

    public static void warn(String msg){
        logger.warning(msg);
    }

}
