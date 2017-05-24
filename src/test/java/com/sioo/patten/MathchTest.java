package com.sioo.patten;

import com.sioo.db.mybatis.SessionFactory;
import com.sioo.db.mybatis.mapper.SysCacheMapper;
import com.sioo.log.LogInfo;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by morrigan on 2017/4/25.
 */
public class MathchTest {

    @Test
    public void test() {
        String regx = "因高三年级同学面临毕业，您的孩子[#.{1,4}#]借衡水中学（学校）图书馆图书[#.{1,15}#]价值[#.{1,5}#]元未还。因高三年级学习紧张，为不影响孩子学习，请您务必于[#.{1,4}#]年[#.{1,2}#]月[#.{1,2}#]日前把图书归还到衡水中学图书馆（衡水市桃城区英才路228号）。不能归还图书的，请按赔偿金额到衡水中学图书馆办理图书赔偿手续。不能到校办理图书赔偿的，可把赔偿金额转到6222080407000120802（工行、李卫红），并发短信注明（孩子班级、名字、赔偿金额）到13582680800。图书馆工作人员：姚 辉15175816993李卫红13582680800【家校互联】";
        String content = "因高三年级同学面临毕业，您的孩子张美佳借衡水中学（学校）图书馆图书《滚蛋吧!肿瘤君》价值89.4元未还。因高三年级学习紧张，为不影响孩子学习，请您务必于2017年5月20日前把图书归还到衡水中学图书馆（衡水市桃城区英才路228号）。不能归还图书的，请按赔偿金额到衡水中学图书馆办理图书赔偿手续。不能到校办理图书赔偿的，可把赔偿金额转到6222080407000120802（工行、李卫红），并发短信注明（孩子班级、名字、赔偿金额）到13582680800。图书馆工作人员：姚 辉15175816993李卫红13582680800【家校互联】";
        regx = regx.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\^", "\\\\^").replaceAll("\\$", "\\\\$").replaceAll("\\?", "\\\\?").replaceAll("\\+", "\\\\+").replaceAll("\\*", "\\\\*").replaceAll("\\|", "\\\\|");
        regx = regx.replaceAll("\\[\\#", "(").replaceAll("\\#\\]", ")");
        Pattern compile = Pattern.compile(regx);
        boolean matches = compile.matcher(content).matches();
        System.out.println(matches);

    }

    @Test
    public void test2(){
        String sub_pattern = "[^0-9a-zA-Z\u4E00-\u9FA5]*";
        Pattern pattern = toPattern(sub_pattern, "阿迪");
        String content="看病难说的分手阿迪费看病贵";
        boolean b = pattern.matcher(content).find();
        System.out.println(b);
    }

    private Pattern toPattern(String sub_pattern, String word) {
        try {
            StringBuffer pattern_buff = new StringBuffer();
            pattern_buff.append("^.*");
            for (int j = 0; j < word.length(); j++) {
                String sub_word = word.substring(j, j + 1);
                if (sub_word.equals("\\") || sub_word.equals("^") || sub_word.equals("$") || sub_word.equals("*") || sub_word.equals("+") || sub_word.equals("?")
                        || sub_word.equals("{") || sub_word.equals("}") || sub_word.equals("[") || sub_word.equals("]") || sub_word.equals("(") || sub_word.equals(")")
                        || sub_word.equals(".") || sub_word.equals("|")) {
                    sub_word = "\\" + sub_word;
                }
                pattern_buff.append(sub_word);
                if (j != word.length() - 1) {
                    pattern_buff.append(sub_pattern);
                }
            }
            pattern_buff.append(".*$");
            Pattern pattern = Pattern.compile(pattern_buff.toString());
            return pattern;
        } catch (Exception e) {
            LogInfo.getLog().errorAlert("用户屏蔽词正则表达式转换异常", "[UserBlackWordsCache。toPattern(" + sub_pattern + "," + word + ") ]" + LogInfo.getTrace(e));
        }
        return null;
    }


    public List<Map<String, Object>> findSmsWordsUser(Integer uid, int type) {
        SqlSession session = null;
        try {
            session = SessionFactory.getSessionFactory().openSession();
            SysCacheMapper mapper = session.getMapper(SysCacheMapper.class);
            List<Map<String, Object>> list = mapper.findSmsWordsUser(uid,type);
            return list;
        } catch (Exception ex) {
            LogInfo.getLog().errorAlert("获取用户屏蔽词异常", "[SysCacheDao.findSmsWordsUser(" + uid + ") ]" + LogInfo.getTrace(ex));
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }
}
