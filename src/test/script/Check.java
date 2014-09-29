package test.script;

import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alext on 9/26/14.
 * TODO document class
 */
public class Check {

    @Test
    public void checkMasterTable(){
        try(BufferedReader bufferedReader=new BufferedReader(new FileReader(new File("/home/alext/Documents/gBlaster/research/R/gblaster/bhs/master_table.txt")))){
            final Set<String> names=new HashSet<>();
           bufferedReader.lines().forEach(
                   line->{
                       final String[]split=line.split("\t");
                       if(!names.add(split[0])){
                           System.out.println(split[0]);
                       }
                   }
           );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
