package task;

import app.FileMeta;
import util.DBUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FileSearch {
    public static List<FileMeta> search(String dir,String content) {
        List<FileMeta> metas = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DBUtil.getConnection();
            String sql = "select name,path,is_directory,size,last_modified from file_meta where (path=? or path like ?)";
            if(content != null && content.trim().length() != 0 ){
                sql += "and (name like ? or pinyin like ? or pinyin_first like ?)";
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1,dir);
            ps.setString(2,dir+ File.separator+"%");
            if(content != null && content.trim().length() != 0 ){
                ps.setString(3,"%"+content+"%");
                ps.setString(4,"%"+content+"%");
                ps.setString(5,"%"+content+"%");

            }
            rs = ps.executeQuery();
            while (rs.next()){
                String name = rs.getString("name");
                String path = rs.getString("path");
                Boolean is_directory = rs.getBoolean("is_directory");
                Long size = rs.getLong("size");
                Timestamp last_modified = rs.getTimestamp("last_modified");
                FileMeta fileMeta = new FileMeta(name,path,is_directory,size,new java.util.Date(last_modified.getTime()));
                metas.add(fileMeta);
            }
        }catch (Exception e){
            throw new RuntimeException();
        }finally {
            DBUtil.close(connection,ps,rs);
        }
        return metas;
    }
}
