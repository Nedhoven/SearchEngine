
package engine.index;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implement this compressor with Delta Encoding and Variable-Length Encoding.
 */

public class DeltaVarLenCompressor implements Compressor {

    /**
     * Encodes a list of integers to a byte array.
     */

    @Override
    public byte[] encode(List<Integer> integers) {
        List<Integer> vec = new ArrayList<>();
        int offset = 0;

        for (int num : integers) {
            StringBuilder temp = new StringBuilder();
            List<Integer> byteList = new ArrayList<>();

            // calculating the offset of the number from the previous one
            num -= offset;

            // updating the offset
            offset += num;

            // check if the number is negative
            if (num < 0) {
                System.err.println("list needs to be sorted");
                System.exit(1);
            }

            // check if the number is zero
            if (num == 0) {
                temp.insert(0, '0');
            }

            // a flag to check if the number needs more than one byte
            boolean flag = false;
            int count = 1;

            // binary representation of num
            while (num > 0) {
                if (count == 8) {
                    if (flag) {
                        temp.insert(count - 1, '1');
                        String s = temp.reverse().toString();
                        int b = Integer.parseInt(s, 2);
                        byteList.add(b);
                        temp = new StringBuilder();

                    }
                    else {
                        temp.insert(count - 1, '0');
                        String s = temp.reverse().toString();
                        int b = Integer.parseInt(s, 2);
                        byteList.add(b);
                        temp = new StringBuilder();
                    }
                    flag = true;
                    count = 1;
                    continue;
                }
                int c = num % 2;
                if (c == 0) {
                    temp.insert(count - 1, '0');
                }
                else {
                    temp.insert(count - 1, '1');
                }
                num /= 2;
                count++;
            }

            // filling the left bytes
            for (int j = temp.length(); j < 8; j++) {
                if (j == 7 && flag) {
                    temp.insert(j, '1');
                }
                else {
                    temp.insert(j, '0');
                }
            }
            String s = temp.reverse().toString();
            int b = Integer.parseInt(s, 2);
            byteList.add(b);

            // adding the results
            Collections.reverse(byteList);
            vec.addAll(byteList);
        }

        // creating the byte array out of the results
        byte[] coded = new byte[vec.size()];
        for (int i = 0; i < vec.size(); i++) {
            int ig = vec.get(i);
            coded[i] = (byte) ig;
        }
        return coded;
    }

    /**
     * Decodes part of a byte array to a list of integers.
     *
     * @param bytes bytes to decode
     * @param start starting position to decode
     * @param length number of bytes to decode from start position
     */

    @Override
    public List<Integer> decode(byte[] bytes, int start, int length) {
        List<Integer> decoded = new ArrayList<>();
        List<Integer> preCoded = new ArrayList<>();
        StringBuilder total = new StringBuilder();

        // start reading bytes
        for (int i = start; i < start + length; i++) {
            byte b = bytes[i];

            // binary string representation of the byte
            String binary = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');

            // last byte
            if (binary.charAt(0) == '0') {
                String temp = binary.substring(1);
                total.append(temp);
                preCoded.add(Integer.parseInt(total.toString(), 2));
                total = new StringBuilder();
            }

            // still bytes left to read
            else if (binary.charAt(0) == '1' && i != start + length - 1) {
                String temp = binary.substring(1);
                total.append(temp);
            }

            // bytes left to read but reader is at the end of the length
            else {
                System.err.println("length is too short to complete decoding");
                String temp = binary.substring(1);
                total.append(temp);
                preCoded.add(Integer.parseInt(total.toString(), 2));
            }
        }

        // decoding the numbers
        int offset = 0;
        for (int num : preCoded) {
            num += offset;
            offset = num;
            decoded.add(num);
        }
        return decoded;
    }

}
