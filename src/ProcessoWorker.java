/**
 * ProcessoWorker.java
 * Versão A – Processo: programa executado em uma JVM separada.
 *
 * Este programa NÃO deve ser executado diretamente pelo usuário.
 * Ele é instanciado pelo Main.java via ProcessBuilder, recebendo
 * por argumentos de linha de comando:
 *
 *   args[0] = caminho do arquivo binário da Matriz A
 *   args[1] = caminho do arquivo binário da Matriz B
 *   args[2] = caminho do arquivo de resultado parcial (saída)
 *   args[3] = startRow (linha inicial, inclusiva)
 *   args[4] = endRow   (linha final, exclusiva)
 *
 * Fluxo de execução:
 *   1. Lê as matrizes A e B dos arquivos binários temporários
 *   2. Multiplica as linhas [startRow, endRow) de A × B
 *   3. Grava o resultado parcial no arquivo de saída
 *
 * Cada instância deste processo possui seu próprio espaço de memória
 * (isolamento total entre processos). Não há memória compartilhada.
 */
public class ProcessoWorker {

    public static void main(String[] args) throws Exception {

        // --- Leitura dos argumentos ---
        if (args.length < 5) {
            System.err.println("Uso: ProcessoWorker <arqA> <arqB> <arqResultado> <startRow> <endRow>");
            System.exit(1);
        }

        String arquivoA        = args[0];
        String arquivoB        = args[1];
        String arquivoResultado = args[2];
        int startRow           = Integer.parseInt(args[3]);
        int endRow             = Integer.parseInt(args[4]);

        // --- Leitura das matrizes dos arquivos binários ---
        // (Cada processo lê sua própria cópia das matrizes da memória secundária)
        double[][] A = MatrizUtils.carregarMatriz(arquivoA);
        double[][] B = MatrizUtils.carregarMatriz(arquivoB);
        int n = A.length;

        // --- Cálculo das linhas designadas ---
        // Cria a matriz resultado completa (somente as linhas [startRow, endRow)
        // serão preenchidas e salvas)
        double[][] resultado = new double[n][n];

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < n; j++) {
                double soma = 0.0;
                for (int k = 0; k < n; k++) {
                    soma += A[i][k] * B[k][j];
                }
                resultado[i][j] = soma;
            }
        }

        // --- Gravação do resultado parcial ---
        MatrizUtils.salvarParcial(resultado, startRow, endRow, arquivoResultado);
    }
}
