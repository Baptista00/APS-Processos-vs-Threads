import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main.java
 * Ponto de entrada principal do benchmark.
 *
 * Executa automaticamente:
 *   1. Benchmark da Versão B (Threads)   com 2, 4 e 8 threads
 *   2. Benchmark da Versão A (Processos) com 2, 4 e 8 processos
 *   3. Validação cruzada: verifica que os dois resultados são iguais
 *   4. Impressão da tabela comparativa de tempos
 *
 * Configurações ajustáveis:
 *   N_MATRIZ   - dimensão da matriz (mínimo 1000)
 *   REPETICOES - número total de execuções por cenário (mínimo 6; a 1ª é descartada)
 *   UNIDADES   - quantidades de processos/threads a testar
 */
public class Main {

    // =====================================================================
    //  CONFIGURAÇÕES DO BENCHMARK
    // =====================================================================

    /** Dimensão da matriz quadrada. Altere para 1500 ou 2000 se necessário. */
    static final int N_MATRIZ = 1000;

    /**
     * Total de repetições por cenário.
     * A primeira é descartada (aquecimento da JVM).
     * As demais são usadas para calcular a média.
     */
    static final int REPETICOES = 6; // 1 aquecimento + 5 válidas

    /** Número de unidades (processos/threads) a testar. */
    static final int[] UNIDADES = {2, 4, 8};

    // =====================================================================

    public static void main(String[] args) throws Exception {

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  APS – Benchmarking: Processos vs Threads – Java ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println("Tamanho da matriz: " + N_MATRIZ + "×" + N_MATRIZ);
        System.out.println("Repetições por cenário: " + REPETICOES +
                           " (1 aquecimento + " + (REPETICOES - 1) + " válidas)\n");

        // ==== Geração das matrizes (seed fixa para reprodutibilidade) ====
        System.out.println("Gerando matrizes...");
        double[][] A = MatrizUtils.gerarMatriz(N_MATRIZ, 42L);
        double[][] B = MatrizUtils.gerarMatriz(N_MATRIZ, 123L);
        System.out.println("Matrizes geradas.\n");

        // ==== Salva matrizes em arquivos temporários (usados pelos processos) ====
        File tmpA = File.createTempFile("aps_matrizA_", ".bin");
        File tmpB = File.createTempFile("aps_matrizB_", ".bin");
        tmpA.deleteOnExit();
        tmpB.deleteOnExit();
        MatrizUtils.salvarMatriz(A, tmpA.getAbsolutePath());
        MatrizUtils.salvarMatriz(B, tmpB.getAbsolutePath());
        System.out.println("Arquivos temporários de matrizes gravados.");
        System.out.println("  A: " + tmpA.getAbsolutePath());
        System.out.println("  B: " + tmpB.getAbsolutePath() + "\n");

        // ==== Arrays para armazenar as médias (para a tabela final) ====
        double[] mediasThread   = new double[UNIDADES.length];
        double[] mediasProcesso = new double[UNIDADES.length];

        // ================================================================
        //  VERSÃO B – THREADS
        // ================================================================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  VERSÃO B – THREADS (ExecutorService)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        double[][] ultimoResultadoThread = null;

        for (int idx = 0; idx < UNIDADES.length; idx++) {
            int numThreads = UNIDADES[idx];
            List<Long> tempos = new ArrayList<>();
            double[][] resultadoAtual = null;

            System.out.println("[Threads = " + numThreads + "]");

            for (int rep = 0; rep < REPETICOES; rep++) {
                long inicio = System.nanoTime();
                resultadoAtual = executarComThreads(A, B, numThreads, N_MATRIZ);
                long fim = System.nanoTime();
                long ms = (fim - inicio) / 1_000_000L;

                if (rep == 0) {
                    System.out.println("  Rep 0 (aquecimento): " + ms + " ms [descartada]");
                } else {
                    tempos.add(ms);
                    System.out.println("  Rep " + rep + ": " + ms + " ms");
                }
            }

            double media = tempos.stream().mapToLong(Long::longValue).average().orElse(0);
            mediasThread[idx] = media;
            System.out.printf("  → MÉDIA (reps 1-%d): %.1f ms%n%n", REPETICOES - 1, media);

            if (numThreads == 4) ultimoResultadoThread = resultadoAtual;
        }

        // ================================================================
        //  VERSÃO A – PROCESSOS (múltiplas JVMs)
        // ================================================================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  VERSÃO A – PROCESSOS (múltiplas JVMs)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        double[][] ultimoResultadoProcesso = null;

        for (int idx = 0; idx < UNIDADES.length; idx++) {
            int numProcessos = UNIDADES[idx];
            List<Long> tempos = new ArrayList<>();
            double[][] resultadoAtual = null;

            System.out.println("[Processos = " + numProcessos + "]");

            for (int rep = 0; rep < REPETICOES; rep++) {
                long inicio = System.nanoTime();
                resultadoAtual = executarComProcessos(tmpA, tmpB, numProcessos, N_MATRIZ);
                long fim = System.nanoTime();
                long ms = (fim - inicio) / 1_000_000L;

                if (rep == 0) {
                    System.out.println("  Rep 0 (aquecimento): " + ms + " ms [descartada]");
                } else {
                    tempos.add(ms);
                    System.out.println("  Rep " + rep + ": " + ms + " ms");
                }
            }

            double media = tempos.stream().mapToLong(Long::longValue).average().orElse(0);
            mediasProcesso[idx] = media;
            System.out.printf("  → MÉDIA (reps 1-%d): %.1f ms%n%n", REPETICOES - 1, media);

            if (numProcessos == 4) ultimoResultadoProcesso = resultadoAtual;
        }

        // ================================================================
        //  VALIDAÇÃO CRUZADA
        // ================================================================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  VALIDAÇÃO DO RESULTADO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        if (ultimoResultadoThread != null && ultimoResultadoProcesso != null) {
            // Tolerância de 1e-6 para acomodar diferenças de ordem das operações FP
            boolean valido = MatrizUtils.validarResultados(
                    ultimoResultadoThread, ultimoResultadoProcesso, 1e-6);
            System.out.println("Resultado Thread (4 threads) == Resultado Processo (4 processos): "
                    + (valido ? "✓ VÁLIDO" : "✗ DIVERGÊNCIA DETECTADA"));
            if (!valido)
                System.out.println("  ATENÇÃO: verifique a implementação!");
        }

        // ================================================================
        //  TABELA COMPARATIVA FINAL
        // ================================================================
        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("%-12s | %-20s | %-20s%n", "Unidades", "Threads (ms)", "Processos (ms)");
        System.out.println("─────────────┼──────────────────────┼────────────────────");
        for (int i = 0; i < UNIDADES.length; i++) {
            System.out.printf("%-12d | %-20.1f | %-20.1f%n",
                    UNIDADES[i], mediasThread[i], mediasProcesso[i]);
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Limpeza dos arquivos temporários de matrizes
        tmpA.delete();
        tmpB.delete();
    }

    // ================================================================
    //  MÉTODO: VERSÃO B – EXECUÇÃO COM THREADS
    // ================================================================

    /**
     * Realiza a multiplicação A×B utilizando 'numThreads' threads.
     * Divide as linhas da matriz igualmente entre as threads.
     * Todas as threads compartilham as referências A, B e resultado
     * no heap da mesma JVM.
     *
     * @return Matriz resultado n×n
     */
    static double[][] executarComThreads(double[][] A, double[][] B,
                                          int numThreads, int n)
            throws InterruptedException, ExecutionException {

        double[][] resultado = new double[n][n];
        int linhasPorThread = n / numThreads;

        // ExecutorService gerencia o pool de threads
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures  = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            int startRow = i * linhasPorThread;
            // A última thread absorve qualquer linha restante (n não divisível)
            int endRow = (i == numThreads - 1) ? n : startRow + linhasPorThread;

            futures.add(executor.submit(
                    new MultiplicadorRunnable(A, B, resultado, startRow, endRow)));
        }

        // Aguarda todas as threads finalizarem
        for (Future<?> f : futures)
            f.get();

        executor.shutdown();
        return resultado;
    }

    // ================================================================
    //  MÉTODO: VERSÃO A – EXECUÇÃO COM PROCESSOS (múltiplas JVMs)
    // ================================================================

    /**
     * Realiza a multiplicação A×B usando 'numProcessos' JVMs separadas.
     * Cada processo lê as matrizes dos arquivos temporários, calcula
     * seu intervalo de linhas e grava o resultado parcial em outro
     * arquivo temporário. O processo principal aguarda todos e monta
     * a matriz resultado final.
     *
     * Estratégia de comunicação adotada: ARQUIVOS BINÁRIOS TEMPORÁRIOS
     *   - Evita o custo de serialização texto de uma matriz 1000×1000
     *   - Não limita o tamanho dos dados (como stdin/argumentos fariam)
     *   - Permite leitura paralela pelos processos filhos
     *
     * @return Matriz resultado n×n
     */
    static double[][] executarComProcessos(File tmpA, File tmpB,
                                            int numProcessos, int n)
            throws Exception {

        int linhasPorProcesso = n / numProcessos;

        List<File>    arquivosResultado = new ArrayList<>();
        List<Process> processos         = new ArrayList<>();

        // Classpath da JVM atual (inclui os .class compilados)
        String classpath = System.getProperty("java.class.path");

        // --- Lança os processos filhos ---
        for (int i = 0; i < numProcessos; i++) {
            int startRow = i * linhasPorProcesso;
            int endRow   = (i == numProcessos - 1) ? n : startRow + linhasPorProcesso;

            // Arquivo temporário para o resultado parcial deste processo
            File tmpResultado = File.createTempFile(
                    "aps_res_" + i + "_", ".bin");
            tmpResultado.deleteOnExit();
            arquivosResultado.add(tmpResultado);

            // Comando para iniciar nova JVM executando ProcessoWorker
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-cp", classpath,
                "ProcessoWorker",
                tmpA.getAbsolutePath(),
                tmpB.getAbsolutePath(),
                tmpResultado.getAbsolutePath(),
                String.valueOf(startRow),
                String.valueOf(endRow)
            );

            // Redireciona stderr do filho para o stderr do processo principal
            pb.redirectErrorStream(true);
            processos.add(pb.start());
        }

        // --- Aguarda todos os processos terminarem ---
        for (Process p : processos) {
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                // Imprime possível mensagem de erro do processo filho
                String saida = new String(p.getInputStream().readAllBytes());
                System.err.println("Processo filho terminou com código " + exitCode);
                System.err.println(saida);
            }
        }

        // --- Monta a matriz resultado a partir dos parciais ---
        double[][] resultado = new double[n][n];
        for (File f : arquivosResultado) {
            MatrizUtils.carregarParcial(resultado, f.getAbsolutePath());
            f.delete(); // Limpa o arquivo parcial
        }

        return resultado;
    }
}
