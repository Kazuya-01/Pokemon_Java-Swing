package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class PokemonApp {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;

    public PokemonApp() {
        initialize();
        fetchData("https://pokeapi.co/api/v2/pokemon?limit=15"); 
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tableModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Pokemon Name"}
        );
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void fetchData(String apiUrl) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(apiUrl)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    String responseData = response.body().string();

                    PokemonList pokemonList = objectMapper.readValue(responseData, new TypeReference<PokemonList>() {});
                    if (pokemonList != null && pokemonList.getResults() != null) {
                        displayPokemonData(pokemonList.getResults());
                    } else {
                        System.err.println("Pokemon list is null or empty.");
                        showErrorDialog("Failed to fetch Pokémon data. List is null or empty.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog("Error fetching data: " + e.getMessage());
                }
                return null;
            }
        };
        worker.execute(); 
    }

    private void displayPokemonData(List<Pokemon> pokemonList) {
        SwingUtilities.invokeLater(() -> {
            for (Pokemon pokemon : pokemonList) {
                String pokemonName = pokemon.getName();
                Object[] rowData = {pokemonName};
                tableModel.addRow(rowData);
            }
            adjustColumnWidth(0); 
        });
    }

    private void adjustColumnWidth(int column) {
        SwingUtilities.invokeLater(() -> {
            TableColumnModel columnModel = table.getColumnModel();
            int width = 150; 
            columnModel.getColumn(column).setPreferredWidth(width);
        });
    }

    private void showErrorDialog(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                PokemonApp window = new PokemonApp();
                window.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
