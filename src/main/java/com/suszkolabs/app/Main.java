package com.suszkolabs.app;

import com.suszkolabs.scrapper.Scrapper;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        Scrapper scrapper = new Scrapper();

        scrapper.buildDatabaseScript(false, true);
    }
}
