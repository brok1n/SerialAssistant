package com.brok1n.java.fx.serialassistant.view;

import com.brok1n.java.fx.serialassistant.AppDelegate;
import com.brok1n.java.fx.serialassistant.utils.Butils;
import com.brok1n.java.fx.serialassistant.utils.SystemConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;

/**
 * Created by brok1n on 2017/2/16.
 */
public class LoadingController {

    @FXML
    private Label loadingStatusLabel;

    @FXML
    private Label osNameLabel;

    @FXML
    private Label osVersionLabel;

    @FXML
    private Label osArchLabel;

    public void init()
    {
        new Thread( new ConfigRunnable() ).start();
    }

    class ConfigRunnable implements Runnable
    {
        int index;
        @Override
        public void run() {

            loadingStatusLabel.setText("正在检测当前系统信息...");

            SystemConfig.init();

            osNameLabel.setText(SystemConfig.getOsName());
            osVersionLabel.setText(SystemConfig.getOsVersion());
            osArchLabel.setText(SystemConfig.getOsArch());

            setStatusText("开始复制所需库文件...");

            setStatusText("开始复制:" + SystemConfig.getSerialFilePath() );

            boolean status = Butils.copyInputStreamToFile( getClass().getResourceAsStream( SystemConfig.getSerialFilePath()), new File(SystemConfig.getSerialFileTargetPath()) );
            setStatusText( ( status ? "复制成功!:" : "复制失败:" ) + SystemConfig.getSerialFilePath() );
            if( status )
            {
                setStatusText("初始化成功！请稍后...");
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        AppDelegate.showHome();
                    }
                });
            }
            else
            {
                setStatusText("初始化失败！请使用管理员权限重新运行本程序。");
            }



        }
    }

    /**
     * 修改当前显示的状态信息
     * */
    public void setStatusText( final String msg )
    {
        System.out.println( msg );
        Platform.runLater(new Runnable() {
            @Override public void run() {
                loadingStatusLabel.setText(msg);
            }
        });
    }

}
