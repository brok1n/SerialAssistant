package com.brok1n.java.fx.serialassistant.utils;

import java.io.File;
import java.io.InputStream;

/**
 * Created by brok1n on 2017/2/16.
 */
public class SystemConfig {

    //当前系统信息
    private String osName;
    private String osVersion;
    private String osArch;

    //java环境变量位置
    private String javaHome;

    //临时目录
    private String tmpDir;

    public static final String LIBRARY_FILE_BASE_PATH = "/file/";

    //串口库Serial文件路径
    private String serialFileName = "rxtxSerial";
    private String serialFilePath = "";
    private String serialFileTargetPath;
    //串口Parallel文件路径
    private String parallelFileName = "rxtxParallel";
    private String parallelFilePath = "";
    private String parallelFileTargetPath;

    //单利
    private static SystemConfig info = new SystemConfig();

    public static void init()
    {
        info.setOsName( System.getProperty("os.name").toLowerCase() );
        info.setOsVersion( System.getProperty("os.version").toLowerCase() );
        info.setOsArch( System.getProperty("os.arch").toLowerCase() );

        info.javaHome = System.getenv("JAVA_HOME");
        info.tmpDir = System.getProperty("java.io.tmpdir");

        //获取系统的java.library.path的所有路径
        String libpath = System.getProperty("java.library.path");

        String[] lp = null;
        //验证并根据系统路径分隔符分割出每个可用的路径
        if( libpath != null )
        {
            lp = libpath.split(System.getProperty("path.separator"));
        }

        //如果存在多个可用的java.library.path路径
        //就遍历所有的路径
        if( lp != null && lp.length > 0 )
        {
            for (String path : lp) {

                //去除 .  .. 这两种路径
                if (path.length() < 3)
                {
                    continue;
                }

                File file = new File(path);
                //如果这个路径不存在，就创建这个路径
                //同时捕获目录创建可能出现的各种异常
                if (!file.exists())
                {
                    try {
                        file.mkdirs();
                    }catch (Exception e){}
                }

                if( file.canRead() && file.canWrite() )
                {
                    info.tmpDir = path;
                    break;
                }
            }
        }

        //要拷贝的库文件路径
        String tmpPath = LIBRARY_FILE_BASE_PATH;

        //判断系统类型
        if( getOsName().indexOf("win") != -1 && getOsName().indexOf("linux") == -1 && getOsName().indexOf("mac") == -1 ) {
            //win
            tmpPath += "win/";
            info.serialFileName += ".dll";
            info.parallelFileName += ".dll";
        }
        else if( getOsName().indexOf("mac") != -1 )
        {
            //mac
            tmpPath += "mac/";
            info.serialFileName = "lib" + info.serialFileName + ".jnilib";
            info.parallelFileName = "lib" + info.parallelFileName + ".jnilib";
        }
        else
        {
            //linux
            tmpPath += "linux/";
            info.serialFileName = "lib" + info.serialFileName + ".so";
            info.parallelFileName = "lib" + info.parallelFileName + ".so";
        }

        //判断是否是64位
        if( getOsArch().indexOf("64") >= 0 )
        {
            tmpPath += "64/";
        }
        else
        {
            tmpPath += "32/";
        }

        //库文件的源文件位置
        info.serialFilePath = tmpPath + info.serialFileName;
        info.parallelFilePath = tmpPath + info.parallelFileName;

        //库文件要复制到的地方
        info.serialFileTargetPath = info.tmpDir + File.separator + info.serialFileName;
        info.parallelFileTargetPath = info.tmpDir + File.separator + info.parallelFileName;

        //有些获取到的目录并不存在，同时可能会存在没有写入权限的情况存在
        //所以需要创建所有的父级目录同时捕获异常
        try {
            new File(info.serialFileTargetPath).getParentFile().mkdirs();
            new File(info.parallelFileTargetPath).getParentFile().mkdirs();
        }catch (Exception e)
        {}

    }


    public static String getOsName() {
        return info.osName;
    }

    private void setOsName(String osName) {
        this.osName = osName;
    }

    public static String getOsVersion() {
        return info.osVersion;
    }

    private void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public static String getOsArch() {
        return info.osArch;
    }

    private void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public static String getJavaHome() {
        return info.javaHome;
    }

    public static String getSerialFilePath() {
        return info.serialFilePath;
    }

    public static String getSerialFileTargetPath() {
        return info.serialFileTargetPath;
    }

    public static String getParallelFilePath() {
        return info.parallelFilePath;
    }

    public static String getParallelFileTargetPath() {
        return info.parallelFileTargetPath;
    }

    public static String getTmpDir() {
        return info.tmpDir;
    }

    public static String getSerialFileName() {
        return info.serialFileName;
    }

    public static String getParallelFileName() {
        return info.parallelFileName;
    }


    @Override
    public String toString() {
        return "SystemConfig{" +
                "osName='" + osName + '\'' +
                ",\nosVersion='" + osVersion + '\'' +
                ",\nosArch='" + osArch + '\'' +
                ",\njavaHome='" + javaHome + '\'' +
                ",\ntmpDir='" + tmpDir + '\'' +
                ",\nserialFileName='" + serialFileName + '\'' +
                ",\nserialFilePath='" + serialFilePath + '\'' +
                ",\nserialFileTargetPath='" + serialFileTargetPath + '\'' +
                ",\nparallelFileName='" + parallelFileName + '\'' +
                ",\nparallelFilePath='" + parallelFilePath + '\'' +
                ",\nparallelFileTargetPath='" + parallelFileTargetPath + '\'' +
                '}';
    }
}
