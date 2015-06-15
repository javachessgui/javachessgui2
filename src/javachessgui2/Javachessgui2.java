package javachessgui2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Javachessgui2 extends Application{
    
    
    
    public static TextArea message_text=new TextArea();
    
    final static String message_text_style=
            "-fx-font-size: 18px;"+
            "-fx-border-width: 10px;"
            + "-fx-border-radius: 10px;"
            + "-fx-border-style: solid;"
            + "-fx-control-inner-background: #efefff;"
            + "-fx-border-color: #afafff;";
    
    public static int timer;
    public static void system_message(String what,int set_timer)
    {
        
        timer=set_timer;
        
        message_text.setText(what);
        message_text.setStyle("-fx-opacity: 1;"+message_text_style);
        message_text.toFront();
        
        message_text.setMinHeight(Gui.board_size);
        message_text.setMaxHeight(Gui.board_size);
        message_text.setMinWidth(Gui.board_size);
        message_text.setMaxWidth(Gui.board_size);
                
        Thread system_message_thread=new Thread(new Runnable()
        {

            public void run()
            {

                try
                {
                    Thread.sleep(timer);
                }
                catch(InterruptedException ex)
                {

                }

                Platform.runLater(new Runnable()
                {

                    public void run()
                    {

                        message_text.setStyle("-fx-opacity: 0;"+message_text_style);
                        message_text.toBack();

                    }   

                });

            }   

        });

        system_message_thread.start();
                
    }
    
    
    
    @Override public void start(Stage primaryStage)
    {
        
        primaryStage.setX(0);
        primaryStage.setY(0);
                
        Gui.init(primaryStage);
        
        message_text.setWrapText(true);
        
        message_text.setStyle("-fx-opacity: 0;"+message_text_style);
        
        Gui.root.getChildren().add(message_text);
        
        system_message("Welcome!",3000);
        
        Gui.show();
        
    }
    
    @Override public void stop()
    {
        Gui.shut_down();
    }

    public static void main(String[] args)
    {
        
        System.out.println("application started");
        launch(args);
        System.out.println("application stopped");
        
    }
    
}
