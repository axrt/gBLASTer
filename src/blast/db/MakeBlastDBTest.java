package blast.db;

import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by alext on 6/16/14.
 * TODO document class
 */
public class MakeBlastDBTest {

    @Test
    public void test(){
         final MakeBlastDB makeBlastDB=new  MakeBlastDB.MakeBlastDBBuilder("test")
                 .pathToMakeBlastDb(Paths.get("/bin/makeblastdb"))
                 .pathToDbFolder(Paths.get("/home/alext/Downloads/tmp"))
                 .pathToSequenceFile(Paths.get("/home/alext/Downloads/tmp/test.nu"))
                 .type(MakeBlastDB.DBType.NUCL)
                 .build();
        final ExecutorService executorService= Executors.newSingleThreadExecutor();
        final Future<Optional<File>> fileFuture=executorService.submit(makeBlastDB);
        try {
            fileFuture.get().ifPresent(System.out::println);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
