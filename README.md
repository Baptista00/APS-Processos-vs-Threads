# APS – Benchmarking: Processos vs Threads

**Centro Universitário de Brusque – UNIFEBE**  
Curso: Sistemas de Informação | Disciplina: Sistemas Operacionais – SI03A  
Professor: Sidnei Baron

**Alunos:** Lucas Schwarz Baptista, Vinicius Imhof Waldrigues, Lucas Gianesini e Matheus Guilherme

---

## Objetivo

Análise empírica de desempenho entre **Processos (múltiplas JVMs)** e **Threads (ExecutorService)** em Java, utilizando multiplicação de matrizes quadradas 1000×1000 como carga de trabalho.

## Estrutura do Projeto

```
.
├── src/
│   ├── Main.java                  # Orquestra o benchmark completo
│   ├── MultiplicadorRunnable.java # Worker de thread (Versão B)
│   ├── ProcessoWorker.java        # Worker de processo/JVM (Versão A)
│   └── MatrizUtils.java           # Geração, I/O e validação de matrizes
├── Relatorio_APS_Processos_vs_Threads.docx
└── README.md
```

## Como Compilar e Executar

```bash
# Compilar
javac -d bin src/*.java

# Executar benchmark completo
java -cp bin Main
```

## Resultados Obtidos

| Unidades | Threads (ms) | Processos (ms) |
|----------|-------------|----------------|
| 2        | 2.086,2     | 2.350,0        |
| 4        | 926,4       | 1.660,4        |
| 8        | 1.058,4     | 1.790,0        |

> Matriz 1000×1000 | 5 execuções por cenário (1ª descartada como aquecimento da JVM) | Java 26 (OpenJDK)

**Validação:** ✓ Os dois métodos produziram resultados numericamente idênticos.

## Estratégia de Comunicação entre Processos

Os dados são transferidos via **arquivos binários temporários** (DataOutputStream/DataInputStream).  
As matrizes A e B (~8 MB cada) são gravadas antes do benchmark e lidas por cada processo filho.  
Os resultados parciais são gravados em arquivos separados por processo e consolidados pelo processo principal.

## Validação

Ambas as versões produzem resultados numericamente idênticos, validados com tolerância de 1×10⁻⁶  
usando seeds fixas para geração das matrizes (`seed_A = 42`, `seed_B = 123`).

## Principais Conclusões

- **Threads foram mais rápidas** em todos os cenários — sem overhead de inicialização de JVM e com acesso direto à memória compartilhada
- **Processos consumiram mais memória** — cada JVM aloca seu próprio heap (~300 MB por instância)
- **Processos oferecem maior isolamento** — falha em um processo não afeta os demais
- **Escalabilidade das Threads** foi expressiva de 2 para 4 unidades (2.086ms → 926ms, ganho de ~2,25×)
- **Com 8 unidades**, ambas as versões sofreram leve degradação, indicando saturação dos núcleos físicos disponíveis
