package fr.ladybug.team.classes;

public class DoubleNested {
    class Nested {
        class Hook {
            int kek() {
                return 5;
            }
        }
    }

    protected abstract class AbstractNested {
        String whoCaresCauseOnlyDeclarationsAvailable;
    }
}
