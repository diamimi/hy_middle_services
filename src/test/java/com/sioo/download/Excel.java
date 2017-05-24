package com.sioo.download;

import org.junit.Test;

/**
 * Created by morrigan on 2017/5/11.
 */
public class Excel {

    @Test
    public void test(){
         for(int i=0;i<11;i++){
             String content="SELECT senddate,mobile,content from sms_user_history_";
            content=content+(170501+i)+" where uid=90001 UNION";
             System.out.println(content);
        }
    }

    @Test
    public void test1() {
        String a="s";
        String b="d";
        System.out.println(a==b);

    }
}
