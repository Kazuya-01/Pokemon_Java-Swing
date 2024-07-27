package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class PokemonApp {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;

    public PokemonApp() {
        initialize();
        fetchData("https://pokeapi.co/api/v2/pokemon?limit=30");
    }

    private void initialize() {
        frame = new JFrame("Pokemon App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); 

        tableModel = new DefaultTableModel(new Object[][]{}, new String[]{"Pokemon Name", "Pokemon URL"});
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String pokemonUrl = (String) table.getValueAt(selectedRow, 1);
                    fetchPokemonDetail(pokemonUrl);
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(table);
        frame.getContentPane().add(tableScrollPane, BorderLayout.CENTER);
    }

    private void fetchData(String apiUrl) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(apiUrl).build();
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    ObjectMapper objectMapper = new ObjectMapper();
                    String responseData = response.body().string();

                    PokemonList pokemonList = objectMapper.readValue(responseData, PokemonList.class);
                    if (pokemonList != null && pokemonList.getResults() != null) {
                        displayPokemonData(pokemonList.getResults());
                    } else {
                        showErrorDialog("Failed to fetch Pokémon data. List is null or empty.");
                    }
                } catch (IOException e) {
                    showErrorDialog("Error fetching data: " + e.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }

    private void displayPokemonData(List<Pokemon> pokemonList) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Pokemon pokemon : pokemonList) {
                String pokemonName = pokemon.getName();
                String pokemonUrl = pokemon.getUrl();
                Object[] rowData = {pokemonName, pokemonUrl};
                tableModel.addRow(rowData);
            }
        });
    }

    private void fetchPokemonDetail(String pokemonUrl) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    PokemonDetail pokemonDetail = PokemonService.getPokemonDetail(pokemonUrl);
                    displayPokemonDetailDialog(pokemonDetail);
                } catch (IOException e) {
                    showErrorDialog("Error fetching data: " + e.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }

    private void displayPokemonDetailDialog(PokemonDetail pokemonDetail) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder detailBuilder = new StringBuilder();
            detailBuilder.append("<html><body>");
            detailBuilder.append("<h2>").append("Name: ").append(pokemonDetail.getName()).append("</h2>");
            detailBuilder.append("<p>").append("Height: ").append(pokemonDetail.getHeight()).append("</p>");
            detailBuilder.append("<p>").append("Weight: ").append(pokemonDetail.getWeight()).append("</p>");
            detailBuilder.append("<p>").append("<b>Abilities:</b><br>");
            if (pokemonDetail.getAbilities() != null) {
                for (AbilityEntry abilityEntry : pokemonDetail.getAbilities()) {
                    Ability ability = abilityEntry.getAbility();
                    detailBuilder.append("- ").append(ability.getName()).append("<br>");
                }
            } else {
                detailBuilder.append("N/A<br>");
            }
            detailBuilder.append("<p>").append("<b>Types:</b><br>");
            if (pokemonDetail.getTypes() != null) {
                for (PokemonType type : pokemonDetail.getTypes()) {
                    detailBuilder.append("- ").append(type.getType().getName()).append("<br>");
                }
            } else {
                detailBuilder.append("N/A<br>");
            }
            detailBuilder.append("<img src='https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/")
                    .append(pokemonDetail.getId())
                    .append(".png' width='200' height='200'><br>"); 
            detailBuilder.append("</body></html>");

            JOptionPane.showMessageDialog(frame, detailBuilder.toString(), "Pokemon Detail", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void showErrorDialog(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    public void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            PokemonApp window = new PokemonApp();
            window.show();
        });
    }
}
