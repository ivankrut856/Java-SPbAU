package fr.ladybug.team;

import java.lang.reflect.Member;
import java.util.List;

public class IndevTestClass<T extends String & Member> {
    public int name;
    static private String data;
    protected T what;

    void justMethod(int v0, java.lang.String v1, T v2) {
        return;
    }
    public <T extends java.lang.Object> int whatStuffAreYou(T v3) {
        return (new int[1])[0];
    }
    public static <E extends java.lang.Object> java.lang.String anotherMethod(E v4) {
        return null;
    }
    public static void Main(String[] args, List<? super String> list) {

    }

    class InnerOne {
        int kek;
    }

    static class NestedOne {
        int kok;
    }

    interface wierdOne<F> {
        int wow();
    }

    class TemplatedOne<F extends String & Member> { }
}
