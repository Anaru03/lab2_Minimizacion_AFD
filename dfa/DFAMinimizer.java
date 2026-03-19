import java.util.*;

/**
 * Implementa el algoritmo de minimización de AFD
 * mediante refinamiento de particiones (algoritmo de Hopcroft).
 *
 * Pasos:
 *  1. Partición inicial: {estados finales} y {estados no finales}.
 *  2. Refinar particiones hasta que no haya cambios.
 *  3. Construir el AFD minimizado a partir de las particiones finales.
 */
public class DFAMinimizer {

    private final dfa original;

    public DFAMinimizer(dfa original) {
        this.original = original;
    }

    public dfa minimize() {

        Map<Integer, Map<Character, Integer>> transitions = original.getTransitions();
        Set<Integer> finalStates  = original.getFinalStates();
        int          initialState = original.getInitialState();

        // ── 1. Recolectar todos los estados y el alfabeto ──────────────────
        Set<Integer>   allStates = new HashSet<>(transitions.keySet());
        Set<Character> alphabet  = new HashSet<>();
        for (Map<Character, Integer> row : transitions.values())
            alphabet.addAll(row.keySet());

        // Aseguramos que los estados destino también estén incluidos
        for (Map<Character, Integer> row : transitions.values())
            allStates.addAll(row.values());

        // ── 2. Partición inicial ───────────────────────────────────────────
        Set<Integer> nonFinal = new HashSet<>(allStates);
        nonFinal.removeAll(finalStates);

        List<Set<Integer>> partitions = new ArrayList<>();
        if (!finalStates.isEmpty())   partitions.add(new HashSet<>(finalStates));
        if (!nonFinal.isEmpty())      partitions.add(nonFinal);

        // ── 3. Refinamiento iterativo ──────────────────────────────────────
        boolean changed = true;
        while (changed) {
            changed = false;
            List<Set<Integer>> newPartitions = new ArrayList<>();

            for (Set<Integer> group : partitions) {
                List<Set<Integer>> split = splitGroup(group, partitions, alphabet, transitions);
                newPartitions.addAll(split);
                if (split.size() > 1) changed = true;
            }
            partitions = newPartitions;
        }

        // ── 4. Asignar IDs nuevos a cada partición ─────────────────────────
        // El grupo que contiene el estado inicial recibe ID 0
        Map<Integer, Integer> stateToGroup = new HashMap<>();
        int groupId = 0;

        // Primero encontramos el grupo del estado inicial para darle ID 0
        int initialGroupIndex = -1;
        for (int i = 0; i < partitions.size(); i++) {
            if (partitions.get(i).contains(initialState)) {
                initialGroupIndex = i;
                break;
            }
        }

        // Reasignamos para que el grupo inicial sea el índice 0
        if (initialGroupIndex != 0) {
            Collections.swap(partitions, 0, initialGroupIndex);
        }

        for (Set<Integer> group : partitions) {
            for (int state : group)
                stateToGroup.put(state, groupId);
            groupId++;
        }

        // ── 5. Construir transiciones del AFD minimizado ───────────────────
        Map<Integer, Map<Character, Integer>> minTransitions = new HashMap<>();
        Set<Integer> minFinalStates = new HashSet<>();

        for (int g = 0; g < partitions.size(); g++) {
            Set<Integer> group = partitions.get(g);

            // Representante del grupo (cualquier estado del grupo)
            int representative = group.iterator().next();

            minTransitions.put(g, new HashMap<>());

            if (transitions.containsKey(representative)) {
                for (char c : alphabet) {
                    Integer dest = transitions.get(representative).get(c);
                    if (dest != null) {
                        minTransitions.get(g).put(c, stateToGroup.get(dest));
                    }
                }
            }

            // Es final si algún estado del grupo es final
            for (int s : group) {
                if (finalStates.contains(s)) {
                    minFinalStates.add(g);
                    break;
                }
            }
        }

        return new dfa(0, minFinalStates, minTransitions);
    }

    /**
     * Divide un grupo en subgrupos según si los estados
     * se comportan igual con respecto a todas las particiones actuales.
     */
    private List<Set<Integer>> splitGroup(
            Set<Integer>                         group,
            List<Set<Integer>>                   partitions,
            Set<Character>                       alphabet,
            Map<Integer, Map<Character, Integer>> transitions) {

        // Firma: para cada símbolo, en qué partición cae el destino (-1 = sin transición)
        Map<Map<Character, Integer>, Set<Integer>> signatureMap = new LinkedHashMap<>();

        for (int state : group) {
            Map<Character, Integer> signature = new HashMap<>();

            for (char c : alphabet) {
                Integer dest = null;
                if (transitions.containsKey(state))
                    dest = transitions.get(state).get(c);

                if (dest == null) {
                    signature.put(c, -1);
                } else {
                    signature.put(c, getPartitionIndex(dest, partitions));
                }
            }

            signatureMap.computeIfAbsent(signature, k -> new HashSet<>()).add(state);
        }

        return new ArrayList<>(signatureMap.values());
    }

    /** Devuelve el índice de la partición a la que pertenece un estado. */
    private int getPartitionIndex(int state, List<Set<Integer>> partitions) {
        for (int i = 0; i < partitions.size(); i++)
            if (partitions.get(i).contains(state)) return i;
        return -1;
    }
}
