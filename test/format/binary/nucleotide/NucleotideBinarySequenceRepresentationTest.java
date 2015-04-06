package format.binary.nucleotide;

import alphabet.character.nucleotide.Nucleotide;
import alphabet.nucleotide.NucleotideAlphabet;
import format.SequenceRepresentation;
import format.binary.nucleotide.NucleotideBinarySequenceRepresentation;
import org.junit.Assert;
import org.junit.Test;
import sequence.nucleotide.NucleotideSequence;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class NucleotideBinarySequenceRepresentationTest {

    @Test
    public void convertTest() throws FileNotFoundException {

        final NucleotideSequence<Nucleotide> sequence1 = NucleotideSequence.get("ATGCatgc-?NRMWSKYVHDBnrmwskyvhdb", "Test1");
        final NucleotideSequence<Nucleotide> sequence2 = NucleotideSequence.get("ATGCatgc-?NRMWSKYVHDBnrmwskyvhdb", "Test2");

        final List<NucleotideSequence<Nucleotide>> sequences = Arrays.asList(sequence1, sequence2);

        final SequenceRepresentation<Nucleotide, NucleotideSequence<Nucleotide>> representation = new NucleotideBinarySequenceRepresentation
                .Builder()
                .alphabet(NucleotideAlphabet.get())
                .header("test".getBytes())
                .sequences(sequences)
                .stop(new byte[]{(byte) 0x00000000})
                .build();


        final File testFile1 = new File("converted.file");
        final File testFile2 = new File("plain.file");
        try (FileOutputStream fileOutputStream = new FileOutputStream(testFile1)) {
            fileOutputStream.write(representation.convert());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int fileSizeCount = 0;
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(testFile2))) {

            final String header = "test";
            bufferedWriter.write(header);
            bufferedWriter.write(sequences.size());
            bufferedWriter.write(0);
            bufferedWriter.write(0);
            for (NucleotideSequence<Nucleotide> n : sequences) {
                bufferedWriter.write(n.getAc());
                bufferedWriter.write(n.getSequence());
                System.out.println(n.getSequence().getBytes().length);
                bufferedWriter.write('s');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Now read back
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(testFile1))) {
            byte[] h = new byte[4];
            final int n = dataInputStream.read(h);
            final String header = new String(h, 0, n);
            System.out.println("Header is: " + header);
            Assert.assertEquals(header, "test");
            final int numberOfSeqs = dataInputStream.readInt();
            System.out.println("Number of sequences is: " + numberOfSeqs);
            Assert.assertEquals(numberOfSeqs, 2);
            final int[] starts = new int[numberOfSeqs + 1];
            for (int i = 0; i < numberOfSeqs + 1; i++) {
                starts[i] = dataInputStream.readInt();
                if (i < numberOfSeqs) {
                    System.out.println("Sequence " + i + " starts at:" + starts[i]);
                }
            }
            for (int i = 0; i < starts.length - 1; i++) {

                byte[] buffer = new byte[32];
                dataInputStream.read(buffer);
                final String ac = new String(buffer, 0, buffer.length);
                System.out.println("Sequence " + i + " AC is: " + ac);
                buffer = new byte[starts[i + 1] - starts[i] - 32];
                dataInputStream.read(buffer);
                final String sequence = NucleotideAlphabet.get().decode(Arrays.copyOf(buffer, buffer.length - 1));
                System.out.println("Sequence " + i + ": " + sequence);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Now random sequence access test
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(testFile1))) {
            byte[] h = new byte[4];
            final int n = dataInputStream.read(h);
            final String header = new String(h, 0, n);
            System.out.println("Header is: " + header);
            Assert.assertEquals(header, "test");
            final int numberOfSeqs = dataInputStream.readInt();
            System.out.println("Number of sequences is: " + numberOfSeqs);
            Assert.assertEquals(numberOfSeqs, 2);
            final int[] starts = new int[numberOfSeqs + 1];
            for (int i = 0; i < numberOfSeqs + 1; i++) {
                starts[i] = dataInputStream.readInt();
                if (i < numberOfSeqs) {
                    System.out.println("Sequence " + i + " starts at:" + starts[i]);
                }
            }
            byte[] buffer = new byte[32 + 33];
            System.out.println(dataInputStream.skipBytes(starts[1] - 20));
            dataInputStream.read(buffer);
            System.out.println("AC is: " + new String(buffer, 0, 32));
            System.out.println("Sequence is: " + NucleotideAlphabet.get().decode(Arrays.copyOfRange(buffer, 32, buffer.length - 1)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        testFile1.delete();
        testFile2.delete();
    }


}
