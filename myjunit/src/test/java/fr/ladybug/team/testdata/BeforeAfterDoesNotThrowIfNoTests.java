package fr.ladybug.team.testdata;

import fr.ladybug.team.annotations.After;
import fr.ladybug.team.annotations.Before;

public class BeforeAfterDoesNotThrowIfNoTests {

    @Before
    public void before() {
        throw new RuntimeException("Before thrown an exception");
    }

    @After
    public void after() {
        throw new RuntimeException("After thrown an exception");
    }
}
