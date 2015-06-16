package javachessgui2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static javachessgui2.Gui.current_style;

public class MySerialize
{
    
    public static Boolean write(String path,Object what)
    {
        
        FileOutputStream fileOut;
        
        try
        {
            fileOut =
            new FileOutputStream(path);
        }
        catch(FileNotFoundException e)
        {
            return false;
        }
        
        ObjectOutputStream out;
        try
        {
            out = new ObjectOutputStream(fileOut);
        }
        catch(IOException e)
        {
            return false;
        }
        
        try
        {
            out.writeObject(what);
        }
        catch(IOException e)
        {
            return false;
        }
        
        try
        {
            out.close();
        }
        catch(IOException e)
        {

        }
        
        try
        {
            fileOut.close();
        }
        catch(IOException e)
        {

        }
        
        return true;
        
    }
    
    public static Object read(String path)
    {
        
        Object o=new Object();
        
        FileInputStream fileIn;
        
        try
        {
            fileIn = new FileInputStream(path);
        }
        catch(FileNotFoundException e)
        {
            return null;
        }
        
        ObjectInputStream in;
        try
        {
            in = new ObjectInputStream(fileIn);
        }
        catch(IOException e)
        {
            return null;
        }
        
        try
        {
            try
            {
                o = in.readObject();
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        catch(ClassNotFoundException e)
        {
            return null;
        }
        
        return o;
    }
    
    public MySerialize()
    {
        
    }
    
}
