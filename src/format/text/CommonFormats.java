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
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(toCheck));
            final String line = bufferedReader.readLine();
            if (line != null && line.startsWith(FASTA_START) && bufferedReader.readLine() != null) {
                return true;
            }
            return false;
        }

        @Override
        public String getAc(InputStream record) throws IOException {
            return new BufferedReader(new InputStreamReader(record)).readLine();
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
        public Stream<InputStream> iterateRecords(InputStream multiRecord, Path toTmpFile) {


            Iterator<InputStream> iter = new Iterator<InputStream>() {
                private final File tmpFile = toTmpFile.toFile();
                private String line;
                private boolean empty = false;
                private final PushbackInputStream pushbackInputStream=new PushbackInputStream(multiRecord);

                private void cleanup() {
                    try {
                        pushbackInputStream.close();
                        tmpFile.delete();
                    } catch (IOException ee) {
                        throw new UncheckedIOException(ee);
                    }
                }

                @Override
                public boolean hasNext() {
                    if (this.empty) {
                        cleanup();
                    } else{
                        if(this.tmpFile.exists()){
                            tmpFile.delete();
                        }
                    }
                    return !this.empty;
                }

                @Override
                public InputStream next() {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.tmpFile))) {
                        byte[]buffer=new byte[1];
                        int count=0;
                        int read;
                        while((read=pushbackInputStream.read(buffer))!=-1){
                            if(count!=0&&FASTA_START.charAt(0)==(char)buffer[0]){
                                pushbackInputStream.unread(buffer);
                                break;
                            }
                            bufferedWriter.write((char)buffer[0]);
                            count++;
                        }
                        if(read==-1){
                           this.empty=true;
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

