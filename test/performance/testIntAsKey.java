package performance;

import org.junit.Test;

import java.util.*;

/**
 * Created by alext on 24/5/14.
 */
public class testIntAsKey {
    @Test
    public void testPerformanceOfAnIntegerAsKeyAsComparedToString() {

        final Random r = new Random();
        final String[] letters = {"A", "B", "C", "D"};
        final Character[] chars = {'A', 'B', 'C', 'D'};

        final Map<String, Character> stringCharacterMap = new HashMap<>();
        final Map<Integer, Character> integerCharacterHashMap = new HashMap<>();
        final Map<Integer, Character> sudoByteIntegerCharacterHashMap = new HashMap<>();
        final List<String> keyStrings = new ArrayList<>();
        final List<Integer> keyIntegers = new ArrayList<>();
        final List<Integer> keySudoByteIntegers = new ArrayList<>();

        final Map<ByteWrapper, Character> byteWrapperCharacterHashMap = new HashMap<>();
        final List<byte[]> byteWrapperKernels = new ArrayList<>();

        final Map<byte[], Character> plainByteKeysMap = new HashMap<>();

        final Map<String, Character> stringFromBytesMap = new HashMap<>();


        final int[] matchInt = new int[64];
        final char[] matchChar = new char[matchInt.length];

        int key = 0;
        int row = 0;
        for (int i = 0; i < letters.length; i++) {
            for (int j = 0; j < letters.length; j++) {
                for (int k = 0; k < letters.length; k++) {

                    final String s = letters[i].concat(letters[j]).concat(letters[k]);
                    stringCharacterMap.put(s, chars[r.nextInt(chars.length)]);
                    keyStrings.add(s);

                    integerCharacterHashMap.put(key, chars[r.nextInt(chars.length)]);
                    keyIntegers.add(key);
                    key += 10000;

                    final byte[] b = new byte[3];
                    r.nextBytes(b);
                    final int sudoInteger = toInt(b, 0);
                    sudoByteIntegerCharacterHashMap.put(sudoInteger, chars[r.nextInt(chars.length)]);
                    keySudoByteIntegers.add(sudoInteger);

                    matchInt[row] = sudoInteger;
                    matchChar[row++] = chars[r.nextInt(chars.length)];

                    byteWrapperCharacterHashMap.put(new ByteWrapper(b), chars[r.nextInt(chars.length)]);
                    byteWrapperKernels.add(b);

                    plainByteKeysMap.put(b, chars[r.nextInt(chars.length)]);
                    stringFromBytesMap.put(new String(b, 0, b.length), chars[r.nextInt(chars.length)]);

                }
            }
        }

        int numberOfRuns = 10_000_000;

        final String[] stringsToSearch = new String[numberOfRuns];
        for (int i = 0; i < numberOfRuns; i++) {
            stringsToSearch[i] = keyStrings.get(r.nextInt(keyStrings.size()));
        }
        final Integer[] integersToSearch = new Integer[numberOfRuns];
        for (int i = 0; i < numberOfRuns; i++) {
            integersToSearch[i] = keyIntegers.get(r.nextInt(keyIntegers.size()));
        }
        final Integer[] integersFromBytesToSearch = new Integer[numberOfRuns];
        for (int i = 0; i < numberOfRuns; i++) {
            integersFromBytesToSearch[i] = keySudoByteIntegers.get(r.nextInt(keySudoByteIntegers.size()));
        }
        final byte[][] bytes = new byte[numberOfRuns][];
        for (int i = 0; i < numberOfRuns; i++) {
            bytes[i] = byteWrapperKernels.get(r.nextInt(byteWrapperKernels.size()));
        }

        Date start = new Date();
        for (int i = 0; i < numberOfRuns; i++) {
            stringCharacterMap.get(stringsToSearch[i]);
        }
        Date stop = new Date();
        System.out.println("String-as-key search took: " + (stop.getTime() - start.getTime()) + " milis.");

        start = new Date();
        for (int i = 0; i < numberOfRuns; i++) {
            integerCharacterHashMap.get(integersToSearch[i]);
        }
        stop = new Date();
        System.out.println("Integer-as-key search took: " + (stop.getTime() - start.getTime()) + " milis.");

        start = new Date();
        for (int i = 0; i < numberOfRuns; i++) {
            sudoByteIntegerCharacterHashMap.get(toInt(bytes[i], 0));
        }
        stop = new Date();
        System.out.println("Sudo-Byte-Integer-as-key search took: " + (stop.getTime() - start.getTime()) + " milis.");

        start = new Date();
        for (int i = 0; i < numberOfRuns; i++) {
            final char c = matchChar[findSudoInteger(integersFromBytesToSearch[i], matchInt)];
        }
        stop = new Date();
        System.out.println("Sudo-Byte-Integer-in-array linear search took: " + (stop.getTime() - start.getTime()) + " milis.");

        start = new Date();
        final ByteWrapper bw = new ByteWrapper(null);
        for (int i = 0; i < numberOfRuns; i++) {
            bw.setTriByte(bytes[i]);
            byteWrapperCharacterHashMap.get(bw);
        }
        stop = new Date();
        System.out.println("Sudo-Byte-Integer-in-Byte-Wrapper search took: " + (stop.getTime() - start.getTime()) + " milis.");

        start = new Date();
        for (int i = 0; i < numberOfRuns; i++) {
            plainByteKeysMap.get(bytes[i]);
        }
        stop = new Date();
        System.out.println("Sudo-Byte-Integer-Plain-Byte-Map search took: " + (stop.getTime() - start.getTime()) + " milis.");

        start = new Date();
        for (int i = 0; i < numberOfRuns; i++) {
            stringFromBytesMap.get(new String(bytes[i], 0, bytes[i].length));
        }
        stop = new Date();
        System.out.println("Sudo-Byte-String-Map search took: " + (stop.getTime() - start.getTime()) + " milis.");
    }

    public static int toInt(byte[] bytes, int offset) {
        int ret = 0;
        for (int i = 0; i < 4 && i + offset < bytes.length; i++) {
            ret <<= 8;
            ret |= (int) bytes[i] & 0xFF;
        }
        return ret;
    }

    public static int findSudoInteger(int sudo, int[] map) {
        for (int i = 0; i < map.length; i++) {
            if (sudo == map[i]) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    public static class ByteWrapper {
        private byte[] triByte;

        public ByteWrapper(byte[] triByte) {
            this.triByte = triByte;
        }

        public void setTriByte(byte[] triByte) {
            this.triByte = triByte;
        }

        public byte[] getTriByte() {

            return triByte;
        }

        @Override
        public int hashCode() {
            return this.triByte.hashCode();
        }

        @Override
        public boolean equals(Object obj) {

            if (obj instanceof ByteWrapper) {
                final ByteWrapper otherWrapper = (ByteWrapper) obj;
                for (int i = 0; i < otherWrapper.getTriByte().length; i++) {
                    if (this.getTriByte()[i] != otherWrapper.getTriByte()[i]) {
                        return false;
                    }
                }
                return true;
            }


            return false;
        }
    }
}
