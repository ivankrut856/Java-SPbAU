package fr.ladybug.team;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectorTest {

    @Test
    void testIn7dev() {
//        Reflector.printStructure(IndevTestClass.class, "Test.java");
        Reflector.diffClasses(IndevTestClass.class, IndevTestClass2.class);
    }
}