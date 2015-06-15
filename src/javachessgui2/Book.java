package javachessgui2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class BookMoveComparator implements Comparator<BookMove>
{
    public int compare(BookMove b1, BookMove b2)
    {
        if(b1.notation!=b2.notation)
        {
            return b2.notation-b1.notation;
        }
        else if(b1.eval!=b2.eval)
        {
            return b2.eval-b1.eval;
        }
        else
        {
            return b2.count-b1.count;
        }
    }
}

class BookMove
{
    
    public String san;
    public int notation;
    public Boolean is_analyzed;
    public int eval;
    public int count;
    
    public Hashtable report_hash()
    {
        Hashtable hash=new Hashtable();
        hash.put("notation", ""+notation);
        hash.put("is_analyzed", ""+is_analyzed);
        hash.put("eval", ""+eval);
        hash.put("count", ""+count);
        
        return hash;
    }
    
    public void set_from_hash(Hashtable hash)
    {
        if(hash.get("notation")!=null)
        {
            notation=Integer.parseInt(hash.get("notation").toString());
        }
        
        if(hash.get("is_analyzed")!=null)
        {
            is_analyzed=hash.get("is_analyzed").toString().equals("true");
        }
        
        if(hash.get("eval")!=null)
        {
            eval=Integer.parseInt(hash.get("eval").toString());
        }
        
        if(hash.get("count")!=null)
        {
            count=Integer.parseInt(hash.get("count").toString());
        }
        
    }
    
    public BookMove(String set_san)
    {
        san=set_san;
        notation=-1;
        is_analyzed=false;
        eval=0;
        count=0;
    }
    
}

public class Book {

    String dir="book";
    
    Hashtable book=new Hashtable();
    
    private String fen_to_name(String fen)
    {
        return dir+File.separator+Encode32.encode(fen, true)+".txt";
    }
    
    public void store_pos(String fen,Hashtable hash)
    {

        fen=Board.fen_to_raw(fen);

        String name=fen_to_name(fen);

        MyFile pos_file=new MyFile(name);

        pos_file.from_hash(hash);
        
    }
    
    public Hashtable get_pos(String fen)
    {

        fen=Board.fen_to_raw(fen);

        Object pos_obj=book.get(fen);

        if(pos_obj==null)
        {

            String name=fen_to_name(fen);

            File f = new File(name);

            if(f.exists())
            {

                MyFile look_up=new MyFile(name);
                Hashtable pos=look_up.to_hash();

                book.put(fen,pos);

                return pos;

            }
            else
            {

                return new Hashtable();

            }

        }
        else
        {

            return (Hashtable)pos_obj;

        }

    }
    
    public void add_move(String fen_before,String san)
    {
        
        Hashtable pos=get_pos(fen_before);
                                
        if(pos.get(san)==null)
        {
            BookMove new_book_move=new BookMove(san);
            new_book_move.count=1;
            pos.put(san,new_book_move.report_hash());
        }
        else
        {
            BookMove old_book_move=new BookMove(san);
            old_book_move.set_from_hash((Hashtable)pos.get(san));
            old_book_move.count++;
            pos.put(san,old_book_move.report_hash());
        }

        store_pos(fen_before,pos);
        
    }
    
    String[] notation_list={"!!  winning","!   strong","!?  promising","-   stable","?!  interesting","?   bad","??  losing"};
    String[] notation_list_short={"??","?","?!","-","!?","!","!!"};
    
    public int no_book_moves;
    public ArrayList<BookMove> book_list;
    ObservableList<String> items;
    
    public void update_book(String fen)
    {

        Hashtable book_moves=get_pos(fen);

        no_book_moves=0;

        book_list=new ArrayList<BookMove>();

        Set<String> keys = book_moves.keySet();

        for(String key: keys)
        {

            Hashtable value=(Hashtable)(book_moves.get(key));

            BookMove book_move=new BookMove(key);

            book_move.set_from_hash(value);

            book_list.add(book_move);

        }

        // sort book list

        book_list.sort(new BookMoveComparator());

        no_book_moves=book_list.size();

        String[] temp_list=new String[200];
        int temp_ptr=0;

        for (BookMove temp : book_list) {

            String notation_as_string="N/A";
            if(temp.notation>=0)
            {
                notation_as_string=notation_list_short[temp.notation];
            }

            String eval="_";
            if(temp.is_analyzed)
            {
                eval=""+temp.eval;
            }
            String book_line=String.format("%-10s %-4s %5d %8s",temp.san,notation_as_string,temp.count,eval);
            temp_list[temp_ptr++]=book_line;
        }

        items =FXCollections.observableArrayList(
        Arrays.copyOfRange(temp_list, 0, no_book_moves)
        );

    }
    
    public void record_eval(String fen,String san,int eval)
    {

        Hashtable pos=get_pos(fen);

        if(pos.get(san)==null)
        {
            BookMove new_book_move=new BookMove(san);
            new_book_move.count=1;
            new_book_move.is_analyzed=true;
            new_book_move.eval=eval;
            pos.put(san,new_book_move.report_hash());
        }
        else
        {
            BookMove old_book_move=new BookMove(san);
            old_book_move.set_from_hash((Hashtable)pos.get(san));
            old_book_move.is_analyzed=true;
            old_book_move.eval=eval;
            pos.put(san,old_book_move.report_hash());
        }

        store_pos(fen,pos);

    }
    
    public Book(String set_dir)
    {
        
        if(set_dir!=null)
        {
            
            dir=set_dir;
            
        }
        
        try
        {
            new File(dir).mkdir();
        }
        catch(Exception e)
        {
            
        }
        
        
    }
    
}
