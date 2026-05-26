/**
 * MultiplicadorRunnable.java
 * Implementa Runnable para a Versão B (Threads).
 *
 * Cada instância desta classe é executada por uma thread separada.
 * Todas as threads compartilham as mesmas referências às matrizes A, B
 * e ao array 'resultado' — este é o heap compartilhado da JVM.
 * Não há necessidade de sincronização porque cada thread escreve em
 * intervalos de linhas distintos e não sobrepostos.
 */
public class MultiplicadorRunnable implements Runnable {

    private final double[][] A;         // Matriz de entrada A (somente leitura)
    private final double[][] B;         // Matriz de entrada B (somente leitura)
    private final double[][] resultado; // Matriz resultado (escrita exclusiva no intervalo)
    private final int startRow;         // Linha inicial (inclusiva)
    private final int endRow;           // Linha final (exclusiva)

    /**
     * @param A         Matriz A compartilhada no heap da JVM
     * @param B         Matriz B compartilhada no heap da JVM
     * @param resultado Matriz resultado compartilhada no heap da JVM
     * @param startRow  Primeira linha que esta thread deve calcular
     * @param endRow    Última linha + 1 (exclusiva) que esta thread deve calcular
     */
    public MultiplicadorRunnable(double[][] A, double[][] B,
                                  double[][] resultado,
                                  int startRow, int endRow) {
        this.A         = A;
        this.B         = B;
        this.resultado = resultado;
        this.startRow  = startRow;
        this.endRow    = endRow;
    }

    /**
     * Calcula as linhas [startRow, endRow) da matriz resultado = A × B.
     * Algoritmo ingênuo O(n²) por linha: para cada célula (i,j) calcula
     * o produto escalar da linha i de A com a coluna j de B.
     */
    @Override
    public void run() {
        int n = A.length;
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < n; j++) {
                double soma = 0.0;
                for (int k = 0; k < n; k++) {
                    soma += A[i][k] * B[k][j];
                }
                resultado[i][j] = soma;
            }
        }
    }
}
