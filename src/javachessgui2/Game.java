package javachessgui2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GameNodeComparator implements Comparator<GameNode>
{
    public int compare(GameNode gn1, GameNode gn2)
    {
        return gn1.rank-gn2.rank;
    }
}

class GameNode
{
    
    String fen;
    
    Hashtable child_nodes=new Hashtable();
    
    GameNode parent_node=null;
    
    int add_san_cnt=0;
    
    int rank=0;
    
    int num_childs=0;
    
    String gen_san="";
    
    Boolean mainline=true;
    Boolean primary=true;
    
    public GameNode(String set_fen)
    {
        fen=set_fen;
    }
    
    
}

public class Game {
    
    public String pgn;
    
    public Boolean flip=false;
    
    public Boolean flip_set=false;
    public Boolean set_flip=false;
    
    final private int MAX_MOVES=250;
        
    String[] pgn_lines=new String[MAX_MOVES];
    
    private String initial_position;
    
    private Hashtable pgn_header_hash=new Hashtable();
    
    public GameNode nodes;
    
    public GameNode[] game;
    
    public int move_ptr=0;
    
    public int calc_ptr=0;
    
    public GameNode current_node;
    
    public Board board=new Board();
    
    public void set_from_fen(String fen)
    {
        board.set_from_fen(fen);
        nodes=new GameNode(fen);
        
        current_node=nodes;
        
        calc_game_to_end();
        
    }
    
    public void make_move(Move m)
    {
        String san=board.to_san(m);
        
        make_san_move(san);
    }
    
    public void make_san_move(String san)
    {
        
        Move m=board.san_to_move(san);
        
        board.make_move(m);
        
        GameNode move_node=(GameNode)current_node.child_nodes.get(san);
        
        if(move_node==null)
        {
            
            move_node=new GameNode(board.report_fen());
            
            current_node.child_nodes.put(san,move_node);
            
            current_node.num_childs++;
            
            current_node.add_san_cnt++;
            
            move_node.rank=current_node.add_san_cnt;
            
            move_node.parent_node=current_node;
            
            move_node.gen_san=san;
            
        }
        
        current_node=move_node;
        
        calc_game_to_end();
        
    }
    
    private void calc_game_to_end()
    {
        
        calc_ptr=0;
        
        ArrayList<GameNode> build=new ArrayList<GameNode>();
        
        current_node.mainline=true;
        
        build.add(current_node);
        calc_ptr++;
        
        GameNode calc_node=current_node;
        
        if(current_node.parent_node!=null)
        {
            ArrayList<GameNode> list=sort_node(current_node.parent_node);
            GameNode main_node=list.get(0);
            for(int s=0;s<list.size();s++)
            {
                GameNode opt=list.get(s);
                if(opt!=current_node)
                {
                    opt.mainline=false;
                    opt.primary=(opt==main_node);
                    build.add(opt);
                    calc_ptr++;
                }
            }
        }
        
        while((calc_node=find_forward(calc_node))!=null)
        {
         
            calc_node.mainline=true;
            build.add(calc_node);
            
            calc_ptr++;
            
        }
        
        calc_node=current_node;
        
        move_ptr=0;
        
        while((calc_node=calc_node.parent_node)!=null)
        {
         
            build.add(0,calc_node);
            
            move_ptr++;
            
            calc_ptr++;
            
        }
        
        game=new GameNode[build.size()];
        
        game=build.toArray(game);
        
    }
    
    public void forward()
    {
        GameNode next=find_forward(current_node);
        
        if(next!=null)
        {
            
            current_node=next;
            
            board.set_from_fen(current_node.fen);
            
        }
        
        calc_game_to_end();
    }
    
    
    
    public ArrayList<GameNode> sort_node(GameNode node)
    {
        
        ArrayList<GameNode> node_list=new ArrayList<GameNode>();
            
        Set<String> keys = node.child_nodes.keySet();

        for(String key: keys)
        {

            GameNode add_node=(GameNode)(node.child_nodes.get(key));

            node_list.add(add_node);

        }

        node_list.sort(new GameNodeComparator());
        
        return node_list;
        
    }
    
    public void jump_to(int selected)
    {
        
        current_node=game[selected];
        
        board.set_from_fen(current_node.fen);
        
        calc_game_to_end();
        
    }
    
    public GameNode find_forward(GameNode what)
    {

        Set<String> keys=what.child_nodes.keySet();
        
        if(keys.isEmpty())
        {
            return null;
        }
        
        ArrayList<GameNode> list=sort_node(what);
        
        if(list.size()<=0)
        {
            return null;
        }
        
        return list.get(0);

    }
    
    public void del()
    {
        
        String gen_san=current_node.gen_san;
        
        back();
        
        current_node.child_nodes.remove(gen_san);
        current_node.num_childs=0;
        
        
        calc_game_to_end();
    }
    
    public void back()
    {
        GameNode back=current_node.parent_node;
        
        if(back!=null)
        {
            
            current_node=back;
            
            board.set_from_fen(current_node.fen);
            
        }
        
        calc_game_to_end();
    }
    
    public void reset()
    {
        board.reset();
        set_from_fen(board.report_fen());
    }
    
    public void set_from_pgn_simple(String pgn)
    {
        pgn_lines = pgn.split("\\r?\\n");
        set_from_pgn_lines_simple();
    }
    
    public void set_from_pgn_lines_simple()
    {

        reset();

        move_ptr=0;

        pgn_header_hash.clear();

        initial_position="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

        int line_cnt=0;

        // read headers
        int empty_cnt=0;

        Boolean finished=false;

        do
        {
            String line=pgn_lines[line_cnt++];

            if(line_cnt<pgn_lines.length)
            {
                if(line.length()<2)
                {
                    finished=true;
                }
                else
                {
                    if(line.charAt(0)!='[')
                    {
                        finished=true;
                    }
                    else
                    {

                        // parse header fields

                        Pattern get_header = Pattern.compile("\\[([^ ]+) \"([^\\\"]+)\\\"");
                        Matcher header_matcher = get_header.matcher(line);

                        if(header_matcher.find())
                        {
                            String key=header_matcher.group(1);
                            String value=header_matcher.group(2);
                            //System.out.println("key "+key+" value "+value);

                            if(!key.equals("StartFen"))
                            {
                                pgn_header_hash.put(key,value);
                            }

                            // set initial position if any

                            if(key.equals("FEN"))
                            {
                                initial_position=value;
                            }

                        }

                    }
                }
            }
            else
            {
                finished=true;
            }

        }while(!finished);

        String body="";
        while(line_cnt<pgn_lines.length)
        {
            String line=pgn_lines[line_cnt++];
            if(line.length()<2)
            {
                break;
            }
            body+=line+" ";
        }

        // remove all comments, carriage return, line feed
        body=body.replaceAll("\r|\n|\\{[^\\}]*\\}","");

        //System.out.println("body: "+body);

        MyTokenizer t=new MyTokenizer(body);

        String token;

        board.set_from_fen(initial_position);

        flip_set=false;
        Object flip_obj=pgn_header_hash.get("Flip");
        if(flip_obj!=null)
        {
            flip_set=true;
            flip=flip_obj.toString().equals("true")?true:false;
        }

        while((token=t.get_token())!=null)
        {
            
            if(token.contains(".")||token.contains("*"))
            {
                // does not look a move
                //System.out.println(token+" does not look a move: ");
            }
            else
            {
                //System.out.println(token+" looks a move ");
                make_san_move(token);

            }

        }

        calc_game_to_end();

    }
    
    public String calc_pgn()
    {
        Board dummy=new Board();

        initial_position=nodes.fen;
        dummy.set_from_fen(initial_position);

        int fullmove_number=dummy.fullmove_number;
        int turn=dummy.turn;

        pgn="[FEN \""+initial_position+"\"]\n";
        //start_fen_end_index=pgn.length()-1;
        if(set_flip)
        {
            pgn+="[Flip \""+flip+"\"]\n";
        }

        // add hash headers

        Set<String> keys = pgn_header_hash.keySet();
        for(String key: keys)
        {
            String value=pgn_header_hash.get(key).toString();
            if((!key.equals("FEN"))&&(!key.equals("Flip")))
            {
                pgn+="["+key+" \""+value+"\"]\n";
            }
        }

        pgn+="\n";
        
        current_node=nodes;
        
        calc_game_to_end();

        if(calc_ptr>1)
        {
            pgn+=fullmove_number+". ";

            if(turn==Board.TURN_BLACK)
            {
                pgn+="... ";
            }

            //move_indices[0]=pgn.length();

            pgn+=game[1].gen_san+" ";
        }

        for(int i=2;i<calc_ptr;i++)
        {
            turn=-turn;
            if(turn==Board.TURN_WHITE)
            {
                fullmove_number++;
                pgn+=fullmove_number+". ";
            }
            //move_indices[i]=pgn.length();
            pgn+=game[i].gen_san+" ";
        }

        return pgn;

    }
    
    public Game()
    {
        reset();
    }
    
}
