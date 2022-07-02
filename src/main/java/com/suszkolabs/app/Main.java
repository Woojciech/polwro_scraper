package com.suszkolabs.app;

import com.suszkolabs.scrapper.Scrapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String MATEMATYCY_URL = "https://polwro.com/f,matematycy,6";
    private static final String FIZYCY_URL = "https://polwro.com/f,fizycy,7";
    private static final String INFORMATYCY_URL = "https://polwro.com/f,informatycy,25";
    private static final String CHEMICY_URL = "https://polwro.com/f,chemicy,8";
    private static final String ELEKTRONICY_URL = "https://polwro.com/f,elektronicy,9";
    private static final String JEZYKOWCY_URL = "https://polwro.com/f,jezykowcy,10";
    private static final String HUMANISCI_URL = "https://polwro.com/f,humanisci,11";
    private static final String SPORTOWCY_URL = "https://polwro.com/f,sportowcy,12";
    private static final String POZOSTALI_URL = "https://polwro.com/f,inni,42";

    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        // INFO: wybierz potrzebne ci kategorie oraz nadaj im nazwy (zmiana nazw nie jest rekomendowana)
        List<String> categoryURLs = List.of(MATEMATYCY_URL, FIZYCY_URL, INFORMATYCY_URL, CHEMICY_URL, ELEKTRONICY_URL,
                JEZYKOWCY_URL, SPORTOWCY_URL, HUMANISCI_URL, POZOSTALI_URL);

        Scrapper scrapper = new Scrapper();
        scrapper.buildDatabaseScript(true, false, categoryURLs);
    }
}
