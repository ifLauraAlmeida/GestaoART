package meuapp;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.ArrayList;
import java.util.List;

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
                exportarParaExcel();
                JOptionPane.showMessageDialog(frame, "Dados salvos e exportados para Excel!");
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

    private static void exportarParaExcel() {
        String sql = "SELECT * FROM pessoas";
        List<String[]> dados = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dados.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("nome"), String.valueOf(rs.getInt("idade"))});
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Pessoas");
            Row headerRow = sheet.createRow(0);
            String[] colunas = {"ID", "Nome", "Idade"};

            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
            }

            int rowNum = 1;
            for (String[] linha : dados) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < linha.length; i++) {
                    row.createCell(i).setCellValue(linha[i]);
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream("dados.xlsx")) {
                workbook.write(fileOut);
            }
            workbook.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao exportar para Excel: " + e.getMessage());
        }
    }
}
