package javachessgui2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Javachessgui2 extends Application
{
    
    final static WebView browser = new WebView();
    final static WebEngine webEngine = browser.getEngine();
    static ScrollPane scrollPane = new ScrollPane();
    
    public static TextArea message_text=new TextArea();
    
    final static String scroll_pane_style=
            "-fx-border-width: 10px;"
            + "-fx-border-radius: 10px;"
            + "-fx-border-style: solid;"
            + "-fx-border-color: #afafff;";
    
    public static int timer;
    public static void system_message(String what,int set_timer)
    {
        
        timer=set_timer;
        
        scrollPane.setMinHeight(Gui.board_size);
        scrollPane.setMaxHeight(Gui.board_size);
        scrollPane.setMinWidth(Gui.board_size);
        scrollPane.setMaxWidth(Gui.board_size);
        
        scrollPane.setFitToWidth(true);
        
        scrollPane.setContent(browser);
        
        what=what.replaceAll("\n", "<br>");
        
        webEngine.loadContent(
                "<body style=\"background-color: #efefff;font-size: 20px;font-family: monospace;\">"+
                what+
                "</body>"
        );
        
        scrollPane.setStyle("-fx-opacity: 1;"+scroll_pane_style);
        scrollPane.toFront();
                
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
                        
                        scrollPane.setStyle("-fx-opacity: 0;"+scroll_pane_style);
                        scrollPane.toBack();

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
        
        Gui.root.getChildren().add(scrollPane);
        
        system_message("<center>\n<b>Welcome!</b></center>",3000);
        
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
