package com.jbank.service;

import com.jbank.exceptions.SaldoInsuficinteException;
import com.jbank.exceptions.ValorInvalidoException;
import com.jbank.model.Conta;
import com.jbank.model.ContaCorrente;
import com.jbank.model.ContaPoupanca;
import com.jbank.utils.InputReader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Service
public class AutenticacaoService {



    private JdbcTemplate jdbcTemplate;

    public AutenticacaoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Executa a função de login que o usuário escolher

    public Usuario login () {
        boolean isRunnig = true;

        while ( isRunnig ) {

            System.out.println("\n------ Tela de login do Jbank ------");
            System.out.println(" 1 - Login\n 2 - Cadastrar \n 3 - Sair ");
            System.out.print("Escolha sua Opção: ");
            int option = InputReader.lerInt();
            InputReader.lerString();

            switch ( option ) {
                case 1 -> {
                    Usuario u = fazerLogin();
                    if ( u != null ) return u;

                }
                case 2 -> {
                  Usuario u = fazerCadastro();
                  if ( u != null ) return u;

                }
                case 3 -> {
                    System.out.println("Até a Próxima!");
                    return null;

                }
                default -> {
                    System.out.println("Valor inválido. Tente novamente.");

                }
            }
        }
        return null;
    }

    // Faz o login de um usuário já cadastrado

    public Usuario fazerLogin() {
        System.out.print("Usuário: ");
        String nome = InputReader.lerString().toUpperCase();
        System.out.print("Senha: ");
        String senha = InputReader.lerString();

        String sql = "SELECT * FROM usuarios WHERE UPPER(nome) = UPPER(?) AND senha = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("senha")), nome, senha
            );
        } catch (Exception e) {
            System.out.println("Usuário ou senha incorretos.");
            return null;

        }
    }

    // Metodo que fará o cadastro de acordo com o tipo de conta que o usuário preferir e irá logar nela

   public Usuario fazerCadastro () {
        System.out.print("Insira o seu Nome: ");
        String nome = InputReader.lerString().toUpperCase();
        System.out.print("Digite sua Senha: ");
        String senha = InputReader.lerString();

        try {
            String sqlUser = "INSERT INTO usuarios (nome, senha) VALUES (?, ?) RETURNING id";
            Integer novoId = jdbcTemplate.queryForObject( sqlUser, Integer.class, nome, senha);

            return new Usuario( novoId, nome, senha);

        } catch ( Exception e ) {
            System.out.println("ERRO: " + e.getMessage());
            return null;
       }
    }

    /*
    Busca uma Conta pelo numero pelos usuários e ocm a função getContas()
    para depois retornar o a conta com o numero passado no parâmetro
     */

    public Conta buscarContaPorNumeroGlobal( int numeroProcurado ) {
        String sql = """
                     SELECT c.*, u.nome AS titular
                     FROM contas c
                     JOIN usuarios u ON c.usuario_id = u.id
                     WHERE c.numero = ?
                     """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum ) -> {
                String tipo = rs.getString("tipo");
                String nomeTitular = rs.getString("titular");
                double saldo = rs.getDouble("saldo");
                int num = rs.getInt("numero");

                if ( "POUPANÇA".equalsIgnoreCase( tipo ) ) {
                    return new ContaPoupanca( nomeTitular, num, saldo );
                } else {
                    return new ContaCorrente( nomeTitular, num, saldo );
                }
            }, numeroProcurado);
        } catch ( Exception e ) {
            System.out.println("Conta " + numeroProcurado + " não encontrada no banco.");
            return null;
        }
    }

    /**
     * Recupera todas as contas vinculadas a um ID de usuário específico.
     * @param usuarioId Identificador único do usuário no banco de dados.
     * @param nomeDoTitular Nome do proprietário para instanciar os objetos de Conta.
     * @return Uma {@code List<Conta>} contendo instâncias de ContaCorrente ou ContaPoupanca.
     * Retorna uma lista vazia caso o usuário não possua contas.
     */
    public List<Conta> buscarContasDoUsuario ( int usuarioId, String nomeDoTitular ) {
        String sql = "SELECT * FROM contas WHERE usuario_id = ?";

        try {
            return jdbcTemplate.query( sql, ( rs, rowNum ) -> {

                int numero = rs.getInt("numero");
                double saldo = rs.getDouble("saldo");
                String tipo = rs.getString("tipo");

                if ( "POUPANÇA".equalsIgnoreCase( tipo )) {
                    return new ContaPoupanca( nomeDoTitular, numero, saldo );
                } else {
                    return new ContaCorrente( nomeDoTitular, numero, saldo );
                }
            }, usuarioId );

        } catch ( DataAccessException e ) {
            System.out.println("ERRO: " + e.getMostSpecificCause().getMessage());
            return List.of();

        }
    }

    public void listarDestinatarios() {
        String sql = """
                     SELECT u.id, u.nome, c.numero, c.tipo
                     FROM usuarios u 
                     JOIN contas c ON u.id = c.usuario_id 
                     ORDER BY u.nome
                     """;

        try {
            System.out.println("\n------ DESTINATÁRIOS DISPONÍVEIS ------");

            jdbcTemplate.query( sql, (rs -> {
                System.out.printf("ID: %d | Nome: %-15s | Conta: %d (%s)%n",
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("numero"),
                        rs.getString("tipo"));

            }));

            System.out.println("-------------------------------------------------------\n");

        } catch ( DataAccessException e ) {
            System.out.println("ERRO: " + e.getMostSpecificCause().getMessage());

        }

    }

    /**
     * @param usuario é o usuário para qual será criado uma nova conta.
     * @return {@code true} se a conta foi criada com sucesso; {@code false} se o usurio já atingiu o limite de contas
     * ou cancelou a operação.
     */
    public boolean adicionarNovaConta( Usuario usuario ) {
        System.out.println("\n------ ABRIR NOVA CONTA ------");

        boolean temCorrente = false;
        boolean temPoupanca = false;

        for ( Conta conta : usuario.getContas() ) {
            if ( conta instanceof ContaPoupanca ) temPoupanca = true;
            if ( conta instanceof ContaCorrente ) temCorrente = true;
        }

        if ( temCorrente && temPoupanca ) {
            System.out.println("Você já tem Duas contas!");
            return false;
        }

        System.out.println("1 - Conta Corrente\n2 - Conta Poupança");
        System.out.print("Escolha o tipo: ");
        int tipo = InputReader.lerInt();

        if ( tipo == 1 && temCorrente ) {
            System.out.println("ERRO: Você já tem uma Conta CORRENTE!");
            return false;
        } else if ( tipo == 2 && temPoupanca ) {
            System.out.println("ERRO: Você já tem uma Conta POUPANÇA!");
        }

        Conta novaConta;
        String tipoStr;

        int numeroNovaConta = 1000 + ( int ) ( Math.random() * 9000 );

        if ( tipo == 1 ) {
            novaConta = new ContaCorrente(usuario.getNome(), numeroNovaConta, 0.0);
            tipoStr = "CORRENTE";
        } else if ( tipo == 2 ){
            novaConta = new ContaPoupanca(usuario.getNome(), numeroNovaConta, 0.0);
            tipoStr = "POUPANÇA";
        } else {
            System.out.println("Cancelando a Criação da Conta...");
            return false;
        }


        try {
            String sql = "INSERT INTO contas (numero, saldo, tipo, usuario_id) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, numeroNovaConta, 0.0, tipoStr, usuario.getId());

            System.out.printf("Conta %s n° %d criada com sucesso!\n", tipoStr, numeroNovaConta);

            usuario.setContas(buscarContasDoUsuario(usuario.getId(), usuario.getNome()));

            return true;

        } catch ( DataAccessException e ) {
            System.out.println("ERRO: " + e.getMostSpecificCause().getMessage());
            return false;
        }
    }

    public void salvarAlteracoes( Conta conta ) {
        String sql = "UPDATE contas SET saldo = ? WHERE numero = ?";

        try {
            int linhasAfetadas = jdbcTemplate.update(sql, conta.getSaldo(), conta.getNumero());
            if (linhasAfetadas > 0) {
                System.out.println("");
            }
        } catch ( DataAccessException e ) {
            System.out.println("ERRO: " + e.getMostSpecificCause().getMessage());
        }
    }

    /**
     *  Executa a transferência de valores entre duas contas distintas.
     *  <p>
     *  O método valida se o valor é positivo, se a conta de origem possui saldo suficiente e impede transferências
     *  para a própria conta.
     *  A operação é protegida com @Transactional em caso de qualquer no Banco de dados, garantindo que nenhum dado
     *  seja alterado (Rollback).
     *  </p>
     *  @param origem A conta que terá o valor debitado.
     *  @param destino A conta que receberá o valor.
     *  @param valor O valor a ser transferido (deve ser maior do que zero).
     *  @throws DataAccessException Caso ocorra falha na conectividade com o Postgre.
     *  @throws IllegalArgumentException Se o valor for iválido ou as contas forem iguais.
     */

    @Transactional
    public void realizarTransferencia ( Conta origem, Conta destino, double valor ) {
        if ( valor <= 0 ) throw new IllegalArgumentException("Valor deve ser Positivo!");
        if ( origem.getSaldo() < valor ) throw new IllegalStateException("Saldo Insuficiente");
        if ( origem.getNumero() == destino.getNumero() ) throw new IllegalArgumentException("Conta destino Igual a Origem!");
        try {
            jdbcTemplate.update("UPDATE contas SET saldo = saldo - ? WHERE numero = ? ", valor, origem.getNumero());

            jdbcTemplate.update("UPDATE contas SET saldo = saldo + ? WHERE numero = ?", valor, destino.getNumero());

            jdbcTemplate.update("INSERT INTO extrato (conta_numero, tipo, valor) VALUES (?, ?, ?)",
                            origem.getNumero(), "TRANSFERENCIA_ENVIADA", -valor);

            jdbcTemplate.update("INSERT INTO extrato (conta_numero, tipo, valor) VALUES (?, ?, ?)",
                            destino.getNumero(), "TRANSFERENCIA_RECEBIDA", valor);

        } catch ( DataAccessException e ) {
            System.out.println("ERRO CRÍTICO: " + e.getMostSpecificCause().getMessage());

            throw e;
        }

    }

    @Transactional
    public void realizarDeposito ( Conta destino ) {
        System.out.println("Valor a Depositar: ");
        double valor = InputReader.lerDouble();

        try {
            destino.depositar( valor );
            salvarAlteracoes( destino );

            System.out.println("Atualizando Banco de Dados...");

            jdbcTemplate.update("INSERT INTO extrato (conta_numero, tipo, valor) VALUES (?, ?, ?)",
                            destino.getNumero(), "DEPOSITO", valor);

            System.out.printf("%.2f Deposito com sucesso!", valor);

        } catch ( ValorInvalidoException | DataAccessException e ) {
            System.out.println("ERRO: " + e.getMessage());

        }
    }


    @Transactional
    public void realizarSaque ( Conta destino ) {
        System.out.println("Quanto Sacar: ");
        double valor = InputReader.lerDouble();

        try {
            destino.sacar( valor );
            salvarAlteracoes( destino );

            System.out.println("Atualizando Banco de Dados...");

            jdbcTemplate.update("INSERT INTO extrato (conta_numero, tipo, valor) VALUES (?, ?, ?)",
                            destino.getNumero(), "SAQUE", -valor);

            System.out.println("Saque realizado com sucesso!");

        } catch ( ValorInvalidoException | SaldoInsuficinteException e ) {
            System.out.println("ERRO: " + e.getMessage());

        }
    }

    public Conta trocarDeConta ( Usuario usuario, Conta conta ) {
        List<Conta> contas = usuario.getContas();

        for ( Conta c : contas ) {

          if ( c.getNumero() != conta.getNumero() ) {
              String tipo = ( c instanceof ContaCorrente ) ? "CORRENTE" : "POUPANÇA";
              System.out.printf("\nMudando para a Conta %s...\n", tipo);

              return c;

          }
        }

        System.out.println("Você não tem uma conta para Trocar!");
        return null;
    }

    public void exibirExtrato ( int numeroUsuario, Conta contaSelecionada ) {
        System.out.println("\n------ EXTRATO DETALHADO (Conta " + numeroUsuario + ") ------\n");

        System.out.printf("------ Saldo Atual: R$%.2f ------\n", contaSelecionada.getSaldo());

        String sql = "SELECT tipo, valor, data_movimentacao FROM extrato WHERE conta_numero = ? ORDER By data_movimentacao DESC";

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList( sql, numeroUsuario );
            if ( rows.isEmpty() ) {
                System.out.println("Nenhuma Movimentação Encontrada.");

            }

            for ( Map<String, Object> row : rows ) {
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String dataFormatada = ((java.sql.Timestamp) row.get("data_movimentacao")).toLocalDateTime()
                                        .format(formatador);

                String tipo = ( String ) row.get("tipo");
                Double valor = (( Number ) row.get("valor")).doubleValue();

                String cor = ( valor < 0 ) ? "\u001b[31m" : "\u001b[32m";
                String reset = "\u001B[0m";

                System.out.printf("%s | %-20s | %sR$ %.2f%s\n", dataFormatada, tipo, cor, valor, reset);

            }
            System.out.println("-------------------------------------------------------\n");

        } catch ( DataAccessException e ) {
            System.out.println("ERRO DE CONEXÃO: Não foi possível acessar o banco de dados.");
            System.out.println("Detalhe técnico: " + e.getMostSpecificCause().getMessage());

        }

    }
}
