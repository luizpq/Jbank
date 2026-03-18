package com.jbank;

import com.jbank.model.Conta;
import com.jbank.model.ContaCorrente;
import com.jbank.service.AutenticacaoService;
import com.jbank.service.Usuario;
import com.jbank.utils.InputReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;


@SpringBootApplication
public class BancoLuizApplication implements CommandLineRunner {

    @Autowired
    private AutenticacaoService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public static void main(String[] args) {
        SpringApplication.run(BancoLuizApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        boolean isRunning = true;

        // Loop principal que mantém o aplicativo rodando até o usuário decidir sair (Opção Sair no Login)
        while ( isRunning ) {

            Usuario usuarioLogado = service.login();


            if ( usuarioLogado != null ) {
                // Passo 2: Sincronização. Busca as contas do usuário no banco para carregar no objeto em memória.
                List<Conta> contasDoBanco = service.buscarContasDoUsuario(usuarioLogado.getId(), usuarioLogado.getNome());
                usuarioLogado.setContas( contasDoBanco );

                // Passo 3: Onboarding. Se o usuário for novo e não tiver contas, força a abertura da primeira.
                if ( usuarioLogado.getContas().isEmpty() ) {

                    System.out.println("Bem-vindo ao JBank! Vamos abrir sua primeira conta?");
                    service.adicionarNovaConta( usuarioLogado );

                    // O fluxo continua para que ele possa usar a conta que acabou de criar
                }

                // Passo 4: Seleção. O usuário escolhe com qual conta (Corrente/Poupança) deseja operar agora.

                Conta contaParaUsar = menuOperacoes( usuarioLogado );

                // Passo 5: Operação. Entra no menu de ações financeiras (Saque, Pix, Extrato).
                if ( contaParaUsar != null ) {
                    realizarAcoesNaConta( contaParaUsar, service, usuarioLogado );

                }
            } else {
                // Se o login retornar null, significa que o usuário escolheu "Sair" na tela inicial.
                isRunning = false;
                break;
            }
        }
    }


    public void realizaFluxoTransferencia ( Conta contaSelecionada ) {
        service.listarDestinatarios();

        System.out.println("Digite o número da conta de destino: ");
        int numDestino = InputReader.lerInt();

        Conta contaDestino = service.buscarContaPorNumeroGlobal( numDestino );

        if ( contaDestino == null ) {
            System.out.println("ERRO: Conta de destino não Encontrada!");
            return;
        }

        try {
            System.out.println("Valor da Transferência: ");
            double valorTransf = InputReader.lerDouble();

            service.realizarTransferencia( contaSelecionada , contaDestino, valorTransf );

            System.out.println("Transferência Realizada com Sucesso!");

        } catch ( Exception e ) {
            System.out.println("ERRO: " + e.getMessage());
        }

    }


    // Método que define qual conta o usuário vai usar
    public Conta menuOperacoes( Usuario logado ) {
        List<Conta> contas = logado.getContas();

        // Mostra as contas do Usuário
        while ( true ) {

            System.out.println("\n------ CONTA(S) do USUÁRIO ------");
            for (int i = 0; i < contas.size(); i++) {
                String tipo = (contas.get(i) instanceof ContaCorrente) ? "Corrente" : "Poupança";
                System.out.printf("[%d] - %s (Nº %d)\n", i, tipo, contas.get(i).getNumero());

            }

            System.out.print("Digite o índice da conta: ");

            try {
                int index = InputReader.lerInt();
                InputReader.lerString(); // Limpa o Scanner

                if ( index == -1 ) {
                    System.out.println("Cancelando Operação...");
                    return null;
                }

                if ( index >= 0 && index < contas.size() ) {
                    return contas.get( index );
                } else {
                    System.out.println("ERRO: Indíce fora do alcance. Tente novamente.");
                }

            } catch (Exception e) {
                System.out.println("ERRO: Entrada Inválida. Digite um número.");
                InputReader.lerString();
            }
        }
    }

    /**
     *  Interface de operações financeiras do JBank.
     *  Gerencia o loop de ações (Saque, Depósito, Transferência) numa conta específica.
     */

    public void realizarAcoesNaConta( Conta contaSelecionada, AutenticacaoService service, Usuario usuario ) {
        int escolha = 0;
        boolean notRunning = false;

        Conta contaAtual = contaSelecionada;

        // Loop de operações: mantém o usuário "dentro" da conta até ele escolher Sair ou Trocar.
        while ( !notRunning ) {

            // Exibição do Menu Principal de Ações
            String tipo = ( contaAtual instanceof ContaCorrente ) ? "CORRENTE" : "POUPANÇA";
            System.out.printf("\n>>> CONTA ATUAL: %d (%s) | SALDO: R$%.2f <<<\n",
                                contaAtual.getNumero(), tipo, contaAtual.getSaldo());

            System.out.println("\n------ J-BANK MENU ------");
            System.out.println(" 1 - Ver Extrato Bancário\n 2 - Sacar\n 3 - Depositar\n 4 - Transferir\n " +
                    "5 - Adiconar Conta Nova\n 6 - Trocar de Conta\n 7 - Sair");

            System.out.println("Escolha: ");
            escolha = InputReader.lerInt();

            switch ( escolha ) {
                // Operações de Leitura e Escrita no Banco (via Service)
                case 1 -> service.exibirExtrato( contaAtual.getNumero(), contaAtual );
                case 2 -> service.realizarSaque( contaAtual );
                case 3 -> service.realizarDeposito( contaAtual );

                // Operação Complexa: Envolve busca de destinatário e transação SQL
                case 4 -> realizaFluxoTransferencia( contaAtual );

                // Gestão de Contas: Permiti adicionar uma segunda conta (Corrente/Poupança) sem deslogar
                case 5 -> service.adicionarNovaConta( usuario );

                // Troca de Contexto: Sai do loop atual para escolher outra conta do mesmo usuário
                case 6 -> {
                    Conta nova = service.trocarDeConta( usuario, contaAtual );
                    // Se o usuário possuir outra conta, reinicia o menu com a nova conta
                    if ( nova != null ) contaAtual = nova;

                }

                // Encerramento de sessão na conta atual
                case 7 -> {
                    System.out.println("Deslogando...");
                    notRunning = true;

                }

                default -> System.out.println("Opção Inválida. Tente novamente.");
            }

        }
    }
}