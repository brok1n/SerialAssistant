package com.brok1n.java.fx.serialassistant;

import com.brok1n.java.fx.serialassistant.view.HomeController;
import com.brok1n.java.fx.serialassistant.view.LoadingController;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Created by brok1n on 2017/2/16.
 */
public class AppDelegate extends Application {

    public static Stage static_stage;

    private static Scene m_scene;

    @Override
    public void start(Stage primaryStage) throws Exception {

        static_stage = primaryStage;

        initView();

        show();
    }

    public void initView() throws Exception
    {
        FXMLLoader loader	= new FXMLLoader();
        loader.setLocation( getClass().getResource( "view/loading.fxml" ) );
        Parent root = (Parent) loader.load();

        LoadingController loadingController = (LoadingController)loader.getController();
        loadingController.init( );
        m_scene = new Scene(root, 600, 400);
    }

    public void show()
    {
        //static_stage.initStyle(StageStyle.TRANSPARENT);//隐藏默认标题栏
        static_stage.setScene(m_scene);
        static_stage.setTitle("串口助手 by brok1n V1.0.0");
        static_stage.show();
    }

    public static void showHome()
    {
        try {
            FXMLLoader loader	= new FXMLLoader();
            loader.setLocation( AppDelegate.class.getResource( "view/home.fxml" ) );
            Parent root = (Parent) loader.load();

            HomeController homeController = (HomeController)loader.getController();
            homeController.init( );

            m_scene = new Scene(root, 700, 600);

            static_stage.hide();
            static_stage.setTitle("串口助手 by brok1n V1.0.0");
            static_stage.setScene(m_scene);
            static_stage.show();

        }catch (IOException e)
        {}
    }
}
