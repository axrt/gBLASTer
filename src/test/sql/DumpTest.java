package test.sql;

import org.junit.Test;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alext on 6/23/14.
 * TODO document class
 */
public class DumpTest {

    @Test
    public void test(){

        final File pathToFile=new File("/home/alext/dumps/gblaster_first_10/gblaster_blasts.sql");
        try(BufferedReader bufferedReader=new BufferedReader(new FileReader(pathToFile))){
            final Pattern p = Pattern.compile("<Iteration>");
            System.out.println(bufferedReader.lines().mapToInt(line -> {

                final Matcher m = p.matcher(line);
                int count = 0;
                while (m.find()) {
                    count += 1;
                }
                return count;
            }).sum());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
