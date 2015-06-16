package javachessgui2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Engine {
    
    static FileChooser file_chooser=new FileChooser();
    
    static Stage file_chooser_stage=new Stage();
    
    String uci_engine_path;
    
    
    private Runnable runnable_engine_read_thread;
    private Runnable runnable_engine_write_thread;
    
    private Thread engine_read_thread;
    private Thread engine_write_thread;
    
    private ProcessBuilder uci_engine_process_builder;
    private Process uci_engine_process;
    
    private InputStream engine_in;
    private OutputStream engine_out;
    
    Boolean engine_intro=true;
    
    String uci_puff="";
    
    Boolean engine_running=false;
    
    String pv="";
    String bestmove_algeb="";
    Move bestmove=new Move();
    int depth=0;
    int score_mate=0;
    int score_cp=0;
    String score_verbal="";
    int score_numerical=0;
    
    public void receive_intro(String what)
    {
        System.out.println("engine intro: "+what);
    }
    
    public void update_engine()
    {
        System.out.println("depth: "+depth+" score: "+score_numerical+" pv: "+pv);
    }
    
    public String get_config_path()
    {
        return null;
    }
    
    public void set_config_path(String set_path)
    {
        
    }
    
    public void consume_engine_out(String uci)
    {
        
        if(engine_intro)
        {
            
            uci=uci.replaceAll("^info string ", "");
            
            uci_puff+=uci+"\n";
            
            Platform.runLater(new Runnable()
            {
                
                public void run()
                {
                    
                    //Gui.engine_text_area.setText(uci_puff);
                    receive_intro(uci_puff);
                    
                }
               
            });
            
            return;
            
        }
        
        uci=uci.replaceAll("[\\r\\n]*","");
        uci+=" ";
        
        Pattern get_bestmove = Pattern.compile("(^bestmove )(.*)");
        Matcher bestmove_matcher = get_bestmove.matcher(uci);
        
        if (bestmove_matcher.find( )) {
           engine_running=false;
           return;
        }
        
        Pattern get_pv = Pattern.compile("( pv )(.{4,})");
        Matcher pv_matcher = get_pv.matcher(uci);
        
        if (pv_matcher.find( )) {
           pv=pv_matcher.group(2);
           String[] pv_parts=pv.split(" ");
           
           bestmove_algeb=pv_parts[0];
        }
        
        Pattern get_depth = Pattern.compile("(depth )([^ ]+)");
        Matcher depth_matcher = get_depth.matcher(uci);
        
        if (depth_matcher.find( )) {
           
           depth=Integer.parseInt(depth_matcher.group(2));
        }
        
        Pattern get_score_cp = Pattern.compile("(score cp )([^ ]+)");
        Matcher score_cp_matcher = get_score_cp.matcher(uci);
        
        if (score_cp_matcher.find( )) {
           score_cp=Integer.parseInt(score_cp_matcher.group(2));
           score_verbal="cp "+score_cp;
           score_numerical=score_cp;
        }
        
        Pattern get_score_mate = Pattern.compile("(score mate )([^ ]+)");
        Matcher score_mate_matcher = get_score_mate.matcher(uci);
        
        if (score_mate_matcher.find( )) {
           score_mate=Integer.parseInt(score_mate_matcher.group(2));
           score_verbal="mate "+score_mate;
           score_numerical=
                   score_mate<0?
                   -10000-score_mate
                   :
                   10000-score_mate
                   ;
        }
        
        //Gui.update_engine();
        update_engine();
        
    }
    
    private void issue_command(String command)
    {
        try
        {
                        
            engine_out.write(command.getBytes());
            engine_out.flush();

        }
        catch(IOException ex)
        {

        }
    }
    
    public String fen="";
    public void go()
    {
        
        if(!is_engine_installed())
        {
            return;
        }
        
        if(engine_running)
        {
            
        }
        else
        {
            
            engine_intro=false;
            String set_fen_command="";
            if(!fen.equals(""))
            {
                set_fen_command="position fen "+fen+"\n";
            }
            issue_command(set_fen_command+"go infinite\n");
            engine_running=true;
            
        }
    }
    
    public void stop()
    {
        
        if(engine_running)
        {
            
            issue_command("stop\n");

            while(engine_running)
            {
                
                try
                {
                    
                    Thread.sleep(100);
                    
                }
                catch(InterruptedException ex)
                {
                    
                    Thread.currentThread().interrupt();
                    
                }
            
            }
            
            
        }
        
    }
    
    public Boolean is_engine_installed()
    {
        if(uci_engine_path==null)
        {
            return false;
        }
        if(uci_engine_path.equals(""))
        {
            return false;
        }
        return true;
    }
    
    public void stop_engine_process()
    {
        
        if(is_engine_installed())
        {
            engine_read_thread.interrupt();
    
            uci_engine_process.destroy();
            
            uci_engine_path="";
        }
        
    }
    
    
    public Boolean load_engine(String set_path)
    {
        
        stop_engine_process();
        
        engine_intro=true;
        uci_puff="";
        
        if(set_path==null)
        {
            uci_engine_path="";
            
            File file = file_chooser.showOpenDialog(file_chooser_stage);
            
            if(file!=null)
            {
                uci_engine_path=file.getPath();
            }
        }
        else if(set_path.equals(""))
        {
            uci_engine_path="";
        
            //String config_path=Gui.config.uci_engine_path;
            String config_path=get_config_path();
        
            if(config_path!=null)
            {
                uci_engine_path=config_path;
            }
        }
        else
        {
            uci_engine_path=set_path;
        }
        
        if(!(uci_engine_path.equals("")))
        {

            uci_engine_process_builder=new ProcessBuilder(uci_engine_path);

            try {
                   uci_engine_process=uci_engine_process_builder.start();
                }
            catch(IOException ex)
                {
                    System.out.println("error loading engine");
                    uci_engine_path="";
                    
                    return false;
                }
            
            engine_in=uci_engine_process.getInputStream();
            engine_out=uci_engine_process.getOutputStream();

            runnable_engine_read_thread=new Runnable(){
                public void run()
                {
                    
                    String buffer="";
                    
                    while (!Thread.currentThread().isInterrupted())
                    {
                   
                        try
                        {

                             char chunk=(char)engine_in.read();

                             if(chunk=='\n')
                             {
                                 //consume_engine_out(buffer);
                                 
                                 consume_engine_out(buffer);
                                 
                                 buffer="";
                             }
                             else
                             {
                                 buffer+=chunk;
                             }

                        }
                        catch(IOException ex)
                        {

                            System.out.println("engine read IO exception");

                        }

                    }
                    
                }
                    
            };
            
            engine_read_thread=new Thread(runnable_engine_read_thread);

            engine_read_thread.start();
            
            //Gui.config.uci_engine_path=uci_engine_path;
            set_config_path(uci_engine_path);
            
            System.out.println("engine loaded "+uci_engine_path);
            
            MyFile engine_list=new MyFile("engine_list.txt");
            
            engine_list.add_line(uci_engine_path);
            
            return true;

        }
        
        return false;
        
    }
    
    
    public Engine()
    {
        
    }
    
}
