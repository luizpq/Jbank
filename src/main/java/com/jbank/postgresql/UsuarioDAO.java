package com.jbank.postgresql;
import com.jbank.service.Usuario;

import java.sql.*;

public class UsuarioDAO {

    public void salvar (Usuario usuario ) {
        String sql = "INSERT INTO usuarios (nome, senha) VALUES (?, ?)";

        try (Connection conn = ConexaoBanco.conectar()) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getSenha());
            stmt.executeUpdate();

            System.out.println("Usuário salvo no PostgreSQL!");
        } catch ( SQLException e ) {
            System.err.println("Erro ao salvar: " + e.getMessage());
        }
    }

    public Usuario buscarPorLogin ( String nome, String senha ) {
        String sql = "SELECT * FROM usuarios WHERE nome = ? AND senha = ?";

        try ( Connection conn = ConexaoBanco.conectar();
              PreparedStatement stmt = conn.prepareStatement( sql ) ) {

            stmt.setString(1, nome);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();

            if ( rs.next() ) {
                return new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("senha"));
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        }

        return null;
    }
}
