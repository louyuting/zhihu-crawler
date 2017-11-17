package com.crawl.core.util;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/16
 */
public class TimeDelayUtil {

    public static void delayMilli(int num){
        long count=0;
        for(int s=0; s<num;s++){
            for(long j=0; j<900l; j++){
                for (long i=0; i<400l; i++){
                    count = count * i;
                    count = count/1000;
                }
            }
        }

    }


    public static void delayOneSecond(){
        long count=0;
        for(long j=0; j<900000l; j++){
            for (long i=0; i<350l; i++){
                count = count * i;
                count = count/1000;
            }
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for (int i=0; i<10; i++){
            delayMilli(100);
        }
        long end = System.currentTimeMillis();
        System.err.println((end - start));
    }
}
