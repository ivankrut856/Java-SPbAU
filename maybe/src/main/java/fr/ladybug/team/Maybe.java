package fr.ladybug.team;

import java.util.NoSuchElementException;
import java.util.function.Function;

public class Maybe<T> {
    private boolean isReal;
    private T value;

//    private final static Maybe<T> nothingObject = new Maybe<T>(null, false);

    private Maybe(T t, boolean isReal) {
        this.isReal = isReal;
        value = t;
    }

    public static <T> Maybe<T> just(T t) {
        return new Maybe<>(t, true);
    }

    public static <T> Maybe<T> nothing() {
//        return nothingObject;
        return new Maybe<>(null ,false);
    }

    public T get() {
        if (!isReal)
            throw new NoSuchElementException();
        return value;
    }

    public boolean isPresent() {
        return isReal;
    }

    public <U> Maybe<U> map(Function<? super T, ? extends U> mapper) {
        if (!isReal)
            return nothing();
        return just(mapper.apply(value));
    }
}
