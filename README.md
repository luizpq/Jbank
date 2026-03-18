Objetivos do Projeto:

   . Praticar o uso de Herança e Polimorfismo (Contas Corrente vs. Poupança).

   . Implementar persistência de dados real utilizando Spring JDBC Template.

   . Garantir a consistência financeira através de Transações SQL (@Transactional).

   . Desenvolver um fluxo de navegação resiliente com tratamento de exceções customizadas.

Principais Funcionalidades:

   . Gestão de Acesso: Sistema de Login e Cadastro de usuários com senhas seguras.

   . Multicontas: Suporte para um único usuário possuir e gerenciar múltiplas contas (Corrente e Poupança).

Operações Financeiras:

   . Saque e Depósito: Com atualização instantânea no banco de dados.

   . Transferências (Pix): Sistema de transferência entre contas com validação de existência de destinatário e rollback automático em caso de erro.

   . Extrato Detalhado: Histórico completo de movimentações com data, tipo de operação e realce visual (cores no terminal) para valores positivos e negativos.

   . Busca Global: Listagem de usuários cadastrados para facilitar transferências.

Tecnologias e Ferramentas:

   . Linguagem: Java 21 (OpenJDK)

   . Framework: Spring Boot 3.4

   . Banco de Dados: PostgreSQL 16

   . Acesso a Dados: Spring JDBC Template

   . Ambiente de Desenvolvimento: Arch Linux + IntelliJ IDEA

Arquitetura do Sistema:

   O projeto foi estruturado seguindo o padrão de camadas para facilitar a manutenção:

   . Model: Classes fundamentais e lógica de POO (Entidades e Herança).

   . Service: Camada de inteligência onde residem as regras de negócio e chamadas ao banco.

   . Utils/Exceptions: Classes auxiliares para leitura de dados e tratamento de erros específicos.

   . Application: Orquestração da interface de usuário e menus de navegação.

Os Maiores Desafios:

   . Atomicidade nas Transferências: Implementação de @Transactional para garantir que o dinheiro nunca "suma" caso o banco falhe no meio de uma operação de envio.

   . Sincronização Memória vs. Banco: Garantia de que o objeto Java reflita sempre o saldo atualizado após uma operação no PostgreSQL.

   . Tratamento de Exceções: Uso de getMostSpecificCause() para capturar e tratar erros nativos do driver do Postgres, tornando o debug mais ágil.
