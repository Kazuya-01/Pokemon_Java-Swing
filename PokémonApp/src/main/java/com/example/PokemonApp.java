package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class PokemonApp {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea detailTextArea;

    public PokemonApp() {
        initialize();
        fetchData("https://pokeapi.co/api/v2/pokemon?limit=30");
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tableModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Pokemon Name", "Pokemon URL"}
        );
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setWidth(0);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        String pokemonUrl = (String) table.getValueAt(selectedRow, 1);
                        fetchPokemonDetail(pokemonUrl);
                    }
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(table);
        frame.getContentPane().add(tableScrollPane, BorderLayout.WEST);

        detailTextArea = new JTextArea();
        detailTextArea.setEditable(false);
        JScrollPane detailScrollPane = new JScrollPane(detailTextArea);
        frame.getContentPane().add(detailScrollPane, BorderLayout.CENTER);
    }

    private void fetchData(String apiUrl) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    List<Pokemon> pokemonList = PokemonService.getPokemonList(apiUrl);
                    displayPokemonData(pokemonList);
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
                String pokemonUrl = pokemon.getUrl();
                Object[] rowData = {pokemonName, pokemonUrl};
                tableModel.addRow(rowData);
            }
            adjustColumnWidth(0);
        });
    }

    private void fetchPokemonDetail(String pokemonUrl) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    PokemonDetail pokemonDetail = PokemonService.getPokemonDetail(pokemonUrl);
                    displayPokemonDetail(pokemonDetail);
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog("Error fetching data: " + e.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }

    private void displayPokemonDetail(PokemonDetail pokemonDetail) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder detailBuilder = new StringBuilder();
            detailBuilder.append("Name: ").append(pokemonDetail.getName()).append("\n");
            detailBuilder.append("Height: ").append(pokemonDetail.getHeight()).append("\n");
            detailBuilder.append("Weight: ").append(pokemonDetail.getWeight()).append("\n");
            detailBuilder.append("Abilities:\n");
            if (pokemonDetail.getAbilities() != null) {
                for (AbilityEntry abilityEntry : pokemonDetail.getAbilities()) {
                    Ability ability = abilityEntry.getAbility();
                    detailBuilder.append("- ").append(ability.getName()).append("\n");
                }
            } else {
                detailBuilder.append("N/A\n");
            }
            detailBuilder.append("Types:\n");
            if (pokemonDetail.getTypes() != null) {
                for (PokemonType type : pokemonDetail.getTypes()) {
                    detailBuilder.append("- ").append(type.getType().getName()).append("\n");
                }
            } else {
                detailBuilder.append("N/A\n");
            }
            detailTextArea.setText(detailBuilder.toString());
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
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            PokemonApp window = new PokemonApp();
            window.show();
        });
    }
}
