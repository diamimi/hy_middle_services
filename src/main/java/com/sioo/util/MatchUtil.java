package com.sioo.util;

import org.apache.commons.lang.StringUtils;

/**
 * 屏蔽词过滤
 * Created by morrigan on 2017/4/25.
 */
public class MatchUtil {

    private static MatchUtil matchUtil;

    public static MatchUtil getInstance() {
        if (matchUtil == null) {
            matchUtil= new MatchUtil();
        }
       return matchUtil;
    }

    public String match(String content, String regx) {
        String[] regs = regx.split("&");
        boolean regResult = true;
        for (String reg : regs) {
            if (StringUtils.startsWith(reg, "(") && StringUtils.endsWith(reg, ")")) {
                reg = reg.substring(1, reg.length() - 1);
            }
            String[] splits = reg.split("\\|");
            int result = 0;
            for (String split : splits) {
                if (StringUtils.contains(content, split)) {
                    result = 1;
                    break;
                }
            }
            if (result != 1) {
                regResult = false;
                break;
            }
        }
        if (regResult) {
            return regx;
        }
        return null;
    }

}
