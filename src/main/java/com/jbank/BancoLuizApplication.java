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

        while ( isRunning ) {

            // Usa o service para mostrar a tela de login ao Usuário
            Usuario usuarioLogado = service.login();


            if ( usuarioLogado != null ) {
                // Busca no banco se existem contas
                List<Conta> contasDoBanco = service.buscarContasDoUsuario(usuarioLogado.getId(), usuarioLogado.getNome());
                usuarioLogado.setContas( contasDoBanco );

                // SÓ mostra a mensagem se NÃO for um cadastro novo ou se realmente deveria haver contas
                if ( usuarioLogado.getContas().isEmpty() ) {
                    // Em vez de apenas printar, você pode chamar o abrir conta direto
                    System.out.println("Bem-vindo ao JBank! Vamos abrir sua primeira conta?");
                    service.adicionarNovaConta( usuarioLogado );

                    // Após adicionar, o programa segue para o menu de operações
                }

                // Faz o usuário escolher qual conta ele quer usar

                Conta contaParaUsar = menuOperacoes( usuarioLogado );

                if ( contaParaUsar != null ) {
                    realizarAcoesNaConta( contaParaUsar, service, usuarioLogado );

                }
            } else {
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

    // Recebe um usuário e retorna suas contas e Indices para o Usuário escolher em qual conta logar

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

    // Interface do APP Jbank

    public void realizarAcoesNaConta( Conta contaSelecionada, AutenticacaoService service, Usuario usuario ) {
        int escolha = 0;
        boolean notRunning = false;

        Conta contaAtual = contaSelecionada;

        while ( !notRunning ) {

            String tipo = (contaAtual instanceof ContaCorrente) ? "CORRENTE" : "POUPANÇA";
            System.out.printf("\n>>> CONTA ATUAL: %d (%s) | SALDO: R$%.2f <<<\n",
                                contaAtual.getNumero(), tipo, contaAtual.getSaldo());

            System.out.println("\n------ J-BANK MENU ------");
            System.out.println(" 1 - Ver Extrato Bancário\n 2 - Sacar\n 3 - Depositar\n 4 - Transferir\n " +
                    "5 - Adiconar Conta Nova\n 6 - Trocar de Conta\n 7 - Sair");

            System.out.println("Escolha: ");
            escolha = InputReader.lerInt();

            switch ( escolha ) {
                case 1 -> service.exibirExtrato( contaAtual.getNumero(), contaAtual );

                case 2 -> service.realizarSaque( contaAtual );

                case 3 -> service.realizarDeposito( contaAtual );

                case 4 -> realizaFluxoTransferencia( contaAtual );

                case 5 -> service.adicionarNovaConta( usuario );

                case 6 -> {
                    Conta nova = service.trocarDeConta( usuario, contaAtual );
                    if ( nova != null ) contaAtual = nova;

                }
                case 7 -> {

                    System.out.println("Deslogando...");
                    notRunning = true;
                }

                default -> System.out.println("Opção Inválida!");
            }
        }
    }
}