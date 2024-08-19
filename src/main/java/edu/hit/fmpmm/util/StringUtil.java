package edu.hit.fmpmm.util;

import cn.hutool.core.util.RandomUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    /**
     * 判断手机号是否是合法的
     * @param phone 待校验的手机号
     * @return 合法的true 不合法false
     */
    public static boolean isPhoneLegal(String phone) {
        if (phone == null || phone.length() == 0) {
            return false;
        }
        Pattern pattern = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(16[0-9])|(17[0-9])|(18[0-9]))\\d{8}$+");
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    /**
     * 判断密码是否合法
     * @param pwd 密码
     * @return 合法true 不合法false
     */
    public static boolean isPwdLegal(String pwd) {
        if (pwd == null || pwd.length() == 0 || pwd.length() > 20) {
            return false;
        }
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(pwd);  // 不包含中文
        return !m.find();
    }

    /**
     * 产生长度为len的随机字符串
     * @param len 随机字符串的长度
     * @return 字符串
     */
    public static String randomString(int len) {
        return RandomUtil.randomString(len);
    }
}
