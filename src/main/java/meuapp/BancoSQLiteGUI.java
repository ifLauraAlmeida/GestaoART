package meuapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BancoSQLiteGUI {
    private static final String DB_NAME = "meuBanco.db";
    private static final String URL = "jdbc:sqlite:" + DB_NAME;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            criarBancoSeNaoExiste();
            criarJanela();
        });
    }

    private static void criarBancoSeNaoExiste() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS pessoas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "idade INTEGER)";
            stmt.execute(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar banco: " + e.getMessage());
        }
    }

    private static void criarJanela() {
        JFrame frame = new JFrame("Cadastro de Pessoa");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new GridLayout(3, 2));

        JLabel labelNome = new JLabel("Nome:");
        JTextField fieldNome = new JTextField();

        JLabel labelIdade = new JLabel("Idade:");
        JTextField fieldIdade = new JTextField();

        JButton btnSalvar = new JButton("Salvar");

        btnSalvar.addActionListener(e -> {
            String nome = fieldNome.getText();
            try {
                int idade = Integer.parseInt(fieldIdade.getText());
                inserirDados(nome, idade);
                JOptionPane.showMessageDialog(frame, "Dados salvos com sucesso!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Idade deve ser um número válido.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(labelNome);
        frame.add(fieldNome);
        frame.add(labelIdade);
        frame.add(fieldIdade);
        frame.add(new JLabel());
        frame.add(btnSalvar);

        frame.setVisible(true);
    }

    private static void inserirDados(String nome, int idade) {
        String sql = "INSERT INTO pessoas (nome, idade) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setInt(2, idade);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao inserir dados: " + e.getMessage());
        }
    }
}
