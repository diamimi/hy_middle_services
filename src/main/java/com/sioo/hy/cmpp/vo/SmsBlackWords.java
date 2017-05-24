package com.sioo.hy.cmpp.vo;

import java.io.Serializable;

/**
 * Created by HQ
 * Mail:94554798@qq.com
 * Date:2017/3/29 17:44
 */
public class SmsBlackWords implements Serializable {

    private static final long serialVersionUID = 1L;

    private String words;

    private Integer screentype;

    private Integer group_id;

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public Integer getScreentype() {
        return screentype;
    }

    public void setScreentype(Integer screentype) {
        this.screentype = screentype;
    }

    public Integer getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Integer group_id) {
        this.group_id = group_id;
    }

}
