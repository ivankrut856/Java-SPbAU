package fr.ladybug.team.testdata;

import fr.ladybug.team.annotations.After;

public class InvalidAfterSignature {
    @After
    public void after(String s) {

    }
}
