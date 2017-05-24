package com.sioo.hy.cmpp.vo;

import java.io.Serializable;
import java.util.regex.Pattern;

public class ReleaseTemplateVo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ReleaseTemplateVo() {
    }

    public ReleaseTemplateVo(int type, Long effectivetime, String content) {
        this.type = type;
        this.effectivetime = effectivetime;
        if (pattern == null) {
            //增加replaceAll("\\^", "\\\\^").replaceAll("\\$", "\\\\$").replaceAll("\\?", "\\\\?").replaceAll("\\+", "\\\\+").replaceAll("\\*", "\\\\*").replaceAll("\\|", "\\\\|")替换内容中的特殊符号
            //2017-03-29日陈泉霖修改
            content = content.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\^", "\\\\^").replaceAll("\\$", "\\\\$").replaceAll("\\?", "\\\\?").replaceAll("\\+", "\\\\+").replaceAll("\\*", "\\\\*").replaceAll("\\|", "\\\\|");
            content = content.replaceAll("\\[\\#", "(").replaceAll("\\#\\]", ")");
            this.pattern = Pattern.compile(content);
        }
    }

    private int type;
    private Long effectivetime;
    private Pattern pattern;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getEffectivetime() {
        return effectivetime;
    }

    public void setEffectivetime(Long effectivetime) {
        this.effectivetime = effectivetime;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public void setPattern(String content) {
        if (pattern == null) {
            content = content.replaceAll("[#", "(").replaceAll("#]", ")");
            pattern = Pattern.compile(content);
        }
    }
}
