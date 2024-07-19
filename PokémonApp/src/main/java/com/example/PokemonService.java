package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class PokemonService {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Pokemon> getPokemonList(String apiUrl) throws IOException {
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            PokemonList pokemonList = mapper.readValue(responseBody, new TypeReference<PokemonList>(){});
            return pokemonList.getResults();
        }
    }

    public static PokemonDetail getPokemonDetail(String pokemonUrl) throws IOException {
        Request request = new Request.Builder()
                .url(pokemonUrl)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            PokemonDetail pokemonDetail = mapper.readValue(responseBody, PokemonDetail.class);
            return pokemonDetail;
        }
    }
}
