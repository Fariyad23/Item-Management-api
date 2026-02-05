package com;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ItemClientSwing extends JFrame {

    private final JTextField baseUrlField = new JTextField("http://localhost:8080");
    private final JTextField nameField = new JTextField();
    private final JTextField descField = new JTextField();
    private final JTextField priceField = new JTextField();

    private final JTextArea outputArea = new JTextArea();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ItemClientSwing() {
        setTitle("Item API - Swing Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        // Top: Base URL
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Base URL:"), BorderLayout.WEST);
        top.add(baseUrlField, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        // Center: Form + Buttons + Output
        JPanel center = new JPanel(new GridBagLayout());
        root.add(center, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        center.add(new JLabel("Name"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        center.add(nameField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        center.add(new JLabel("Description"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        center.add(descField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        center.add(new JLabel("Price"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        center.add(priceField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton addBtn = new JButton("Add Item (POST /items)");
        JButton getAllBtn = new JButton("Get All (GET /items)");
        JButton clearBtn = new JButton("Clear Output");

        btnPanel.add(addBtn);
        btnPanel.add(getAllBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        center.add(btnPanel, gbc);

        // Output
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(outputArea);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        center.add(scroll, gbc);

        // Actions
        addBtn.addActionListener(e -> addItem());
        getAllBtn.addActionListener(e -> getAllItems());
        clearBtn.addActionListener(e -> outputArea.setText(""));
    }

    private void addItem() {
        String baseUrl = baseUrlField.getText().trim();
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String priceStr = priceField.getText().trim();

        if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Description, Price required!", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a positive number!", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String json = "{\n" +
                "  \"name\": \"" + escapeJson(name) + "\",\n" +
                "  \"description\": \"" + escapeJson(desc) + "\",\n" +
                "  \"price\": " + price + "\n" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/items"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        append("➡️ POST " + baseUrl + "/items");
        append("Request:\n" + json);

        runAsync(request);
    }

    private void getAllItems() {
        String baseUrl = baseUrlField.getText().trim();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/items"))
                .GET()
                .build();

        append("➡️ GET " + baseUrl + "/items");
        runAsync(request);
    }

    private void runAsync(HttpRequest request) {
        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                SwingUtilities.invokeLater(() -> {
                    append("✅ Status: " + response.statusCode());
                    append("Response:\n" + response.body());
                    append("--------------------------------------------------");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    append("❌ Error: " + ex.getMessage());
                    append("Tip: Make sure backend is running (localhost:8080).");
                    append("--------------------------------------------------");
                });
            }
        }).start();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void append(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ItemClientSwing().setVisible(true));
    }
}
