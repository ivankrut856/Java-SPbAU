package fr.ladybug.team;

import java.util.*;

public class Injector {

    private static HashMap<String, State> state = new HashMap<>();
    private static HashMap<String, Object> instances = new HashMap<>();
    public static Object initialize(String rootClassName, List<Class<?>> availableDependencies)
            throws Exception {
        for (int i = 0; i < availableDependencies.size(); i++) {
            instantiate(availableDependencies.get(i), availableDependencies);
        }
        if (!availableDependencies.contains(rootClassName)) {
            availableDependencies = new ArrayList<Class<?>>(availableDependencies);
            availableDependencies.add(Class.forName(rootClassName));
        }
        instantiate(Class.forName(rootClassName), availableDependencies);
        return instances.get(rootClassName);
    }

    private static void instantiate(Class<?> clazz, List<Class<?>> dependencies) throws Exception {
        if (!dependencies.contains(clazz)) {
            state.clear();
            instances.clear();
            throw new ImplementationNotFoundException();
        }

        var currentState = state.getOrDefault(clazz.getName(), State.OPEN);

        if (currentState == State.IN_PROGRESS) {
            state.clear();
            instances.clear();
            throw new InjectionCycleException();
        }
        if (currentState == State.DONE) {
            return;
        }

        state.put(clazz.getName(), State.IN_PROGRESS);

        for (var currentDependency : clazz.getConstructors()[0].getParameterTypes()) {
            int currentMatched = 0;
            for (var currentAvailableDependency : dependencies) {
                if (currentDependency.isAssignableFrom(currentAvailableDependency)) {
                    instantiate(currentAvailableDependency, dependencies);
                    currentMatched++;
                }
                if (currentMatched > 1) {
                    state.clear();
                    instances.clear();
                    throw new AmbiguousImplementationException();
                }
            }
            if (currentMatched == 0) {
                state.clear();
                instances.clear();
                throw new ImplementationNotFoundException();
            }
        }

        List<Object> matched = new ArrayList<>();
        for (var currentDependency : clazz.getConstructors()[0].getParameterTypes()) {
            matched.add(instances.get(currentDependency.getName()));
        }
        instances.put(clazz.getName(), clazz.getConstructors()[0].newInstance(matched.toArray()));

        state.put(clazz.getName(), State.DONE);
    }

    private enum State {
        OPEN,
        IN_PROGRESS,
        DONE,
    }
}
