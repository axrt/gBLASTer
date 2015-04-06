package psimscan;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by alext on 4/6/15.
 */
public class QSimScanHelper {

    private QSimScanHelper() {
        throw new AssertionError("Non-instantiable!");
    }

    public static long numberOfFastas(Path toFile) throws IOException {
        return Files.lines(toFile).filter(line -> line.startsWith(">")).count();
    }

    public static List<Path> splitFasta(Path toFastaFile, int fastasPerBatch) throws Exception {

        final List<Path> partPath = new ArrayList<>();
        final String[] split;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(toFastaFile.toFile()))) {
            final StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
            split = stringBuilder.toString().split(">");
            int counter = 0;
            int filesWritten = 1;
            final List<String> splitList = Arrays.asList(split);
            final Queue<String> queue = new LinkedList<>(splitList.subList(1, splitList.size()));
            while (!queue.isEmpty()) {
                final Path toPart = toFastaFile.resolveSibling(toFastaFile.getFileName() + "." + filesWritten + ".part");
                partPath.add(toPart);
                try (BufferedWriter bufferedWriter = new BufferedWriter(
                        new FileWriter(toPart.toFile()))) {
                    while (!queue.isEmpty() && counter < fastasPerBatch) {
                        bufferedWriter.write(">");
                        bufferedWriter.write(queue.poll());
                        bufferedWriter.newLine();
                        counter++;
                    }
                    counter = 0;
                    filesWritten++;
                }
            }
        }
        return partPath;
    }
}
