package fr.ladybug.team.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation requests Tester to run the method and provide test results */
@Target(value= ElementType.METHOD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface Test {
    /**
     * Gives the array of the Classes of the exceptions one of which must be thrown during method invocation.
     * Or empty array if no such exceptions
     * @return the Classes of the exception to be thrown or empty array
     */
    Class<?>[] expected() default {};

    /**
     * Returns empty array if the method must not be ignored or single element array with a message with the reason for ignore
     * @return single element array with a message with the reason for ignore of empty array if no such reason
     */
    String[] ignore() default {};
}
