# APS – Benchmarking: Processos vs Threads

**Centro Universitário de Brusque – UNIFEBE**  
Curso: Sistemas de Informação | Disciplina: Sistemas Operacionais – SI03A  
Professor: Sidnei Baron
Alunos: Lucas Schwarz Baptista,Lucas Gianesini, Vinicius Imhof Waldrigues, Matheus Guilherme.

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
└── README.md
```

## Como Compilar e Executar

```bash
# Compilar
javac -d bin src/*.java

# Executar benchmark completo
java -cp bin Main
```

Requer Java 11+.

## Resultados Obtidos

| Unidades | Threads (ms) | Processos (ms) |
|----------|-------------|----------------|
| 2        | 1.739,2     | 2.655,6        |
| 4        | 1.680,4     | 3.695,4        |
| 8        | 1.678,6     | 5.530,4        |

> Matriz 1000×1000 | 5 execuções por cenário (1ª descartada como aquecimento) 

## Estratégia de Comunicação entre Processos

Os dados são transferidos via **arquivos binários temporários** (DataOutputStream/DataInputStream). As matrizes A e B (~8 MB cada) são gravadas antes do benchmark e lidas por cada processo filho. Os resultados parciais são gravados em arquivos separados por processo e consolidados pelo processo principal.

## Validação

Ambas as versões produzem resultados numericamente idênticos, validados com tolerância de 1×10⁻⁶ usando seeds fixas para geração das matrizes.
