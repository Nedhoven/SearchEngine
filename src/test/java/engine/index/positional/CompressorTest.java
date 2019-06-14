
package engine.index.positional;

import engine.index.Compressor;
import engine.index.DeltaVarLenCompressor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class CompressorTest {

    private Compressor compressor = new DeltaVarLenCompressor();

    @Test
    public void test1() {
        Compressor compressor = new DeltaVarLenCompressor();
        Function<List<Integer>, byte[]> encodeFunc = compressor::encode;
        Function<byte[], List<Integer>> decodeFunc = compressor::decode;
        Stream.of(
                Arrays.asList(0),
                Arrays.asList(Integer.MAX_VALUE),
                Arrays.asList(Integer.MAX_VALUE, Integer.MAX_VALUE),
                Arrays.asList(0, Integer.MAX_VALUE),
                Arrays.asList(1, 1, 1),
                Arrays.asList(1, 2, 3),
                Arrays.asList(3, 3, 7, 23, 23, 65, 294, 2190, 238923)
        ).forEach(l -> assertEquals(
                "Function composition (decode . encode) should be equivalent to id",
                Function.identity().apply(l),
                decodeFunc.compose(encodeFunc).apply(l)
        ));
        Stream.of(
                new byte[] { 0x00 },
                new byte[] { (byte)0x87, (byte)0xff, (byte)0xff, (byte)0xff, 0x7f },
                new byte[] { (byte)0x87, (byte)0xff, (byte)0xff, (byte)0xff, 0x7f, 0x00 },
                new byte[] { 0x00, (byte)0x87, (byte)0xff, (byte)0xff, (byte)0xff, 0x7f },
                new byte[] { 0x01, 0x00, 0x00 },
                new byte[] { 0x01, 0x01, 0x01 },
                new byte[] {
                        0x03,
                        0x00,
                        0x04,
                        0x10,
                        0x00,
                        0x2a,
                        (byte)0x81, 0x65,
                        (byte)0x8e, 0x68,
                        (byte)0x8e, (byte)0xb9, 0x3d
                }
        ).forEach(b -> assertArrayEquals(
                "Function composition (encode . decode) should be equivalent to id",
                (byte[]) Function.identity().apply(b),
                encodeFunc.compose(decodeFunc).apply(b)
        ));
    }

    @Test
    public void test2() {
        Compressor compressor = new DeltaVarLenCompressor();
        for (int i = 0; i <= 127; i++)
            assertArrayEquals(
                    new byte[] { (byte)i },
                    compressor.encode(Arrays.asList(i))
            );
    }

    @Test
    public void test3() {
        Compressor compressor = new DeltaVarLenCompressor();
        assertArrayEquals(
                new byte[] { (byte)0b10000001, 0b00000000 },
                compressor.encode(Arrays.asList(128))
        );
        assertArrayEquals(
                new byte[] { (byte)0b10000001, (byte)0b10000000, 0b00000000 },
                compressor.encode(Arrays.asList(16384))
        );
        assertArrayEquals(
                new byte[] {
                        (byte)0b10000111, (byte)0b11111111, (byte)0b11111111,
                        (byte)0b11111111, 0b01111111
                },
                compressor.encode(Arrays.asList(Integer.MAX_VALUE))
        );
    }

    @Test
    public void test4() {
        Compressor compressor = new DeltaVarLenCompressor();
        assertArrayEquals(
                new byte[] { 1, 0, 0 },
                compressor.encode(Arrays.asList(1, 1, 1))
        );
        assertArrayEquals(
                new byte[] { 1, 1, 1 },
                compressor.encode(Arrays.asList(1, 2, 3))
        );
        List<Integer> integers = new ArrayList<>();
        byte[] expected = new byte[128];
        for (int i = 0; i <= 127; i++) {
            integers.add(i * (i + 1) / 2);
            expected[i] = (byte)i;
        }
        assertArrayEquals(expected, compressor.encode(integers));
        assertArrayEquals(
                new byte[] {
                        (byte)0b10000001, 0b00000000,
                        (byte)0b10000001, 0b00000000,
                        (byte)0b10000001, 0b00000000
                },
                compressor.encode(Arrays.asList(128, 2 * 128, 3 * 128))
        );
    }

    @Test
    public void test5() {
        Compressor compressor = new DeltaVarLenCompressor();
        for (int i = 0; i <= 127; i++)
            assertEquals(
                    Arrays.asList(i),
                    compressor.decode(new byte[] { (byte)i })
            );
    }

    @Test
    public void test6() {
        Compressor compressor = new DeltaVarLenCompressor();
        assertEquals(
                Arrays.asList(128),
                compressor.decode(new byte[] { (byte)0b10000001, 0b00000000 })
        );
        assertEquals(
                Arrays.asList(16384),
                compressor.decode(new byte[] { (byte)0b10000001, (byte)0b10000000, 0b00000000 })
        );
        assertEquals(
                Arrays.asList(Integer.MAX_VALUE),
                compressor.decode(new byte[] {
                        (byte)0b10000111, (byte)0b11111111, (byte)0b11111111,
                        (byte)0b11111111, 0b01111111
                })
        );
    }

    @Test
    public void test7() {
        Compressor compressor = new DeltaVarLenCompressor();
        assertEquals(
                Arrays.asList(1, 1, 1),
                compressor.decode(new byte[] { 1, 0, 0 })
        );
        assertEquals(
                Arrays.asList(1, 2, 3),
                compressor.decode(new byte[] { 1, 1, 1 })
        );
        byte[] code = new byte[128];
        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i <= 127; i++) {
            code[i] = (byte)i;
            expected.add(i * (i + 1) / 2);
        }
        assertEquals(expected, compressor.decode(code));
        assertEquals(
                Arrays.asList(128, 2 * 128, 3 * 128),
                compressor.decode(new byte[] {
                        (byte)0b10000001, 0b00000000,
                        (byte)0b10000001, 0b00000000,
                        (byte)0b10000001, 0b00000000
                })
        );
    }

    private void fullTest(List<Integer> ints, byte[] bytes) {
        assertArrayEquals(bytes, compressor.encode(ints)); // To test encode function
        assertEquals(ints, compressor.decode(bytes)); // To test decode function with offset 0 and full length of input
    }

    @Test
    public void test8() {
        fullTest(Arrays.asList(16384), new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000000});
        fullTest(Arrays.asList(16386), new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000010});
        fullTest(Arrays.asList(16383), new byte[]{(byte)0b11111111, (byte)0b01111111});
        fullTest(Arrays.asList(131), new byte[]{(byte)0b10000001, (byte)0b00000011});
        fullTest(Arrays.asList(128), new byte[]{(byte)0b10000001, (byte)0b00000000});
        fullTest(Arrays.asList(127), new byte[]{(byte)0b01111111});
        fullTest(Arrays.asList(3), new byte[]{(byte)0b00000011});
        fullTest(Arrays.asList(0), new byte[]{(byte)0b00000000});
    }

    @Test
    public void test9() {
        fullTest(Arrays.asList(3, 5, 8, 13, 20),
                new byte[]{(byte)0b00000011, (byte)0b00000010, (byte)0b00000011, (byte)0b00000101, (byte)0b00000111});
        fullTest(Arrays.asList(128, 16512, 16515, 16520, 16527),
                new byte[]{(byte)0b10000001, (byte)0b00000000, (byte)0b10000001, (byte)0b10000000, (byte)0b00000000, (byte)0b00000011, (byte)0b00000101, (byte)0b00000111});
        fullTest(Arrays.asList(128, 131, 136, 143, 16527),
                new byte[]{(byte)0b10000001, (byte)0b00000000, (byte)0b00000011, (byte)0b00000101, (byte)0b00000111, (byte)0b10000001, (byte)0b10000000, (byte)0b00000000});
        fullTest(Arrays.asList(16384, 32770, 49153),
                new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000000, (byte)0b10000001, (byte)0b10000000, (byte)0b00000010, (byte)0b11111111, (byte)0b01111111});
        fullTest(Arrays.asList(16386, 16513, 16516),
                new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000010, (byte)0b01111111, (byte)0b00000011});
        fullTest(Arrays.asList(0, 0, 0, 0),
                new byte[]{(byte)0b00000000, (byte)0b00000000, (byte)0b00000000, (byte)0b00000000});
    }

    @Test
    public void test10() {
        byte[] test = new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000010, (byte)0b01111111, (byte)0b00000011, (byte)0b10000001, (byte)0b00000011};
        List<Integer> result1 = compressor.decode(test, 0, 3);
        List<Integer> result2 = compressor.decode(test, 3, 1);
        List<Integer> result3 = compressor.decode(test, 3, 2);
        List<Integer> result4 = compressor.decode(test, 3, 4);
        List<Integer> expected1 = Arrays.asList(16386);
        List<Integer> expected2 = Arrays.asList(127);
        List<Integer> expected3 = Arrays.asList(127, 130);
        List<Integer> expected4 = Arrays.asList(127, 130, 261);
        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
        assertEquals(expected3, result3);
        assertEquals(expected4, result4);
    }

    @Test
    public void test11() {
        List<Integer> integers1 = Arrays.asList(1, 2, 3, 4, 5, 6);
        byte[] expected1 = {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
        assertArrayEquals(expected1, compressor.encode(integers1));
    }

    //Test encoding, after delta encoding they should be 128,256,512,1024,2048
    @Test
    public void test12() {
        List<Integer> integers2 = Arrays.asList(128, 384, 896, 1920, 3968);
        byte[] expected2 = {(byte) 0x81, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x88, (byte) 0x00,
                (byte) 0x90, (byte) 0x00};
        assertArrayEquals(expected2, compressor.encode(integers2));
    }

    @Test
    public void test13() {
        List<Integer> integers3 = Arrays.asList(Integer.MAX_VALUE);
        byte[] expected3 = {(byte) 0x87, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f};
        assertArrayEquals(expected3, compressor.encode(integers3));
    }

    @Test
    public void test14() {
        List<Integer> expected1 = Arrays.asList(1, 2, 3, 4, 5, 6);
        byte[] bytes1 = {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
        List<Integer> actual1 = compressor.decode(bytes1);
        assertEquals(expected1.size(), actual1.size());
        for (int i = 0; i < expected1.size(); i++) {
            assertEquals(expected1.get(i), actual1.get(i));
        }
    }

    @Test
    public void test15() {
        List<Integer> expected2 = Arrays.asList(128, 384, 896, 1920, 3968);
        byte[] bytes2 = {(byte) 0x81, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x88, (byte) 0x00,
                (byte) 0x90, (byte) 0x00};
        List<Integer> actual2 = compressor.decode(bytes2);
        assertEquals(expected2.size(), actual2.size());
        for (int i = 0; i < expected2.size(); i++) {
            assertEquals(expected2.get(i), actual2.get(i));
        }
    }

    @Test
    public void test16() {
        List<Integer> expected3 = Arrays.asList(Integer.MAX_VALUE);
        byte[] bytes3 = {(byte) 0x87, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f};
        List<Integer> actual3 = compressor.decode(bytes3);
        assertEquals(expected3.size(), actual3.size());
        for (int i = 0; i < expected3.size(); i++) {
            assertEquals(expected3.get(i), actual3.get(i));
        }
    }

    @Test
    public void test17() {
        byte[] bytes1 = {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
        List<Integer> actual1 = compressor.decode(bytes1,4,2);
        List<Integer> expected1 = Arrays.asList(1,2);
        assertEquals(expected1.size(),actual1.size());
        for (int i = 0; i < expected1.size(); i++) {
            assertEquals(expected1.get(i),actual1.get(i));
        }
    }

    @Test
    public void test18() {
        byte[] bytes2 = {(byte) 0x81, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x88, (byte) 0x00,
                (byte) 0x90, (byte) 0x00};
        List<Integer> actual2 = compressor.decode(bytes2,4,6);
        List<Integer> expected2 = Arrays.asList(512, 1536, 3584);
        assertEquals(expected2.size(),actual2.size());
        for (int i = 0; i < expected2.size(); i++) {
            assertEquals(expected2.get(i),actual2.get(i));
        }
    }

}
