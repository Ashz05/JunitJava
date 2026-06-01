package org.ThreadCheck;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestThread {
    ThreadTest object;

    @BeforeAll static void beforeAll(){
        System.out.println("Starting Test Cases!");
    }
    @BeforeEach void init(){
        object = new ThreadTest(100);
        System.out.println("Thread is Started!");
    }

    @Test void TestThread() throws InterruptedException{
        int result = object.ThreadWork();
        int expected = 200;

        assertEquals(expected,result);
    }

    @AfterAll static void afterAll(){
        System.out.println("EndingTest Cases!");
    }

}
