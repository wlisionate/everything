package util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PinyinUtil {
    /** * 中文字符格式 */
    private static final String CHINESE_PATTERN = "[\\u4E00-\\u9FA5]";
    /**
     * 汉语拼音格式化类
     */
    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        //设置为小写
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //设置没有音调
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //设置V，比如lv
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }
    public static boolean containsChinese(String name){
        return name.matches(".*"+CHINESE_PATTERN+".*");
    }
    /**
     *通过文件名获取全拼＋拼音首字母
     * @param name
     * @return
     */
    public static String[] get(String name){
        String[] result = new String[2];
        StringBuilder pinyin = new StringBuilder();
        StringBuilder pinyinFirst = new StringBuilder();
        for(char c:name.toCharArray()){
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c,FORMAT);
                if(pinyins == null || pinyins.length == 0){
                    pinyin.append(c);
                    pinyinFirst.append(c);
                }else {
                    pinyin.append(pinyins[0]);
                    pinyinFirst.append(pinyins[0].charAt(0));
                }
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                pinyin.append(c);
                pinyinFirst.append(c);
            }
        }
        result[0] = pinyin.toString();
        result[1] = pinyinFirst.toString();
        return result;
    }

    /**
     * 和
     * @param name
     * @param fullSpell true表示全拼，false表示拼音首字母
     * @return 包含多音字的字符串二维数组
     */
    public static String[][] get(String name,boolean fullSpell){
        char []chars = name.toCharArray();
        String[][] result = new String[chars.length][];
        for(int i = 0;i<chars.length;i++){
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(chars[i],FORMAT);
                if(pinyins == null || pinyins.length == 0){
                    result[i] = new String[]{String.valueOf(chars[i])};
                }else if(fullSpell){//全拼
                    result[i] = pinyins;
                }else {//拼音首字母
                    result[i] = unique(pinyins,fullSpell);
                }
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                result[i] = new String[]{String.valueOf(chars[i])};
            }
        }
        return result;
    }

    /**
     * 进行类排列组合
     * 每个中文字符返回的拼音都是字符串数组，每两个字符串数组合并为一个字符串数组
     * 依次类推
     * @param pinyinArray
     */
    public static String[] compose(String[][] pinyinArray){
        if(pinyinArray == null || pinyinArray.length == 0){
            return null;
        }else if(pinyinArray.length == 1){
            return pinyinArray[0];
        }else {
            for(int i = 1;i<pinyinArray.length;i++){
                pinyinArray[0] = compose(pinyinArray[0],pinyinArray[1]);
            }
            return pinyinArray[0];
        }
    }

    /**
     * 字符串数组去重
     * @param array
     * @param fullSpell
     * @return
     */
    public static String[] unique(String[] array,boolean fullSpell){
        Set<String> set = new HashSet<>();
        for(String s:array){
            if(fullSpell){
                set.add(s);
            }else {
                set.add(String.valueOf(s.charAt(0)));
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 合并两个拼音数组
     * @param pinyins1
     * @param pinyins2
     * @return
     */
    public static String[] compose(String[] pinyins1,String[] pinyins2 ){
        String[]result = new String[pinyins1.length*pinyins2.length];
        for(int i = 0;i<pinyins1.length;i++){
            for(int j = 0;j<pinyins2.length;j++){
                result[i*pinyins2.length+j] = pinyins1[i]+pinyins2[j];
            }
        }
        return result;
    }
    public static void main(String[] args) {
        System.out.println(Arrays.toString(compose(get("和长和",true))));
        System.out.println(Arrays.toString(compose(get("和长和",false))));
    }
}
