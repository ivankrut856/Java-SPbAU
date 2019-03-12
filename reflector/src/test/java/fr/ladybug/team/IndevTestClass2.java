package fr.ladybug.team;

import java.lang.reflect.Member;
import java.util.List;

public class IndevTestClass2<T extends String & Member> {
    public double name;

    class TemplatedOne<F extends String & Member> { }
}
