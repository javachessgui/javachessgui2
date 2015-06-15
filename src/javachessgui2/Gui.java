package javachessgui2;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ColorPicker;
import javafx.event.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javax.swing.JOptionPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.ListCell;
import javafx.util.Callback;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class GameListFormatCell extends ListCell<String> {
    
    public static Color get_color(String item)
    {
        
        if(item.contains("--->"))
        {
             return(Color.RED);
        }
        else if(item.contains("-->"))
        {
             return(Color.BLUE);
        }
        else
        {
             return(Color.GREEN);
        }
        
         
    }

     public GameListFormatCell() {    }
       
     @Override protected void updateItem(String item, boolean empty) {
         // calling super here is very important - don't skip this!
         super.updateItem(item, empty);
         
         setText(item);
         
         if(item==null)
         {
             return;
         }
         
         Color c=get_color(item);
         
         setTextFill(c);
         
         }
     }

class FxUtils
{
    public static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
    }
}

class Config implements java.io.Serializable
{
    int target_board_size=400;
    
    String uci_engine_path=null;
    
    String initial_dir;
    
    public Config()
    {

    }
}

class BoardStyle implements java.io.Serializable
{
    
    public void copy(BoardStyle what)
    {
        
        for(int i=0;i<NUM_COLORS;i++)
        {
            colors[i]=what.colors[i];
        }
        
        for(int i=0;i<NUM_CHECKS;i++)
        {
            checks[i]=what.checks[i];
        }
        
        for(int i=0;i<NUM_SLIDERS;i++)
        {
            sliders[i]=what.sliders[i];
        }
        
    }
    
    public BoardStyle clone()
    {
        BoardStyle clone=new BoardStyle();
        clone.copy(this);
        return clone;
    }
    
    public String font="MERIFONTNEW.TTF";
    
    final static String[] color_names={
        "Board color",
        "White piece color",
        "Black piece color",
        "Light square color",
        "Dark square color",
        "White contour",
        "Black contour"
    };
    
    final static int BOARD_COLOR=0;
    final static int WHITE_PIECE_COLOR=1;
    final static int BLACK_PIECE_COLOR=2;
    final static int LIGHT_SQUARE_COLOR=3;
    final static int DARK_SQUARE_COLOR=4;
    final static int WHITE_CONTOUR_COLOR=5;
    final static int BLACK_CONTOUR_COLOR=6;
    
    final static int NUM_COLORS=7;
    
    public String colors[]={
        "#FFFFFF",
        "#000000",
        "#000000",
        "#AFDFAF",
        "#7FAF7F",
        "#7F7F7F",
        "#7F7F7F"
    };
    
    final static int NUM_SLIDERS=2;
    
    public static int slider_mins[]={0,0};
    public static int slider_maxs[]={10,50};
    public static String slider_labels[]={"Padding","Inner padding"};
    
    public Color colors(int i)
    {
        return Color.web(colors[i]);
    }
    
    final static String[] check_names={
        "Font only",
        "Do fill",
        "Do stroke"
    };
    
    final static int FONT_ONLY=0;
    final static int DO_FILL=1;
    final static int DO_STROKE=2;
    
    final static int NUM_CHECKS=3;
    
    public Boolean[] checks={false,true,true};
    
    final static int PADDING_PERCENT=0;
    final static int INNER_PADDING_PERCENT=1;
    
    public int[] sliders={10,10};
    
    public BoardStyle()
    {
        
    }
}

class MyButton extends Button {
    
    public String text;
    
    public static EventHandler<ActionEvent> button_pressed_handler;
    
    public MyButton(String set_text)
    {
        text=set_text;
        
        super.setText(set_text);
        
        super.setOnAction(button_pressed_handler);
    }
    
    public MyButton(String set_text,ImageView image)
    {
        
        super(set_text,image);
        
        text=set_text;
        
        super.setOnAction(button_pressed_handler);
    }
    
}

public class Gui {
    
    static int legal_move_list_width=60;
    static int game_move_list_width=100;
    static int hbox_padding=2;
    static int move_lists_width=legal_move_list_width+game_move_list_width+hbox_padding;
    static int engine_text_area_width=200;
    static int bottom_bar_height=120;
    
    static TextArea engine_text_area=new TextArea();
    
    static TextField save_pgn_as_text=new TextField();
    
    static FileChooser file_chooser=new FileChooser();
    
    static Stage file_chooser_stage=new Stage();
    
    static Engine engine=new Engine();
    
    static int margin_percent=10;
    
    static ListView<String> legal_move_list = new ListView<String>();
    static ListView<String> game_list = new ListView<String>();
    
    private static TextField fen_text;
    
    private static Clipboard clip=Clipboard.getSystemClipboard();
    
    static Stage gui_stage;
    
    /////////////////////////////////////////////
    
    static StackPane root = new StackPane();
        
    static Scene scene = new Scene(root);
    
    /////////////////////////////////////////////
    
    static FlowPane board_controls_box=new FlowPane();
    static VBox board_pane_vertical_box=new VBox(2);
    
    static Group board_canvas_group;
    
    /////////////////////////////////////////////
    
    static Canvas board_canvas;
    static Canvas engine_canvas;
    static Canvas drag_canvas;
    
    static MyButton flip_button;
    static MyButton style_button;
    static MyButton reset_button;
    static MyButton clip_to_fen_button;
    static MyButton fen_to_clip_button;
    static MyButton clip_to_pgn_button;
    static MyButton pgn_to_clip_button;
    static MyButton back_button;
    static MyButton forward_button;
    static MyButton del_button;
    static MyButton begin_button;
    static MyButton end_button;
    static MyButton load_engine_button;
    static MyButton go_button;
    static MyButton make_button;
    static MyButton stop_button;
    static MyButton open_pgn_button;
    static MyButton save_pgn_button;
    static MyButton save_pgn_as_button;
    
    /////////////////////////////////////////////
    
    static Boolean flip=false;
    
    static int piece_size;
    
    static int full_piece_size;
    
    static Font chess_font;
    
    static InputStream font_stream;
    
    static GraphicsContext board_canvas_gc;
    static GraphicsContext engine_canvas_gc;
    static GraphicsContext drag_canvas_gc;
    
    static int board_margin;
    static int board_padding;
    static int board_inner_padding;
    static int board_square_size;
    static int half_board_square_size;
    
    static int board_size;
    
    static BoardStyle current_style=null;
    
    /////////////////////////////////////////////
    
    static Hashtable translit_light=new Hashtable();
    static Hashtable translit_dark=new Hashtable();
    
    /////////////////////////////////////////////
        
    static Game game=new Game();
    
    /////////////////////////////////////////////
    
    public static void show()
    {
        gui_stage.show();
    }
    
    /////////////////////////////////////////////
    
    public static void shut_down()
    {
        engine.stop_engine_process();
        System.out.println("gui shut down");
    }
    
    /////////////////////////////////////////////
    
    static void flip()
    {
        
        flip=!flip;
        
        restart_engine=false;
        
        check_engine_after_making_move();
        
    }
    
    static ColorPicker[] color_pickers=new ColorPicker[BoardStyle.NUM_COLORS];
    static CheckBox[] checks=new CheckBox[BoardStyle.NUM_CHECKS];
    static Slider[] sliders=new Slider[BoardStyle.NUM_SLIDERS];
    static TextField save_as_text;
    static Button save_as_button;
    static Group create_setup_board_group()
    {
        Group setup_board_group=new Group();
        
        GridPane setup_board_grid=new GridPane();
        
        setup_board_grid.setPadding(new Insets(20,20,20,20));
        
        int grid_cnt=0;
        
        String[] list=MyDirectory.list("boardstyles");
        
        if(list!=null)
        {
            ComboBox load_style_combo = new ComboBox();
            
            load_style_combo.setOnAction((Event ev) -> {
            String path=load_style_combo.getSelectionModel().getSelectedItem().toString();    
            read_in_style("boardstyles"+File.separator+path+".ser");
            resize_target_board_size(config.target_board_size);
            save_as_text.setText(path);
            });
            
            for(int i=0;i<list.length;i++)
            {
                Pattern get_name = Pattern.compile("([^\\"+File.separator+"\\.]+)\\.ser$");
                Matcher get_name_matcher = get_name.matcher(list[i]);

                if (get_name_matcher.find( )) {
                   list[i]=get_name_matcher.group(1);
                }
            }
            
            ObservableList<String> items =FXCollections.observableArrayList(
            list
            );
            
            load_style_combo.setItems(items);
            
            Label load_style_label=new Label("Load style");
        
            setup_board_grid.setConstraints(load_style_label,0,grid_cnt);
            setup_board_grid.setConstraints(load_style_combo,1,grid_cnt);

            setup_board_grid.getChildren().addAll(load_style_label,load_style_combo);
            
            grid_cnt++;

        
        }
        
        for(int i=0;i<BoardStyle.NUM_CHECKS;i++)
        {
            
            Label check_label=new Label(BoardStyle.check_names[i]);
            
            CheckBox check=new CheckBox();
            
            checks[i]=check;
        
            check.setSelected(current_style.checks[i]);
        
            check.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                    
                    CheckBox chk = (CheckBox) event.getSource();
                    for(int i=0;i<BoardStyle.NUM_CHECKS;i++)
                    {
                        if(chk==checks[i])
                        {
                            current_style.checks[i]=chk.isSelected();
                            
                            if(i==BoardStyle.FONT_ONLY)
                            {
                                current_style.colors[BoardStyle.DARK_SQUARE_COLOR]="#FFFFFF";
                                current_style.colors[BoardStyle.LIGHT_SQUARE_COLOR]="#FFFFFF";
                                current_style.colors[BoardStyle.WHITE_PIECE_COLOR]="#000000";
                                current_style.colors[BoardStyle.BLACK_PIECE_COLOR]="#000000";

                                color_pickers[BoardStyle.DARK_SQUARE_COLOR].setValue(Color.web("#FFFFFF"));
                                color_pickers[BoardStyle.LIGHT_SQUARE_COLOR].setValue(Color.web("#FFFFFF"));
                                color_pickers[BoardStyle.WHITE_PIECE_COLOR].setValue(Color.web("#000000"));
                                color_pickers[BoardStyle.BLACK_PIECE_COLOR].setValue(Color.web("#000000"));

                                current_style.checks[BoardStyle.DO_STROKE]=false;
                                checks[BoardStyle.DO_STROKE].setSelected(false);
                                current_style.checks[BoardStyle.DO_FILL]=true;
                                checks[BoardStyle.DO_FILL].setSelected(true);
                            }
                            draw_board();
                            break;
                        }
                    }
                }
                
                });
        
            setup_board_grid.setConstraints(check_label,0,grid_cnt);
            setup_board_grid.setConstraints(check,1,grid_cnt);
            
            grid_cnt++;
        
            setup_board_grid.getChildren().addAll(check_label,check);
            
        }
        
        for(int i=0;i<BoardStyle.NUM_COLORS;i++)
        {
            
            Label color_label=new Label(BoardStyle.color_names[i]);
        
            final ColorPicker colorPicker = new ColorPicker();
            colorPicker.setValue(current_style.colors(i));

            color_pickers[i]=colorPicker;

            colorPicker.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    for(int i=0;i<BoardStyle.NUM_COLORS;i++)
                    {
                        if(t.getSource()==color_pickers[i])
                        {
                            current_style.colors[i]=FxUtils.toRGBCode(colorPicker.getValue());
                            draw_board();
                            break;
                        }
                    }
                }
            });

            setup_board_grid.setConstraints(color_label,0,grid_cnt);
            setup_board_grid.setConstraints(colorPicker,1,grid_cnt);
            
            grid_cnt++;
            
            setup_board_grid.getChildren().addAll(color_label,colorPicker);
            
        }
        
        ComboBox comboBox = new ComboBox();
        
        comboBox.getItems().addAll("MERIFONTNEW.TTF","AVENFONT.TTF","LUCEFONT.TTF");
        //comboBox.setValue("MERIFONTNEW.TTF");
        
        comboBox.setOnAction((Event ev) -> {
            current_style.font=comboBox.getSelectionModel().getSelectedItem().toString();    
            resize_target_board_size(config.target_board_size);
            
        });
        
        Label font_label=new Label("Font");
        
        setup_board_grid.setConstraints(font_label,0,grid_cnt);
        setup_board_grid.setConstraints(comboBox,1,grid_cnt);
        
        setup_board_grid.getChildren().addAll(font_label,comboBox);
        
        grid_cnt++;
        
        for(int i=0;i<BoardStyle.NUM_SLIDERS;i++)
        {
            sliders[i]=new Slider();
            Label slider_label=new Label(BoardStyle.slider_labels[i]);
            
            setup_board_grid.getChildren().addAll(slider_label,sliders[i]);
            
            setup_board_grid.setConstraints(slider_label,0,grid_cnt);
            setup_board_grid.setConstraints(sliders[i],1,grid_cnt);
            
            sliders[i].setMin(BoardStyle.slider_mins[i]);
            sliders[i].setMax(BoardStyle.slider_maxs[i]);
            
            sliders[i].setShowTickLabels(true);
            sliders[i].setShowTickMarks(true);
            
            sliders[i].setMajorTickUnit(10);
            sliders[i].setMinorTickCount(5);
            sliders[i].setBlockIncrement(10);
            
            sliders[i].setValue(current_style.sliders[i]);
            
            grid_cnt++;
            
        }
        
        sliders[0].valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    current_style.sliders[0]=new_val.intValue();
                    resize_target_board_size(config.target_board_size);
            }
        });
        
        sliders[1].valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    current_style.sliders[1]=new_val.intValue();
                    resize_target_board_size(config.target_board_size);
            }
        });
        
        save_as_button=new Button("Save style as:");
        
        save_as_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                    String path="boardstyles"+File.separator+save_as_text.getText()+".ser";
                    MySerialize.write(path, current_style);
                }
        });
        
        save_as_text=new TextField();
        
        setup_board_grid.setConstraints(save_as_button,0,grid_cnt);
        setup_board_grid.setConstraints(save_as_text,1,grid_cnt);
        
        setup_board_grid.getChildren().addAll(save_as_button,save_as_text);
        
        grid_cnt++;
        
        Button ok_button=new Button("Ok");
        
        ok_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                    String path="board_setup.ser";
                    MySerialize.write(path, current_style);
                    setup_board_modal.close();
                }
        });
        
        Button cancel_button=new Button("Cancel");
        
        cancel_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                    setup_board_modal.close();
                    current_style=old_style;
                    resize_target_board_size(config.target_board_size);
                }
        });
        
        setup_board_grid.setConstraints(ok_button,0,grid_cnt);
        setup_board_grid.setConstraints(cancel_button,1,grid_cnt);
        
        setup_board_grid.getChildren().addAll(ok_button,cancel_button);
        
        grid_cnt++;
        
        setup_board_grid.setHgap(10);
        setup_board_grid.setVgap(5);
        
        setup_board_group.getChildren().add(setup_board_grid);
        
        return setup_board_group;
    }
    
    static void stop_engine()
    {
        engine.stop();
        restart_engine=false;
    }
    
    
    static void reset()
    {
        
        stop_engine();
        
        game.reset();
        
        check_engine_after_making_move();
    }
    
    static void clip_to_pgn()
    {
        stop_engine();
        
        String pgn=clip.getString();
        game.set_from_pgn_tree(pgn);
        
        check_engine_after_making_move();
    }
    
    static void pgn_to_clip()
    {
        ClipboardContent content = new ClipboardContent();
        content.putString(game.calc_pgn_tree());
        clip.setContent(content);
    }
    
    static void clip_to_fen()
    {
        
        stop_engine();
        
        String fen=clip.getString();
        game.set_from_fen(fen);
        
        check_engine_after_making_move();
    }
    
    static void fen_to_clip()
    {
        ClipboardContent content = new ClipboardContent();
        content.putString(game.board.report_fen());
        clip.setContent(content);
    }
    
    static MyModal setup_board_modal;
    static BoardStyle old_style;
    static void style()
    {
        
        setup_board_modal=new MyModal(create_setup_board_group(),"Setup Board");
        
        old_style=current_style.clone();
        
        setup_board_modal.show_and_wait();
        
        
    }
    
    /////////////////////////////////////////////
    
    static void update_pgn()
    {
        if(game.flip_set)
        {
            flip=game.flip;
        }
    }
    
    static String last_open_pgn_path="";
    static void open_pgn()
    {
        
        stop_engine();
        
        if(config.initial_dir!=null)
        {
            File dir=new File(config.initial_dir);

            file_chooser.setInitialDirectory(dir);
        }

        File file = file_chooser.showOpenDialog(file_chooser_stage);

        if(file==null){return;}

        String path=file.getPath();

        save_pgn_as_text.setText(path);

        config.initial_dir=path.substring(0,path.lastIndexOf(File.separator));

        MySerialize.write("config.ser", config);

        MyFile my_file=new MyFile(path);

        game.pgn_lines=my_file.read_lines();

        game.set_from_pgn_lines_tree();
        
        last_open_pgn_path=path;
        
        update_pgn();
        
        check_engine_after_making_move();
    }
    
    static int color_r;
    static int color_g;
    static int color_b;
    public static void update_engine()
    {
        
        if(!engine.engine_running)
        {
            return;
        }
    
        
        if((engine.bestmove_algeb!="")&&(engine.bestmove_algeb!=null))
        {
            
            color_r=0;
            color_g=0;
            
            int color_limit=500;
            int draw_limit=80;
            int min_color=127;
            
            color_b=min_color;
            
            if(engine.score_numerical<-color_limit)
            {
                color_r=255;
                color_b=0;
            }
            else if(engine.score_numerical<-draw_limit)
            {
                color_r=-engine.score_numerical/color_limit*255;
                if(color_r<min_color){color_r=min_color;}
                color_b=0;
            }
            
            if(engine.score_numerical>color_limit)
            {
                color_g=255;
                color_b=0;
            }
            else if(engine.score_numerical>draw_limit)
            {
                color_g=engine.score_numerical/color_limit*255;
                if(color_g<min_color){color_g=min_color;}
                color_b=0;
            }
            
            Color score_color=Color.rgb(color_r,color_g,color_b);
            
            Move bestmove=new Move();
            bestmove.from_algeb(engine.bestmove_algeb);
            
            int from_x=index_to_px(bestmove.from.i);
            int from_y=index_to_px(bestmove.from.j);
            int to_x=index_to_px(bestmove.to.i);
            int to_y=index_to_px(bestmove.to.j);
            
            Platform.runLater(new Runnable()
            {
                
                public void run()
                {
                     
                     engine_text_area.setText(
                             "depth "+engine.depth+
                             "\npv "+engine.pv+
                             "\nscore "+engine.score_verbal+
                             "\nscore numerical "+engine.score_numerical+
                             "\nbestmove "+engine.bestmove_algeb
                     );

                     engine_text_area.setStyle("-fx-text-fill: rgb("
                         +color_r+","+color_g+","+color_b
                         +");"
                             
                     );

                     engine_canvas_gc.clearRect(0,0,board_size,board_size);

                     if(engine.engine_running)
                     {
                         
                         engine_canvas_gc.setStroke(score_color);
                         engine_canvas_gc.setLineWidth(10);
                         engine_canvas_gc.strokeRect(0, 0, board_size, board_size);

                         engine_canvas_gc.setFont(new Font("Time New Roman Bold",80));
                         engine_canvas_gc.setLineWidth(4);
                         engine_canvas_gc.setStroke(Color.rgb(180,180,0));
                         engine_canvas_gc.strokeText((engine.score_numerical>0?"+":"")+engine.score_numerical,board_size/2-40,board_size/2+40);

                     }

                     engine_canvas_gc.setStroke(score_color);
                     engine_canvas_gc.setLineWidth(3);
                     engine_canvas_gc.strokeLine(from_x, from_y, to_x, to_y);
                     engine_canvas_gc.setFill(score_color);

                     engine_canvas_gc.fillOval(to_x-5, to_y-5, 10, 10);
                     
                }
                
            });
            
        }
        else
        {
            
            Platform.runLater(new Runnable()
            {
                
                public void run()
                {

                    engine_canvas_gc.clearRect(0,0,board_size,board_size);
                    engine_text_area.setText("");

                }
               
            });
            
        }
    }
    
    static Boolean restart_engine=false;
    static void check_engine_before_making_move()
    {
        if(engine.engine_running)
        {
            engine.stop();
            restart_engine=true;
        }
    }
    
    static void check_engine_after_making_move()
    {
        
        update_game();
        
        if(restart_engine)
        {
            restart_engine=false;
            go();
        }
        else
        {
            
            
            Platform.runLater(new Runnable()
            {
                
                public void run()
                {
                    
                    engine_canvas_gc.clearRect(0,0,board_size,board_size);
                    
                }
               
            });
            
            
        }
        
    }
    
    static void make()
    {
        
        Move m=new Move();
        
        m.from_algeb(engine.bestmove_algeb);
        
        if(game.board.list_legal_moves(m))
        {
            make_move(m);
        }
        
    }
    
    static void go()
    {
        engine.fen=game.board.report_fen();
        engine.go();
    }
    
    static void stop()
    {
        stop_engine();
    }
    
    static void forward()
    {
        
        check_engine_before_making_move();
        
        game.forward();
        
        check_engine_after_making_move();
    }
    
    static void begin()
    {
        
        check_engine_before_making_move();
        
        game.jump_to(0);
        game_list.getSelectionModel().select(0);
        
        check_engine_after_making_move();
    }
    
    static void save_pgn()
    {
        if(last_open_pgn_path.equals(""))
        {
            save_pgn_as();
        }
        else
        {
            MyFile my_file=new MyFile(last_open_pgn_path);

            game.set_flip=true;
            game.flip=flip;
            game.calc_pgn_tree();

            my_file.content=game.pgn;

            my_file.write_content();

            Javachessgui2.system_message(
                    "Saved to file: "+last_open_pgn_path+
                    "\n\nContent:\n\n"+my_file.content,3000);
        }
    }
    
    static void highlight_name_in_path()
    {

        String path=save_pgn_as_text.getText();
        if(path.length()<5)
        {
            return;
        }

        Pattern get_name = Pattern.compile("([^\\"+File.separator+"]+\\.pgn$)");
        Matcher name_matcher = get_name.matcher(path);

        if(name_matcher.find())
        {
            int index=path.indexOf(name_matcher.group(0));
            save_pgn_as_text.requestFocus();
            save_pgn_as_text.positionCaret(index);
            save_pgn_as_text.selectRange(index,path.length()-4);
        }

    }
    
    static MyModal save_pgn_as_modal;
    static void save_pgn_as()
    {
        save_pgn_as_modal=new MyModal(create_save_pgn_as_group(),"Save PGN as");
        save_pgn_as_modal.setxywh(50, 50, 500, 160);
        
        highlight_name_in_path();
        save_pgn_as_modal.show_and_wait();
        
    }
    
    static Group create_save_pgn_as_group()
    {
        Group save_pgn_as_group=new Group();
        
        save_pgn_as_text.setMinWidth(400);
        save_pgn_as_text.setTranslateX(10);
        save_pgn_as_text.setTranslateY(10);
        save_pgn_as_text.setStyle("-fx-font-size: 24px;-fx-font-family: monospace;-fx-font-weight: bold;");
        
        Button save_pgn_as_ok_button=new Button("Ok");
        save_pgn_as_ok_button.setMinHeight(30);
        save_pgn_as_ok_button.setMinWidth(100);
        save_pgn_as_ok_button.setTranslateX(350);
        save_pgn_as_ok_button.setTranslateY(30);
        
        save_pgn_as_ok_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                    
                    String path=save_pgn_as_text.getText();
                    
                    if(path.length()>0)
                    {
                     
                        MyFile my_file=new MyFile(path);

                        game.set_flip=true;
                        game.flip=flip;
                        game.calc_pgn_tree();

                        my_file.content=game.pgn;
                        
                        my_file.write_content();
                        
                        Javachessgui2.system_message(
                                "Saved to file: "+path+
                                "\n\nContent:\n\n"+my_file.content,3000);
                        
                    }
                    
                    save_pgn_as_modal.close();
                }
        });
        
        VBox save_pgn_as_vbox=new VBox();
        
        save_pgn_as_vbox.getChildren().add(save_pgn_as_text);
        save_pgn_as_vbox.getChildren().add(save_pgn_as_ok_button);
        
        save_pgn_as_group.getChildren().add(save_pgn_as_vbox);
        
        return save_pgn_as_group;
    }
    
    static void end()
    {
        
        check_engine_before_making_move();
        
        game.jump_to(game.calc_ptr-1);
        game_list.getSelectionModel().select(game.calc_ptr-1);
        
        check_engine_after_making_move();
    }
    
    static void back()
    {
        
        check_engine_before_making_move();
        
        game.back();
        
        check_engine_after_making_move();
    }
    
    static void del()
    {
        
        check_engine_before_making_move();
        
        game.del();
        
        check_engine_after_making_move();
    }
    
    static void load_engine()
    {
        engine.load_engine(null);
        
        MySerialize.write("config.ser", config);
    }
    
    public static void handle_button_pressed(ActionEvent e)
    {
        MyButton source=(MyButton)e.getSource();
        
        if(source==flip_button){flip();}
        if(source==style_button){style();}
        if(source==reset_button){reset();}
        if(source==clip_to_fen_button){clip_to_fen();}
        if(source==fen_to_clip_button){fen_to_clip();}
        if(source==clip_to_pgn_button){clip_to_pgn();}
        if(source==pgn_to_clip_button){pgn_to_clip();}
        if(source==forward_button){forward();}
        if(source==back_button){back();}
        if(source==del_button){del();}
        if(source==begin_button){begin();}
        if(source==end_button){end();}
        if(source==load_engine_button){load_engine();}
        if(source==go_button){go();}
        if(source==make_button){make();}
        if(source==stop_button){stop();}
        if(source==open_pgn_button){open_pgn();}
        if(source==save_pgn_button){save_pgn();}
        if(source==save_pgn_as_button){save_pgn_as();}
        
    }
    
    static String[] sorted_sans;
    static int num_sans;
    static void update_game_working()
    {
        game.board.list_legal_moves(null);

        num_sans=game.board.legal_sans_cnt;
        
        sorted_sans=Arrays.copyOfRange(game.board.legal_sans, 0, num_sans);

        Arrays.sort(sorted_sans);
        
        ObservableList<String> items =FXCollections.observableArrayList(
            sorted_sans
        );

        legal_move_list.setItems(items);
        
        ArrayList<String> game_array_list=new ArrayList<String>();
        
        String start_fen=game.nodes.fen;
        
        Board dummy=new Board();
        dummy.set_from_fen(start_fen);
        
        int fullmove_number=dummy.fullmove_number;
        int turn=dummy.turn;
        
        for(int i=0;i<game.calc_ptr;i++)
        {
            String san=game.game[i].gen_san;
            String number="";
            if(i>0)
            {
                number=turn==Board.TURN_WHITE?""+fullmove_number+".":"    ...";
                if(game.game[i].mainline)
                {
                    turn=-turn;
                    if(turn==Board.TURN_WHITE)
                    {
                        fullmove_number++;
                    }
                }
                else
                {
                    number="  -->  ";
                }
            }
            game_array_list.add(san.equals("")?"*":number+" "+san);
            
        }
        
        String[] game_array=new String[game_array_list.size()];
        game_array=game_array_list.toArray(game_array);
        
        items =FXCollections.observableArrayList(
            game_array
        );
        
        game_list.setItems(items);
        
        game_list.getSelectionModel().select(game.move_ptr);
        
        draw_board();
        
    }
    
    
    static void update_game()
    {
        game.board.list_legal_moves(null);

        num_sans=game.board.legal_sans_cnt;
        
        sorted_sans=Arrays.copyOfRange(game.board.legal_sans, 0, num_sans);

        Arrays.sort(sorted_sans);
        
        ObservableList<String> items =FXCollections.observableArrayList(
            sorted_sans
        );

        legal_move_list.setItems(items);
        
        ArrayList<String> game_array_list=new ArrayList<String>();
        
        String start_fen=game.nodes.fen;
        
        Board dummy=new Board();
        dummy.set_from_fen(start_fen);
        
        int fullmove_number=dummy.fullmove_number;
        int turn=dummy.turn;
        
        for(int i=0;i<game.calc_ptr;i++)
        {
            String san=game.game[i].gen_san;
            String number="";
            String branching="";
            if(i>0)
            {
                number=turn==Board.TURN_WHITE?""+fullmove_number+".":"    ...";
                if(game.game[i].mainline)
                {
                    turn=-turn;
                    if(turn==Board.TURN_WHITE)
                    {
                        fullmove_number++;
                    }
                }
                else
                {
                    number=game.game[i].primary?"  -->  ":"  --->  ";
                }
                if(game.game[i-1].num_childs>1)
                {
                    branching=" *";
                }
            }
            game_array_list.add(san.equals("")?"*":number+" "+san+branching);
            
        }
        
        String[] game_array=new String[game_array_list.size()];
        game_array=game_array_list.toArray(game_array);
        
        items =FXCollections.observableArrayList(
            game_array
        );
        
        game_list.setItems(items);
        
        game_list.getSelectionModel().select(game.move_ptr);
        game_list.scrollTo(game.move_ptr);
        
        draw_board();
     
    }
    
    public static void make_move(Move m)
    {
        
        check_engine_before_making_move();
        
        game.make_move(m);
        
        check_engine_after_making_move();
    }
    
    public static void make_san_move(String san)
    {
        
        check_engine_before_making_move();
        
        game.make_san_move(san);
        
        check_engine_after_making_move();
    }
    
    static Boolean is_drag_going=false;
    static Square drag_from;
    static Square drag_to;
    static int drag_dx;
    static int drag_dy;
    static Piece drag_piece;
    static EventHandler<MouseEvent> board_canvas_handler = new EventHandler<MouseEvent>()
    {
 
        @Override public void handle(MouseEvent mouseEvent)
        {
            
            int x=(int)mouseEvent.getX();
            int y=(int)mouseEvent.getY();
            String type=mouseEvent.getEventType().toString();
            //System.out.println(type + " x " + x + " y " + y);
            //System.out.println("i "+px_to_index(x)+" j "+px_to_index(y));
            
            if(type.equals("MOUSE_RELEASED"))
            {
                if(is_drag_going==true)
                {
                    is_drag_going=false;
                    
                    drag_to=new Square(px_to_index(x),px_to_index(y));
                    
                    if(drag_to.valid)
                    {
                        
                        Move m=new Move(drag_from,drag_to,new Piece(' '));
                        Boolean legal=game.board.list_legal_moves(m);
                        
                        if(legal)
                        {
                            make_move(m);
                        }
                        else
                        {
                            m=new Move(drag_from,drag_to,new Piece('q'));
                            legal=game.board.list_legal_moves(m);
                            
                            if(legal)
                            {
                                
                                Object[] options = {"Queen",
                                "Rook",
                                "Bishop",
                                "Knight"};
                            
                                int n=0;
                                n = JOptionPane.showOptionDialog(null,
                                "Select:",
                                "Promote piece",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);
                                
                                switch(n)
                                {
                                    case 0:m.prom_piece=new Piece('q');break;
                                    case 1:m.prom_piece=new Piece('r');break;
                                    case 2:m.prom_piece=new Piece('b');break;
                                    case 3:m.prom_piece=new Piece('n');break;
                                    default:m.prom_piece=new Piece('q');
                                }
                                
                                make_move(m);
                            
                            }
                        }
                        
                        drag_canvas_gc.clearRect(0, 0, board_size, board_size);
                    
                    }
                    else
                    {
                        System.out.println("out of board");
                    }
                    
                    update_game();
                    
                }
            }
            
            if(type.equals("MOUSE_DRAGGED"))
            {
                if(is_drag_going==false)
                {
                    is_drag_going=true;
                    
                    drag_from=new Square(px_to_index(x),px_to_index(y));
                    
                    fill_square(drag_from.i,drag_from.j);
                    if(current_style.checks[BoardStyle.FONT_ONLY])
                    {
                        put_piece_xy(new Piece(' '),i_to_font_x(drag_from.i),j_to_font_y(drag_from.j),board_canvas_gc,drag_from.i,drag_from.j);
                    }
                    
                    drag_dx=i_to_font_x(drag_from.i)-x;
                    drag_dy=j_to_font_y(drag_from.j)-y;
                    
                    drag_piece=new Piece(game.board.board[drag_from.i][drag_from.j]);
                }
                else
                {
                    drag_canvas_gc.clearRect(0, 0, board_size, board_size);
                    put_piece_xy(drag_piece,x+drag_dx,y+drag_dy,drag_canvas_gc,0,0);
                }
            }
            
        }
        
    };
    
    /////////////////////////////////////////////
    
    static void init_translit()
    {
        translit_light.put(' ',' ');
        translit_light.put('P','p');
        translit_light.put('N','n');
        translit_light.put('B','b');
        translit_light.put('R','r');
        translit_light.put('Q','q');
        translit_light.put('K','k');
        translit_light.put('p','o');
        translit_light.put('n','m');
        translit_light.put('b','v');
        translit_light.put('r','t');
        translit_light.put('q','w');
        translit_light.put('k','l');
		
        translit_dark.put(' ','+');
        translit_dark.put('P','P');
        translit_dark.put('N','N');
        translit_dark.put('B','B');
        translit_dark.put('R','R');
        translit_dark.put('Q','Q');
        translit_dark.put('K','K');
        translit_dark.put('p','O');
        translit_dark.put('n','M');
        translit_dark.put('b','V');
        translit_dark.put('r','T');
        translit_dark.put('q','W');
        translit_dark.put('k','L');
    }
    
    /////////////////////////////////////////////
    
    static int stage_border_width_x=25;
    static int stage_border_width_y=40;
    static void resize_target_board_size(int set_target_board_size)
    {
        board_pane_vertical_box.getChildren().remove(0);
        
        set_target_board_size(set_target_board_size);
                
        board_pane_vertical_box.getChildren().add(0,board_canvas_group);
        
        engine_text_area.setMinWidth(move_lists_width);
        engine_text_area.setMaxWidth(move_lists_width);
        engine_text_area.setMinHeight(bottom_bar_height);
        engine_text_area.setMaxHeight(bottom_bar_height);
        
        gui_stage.setMinHeight(board_size+bottom_bar_height+stage_border_width_y);
        gui_stage.setMaxHeight(board_size+bottom_bar_height+stage_border_width_y);
        gui_stage.setMinWidth(board_size+move_lists_width+stage_border_width_x);
        gui_stage.setMaxWidth(board_size+move_lists_width+stage_border_width_x);
        
        legal_move_list.setMinHeight(board_size);
        legal_move_list.setMaxHeight(board_size);
        legal_move_list.setMinWidth(legal_move_list_width);
        legal_move_list.setMaxWidth(legal_move_list_width);
        game_list.setMinHeight(board_size);
        game_list.setMaxHeight(board_size);
        game_list.setMinWidth(game_move_list_width);
        game_list.setMaxWidth(game_move_list_width);
        
        board_controls_box.setMinWidth(board_size);
        board_controls_box.setMaxWidth(board_size);
        
        //board_pane_vertical_box.setStyle("-fx-background-color: "+current_style.colors[BoardStyle.BOARD_COLOR]);
        
    }
    
    static int calc_board_size(int set_piece_size)
    {
        return 2*set_piece_size*margin_percent/100+(
                            2*set_piece_size*(
                                current_style.sliders[BoardStyle.PADDING_PERCENT]+
                                current_style.sliders[BoardStyle.INNER_PADDING_PERCENT]
                            )/100+
                            set_piece_size
                        )*8;
    }
    
    static void set_target_board_size(int set_target_board_size)
    {
        
        config.target_board_size=set_target_board_size;
        
        MySerialize.write("config.ser", config);
        
        piece_size=1;
        
        while(calc_board_size(piece_size)<config.target_board_size)
        {
            piece_size++;
        }
        
        board_margin=piece_size*margin_percent/100;
        
        board_padding=piece_size*current_style.sliders[BoardStyle.PADDING_PERCENT]/100;
        board_inner_padding=piece_size*current_style.sliders[BoardStyle.INNER_PADDING_PERCENT]/100;
        
        full_piece_size=piece_size+2*board_inner_padding;
        
        board_square_size=2*(board_padding+board_inner_padding)+piece_size;
        half_board_square_size=board_padding+board_inner_padding+piece_size/2;
        
        board_size=2*board_margin+8*board_square_size;
        
        board_canvas=new Canvas(board_size,board_size);
        engine_canvas=new Canvas(board_size,board_size);
        drag_canvas=new Canvas(board_size,board_size);
        
        board_canvas_group=new Group();
        
        board_canvas_group.getChildren().add(board_canvas);
        board_canvas_group.getChildren().add(engine_canvas);
        board_canvas_group.getChildren().add(drag_canvas);
        
        board_canvas_gc=board_canvas.getGraphicsContext2D();
        engine_canvas_gc=engine_canvas.getGraphicsContext2D();
        drag_canvas_gc=drag_canvas.getGraphicsContext2D();
        
        font_stream = Javachessgui2.class.getResourceAsStream("resources/fonts/"+current_style.font);
        
        chess_font=Font.loadFont(font_stream, piece_size);
        
        board_canvas_gc.setFont(chess_font);
        drag_canvas_gc.setFont(chess_font);
        
        board_canvas_group.setOnMouseDragged(board_canvas_handler);
        board_canvas_group.setOnMouseClicked(board_canvas_handler);
        board_canvas_group.setOnMouseReleased(board_canvas_handler);
        
        draw_board();
    }
    
    /////////////////////////////////////////////
    
    static String fen_char_to_font_at(char fen_char,int i,int j)
    {
        
        int square_color=1-((i+j)%2);
        
        if(!current_style.checks[BoardStyle.FONT_ONLY])
        {
            square_color=1;
        }
                
        Object font_obj=square_color==1?translit_light.get(fen_char):translit_dark.get(fen_char);
        
        if(font_obj!=null)
        {
            return font_obj.toString();
        }
        
        return " ";

    }
    
    static int true_index(int index)
    {
        return flip?(7-index):index;
    }
    
    static int px_to_index(int px)
    {
        return true_index((px-board_margin)/board_square_size);
    }
    
    static int index_to_px(int index)
    {
        return board_margin+true_index(index)*board_square_size+half_board_square_size;
    }
    
    static int index_to_piece_px(int index)
    {
        return board_margin+true_index(index)*board_square_size+board_padding+board_inner_padding;
    }
    
    static int index_to_piece_outer_px(int index)
    {
        return board_margin+true_index(index)*board_square_size+board_padding;
    }
    
    static int i_to_font_x(int i)
    {
        return index_to_px(i)-half_board_square_size+board_padding+board_inner_padding;
    }
    
    static int j_to_font_y(int j)
    {
        return index_to_px(j)+half_board_square_size-board_padding-board_inner_padding;
    }
    
    static void fill_square(int i,int j)
    {
        int square_color=1-((i+j)%2);
                
        board_canvas_gc.setFill(square_color==1?
                current_style.colors(BoardStyle.LIGHT_SQUARE_COLOR)
                :
                current_style.colors(BoardStyle.DARK_SQUARE_COLOR)
        );

        board_canvas_gc.fillRect(index_to_piece_outer_px(i),index_to_piece_outer_px(j),full_piece_size,full_piece_size);
    }
    
    static void put_piece_xy(Piece p,int x,int y,GraphicsContext gc,int i,int j)
    {
        int piece_color=p.color();

        String font=fen_char_to_font_at(
                current_style.checks[BoardStyle.FONT_ONLY]?
                        p.fen_char
                        :
                        p.lower_fen_char()
                ,i,j);
        
        if((!p.empty())||current_style.checks[BoardStyle.FONT_ONLY])
        {
            if(current_style.checks[BoardStyle.DO_FILL])
            {
                gc.setFill(piece_color==Piece.WHITE?
                        current_style.colors(BoardStyle.WHITE_PIECE_COLOR)
                        :
                        current_style.colors(BoardStyle.BLACK_PIECE_COLOR)
                );
                gc.fillText(font,x,y);
            }
            if(current_style.checks[BoardStyle.DO_STROKE])
            {
                gc.setStroke(piece_color==Piece.WHITE?
                        current_style.colors(BoardStyle.WHITE_CONTOUR_COLOR)
                        :
                        current_style.colors(BoardStyle.BLACK_CONTOUR_COLOR)
                );
                gc.strokeText(font,x,y);
            }
        }
    }
    
    static void clear_board()
    {

        board_canvas_gc.setFill(current_style.colors(BoardStyle.BOARD_COLOR));
        board_canvas_gc.fillRect(0,0,board_size,board_size);
        
        board_canvas_gc.setLineWidth(1);
        
    }
    
    static void draw_board()
    {
        
        clear_board();
        
        for(int j=0;j<8;j++)
        {
            for(int i=0;i<8;i++)
            {
                
                fill_square(i,j);
                
                put_piece_xy(game.board.board[i][j],i_to_font_x(i),j_to_font_y(j),board_canvas_gc,i,j);
                
            }
        
        }
        
        fen_text.setText(game.board.report_fen());
        
    }
    
    /////////////////////////////////////////////
    
    static Config config;
    
    static void read_in_config()
    {
        Object o=MySerialize.read("config.ser");

        if(o==null)
        {
            config=new Config();
            return;
        }
        
        config=(Config)o;
    }
    
    static void read_in_style(String path)
    {
        Object o=MySerialize.read(path);
        
        if(o==null)
        {
            if(current_style==null)
            {
                current_style=new BoardStyle();
            }
            return;
        }
        
        current_style=(BoardStyle)o;
        
    }
    
    static VBox moves_vbox=new VBox();
    static HBox moves_hbox=new HBox(hbox_padding);
    public static void init(Stage set_gui_stage)
    {
        
        HBox main_hbox=new HBox(hbox_padding);
        
        game.board.init_move_table();
        
        fen_text=new TextField();
        
        try
        {
            new File("boardstyles").mkdir();
        }
        catch(Exception e)
        {
            
        }
        
        MyButton.button_pressed_handler=new EventHandler<ActionEvent>()
        {
            @Override public void handle(ActionEvent e)
            {
                handle_button_pressed(e);
            }
        };
        
        init_translit();

        read_in_style("board_setup.ser");
        read_in_config();
        
        set_target_board_size(config.target_board_size);
        
        gui_stage=set_gui_stage;
        
        gui_stage.setTitle("Chess GUI");
        gui_stage.setScene(scene);
        
        board_pane_vertical_box.getChildren().add(board_canvas_group);
        
        
        clip_to_fen_button=new MyButton("Clip->Fen");
        fen_to_clip_button=new MyButton("Fen->Clip");
        clip_to_pgn_button=new MyButton("Clip->PGN");
        pgn_to_clip_button=new MyButton("PGN->Clip");
        
        Image imageSave = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/save.bmp"));
        Image imageOpen = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/open.bmp"));
        Image imageDel = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/del.bmp"));
        Image imageBack = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/back.bmp"));
        Image imageForward = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/forward.bmp"));
        Image imageBegin = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/begin.bmp"));
        Image imageEnd = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/end.bmp"));
        Image imageOptions = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/options.bmp"));
        Image imageFlip = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/flip.bmp"));
        Image imageReset = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/reset.bmp"));
        Image imageStart = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/start.bmp"));
        Image imageStop = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/stop.bmp"));
        Image imageMake = new Image(Javachessgui2.class.getResourceAsStream("resources/icons/make.bmp"));
        go_button=new MyButton("",new ImageView(imageStart));
        make_button=new MyButton("",new ImageView(imageMake));
        stop_button=new MyButton("",new ImageView(imageStop));
        flip_button=new MyButton("",new ImageView(imageFlip));
        flip_button.setStyle("-fx-background-color: transparent;");
        style_button=new MyButton("",new ImageView(imageOptions));
        style_button.setStyle("-fx-background-color: transparent;");
        begin_button=new MyButton("",new ImageView(imageBegin));
        begin_button.setStyle("-fx-background-color: transparent;");
        end_button=new MyButton("",new ImageView(imageEnd));
        end_button.setStyle("-fx-background-color: transparent;");
        forward_button=new MyButton("",new ImageView(imageForward));
        forward_button.setStyle("-fx-background-color: transparent;");
        back_button=new MyButton("",new ImageView(imageBack));
        back_button.setStyle("-fx-background-color: transparent;");
        load_engine_button=new MyButton("Engine",new ImageView(imageOpen));
        open_pgn_button=new MyButton("PGN",new ImageView(imageOpen));
        save_pgn_as_button=new MyButton("PGN as",new ImageView(imageSave));
        save_pgn_button=new MyButton("PGN",new ImageView(imageSave));
        del_button=new MyButton("",new ImageView(imageDel));
        del_button.setStyle("-fx-background-color: transparent;");
        reset_button=new MyButton("",new ImageView(imageReset));
        reset_button.setStyle("-fx-background-color: transparent;");
        
        Slider board_size_slider=new Slider();
        
        board_size_slider.setMin(300);
        board_size_slider.setMax(600);
        board_size_slider.setMaxWidth(50);
        
        board_size_slider.setValue(config.target_board_size);
           
        board_size_slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    resize_target_board_size(new_val.intValue());
            }
        });
        
        board_controls_box.getChildren().add(board_size_slider);
        
        board_controls_box.getChildren().add(begin_button);
        board_controls_box.getChildren().add(back_button);
        board_controls_box.getChildren().add(forward_button);
        board_controls_box.getChildren().add(end_button);
        board_controls_box.getChildren().add(del_button);
        
        board_controls_box.getChildren().add(flip_button);
        board_controls_box.getChildren().add(style_button);
        
        board_controls_box.getChildren().add(reset_button);
        
        board_controls_box.getChildren().add(clip_to_fen_button);
        board_controls_box.getChildren().add(fen_to_clip_button);
        
        board_controls_box.getChildren().add(load_engine_button);
        
        board_controls_box.getChildren().add(open_pgn_button);
        board_controls_box.getChildren().add(go_button);
        board_controls_box.getChildren().add(stop_button);
        board_controls_box.getChildren().add(make_button);
        board_controls_box.getChildren().add(save_pgn_button);
        board_controls_box.getChildren().add(save_pgn_as_button);
        board_controls_box.getChildren().add(clip_to_pgn_button);
        board_controls_box.getChildren().add(pgn_to_clip_button);
        
        
        board_pane_vertical_box.getChildren().add(board_controls_box);
        
        board_pane_vertical_box.getChildren().add(fen_text);
        
        main_hbox.getChildren().add(board_pane_vertical_box);
        
        moves_hbox.getChildren().add(legal_move_list);
        moves_hbox.getChildren().add(game_list);
        
        moves_vbox.getChildren().add(moves_hbox);
        moves_vbox.getChildren().add(engine_text_area);
        
        main_hbox.getChildren().add(moves_vbox);
        
        legal_move_list.setMaxWidth(80);
        legal_move_list.setMinWidth(80);
        game_list.setMaxWidth(120);
        game_list.setMinWidth(120);
        
        game_list.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
            {
                @Override public ListCell<String> call(ListView<String> list) {
                    return new GameListFormatCell();
                }
            });
        
        legal_move_list.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                int selected =  legal_move_list.getSelectionModel().getSelectedIndex();
                
                if(valid_index(selected,num_sans))
                {
                
                    String san=sorted_sans[selected];

                    make_san_move(san);

                }
                
            }
            
        });
        
        game_list.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                int selected =  game_list.getSelectionModel().getSelectedIndex();
                
                if(valid_index(selected,game.calc_ptr))
                {
                    
                    check_engine_before_making_move();
                
                    game.jump_to(selected);
                    
                    check_engine_after_making_move();
                    
                }
                
            }
            
        });
        
        root.getChildren().add(main_hbox);
        
        update_game();
        
        resize_target_board_size(config.target_board_size);
        
        if(config.uci_engine_path!=null)
        {
            engine.load_engine(config.uci_engine_path);
        }
        else
        {
            engine.load_engine("");
        }
        
        System.out.println("gui initialized");
        
        
    }
    
    static Boolean valid_index(int index,int max)
    {
        if(index<0)
        {
            return false;
        }
        return (index<max);
    }
    
}
