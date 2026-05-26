# APS – Benchmarking: Processos vs Threads

**Centro Universitário de Brusque – UNIFEBE**  
Curso: Sistemas de Informação | Disciplina: Sistemas Operacionais – SI03A  
Professor: Sidnei Baron

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

Requer Java 17+.

## Resultados Obtidos

| Unidades | Threads (ms) | Processos (ms) |
|----------|-------------|----------------|
| 2        | 1.629,2     | 2.006,2        |
| 4        | 754,6       | 1.317,6        |
| 8        | 648,2       | 1.497,8        |

> Matriz 1000×1000 | 5 execuções por cenário (1ª descartada como aquecimento da JVM)

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
- **Escalabilidade das Threads** foi significativa (1.629ms → 648ms com 8 threads)
- **Processos degradaram com 8 unidades** — overhead de criação de JVMs supera o ganho de paralelismo
