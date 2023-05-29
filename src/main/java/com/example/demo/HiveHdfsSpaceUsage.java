package com.example.demo;


import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class HiveHdfsSpaceUsage {
    private static String driver = "org.apache.hive.jdbc.HiveDriver";

    //注册数据库的驱动
    static {
        try {
            Class.forName(driver);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("HADOOP_USER_NAME", "root");
        //记录程序执行开始时间
        long startTime = System.currentTimeMillis();
        totalHdfsSizeFromNoShell();
//        totalHdfsSizeFromShellCommand();
        //记录程序执行结束时间
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) / 1000);
//

    }

    private static void totalHdfsSizeFromNoShell() throws Exception {
        String tableName = "go_through";
        String tableLocation = getTableLocation(tableName);
        long tableSize = getHdfsPathSizeTotal(tableLocation);
        System.out.println("Hive table " + tableName + " occupies " + Double.valueOf(tableSize)/(1024*1024) + " MB in HDFS.");
    }

    private static void totalHdfsSizeFromShellCommand() {

        String command = "source /etc/profile&&hadoop fs -du /hudi/bvt/go_through/go_through|awk ' { SUM += $1 } END { print SUM/(1024*1024) }'\n";
        String host = "192.168.80.181";
        String username = "root";
        String password = "222222";

        try {
            // 创建SSH连接
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 创建命令执行器
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // 获取命令输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            // 执行命令
            channel.connect();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 关闭连接
            channel.disconnect();
            session.disconnect();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }

    }

    // ... getTableLocation() method ...
    private static String getTableLocation(String tableName) throws Exception {

        String hiveJdbcUrl = "jdbc:hive2://192.168.80.183:10000/bvt";
        String user = "root";
        String password = "";


        Connection connection = DriverManager.getConnection(hiveJdbcUrl, user, password);
        Statement statement = connection.createStatement();

//        ResultSet resultSet = statement.executeQuery("DESCRIBE FORMATTED " + "bvt.test_2");
        ResultSet resultSet = statement.executeQuery("DESCRIBE FORMATTED bvt.go_through");
        String location = null;
        while (resultSet.next()) {
            String col_name = resultSet.getString("col_name").trim();
            String data_type = resultSet.getString("data_type");
            if ("location:".equalsIgnoreCase(col_name)) {
                location = resultSet.getString("data_type").trim();
                break;
            }
        }

        resultSet.close();
        statement.close();
        connection.close();

        return location;
    }

    private static long getHdfsPathSizeTotal(String path) throws Exception {
        return getHdfsPathSize(path);
    }


    private static long getHdfsPathSize(String path) throws Exception {
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        Path hdfsPath = new Path(path);

        long totalSize = 0;
        if (fileSystem.exists(hdfsPath)) {
            FileStatus[] fileStatuses = fileSystem.listStatus(hdfsPath);
            for (FileStatus fileStatus : fileStatuses) {
                if (fileStatus.isDirectory()) {
                    totalSize += getHdfsPathSize(fileStatus.getPath().toString());
                }
                totalSize += fileStatus.getLen();
            }
        }
        fileSystem.close();
        return totalSize;
    }
}
