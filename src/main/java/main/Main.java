package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Filip Stojakovic
 */
public class Main extends Application
{
    public static final String APP_NAME = "ANOVA & kontrasti";
    private static Stage stage;

    public static void main(String[] args)
    {
        //lunch app
        // GUI je odvratan! You've been warned!
        try
        {
            launch(args);
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        stage = primaryStage;
        stage.setResizable(false);  // za dobrobit naroda
        Parent root = FXMLLoader.load(getClass().getResource("./../inputview.fxml"));
        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static Stage getStage()
    {
        return stage;
    }
}
