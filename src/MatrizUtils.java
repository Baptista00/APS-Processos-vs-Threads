import java.io.*;
import java.util.Random;

/**
 * MatrizUtils.java
 * Utilitários para geração, serialização e validação de matrizes.
 *
 * Estratégia de comunicação entre processos:
 *   As matrizes A e B são gravadas em arquivos binários temporários
 *   antes do benchmark. Cada processo filho lê esses arquivos,
 *   calcula seu subconjunto de linhas e grava um arquivo de resultado
 *   parcial. O processo principal então lê todos os parciais e
 *   monta a matriz final. Isso evita a lentidão de transmitir dados
 *   via stdin/stdout e é compatível com matrizes grandes.
 *
 * Formato do arquivo de matriz:
 *   [ int n ][ double[n][n] ]
 *
 * Formato do arquivo parcial:
 *   [ int startRow ][ int endRow ][ int n ][ double[endRow-startRow][n] ]
 */
public class MatrizUtils {

    /**
     * Gera uma matriz n×n com valores aleatórios entre 0 e 10.
     * A semente (seed) garante reprodutibilidade: as mesmas matrizes
     * são geradas para as versões Processo e Thread, permitindo validação.
     */
    public static double[][] gerarMatriz(int n, long seed) {
        double[][] m = new double[n][n];
        Random rand = new Random(seed);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                m[i][j] = rand.nextDouble() * 10.0;
        return m;
    }

    /**
     * Salva a matriz inteira em arquivo binário.
     * Usa BufferedOutputStream para desempenho em escrita sequencial.
     */
    public static void salvarMatriz(double[][] m, String arquivo) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(arquivo), 1 << 16))) {
            int n = m.length;
            dos.writeInt(n);
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    dos.writeDouble(m[i][j]);
        }
    }

    /**
     * Carrega a matriz inteira de um arquivo binário.
     */
    public static double[][] carregarMatriz(String arquivo) throws IOException {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(arquivo), 1 << 16))) {
            int n = dis.readInt();
            double[][] m = new double[n][n];
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    m[i][j] = dis.readDouble();
            return m;
        }
    }

    /**
     * Salva apenas as linhas [startRow, endRow) da matriz resultado.
     * Usado pelo processo filho para gravar seu resultado parcial.
     */
    public static void salvarParcial(double[][] resultado, int startRow,
                                     int endRow, String arquivo) throws IOException {
        int n = resultado[0].length;
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(arquivo), 1 << 16))) {
            dos.writeInt(startRow);
            dos.writeInt(endRow);
            dos.writeInt(n);
            for (int i = startRow; i < endRow; i++)
                for (int j = 0; j < n; j++)
                    dos.writeDouble(resultado[i][j]);
        }
    }

    /**
     * Lê um arquivo parcial e preenche as linhas correspondentes
     * na matriz 'destino' (já alocada com dimensão n×n).
     */
    public static void carregarParcial(double[][] destino, String arquivo) throws IOException {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(arquivo), 1 << 16))) {
            int startRow = dis.readInt();
            int endRow   = dis.readInt();
            int n        = dis.readInt();
            for (int i = startRow; i < endRow; i++)
                for (int j = 0; j < n; j++)
                    destino[i][j] = dis.readDouble();
        }
    }

    /**
     * Valida se dois resultados são equivalentes dentro de uma tolerância
     * numérica, compensando erros de arredondamento em ponto flutuante.
     * Retorna true se todas as diferenças forem menores que 'tolerancia'.
     */
    public static boolean validarResultados(double[][] r1, double[][] r2,
                                             double tolerancia) {
        int n = r1.length;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (Math.abs(r1[i][j] - r2[i][j]) > tolerancia)
                    return false;
        return true;
    }
}
