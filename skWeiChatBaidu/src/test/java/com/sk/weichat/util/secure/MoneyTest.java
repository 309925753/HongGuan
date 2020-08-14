package com.sk.weichat.util.secure;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MoneyTest {

    @Test
    public void fromCent() {
        assertEquals("1.23", Money.fromCent(123));
        assertEquals("1.23", Money.fromCent("123"));
        assertEquals("1", Money.fromCent(100));
        assertEquals("21474836.47", Money.fromCent(Integer.MAX_VALUE));
    }
}