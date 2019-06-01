package fr.ladybug.team.testdata;

import fr.ladybug.team.annotations.Before;

public class InvalidBeforeSignature {
    @Before
    public void before(String s) {

    }
}
