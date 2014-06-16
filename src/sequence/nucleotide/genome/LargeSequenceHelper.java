package sequence.nucleotide.genome;

import alphabet.nucleotide.NucleotideAlphabet;

import java.io.*;
import java.nio.file.Path;

/**
 * Created by alext on 6/16/14.
 * TODO document class
 */
public final class LargeSequenceHelper {

    private LargeSequenceHelper() {
        throw new AssertionError("Non-instantiable!");
    }

    public static InputStream revertLargeNucleotideSequence(InputStream sequenceToRevert, NucleotideAlphabet nucleotideAlphabet, File inputTmpFile,File outputTmpFile ) throws IOException {
        final byte[] buffer = new byte[1];
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(sequenceToRevert);
             BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(new FileOutputStream(inputTmpFile))) {
            while (bufferedInputStream.read(buffer) > -1) {
                bufferedOutputStream.write(buffer);
            }
        }
        final RandomAccessFile inputRFile=new RandomAccessFile(inputTmpFile,"r");
        final RandomAccessFile outputRFile=new RandomAccessFile(outputTmpFile,"rw");
        long length=inputRFile.length()-2;//-1 cuz length, -1 again cuz eof
        for(long i=length;i>-1;i--){
            inputRFile.seek(i);
            inputRFile.read(buffer);
            buffer[0]=(byte)nucleotideAlphabet.rc((char)buffer[0]);
            outputRFile.write(buffer);
        }

        return new FileInputStream(outputTmpFile);
    }

}
