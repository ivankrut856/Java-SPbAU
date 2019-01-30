package fr.ladybug.team;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class MaybeTest {

    @Test
    void testJustNoExcept() {
        Maybe<Integer> m = Maybe.just(4);
    }

    @Test
    void testNothingNoExcept() {
        Maybe<Integer> m = Maybe.nothing();
    }

    @Test
    void testGetExactValue() {
        Maybe<Integer> m = Maybe.just(4);
        assertEquals((Integer)4, m.get());

        Maybe<Integer> k = Maybe.just(null);
        assertNull(k.get());
    }

    @Test
    void testGetNothingCausesException() {
        Maybe<Integer> n = Maybe.nothing();
        assertThrows(NoSuchElementException.class, n::get);
    }

    @Test
    void testIsPresent() {
        Maybe<Integer> m = Maybe.just(4);
        Maybe<Integer> k = Maybe.just(null);
        Maybe<Integer> n = Maybe.nothing();

        assertTrue(m.isPresent());
        assertTrue(k.isPresent());
        assertFalse(n.isPresent());
    }

    private char f(int a) {
        return 'A';
    }

    @Test
    void testConstantMap() {
        Maybe<Integer> m = Maybe.just(4);
        Maybe<Integer> n = Maybe.nothing();
        Maybe<Character> fm = m.map(this::f);
        Maybe<Character> fn = n.map(this::f);
        assertEquals(fm.get(), (Character)'A');
        assertFalse(fn.isPresent());
    }

    int g(int x) {
        return x * x;
    }

    @Test void testConsoleFlow() throws IOException {
        BufferedReader fin = new BufferedReader(new FileReader("testFile.subl"));
        ArrayList<Maybe<Integer>> list = new ArrayList<>();
        String line = fin.readLine();
        while (line != null) {
            try {
                int currentNumber = Integer.parseInt(line);
                list.add(Maybe.just(currentNumber));
            }
            catch (NumberFormatException e) {
                list.add(Maybe.nothing());
            }

            line = fin.readLine();
        }

        for (Maybe<Integer> element : list ) {
            Maybe<Integer> mappedElement = element.map(this::g);
            if (mappedElement.isPresent()) {
                System.out.println(mappedElement.get());
            }
            else {
                System.out.println("nothing");
            }
        }


        fin.close();
    }
}