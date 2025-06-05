# Simulador de Sistema de Arquivos com Journaling

Este projeto implementa um simulador de sistema de arquivos em Java, com suporte a **interface gráfica (GUI) usando Swing**, **persistência de estado** e **journaling de operações**.

# Repositório GitHub

- https://github.com/JVictorVI/SistemaDeArquivos

## Dupla

- Amanda Evellin de Sousa Viana | 2315774
- João Victor da Silva Ferreira | 2314387

## 📌 Parte 1: Introdução ao Sistema de Arquivos com Journaling

### 🔹 O que é um Sistema de Arquivos?

Um sistema de arquivos é a estrutura lógica que permite armazenar, organizar e acessar dados em dispositivos de armazenamento, como discos rígidos e SSDs. Ele define como os arquivos e diretórios são nomeados, armazenados e recuperados. Sem um sistema de arquivos, os dados seriam um bloco contínuo de informações, impossível de interpretar ou utilizar.

### 🔹 O que é Journaling?

Journaling é uma técnica de integridade usada por sistemas de arquivos para registrar operações em um log antes de aplicá-las no disco. Esse log, chamado de _journal_, permite recuperar o estado consistente do sistema mesmo após falhas, como quedas de energia ou falhas do sistema.

#### Tipos de Journaling:

- **Write-Ahead Logging (WAL)**: Registra a intenção de alterar dados antes de aplicá-los, garantindo que nenhuma modificação ocorra sem antes ser registrada.
- **Log-Structured File Systems (LFS)**: Trata o disco como uma estrutura de log contínuo, escrevendo tudo sequencialmente para melhorar a performance.
- **Metadata Journaling**: Apenas as alterações de metadados (como nomes de arquivos e diretórios) são registradas, sendo mais rápido mas menos seguro.

---

## 🧱 Parte 2: Arquitetura do Simulador

### 🔹 Estrutura de Dados

O simulador utiliza as seguintes classes em Java:

- **FSFile**: Representa um arquivo.
- **Directory**: Representa um diretório, podendo conter arquivos e outros diretórios.
- **FileSystemSimulator**: Controla as operações de alto nível como criar, copiar, mover, listar e deletar arquivos/diretórios.
- **FileSystemGUI**: Permite a visualização do sistema de arquivos por meio de uma interface gráfica desenvolvida com Swing.
- **Journal**: Responsável por registrar cada operação executada no sistema.

### 🔹 Implementação do Journaling

O journaling é implementado por meio de um arquivo chamado `journal.log`. Cada linha do log contém:

- A data e hora da operação;
- A ação executada (CREATE, DELETE, RENAME, COPY, etc.);
- O caminho e nome dos arquivos/diretórios envolvidos.

⚠️ O log **não é usado para restaurar** o sistema de arquivos, apenas como histórico de operações. A reconstrução do estado se baseia exclusivamente no arquivo `base.txt`.

---

## ☕ Parte 3: Implementação em Java

### 📂 Classe `FileSystemSimulator`

Responsável por:

- Criar, copiar, renomear e apagar arquivos e diretórios;
- Listar conteúdos de diretórios;
- Gerenciar a persistência via `base.txt`;
- Invocar o `Journal` para registrar as ações.

### 📄 Classe `FSFile` e `Directory`

- **FSFile**: Contém nome e caminho.
- **Directory**: Contém uma lista de arquivos e subdiretórios, além de seu caminho.

### 📝 Classe `Journal`

Gerencia o arquivo `journal.log`, registrando todas as operações com carimbo de data/hora e dados relevantes.

### 🖥️ Classe `FileSystemGUI`

Interface gráfica desenvolvida com Java Swing para interação com o simulador de sistema de arquivos.

🔹 Funcionalidades:

- Criar, copiar, renomear e apagar arquivos e diretórios;
- Listar conteúdo de diretórios;
- Visualizar o conteúdo do arquivo de journal;
- Visualizar a estrutura atual salva no base.txt.

🔹 Organização:

- Interface amigável com campos de entrada, botões de ação e áreas de exibição de resultados e logs;
- Cada ação na interface aciona os métodos da classe FileSystemSimulator;
- Mensagens de sucesso e erro são exibidas ao usuário em tempo real.

---

## ▶️ Como Executar

#### 1. Baixe este repositório.

#### 2. Execute a classe FileSystemGUI.

> A aplicação abrirá uma janela com visualização em árvore do sistema de arquivos.

---

## Estrutura do Projeto

```
├── src/
│   ├── FileSystemSimulator.java       # Lógica principal do sistema de arquivos
│   ├── FileSystemGUI.java             # Interface gráfica com Swing
│   ├── Directory.java                 # Representação de diretório
│   ├── FSFile.java                    # Representação de arquivo
│   ├── TreeItem.java                  # Representação visual de um item na árvore
├── base.txt                           # Estado atual do sistema de arquivos
├── journal.log                        # Registro de operações executadas
├── README.md
```

## Exemplo de Uso

- Clique em "Criar Diretório" ou "Criar Arquivo" após selecionar um nó da árvore.
- Use "Renomear", "Deletar" ou "Copiar Arquivo" com um item selecionado.
- A cada ação, o sistema grava automaticamente o estado no `base.txt` e registra no `journal.log`.
