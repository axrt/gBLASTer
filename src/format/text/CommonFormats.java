package format.text;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public class CommonFormats {
    private CommonFormats() {
        throw new AssertionError("Non-instantiable!");
    }

    public static final Format FASTA = new Fasta();
    public static final LargeFormat LARGE_FASTA = new LargeFasta();

    private static class Fasta implements Format {
        public static final int MAX_LENGTH = 100;
        public static final String FASTA_START = ">";

        @Override
        public boolean checkFormatting(String toCheck) {
            if (!toCheck.startsWith(FASTA_START) || !toCheck.contains("\n")) {
                return false;
            } else return true;
        }

        @Override
        public String getAc(String record) {
            final String[] split = record.split("\n");
            return split[0].substring(1, split[0].length());
        }

        @Override
        public String getSequence(String record) {
            final String[] split = record.split("\n");
            return Arrays.asList(split).stream().skip(1).collect(Collectors.joining());
        }
    }

    public static class LargeFasta extends Fasta implements LargeFormat {
        @Override
        public boolean checkFormatting(InputStream toCheck) throws IOException {
            try (Reader reader = new InputStreamReader(toCheck)) {
                final char[] buffer = new char[256];
                reader.read(buffer);
                if (!String.valueOf(buffer[0]).equals(FASTA_START)) {
                    return false;
                }

                while (reader.read(buffer) != -1) {
                    for (int i = 0; i < buffer.length; i++) {
                        if (buffer[i] == '\n') {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public String getAc(InputStream record) throws IOException {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(record))) {

                final String ac = bufferedReader.readLine();
                if (ac == null || !ac.startsWith(FASTA_START)) {
                    throw new IllegalArgumentException("Not a fasta-formatted record!");
                } else {
                    record.reset();
                    return ac.substring(1, ac.length());
                }
            }
        }

        @Override
        public void passSequence(InputStream record, OutputStream destination) throws IOException {

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(record));
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(destination)) {
                bufferedReader.lines().skip(1).forEach(l -> {
                    try {
                        final byte[] buffer = l.getBytes();
                        bufferedOutputStream.write(buffer);
                    } catch (IOException io) {
                        throw new UncheckedIOException(io);
                    }
                });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
            record.reset();
        }

        @Override
        public Stream<InputStream> iterateRecords(InputStream multiRecord,Path toTmpFile) {


            Iterator<InputStream> iter = new Iterator<InputStream>() {
                private final File tmpFile = toTmpFile.toFile();
                private String line;
                private int counter = 0;
                private boolean empty = false;
                private final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(multiRecord));

                private void cleanup(){
                    try {
                        bufferedReader.close();
                        this.tmpFile.delete();
                    } catch (IOException ee) {
                        throw new UncheckedIOException(ee);
                    }
                }

                @Override
                public boolean hasNext() {
                    if(this.empty){
                       cleanup();
                    }
                    return !this.empty;
                }

                @Override
                public InputStream next() {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.tmpFile))) {
                        while ((line = bufferedReader.readLine()) != null) {
                            if (this.counter == 0) {
                                bufferedWriter.write(line);
                                bufferedWriter.newLine();
                                continue;
                            }
                            if (line.startsWith(FASTA_START)) {
                                break;
                            } else {
                                bufferedWriter.write(line);
                                bufferedWriter.newLine();
                            }
                        }
                        if (line == null) {
                            this.empty = true;
                        }
                        this.counter++;
                        return new FileInputStream(this.tmpFile);
                    } catch (IOException e) {
                           cleanup();
                        throw new UncheckedIOException(e);
                    }
                }
            };
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.NONNULL), false);
        }
    }
}

