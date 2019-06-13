package fr.ladybug.team.testdata;

import fr.ladybug.team.annotations.AfterClass;

public class InvalidAfterClassSignature {
    @AfterClass
    public void afterClass(String s) {

    }
}
