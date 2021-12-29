package ru.aurorahunters.bot;

import java.util.Locale;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.aurorahunters.bot.telegram.AuroraBot;
import ru.aurorahunters.bot.utils.GPSUtils;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        Config.loadConfig();
        Locale.setDefault(new Locale("en", "UK"));
        GPSUtils.initializeZoneEngine();
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new AuroraBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        out.println("Bot started.");
        Config.initializeSchedulers();
    }
}
