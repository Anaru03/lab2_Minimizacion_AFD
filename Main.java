import java.util.Scanner;

/**
 * Programa principal con menú interactivo.
 * Extiende el Lab 1 con minimización de AFD.
 */
public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        dfa automata          = null;   // AFD método directo
        dfa minimizedAutomata = null;   // AFD minimizado

        boolean running = true;

        while (running) {

            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║         MENÚ PRINCIPAL               ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1. Ingresar nueva expresión regular  ║");
            System.out.println("║  2. Minimizar AFD                     ║");
            System.out.println("║  3. Comparar AFD original vs mínimo   ║");
            System.out.println("║  4. Evaluar cadena (AFD minimizado)   ║");
            System.out.println("║  5. Salir                             ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Seleccione una opción: ");

            String option = scanner.nextLine().trim();

            switch (option) {

                // ── Opción 1: construir AFD método directo ─────────────────
                case "1" -> {
                    System.out.print("Ingrese expresión regular: ");
                    String regex = scanner.nextLine().trim();

                    try {
                        RegexParser parser = new RegexParser(regex);
                        Node root = parser.parse();

                        SyntaxTreeBuilder builder =
                                new SyntaxTreeBuilder(root, parser.getPositionCount());
                        builder.build();

                        Builder dfaBuilder = new Builder(builder);
                        automata = dfaBuilder.build();
                        minimizedAutomata = null;   // resetear si había uno previo

                        automata.printTransitionTable("AFD MÉTODO DIRECTO");

                        System.out.println("\nEstados    : " + automata.getStateCount());
                        System.out.println("Transiciones: " + automata.getTransitionCount());
                        System.out.println("\n✔ AFD construido correctamente.");

                    } catch (Exception e) {
                        System.out.println("✘ Error en la expresión regular: " + e.getMessage());
                    }
                }

                // ── Opción 2: minimizar ────────────────────────────────────
                case "2" -> {
                    if (automata == null) {
                        System.out.println("✘ Primero debe ingresar una expresión regular (opción 1).");
                    } else {
                        DFAMinimizer minimizer = new DFAMinimizer(automata);
                        minimizedAutomata = minimizer.minimize();

                        minimizedAutomata.printTransitionTable("AFD MINIMIZADO");

                        System.out.println("\nEstados    : " + minimizedAutomata.getStateCount());
                        System.out.println("Transiciones: " + minimizedAutomata.getTransitionCount());
                        System.out.println("\n✔ Minimización completada.");
                    }
                }

                // ── Opción 3: comparar ─────────────────────────────────────
                case "3" -> {
                    if (automata == null) {
                        System.out.println("✘ Primero construya el AFD (opción 1).");
                    } else if (minimizedAutomata == null) {
                        System.out.println("✘ Primero minimice el AFD (opción 2).");
                    } else {
                        System.out.println("\n╔══════════════════════════════════════════╗");
                        System.out.println("║         COMPARACIÓN DE AUTÓMATAS         ║");
                        System.out.println("╠══════════════╦══════════════╦════════════╣");
                        System.out.printf( "║ %-12s ║ %-12s ║ %-10s ║%n",
                                "Métrica", "Directo", "Minimizado");
                        System.out.println("╠══════════════╬══════════════╬════════════╣");
                        System.out.printf( "║ %-12s ║ %-12d ║ %-10d ║%n",
                                "Estados",
                                automata.getStateCount(),
                                minimizedAutomata.getStateCount());
                        System.out.printf( "║ %-12s ║ %-12d ║ %-10d ║%n",
                                "Transiciones",
                                automata.getTransitionCount(),
                                minimizedAutomata.getTransitionCount());
                        System.out.println("╚══════════════╩══════════════╩════════════╝");

                        int diffStates = automata.getStateCount()
                                       - minimizedAutomata.getStateCount();
                        if (diffStates == 0)
                            System.out.println("\n→ El AFD ya era mínimo. No se redujeron estados.");
                        else
                            System.out.println("\n→ Se eliminaron " + diffStates + " estado(s).");
                    }
                }

                // ── Opción 4: evaluar cadena con AFD minimizado ────────────
                case "4" -> {
                    if (minimizedAutomata == null) {
                        System.out.println("✘ Primero minimice el AFD (opción 2).");
                    } else {
                        System.out.print("Ingrese cadena a evaluar: ");
                        String input = scanner.nextLine();

                        System.out.println("\nSimulación paso a paso:");
                        if (minimizedAutomata.simulate(input))
                            System.out.println("\n✔ CADENA ACEPTADA");
                        else
                            System.out.println("\n✘ CADENA RECHAZADA");
                    }
                }

                // ── Opción 5: salir ────────────────────────────────────────
                case "5" -> {
                    running = false;
                    System.out.println("Saliendo del programa...");
                }

                default -> System.out.println("✘ Opción inválida. Elija entre 1 y 5.");
            }
        }

        scanner.close();
    }
}
