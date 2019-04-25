package com.jw.autonest;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testRandom(){
        Random r = new Random();
        int delay = (r.nextInt(5)+3)*1000 + r.nextInt(10)*100 + r.nextInt(10)*10 + r.nextInt(10);
        System.out.println(delay);
    }
}