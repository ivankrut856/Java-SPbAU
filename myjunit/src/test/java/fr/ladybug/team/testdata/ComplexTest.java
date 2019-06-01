package fr.ladybug.team.testdata;

import fr.ladybug.team.annotations.*;

public class ComplexTest {

    @BeforeClass
    public void sayHello() {

    }

    @AfterClass
    public void sayGoodbye() {

    }

    @Before
    @After
    public void smallTalk() {

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
