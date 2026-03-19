import java.util.*;

/**
 * Construye el AFD a partir de followpos.
 */
public class Builder {

    private final SyntaxTreeBuilder treeBuilder;

    public Builder(SyntaxTreeBuilder builder) {
        this.treeBuilder = builder;
    }

    public dfa build() {

        Map<Integer, Set<Integer>> followpos = treeBuilder.getFollowpos();
        Map<Integer, Character> positionSymbols = treeBuilder.getPositionSymbols();
        int endMarkerPos = treeBuilder.getEndMarkerPosition();

        Set<Character> alphabet = new HashSet<>(positionSymbols.values());
        alphabet.remove('#');

        Map<Set<Integer>, Integer> stateIds = new HashMap<>();
        Map<Integer, Map<Character, Integer>> transitions = new HashMap<>();
        Set<Integer> finalStates = new HashSet<>();

        Queue<Set<Integer>> queue = new LinkedList<>();

        Set<Integer> startState = treeBuilder.getInitialState();
        stateIds.put(startState, 0);
        queue.add(startState);

        int stateCounter = 1;

        while (!queue.isEmpty()) {
            Set<Integer> current = queue.poll();
            int currentId = stateIds.get(current);

            transitions.put(currentId, new HashMap<>());

            for (char symbol : alphabet) {

                Set<Integer> nextState = new HashSet<>();

                for (int pos : current) {
                    if (positionSymbols.get(pos) == symbol) {
                        nextState.addAll(followpos.get(pos));
                    }
                }

                if (!nextState.isEmpty()) {

                    if (!stateIds.containsKey(nextState)) {
                        stateIds.put(nextState, stateCounter++);
                        queue.add(nextState);
                    }

                    transitions.get(currentId)
                               .put(symbol, stateIds.get(nextState));
                }
            }

            if (current.contains(endMarkerPos))
                finalStates.add(currentId);
        }

        return new dfa(0, finalStates, transitions);
    }
}
