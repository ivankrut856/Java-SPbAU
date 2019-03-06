package fr.ladybug.team;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;


public class InjectorTest {
    @Test
    void testEmptyClass() {
        assertDoesNotThrow(() -> Injector.initialize(TestClass1.class.getName(), Collections.emptyList()));
    }

    @Test
    void testSingleDepsClass() {
        assertDoesNotThrow(() -> Injector.initialize(TestClass2.class.getName(), Arrays.asList(TestClass1.class)));
    }

    @Test
    void testSingeInterfaceDepsClass() {
        assertDoesNotThrow(() -> Injector.initialize(TestClass4.class.getName(), Arrays.asList(TestClass1.class, TestClass2.class, TestClass3.class)));
    }

    @Test
    void testDepNotInTheList() {
        assertThrows(ImplementationNotFoundException.class, () -> {
            Injector.initialize(TestClass4.class.getName(), Collections.emptyList());
        });
    }

    @Test
    void testDepsCycle() {
        assertThrows(InjectionCycleException.class, () -> {
            Injector.initialize(TestClass5.class.getName(), Arrays.asList(TestClass5.class, TestClass6.class));
        });
    }

    @Test
    void testMultipleDeps() {
        assertThrows(AmbiguousImplementationException.class, () -> {
            Injector.initialize(TestClass4.class.getName(), Arrays.asList(TestClass3.class, TestClass7.class));
        });
    }
}