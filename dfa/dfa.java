import java.util.*;

/**
 * Representa el Autómata Finito Determinista.
 */
public class dfa {

    private final int initialState;
    private final Set<Integer> finalStates;
    private final Map<Integer, Map<Character, Integer>> transitions;

    public dfa(int initialState,
               Set<Integer> finalStates,
               Map<Integer, Map<Character, Integer>> transitions) {
        this.initialState = initialState;
        this.finalStates  = finalStates;
        this.transitions  = transitions;
    }

    // ── Getters (necesarios para el minimizador) ───────────────────────────
    public int getInitialState()                                  { return initialState; }
    public Set<Integer> getFinalStates()                          { return finalStates; }
    public Map<Integer, Map<Character, Integer>> getTransitions() { return transitions; }

    // ── Estadísticas ───────────────────────────────────────────────────────

    /** Número de estados del autómata. */
    public int getStateCount() {
        Set<Integer> all = new HashSet<>(transitions.keySet());
        for (Map<Character, Integer> row : transitions.values())
            all.addAll(row.values());
        return all.size();
    }

    /** Número total de transiciones del autómata. */
    public int getTransitionCount() {
        int count = 0;
        for (Map<Character, Integer> row : transitions.values())
            count += row.size();
        return count;
    }

    // ── Impresión ──────────────────────────────────────────────────────────

    /** Imprime la tabla de transición con encabezado personalizable. */
    public void printTransitionTable(String title) {

        // Recolectar alfabeto ordenado
        Set<Character> alphabetSet = new TreeSet<>();
        for (Map<Character, Integer> row : transitions.values())
            alphabetSet.addAll(row.keySet());
        List<Character> alphabet = new ArrayList<>(alphabetSet);

        // Ordenar estados
        List<Integer> states = new ArrayList<>(transitions.keySet());
        Collections.sort(states);

        System.out.println("\n=== " + title + " ===");

        // Encabezado
        System.out.printf("%-12s", "Estado");
        for (char c : alphabet) System.out.printf("%-10s", c);
        System.out.println();

        System.out.println("-".repeat(12 + 10 * alphabet.size()));

        // Filas
        for (int state : states) {
            String label = "q" + state;
            if (state == initialState) label = "->" + label;
            if (finalStates.contains(state)) label += "*";
            System.out.printf("%-12s", label);

            for (char c : alphabet) {
                Integer dest = transitions.get(state).get(c);
                System.out.printf("%-10s", dest == null ? "-" : "q" + dest);
            }
            System.out.println();
        }
    }

    /** Imprime la tabla de transición con el título por defecto. */
    public void printTransitionTable() {
        printTransitionTable("TABLA DE TRANSICIÓN");
    }

    // ── Simulación ─────────────────────────────────────────────────────────

    /**
     * Simula la cadena y devuelve true si es aceptada.
     * Explica paso a paso el motivo del rechazo si corresponde.
     */
    public boolean simulate(String input) {
        int current = initialState;

        for (char c : input.toCharArray()) {

            if (!transitions.containsKey(current) ||
                !transitions.get(current).containsKey(c)) {
                System.out.println("  [Rechazo] No existe transición desde q"
                        + current + " con símbolo '" + c + "'.");
                return false;
            }

            int next = transitions.get(current).get(c);
            System.out.println("  q" + current + " --" + c + "--> q" + next);
            current = next;
        }

        if (finalStates.contains(current)) {
            System.out.println("  [Fin] Estado q" + current + " es de aceptación.");
            return true;
        } else {
            System.out.println("  [Rechazo] Estado q" + current
                    + " no es de aceptación.");
            return false;
        }
    }
}
