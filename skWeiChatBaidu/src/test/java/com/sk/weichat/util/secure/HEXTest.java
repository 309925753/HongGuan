package com.sk.weichat.util.secure;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HEXTest {
    private byte[] content = "123456".getBytes();
    private String encoded = "313233343536";

    @Test
    public void encode() {
        assertEquals(encoded, HEX.encode(content));
    }

    @Test
    public void decode() {
        assertArrayEquals(content, HEX.decode(encoded));
    }
}