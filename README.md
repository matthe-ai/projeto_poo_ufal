# Sistema de Gerenciamento de Biblioteca

Projeto acadêmico desenvolvido em **Java 21** para a disciplina de Programação Orientada a Objetos (POO) da UFAL. O sistema tem como objetivo gerenciar as operações de uma biblioteca, incluindo usuários, catálogo de materiais bibliográficos, autenticação, controle de permissões, empréstimos, solicitações de empréstimo e multas.

## Funcionalidades

- Cadastro, consulta, atualização e remoção de usuários, respeitando permissões e regras de negócio.
- Autenticação por login e senha, com gerenciamento de sessão.
- Cadastro, consulta, atualização e remoção de livros e periódicos.
- Pesquisa de materiais pelo título.
- Registro e consulta de empréstimos, com controle de disponibilidade.
- Solicitação de empréstimos por leitores, com aprovação ou rejeição por usuários autorizados.
- Cálculo automático de multas quando um material é devolvido com atraso.
- Consulta de multas por usuário e status, além de pagamento e cancelamento.
- Verificação de autenticação e autorização nas operações dos serviços.
- Exceções específicas para representar erros e violações das regras de negócio.

## Perfis de acesso

O sistema utiliza o enum `Cargo` para definir três perfis: `LEITOR`, `BIBLIOTECARIO` e `ADMINISTRADOR`. As permissões são centralizadas em `AutorizacaoService` e verificadas pelos serviços antes da execução das operações.

| Operação | Leitor | Bibliotecário | Administrador |
|---|:---:|:---:|:---:|
| Consultar catálogo | Sim | Sim | Sim |
| Solicitar empréstimo | Sim | Não | Sim |
| Consultar próprios empréstimos | Sim | Sim | Sim |
| Consultar próprias solicitações | Sim | Não | Sim |
| Atualizar próprios dados | Sim | Sim | Sim |
| Gerenciar catálogo | Não | Sim | Sim |
| Registrar empréstimos e devoluções | Não | Sim | Sim |
| Consultar usuários | Não | Sim | Sim |
| Consultar todas as solicitações | Não | Sim | Sim |
| Gerenciar solicitações | Não | Sim | Sim |
| Consultar todas as multas | Não | Sim | Sim |
| Gerenciar multas | Não | Sim | Sim |
| Cadastrar e remover usuários | Não | Não | Sim |
| Consultar todos os empréstimos | Não | Sim | Sim |

O administrador possui todas as permissões. As operações de gerenciamento de usuários são restritas ao administrador.

## Regras de negócio

### Usuários e autenticação

- Cada usuário possui identificador, nome, login, senha e cargo.
- O login deve ser único.
- As senhas são transformadas em hash com `PBKDF2WithHmacSHA256` antes de serem armazenadas pelo serviço de usuários.
- A senha aceita pelo serviço deve ter entre 6 e 16 caracteres.
- Uma sessão mantém a referência ao usuário autenticado; operações protegidas exigem uma sessão ativa.
- Um usuário não pode remover a própria conta.
- A remoção de um administrador é bloqueada.
- Um usuário que possui empréstimos ativos não pode ser removido.
- A atualização dos dados próprios não permite alterar o cargo.

### Catálogo

O catálogo possui dois tipos concretos de material:

- **Livro:** título, ano de publicação, autor, ISBN e editora.
- **Periódico:** título, ano de publicação, editora, número da edição, data de publicação e tipo (`JORNAL` ou `REVISTA`).

Os materiais compartilham identificador, título, ano, editora e disponibilidade por meio da classe abstrata `MaterialBibliografico`.

As operações de cadastro e atualização validam os campos textuais, o ano de publicação e, no caso de periódicos, o número da edição. Um material indisponível não pode ser removido do catálogo.

### Empréstimos

Os empréstimos relacionam um usuário a um material e registram as datas de empréstimo, previsão de devolução e devolução efetiva, além do status (`ATIVO` ou `DEVOLVIDO`).

| Tipo de material | Prazo de empréstimo |
|---|---:|
| Livro | 14 dias |
| Jornal | 3 dias |
| Revista | 2 dias |

Regras aplicadas:

- Um material precisa estar disponível para ser emprestado.
- Cada usuário pode possuir no máximo **3 empréstimos ativos**.
- Usuários com multas pendentes não podem receber novos empréstimos.
- Ao registrar o empréstimo, o material passa a ficar indisponível.
- Na devolução, o empréstimo é finalizado e o material volta a ficar disponível.
- Não é permitido registrar novamente a devolução de um empréstimo já finalizado.
- Se a devolução ocorrer após a data prevista, o serviço de empréstimos solicita a geração de uma multa.

### Solicitações de empréstimo

- Leitores podem solicitar o empréstimo de materiais.
- Uma solicitação é criada com status `PENDENTE`.
- O mesmo usuário não pode manter mais de uma solicitação pendente para o mesmo material.
- Bibliotecários e administradores podem consultar e gerenciar solicitações.
- Uma solicitação pendente pode ser aprovada ou rejeitada.
- A aprovação tenta registrar o empréstimo e, se a operação for concluída, atualiza a solicitação para `APROVADA`.
- Solicitações já processadas não podem ser aprovadas ou rejeitadas novamente.

Os status possíveis são `PENDENTE`, `APROVADA` e `REJEITADA`.

### Multas

As multas são associadas a um empréstimo e armazenam quantidade de dias de atraso, valor, data de geração, status e, quando paga, data de pagamento.

- A multa é gerada automaticamente durante a devolução quando há atraso.
- O valor é de **R$ 2,00 por dia de atraso**, limitado ao máximo de **R$ 50,00**.
- O repositório impede o cadastro de mais de uma multa para o mesmo empréstimo.
- Uma multa pendente pode ser paga ou cancelada.
- Multas já pagas ou canceladas não podem ser processadas novamente.
- Os status possíveis são `PENDENTE`, `PAGA` e `CANCELADA`.
- O usuário pode consultar suas próprias multas; bibliotecários e administradores podem consultar multas de todos os usuários e gerenciá-las.

## Arquitetura

O código está organizado por responsabilidade, separando o domínio, a persistência em memória, as regras de negócio, os mecanismos de autenticação e a camada de interação.

```text
src/
└── biblioteca/
    ├── Main.java
    ├── auth/
    │   ├── SenhaHashUtil.java
    │   └── Sessao.java
    ├── exception/
    │   └── ...
    ├── model/
    │   ├── usuario/
    │   │   ├── Usuario.java
    │   │   ├── Cargo.java
    │   │   └── Permissao.java
    │   ├── material/
    │   │   ├── MaterialBibliografico.java
    │   │   ├── Livro.java
    │   │   ├── Periodico.java
    │   │   └── TipoPeriodico.java
    │   ├── Emprestimo.java
    │   ├── SolicitacaoEmprestimo.java
    │   ├── Multa.java
    │   └── enums de status
    ├── repository/
    │   ├── Repositorio.java
    │   ├── RepositorioRemovivel.java
    │   └── repositórios concretos
    ├── service/
    │   ├── Service.java
    │   ├── AutorizacaoService.java
    │   ├── UsuarioService.java
    │   ├── CatalogoService.java
    │   ├── EmprestimoService.java
    │   ├── SolicitacaoEmprestimoService.java
    │   └── MultaService.java
    └── view/
        ├── MenuPrincipal.java
        ├── MenuUsuario.java
        ├── MenuBibliotecario.java
        └── MenuAdministrador.java

tests/
└── biblioteca/
    ├── auth/
    ├── models/
    ├── repository/
    └── service/
```

### Responsabilidades das camadas

| Pacote | Responsabilidade |
|---|---|
| `model` | Entidades do domínio, seus atributos, estados e comportamentos. |
| `repository` | Armazenamento e consulta dos objetos em coleções, com operações de inclusão, busca, listagem e atualização; alguns repositórios também permitem remoção. |
| `service` | Regras de negócio, validações, coordenação entre repositórios e verificação de permissões. |
| `auth` | Hash de senhas e gerenciamento da sessão autenticada. |
| `exception` | Exceções específicas para erros de domínio e operações não permitidas. |
| `view` | Organização dos menus e da interação com os diferentes perfis de usuário. |
| `Main` | Ponto de entrada da aplicação. |

### Camada de visualização (`view`)

O pacote `view` organiza a interação com o sistema por meio de menus separados de acordo com o perfil e o contexto de utilização.

- **`MenuPrincipal`:** responsável pelo menu inicial, concentrando as opções de entrada no sistema e acesso à autenticação.
- **`MenuUsuario`:** reúne as opções gerais disponibilizadas ao usuário autenticado, como consultas e operações relacionadas à sua conta.
- **`MenuBibliotecario`:** organiza as operações administrativas da biblioteca, incluindo gerenciamento do catálogo, empréstimos, solicitações e consultas.
- **`MenuAdministrador`:** reúne as operações de administração do sistema, incluindo gerenciamento de usuários e demais funcionalidades administrativas.

Essa separação permite organizar as opções de interação de acordo com as responsabilidades de cada perfil, mantendo as regras de negócio e as verificações de autorização na camada `service`.

### Padrão Repository

A interface genérica `Repositorio<T>` define operações comuns de persistência. `RepositorioRemovivel<T>` estende essa interface para os repositórios que também oferecem remoção.

Os repositórios concretos utilizam `ArrayList` para manter os registros em memória, encapsulando as operações de acesso e manipulação dos dados.

## Conceitos de POO e organização

O projeto aplica conceitos trabalhados na disciplina:

- **Abstração:** `MaterialBibliografico` define a estrutura comum dos materiais.
- **Herança:** `Livro` e `Periodico` especializam `MaterialBibliografico`.
- **Polimorfismo:** os tipos concretos de material implementam `exibirDetalhes()`.
- **Encapsulamento:** atributos das entidades são privados e o acesso ocorre por métodos.
- **Interfaces:** `Repositorio<T>` e `RepositorioRemovivel<T>` definem contratos para persistência.
- **Associação:** `Emprestimo` referencia um `Usuario` e um `MaterialBibliografico`; `Multa` referencia um `Emprestimo`.
- **Enums:** cargos, permissões, tipos de periódico e estados de empréstimos, solicitações e multas são representados por enumerações.
- **Exceções próprias:** erros esperados do domínio são representados por classes específicas.

## Documentação complementar

O diretório [`docs/`](docs/) contém materiais complementares do projeto, incluindo a documentação UML.
