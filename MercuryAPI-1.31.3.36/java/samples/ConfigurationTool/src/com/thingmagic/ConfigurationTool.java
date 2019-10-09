
package com.thingmagic;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ConfigurationTool extends Application 
{
    @Override
    public void start(Stage stage) throws Exception 
    {
        Parent root = FXMLLoader.load(this.getClass().getResource("/fxml/Main.fxml"));        
        Scene scene = new Scene(root);
        stage.setTitle("Autonomous Configuration Tool");
        stage.getIcons().add(new Image(("/images/jadak_icon.png")));
        MainController main = new MainController();
        stage.setScene(scene);
        main.setStage(stage);
        stage.setMaximized(true);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                  System.exit(0);
            }
        });
    }

    public static void main(String[] args)
    {        
        launch(args);
    }
}