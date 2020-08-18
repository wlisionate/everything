package task;

import util.DBUtil;

import java.io.*;
import java.net.FileNameMap;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * 1.初始化数据库
 * 2.读取sql文件
 * 3.执行sql来初始化表
 */
public class DBInit {
    public static String[] readSQL()  {
        try {
            InputStream is = DBInit.class.getClassLoader().getResourceAsStream("init.sql");
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null){
                if(line.contains("--"))//去掉注释的代码
                    line = line.substring(0,line.indexOf("--"));
                sb.append(line);
            }
            String[] split = sb.toString().split(";");
            return split;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("读取sql文件错误");
        }
    }
    public static void init(){
        Connection connection = null;
        Statement statement = null;
        try{
            connection = DBUtil.getConnection();
            String[] sqls = readSQL();
            statement = connection.createStatement();
            for(String sql:sqls){
                statement.executeUpdate(sql);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("读取表失败",e);
        }finally {
            DBUtil.close(connection,statement);
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(readSQL()));
        init();
    }
}
