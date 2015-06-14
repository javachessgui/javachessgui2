package javachessgui2;

import java.io.*;
import java.util.Arrays;

public class MyDirectory {
    
    static String[] list(String path)
    {
        File f = null;
        File[] paths;
      
        try{      
         // create new file
         f = new File(path);
         
         // returns pathnames for files and directory
         paths = f.listFiles();
         
      }catch(Exception e){
         // if any error occurs
         e.printStackTrace();
         return null;
      }
        
        String[] paths_as_string=new String[1000];
        int p_cnt=0;
        
        for(File p:paths)
            {
               if(p_cnt<1000)
               {
                   paths_as_string[p_cnt]=p.toString();
                   p_cnt++;
               }
               
            }
        
        return Arrays.copyOfRange(paths_as_string,0,p_cnt);
    }
    
    public MyDirectory()
    {
        
    }
}
