package fr.ladybug.team.testdata;

import fr.ladybug.team.annotations.*;

public class ComplexTest {

    @BeforeClass
    public static void sayHello() {

    }

    @AfterClass
    public static void sayGoodbye() {

    }

    @Before
    public void smallTalk() {

    }

    @After
    public void smallTalkAgain() {

    }

    @Test
    public void simpleSuccess() {

    }

    @Test
    public void simpleFail() {
        throw new RuntimeException("Not this time");
    }

    @Test(expected = RuntimeException.class)
    public void exceptiveSuccess() {
        throw new RuntimeException("This time");
    }

    @Test(expected = RuntimeException.class)
    public void exceptiveFail() {

    }

    @Test(ignore = "Not this time")
    public void disabledGuy() {

    }
}
