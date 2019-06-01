package fr.ladybug.team.testdata;

import fr.ladybug.team.annotations.BeforeClass;

public class InvalidBeforeClassSignature {
    @BeforeClass
    public void beforeClass(String s) {

    }
}
