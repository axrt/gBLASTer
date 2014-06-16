package format.text;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(toCheck));
            final String line = bufferedReader.readLine();
            if (line != null && line.startsWith(FASTA_START) && bufferedReader.readLine() != null) {
                return true;
            }
            return false;
        }

        @Override
        public String getAc(InputStream record) throws IOException {
            return new BufferedReader(new InputStreamReader(record)).readLine().substring(1);
        }

        @Override
        public void passSequence(InputStream record, OutputStream destination) throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public Stream<InputStream> iterateRecords(InputStream multiRecord, Path toTmpFile) {


            Iterator<InputStream> iter = new Iterator<InputStream>() {
                private final File tmpFile = toTmpFile.toFile();
                private boolean empty = false;
                private final PushbackInputStream pushbackInputStream = new PushbackInputStream(multiRecord);

                private void cleanup() {
                    tmpFile.delete();
                    try {
                        this.pushbackInputStream.close();
                    } catch (IOException e) {
                       throw new UncheckedIOException(e);
                    }
                }

                @Override
                public boolean hasNext() {
                    if (this.empty) {
                        cleanup();
                    } else {
                        if (this.tmpFile.exists()) {
                            this.tmpFile.delete();
                        }
                    }
                    return !this.empty;
                }

                @Override
                public InputStream next() {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.tmpFile))) {
                        byte[] buffer = new byte[1];
                        boolean firsLine=true;
                        int read;
                        while ((read = pushbackInputStream.read(buffer)) != -1) {
                            if (!firsLine && FASTA_START.charAt(0) == (char) buffer[0]) {
                                pushbackInputStream.unread(buffer);
                                break;
                            }
                            if((char)buffer[0]!='\n') {
                                bufferedWriter.write((char) buffer[0]);
                            }
                            firsLine=false;
                        }
                        if (read == -1) {
                            this.empty = true;
                        }
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

