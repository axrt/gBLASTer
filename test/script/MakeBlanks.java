package script;

import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alext on 10/7/14.
 * TODO document class
 */
public class MakeBlanks {

    @Test
    public void make() {

        final Path dir = Paths.get("/home/alext/Documents/gBlaster/bbh");
        List<File> files = Arrays.asList(dir.toFile().listFiles());
        files.stream().forEach(file -> {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                bufferedWriter.write("Wiped out to save space!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
