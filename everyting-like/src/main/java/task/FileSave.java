package task;

import app.FileMeta;
import sun.applet.AppletResourceLoader;
import util.DBUtil;
import util.PinyinUtil;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileSave implements ScanCallback {
//    private int ;

    @Override
    public void callback(File dir) {
        //文件夹下一级子文件和子文件夹保存到目录
        File[] children = dir.listFiles();
        List<FileMeta> locals = new ArrayList<>();
        if(children != null){
            for(File child:children){
                locals.add(new FileMeta(child));
            }
        }
        //获取数据库保存的dir目录的下一级子文件
        List<FileMeta> metas = query(dir);
        //本地没有，数据库有，做删除
        for(FileMeta meta:metas){
            if(!locals.contains(meta)){
                //进行删除:meta表示文件夹，文件夹下所有东西都得删除
                //TODO
                delete(meta);
            }
        }
        //本地有，数据库没有进行插入
        for(FileMeta meta:locals){
            if(!metas.contains(meta)){
                save(meta);
            }
        }

    }

    private void delete(FileMeta meta) {
        Connection connection = null;
        PreparedStatement ps = null;
        try{
            connection = DBUtil.getConnection();
            String sql = "delete from file_meta where (name=? and path=? and is_directory=?)";//删除文件自身
            if(meta.getDirectory()){
                sql+=" or path=? or " +//删除子文件
                        "path like ?";//删除孙后辈
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1,meta.getName());
            ps.setString(2,meta.getPath());
            ps.setBoolean(3,meta.getDirectory());
            if(meta.getDirectory()){
                ps.setString(4,meta.getPath()+File.separator+meta.getName());
                ps.setString(5,meta.getPath()+File.separator+meta.getName()+File.separator);
            }
            ps.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("删除失败"+e);
        }finally {
            DBUtil.close(connection,ps);
        }
    }

    private List<FileMeta> query(File dir){
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FileMeta> list = new ArrayList<>();
        try {
            connection = DBUtil.getConnection();
            String sql = "select name, path,is_directory, size, last_modified" +
                    " from file_meta where path = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1,dir.getPath());
            rs = ps.executeQuery();
            while (rs.next()){
                String name = rs.getString("name");
                String path = rs.getString("path");
                Boolean is_directory = rs.getBoolean("is_directory");
                Long size = rs.getLong("size");
                Timestamp last_modified = rs.getTimestamp("last_modified");
                System.out.println("查询文件信息："+name);
                FileMeta fileMeta = new FileMeta(name,path,is_directory,size,new java.util.Date(last_modified.getTime()));
                list.add(fileMeta);
            }
            return list;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("查询数据库失败",e);
        }finally {
            DBUtil.close(connection,ps,rs);
        }
    }
    public void save(FileMeta meta){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DBUtil.getConnection();
            String sql = "insert into file_meta (name,path,is_directory,size,last_modified,pinyin,pinyin_first) values (?,?,?,?,?,?,?)";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,meta.getName());
            preparedStatement.setString(2,meta.getPath());
            preparedStatement.setBoolean(3,meta.getDirectory());
            preparedStatement.setLong(4,meta.getSize());
            preparedStatement.setString(5,meta.getLastModifiedText());
//            String pinyin = null;
//            String pinyin_first = null;
//            if(PinyinUtil.containsChinese(file.getName())){
//                String[] pinyins = PinyinUtil.get(file.getName());
//                pinyin = pinyins[0];
//                pinyin_first = pinyins[1];
//            }
            preparedStatement.setString(6,meta.getPinyin());
            preparedStatement.setString(7,meta.getPinyinFirst());
            System.out.println("执行文件保存操作"+sql);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("文件保存失败",throwables);
        }finally {
            DBUtil.close(connection,preparedStatement);
        }
    }

    public static void main(String[] args) {
        DBInit.init();
        File file = new File("D:\\比特课件\\pdf\\3.JavaWeb");
        FileSave fileSave = new FileSave();
        fileSave.query(file.getParentFile());
    }
}
