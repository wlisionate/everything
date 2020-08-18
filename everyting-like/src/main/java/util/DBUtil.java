package util;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import task.DBInit;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    private static volatile DataSource DATA_SOURCE;

    /**
     *
     * @return
     */
    private static DataSource getDataSource(){
        if(DATA_SOURCE == null){
            synchronized (DBUtil.class){
                if(DATA_SOURCE == null){
                    //初始化操作
                    SQLiteConfig config = new SQLiteConfig();
                    config.setDateStringFormat(Util.DATE_PATTERN);
                    DATA_SOURCE = new SQLiteDataSource(config);
                    ((SQLiteDataSource)DATA_SOURCE).setUrl(getURL());
                }
            }
        }
        return DATA_SOURCE;
    }

    /**
     * 获取URL
     * @return
     */
    private static String getURL(){
        //获取target编译文件夹的路径
        //默认的根路径为编译文件夹路径
        URL classesURL = DBUtil.class.getClassLoader().getResource("./");
        //获取target/classes文件夹的父目录路径
        String dir = new File(classesURL.getPath()).getParent();
        String url = "jdbc:sqlite://"+dir+File.separator+"everything-like.db";
        System.out.println(url);
        return url;
    }

    /**
     *获取数据库连接
     * @return
     */
    public static Connection getConnection() throws SQLException {
         return getDataSource().getConnection();
    }

    public static void main(String[] args) throws SQLException {
        getConnection();
    }

    public static void close(Connection connection, Statement statement) {
        close(connection,statement,null);
    }
    public static void close(Connection connection, Statement statement, ResultSet set) {
        try {
            if(connection != null) {
                statement.close();
            }
            if(connection != null) {
                connection.close();
            }
            if(set != null)
                set.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("释放数据库资源错误");
        }
    }
}
