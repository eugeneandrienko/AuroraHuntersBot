package ru.aurorahunters.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.aurorahunters.bot.controller.GetDataFromDB;
import ru.aurorahunters.bot.graphbuilder.ArchiveTimeGraph;
import ru.aurorahunters.bot.graphbuilder.TimeGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;

public class MessageHandler {
    private Long chatID;
    private boolean isStarted = false;
    private boolean isTimezoneConfigured;
    private String timezone = "+00:00"; // default timezone if not configured
    private boolean isHistoryConfigured;
    private String archiveDate;
    private boolean isNotifConfigured;

    public MessageHandler(Long chatID) {
        this.chatID = chatID;
    }

    public MessageHandler(Long chatID, boolean isStarted, boolean isTimezoneConfigured, String timezone, boolean
            isHistoryConfigured, String archiveDate, boolean isNotifConfigured) {
        this.chatID = chatID;
        this.isStarted = isStarted;
        this.isTimezoneConfigured = isTimezoneConfigured;
        this.timezone = timezone;
        this.isHistoryConfigured = isHistoryConfigured;
        this.archiveDate = archiveDate;
        this.isNotifConfigured = isNotifConfigured;
    }

    public String setBotStarted() {
        isStarted = true;
        return getInfo();
    }

    public String respondMessage(String input) throws SQLException, ParseException, IOException, TelegramApiException {
        if (isStarted) {
            if (input.contains("/info") || input.equals("/info" + Config.getBot_username())) {
                return getInfo();
            }
            else if (input.contains("/start") || input.equals("/start" + Config.getBot_username())) {
                return "Bot is already started. Type /info to see available commands.";
            }
            else if (input.contains("/chat") || input.equals("/chat" + Config.getBot_username())) {
                return "Please join our community: \nhttps://t.me/aurora_ru";
            }
            else if (input.equals("/map") || input.equals("/map" + Config.getBot_username())) {
                return getAuroraMap();
            }
            else if (input.contains("/last") || input.equals("/last" + Config.getBot_username())) {
                return GetDataFromDB.getLastValues(timezone);
            }
            else if (input.contains("/time_settings") || input.equals("/time_settings" + Config.getBot_username())) {
                return setTimezone(input);
            }
            else if (input.equals("/notif_on") || input.equals("/notif_on" + Config.getBot_username())) {
                return setNotif(true);
            }
            else if (input.equals("/notif_off") || input.equals("/notif_off" + Config.getBot_username())) {
                return setNotif(false);
            }
            else if (input.contains("/weather") || input.equals("/weather" + Config.getBot_username())) {
                return getWeatherLinks();
            }
            else if (input.contains("/skycams") || input.equals("/skycams" + Config.getBot_username())) {
                return getCams();
            }
            else if (input.contains("/links") || input.equals("/links" + Config.getBot_username())) {
                return getLinks();
            }
            else if (input.equals("/graph_all") || input.equals("/graph_all" + Config.getBot_username())) {
                sendImage(TimeGraph.getBzGraph(timezone));
                sendImage(TimeGraph.getSpeedGraph(timezone));
                sendImage(TimeGraph.getDensityGraph(timezone));
            }
            else if (input.equals("/graph_bz") || input.equals("/graph_bz" + Config.getBot_username())) {
                sendImage(TimeGraph.getBzGraph(timezone));
            }
            else if (input.equals("/graph_speed") || input.equals("/graph_speed" + Config.getBot_username())) {
                sendImage(TimeGraph.getSpeedGraph(timezone));
            }
            else if (input.equals("/graph_density") || input.equals("/graph_density" + Config.getBot_username())) {
                sendImage(TimeGraph.getDensityGraph(timezone));
            }
            else if (isHistoryConfigured && input.matches("\\/\\w+") && !input.equals("/history")) {
                if (input.equals("/history_text") || input.equals("/history_text" + Config.getBot_username())) {
                    return getHistoryData();
                }
                else if (input.equals("/history_graph_bz") || input.equals("/history_graph_bz" + Config.getBot_username())) {
                    sendImage(ArchiveTimeGraph.getBzGraph(archiveDate));
                }
                else if (input.equals("/history_graph_speed") || input.equals("/history_graph_speed" + Config.getBot_username())) {
                    sendImage(ArchiveTimeGraph.getSpeedGraph(archiveDate));
                }
                else if (input.equals("/history_graph_density") || input.equals("/history_graph_density" + Config.getBot_username())) {
                    sendImage(ArchiveTimeGraph.getDensityGraph(archiveDate));
                }
                else if (input.equals("/history_graph_all") || input.equals("/history_graph_all" + Config.getBot_username())) {
                    sendImage(ArchiveTimeGraph.getBzGraph(archiveDate));
                    sendImage(ArchiveTimeGraph.getSpeedGraph(archiveDate));
                    sendImage(ArchiveTimeGraph.getDensityGraph(archiveDate));
                }
            }
            else if (input.contains("/history") || input.contains("/contains" + Config.getBot_username())) {
                return setHistoryDate(input);
            }
            return "";
        }
        else {
            return "Bot is is not started. Press /start to initialize it.";
        }
    }

    private String getInfo() {
        return "Hi, i'm AuroraHunters bot. <b>See my available commands below:</b>\n" +
                "/start to start the bot;\n" +
                "/stop to stop the bot;\n" +
                "/info to see this message;\n" +
                "/last to see last values from DSCOVR satellite;\n" +
                "/history to get old DSCOVR values;\n" +
                "/time_settings to change your timezone;\n" +
                "/notif_on to enable notifications;\n" +
                "/notif_off to disable notifications;\n" +
                "/links to get useful links\n";
    }

    public String setTimezone(String input) {
        if (input.contains("/time_settings") || input.equals("/time_settings" + Config.getBot_username())) {
            String regex = "^(?:Z|[+-](?:2[0-3]|[01][0-9]):([03][00]))$";
            String[] temp;
            String delimiter = " ";
            temp = input.split(delimiter);
            String command = temp[0];
            try {
                String argument = temp[1];
                if (argument.matches(regex)) {
                    isTimezoneConfigured = true;
                    timezone = argument;
                    setDbTimezone();
                    return "Your timezone now is <b>UTC" + argument + "</b>";
                } else {
                    return "Please type correct timezone, e.g. /time_settings +03:00";
                }
            } catch (Exception e) {
                if (!isTimezoneConfigured) {
                    return "<b>Timezone is not configured.</b> To configure it, just <b>share with me your GPS " +
                            "location.</b>\n" +
                            "If GPS is not suitable way, you can configure it manually by entering required timezone " +
                            "in UTC format: \n" +
                            "e.g. command for Moscow Standard Time will be /time_settings +03:00";
                } else if (isTimezoneConfigured) {
                    return "Configured timezone is <b>UTC" + timezone + ".</b> If you need to change it, " +
                            "just <b>share with me your GPS location</b> or enter it manually in UTC format: \n" +
                            "e.g. command for Moscow Standard Time will be /time_settings +03:00";
                }
            }
        }
        return "";
    }

    public String setGpsTimezone(String input) throws SQLException {
        isTimezoneConfigured = true;
        timezone = input;
        setDbTimezone();
        return "Your timezone now is <b>UTC" + timezone + "</b>";
    }

    private void setDbTimezone() throws SQLException {
        Config.getDbConnection().setAutoCommit(false);
        final String SQL = "UPDATE sessions SET is_timezone=?, timezone=? where chat_id=?;";
        PreparedStatement ps = Config.getDbConnection().prepareStatement(SQL);
        try {
            ps.setBoolean(1, true);
            ps.setString(2,timezone);
            ps.setLong(3, chatID);
            ps.executeUpdate();
            Config.getDbConnection().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private String setHistoryDate(String input) {
        if (input.contains("/history") || input.equals("/history" + Config.getBot_username())) {
            String regex = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
            String[] temp;
            String delimiter = " ";
            temp = input.split(delimiter);
            String command = temp[0];
            try {
                String argument = temp[1];
                if (argument.matches(regex)) {
                    isHistoryConfigured = true;
                    archiveDate = argument;
                    setDbHistoryDate();
                    return "All archive data will be shown for <b>" + archiveDate + ".</b>\n" +
                            "Highest 24 values will be shown for each hour.\n" +
                            "Please use following commands: \n" +
                            "/history_text - to get text table; \n" +
                            "/history_graph_bz - to get bz_gsm graph for " + archiveDate + "\n" +
                            "/history_graph_speed - to get speed graph for " + archiveDate + "\n" +
                            "/history_graph_density - to get density graph for " + archiveDate + "\n" +
                            "/history_graph_all - to get all three graphs for " + archiveDate + "\n";
                } else {
                    return "Please type correct date, e.g. /history yyyy-MM-dd format";
                }
            } catch (Exception e) {
                if (!isHistoryConfigured) {
                    return "Archive is not configured. Please enter required date in <b>yyyy-MM-dd</b> format: \n" +
                            "e.g. command for <b>July 01 of 2020</b> will be \n" +
                            "/history 2020-07-01";
                } else if (isHistoryConfigured) {
                    return "Archive is configured for<b> " + archiveDate + ".</b>\n If you need to change it, please " +
                            "type it in required format: \n" +
                            "e.g. command for <b>July 01 of 2020</b> will be \n/history 2020-07-01\n" +
                            "All archive data will be shown for <b>" + archiveDate + ".</b>\n" +
                            "Highest 24 values will be shown for each hour.\n" +
                            "Please use following commands: \n" +
                            "/history_text - to get text table; \n" +
                            "/history_graph_bz - to get bz_gsm graph for " + archiveDate + "\n" +
                            "/history_graph_speed - to get speed graph for " + archiveDate + "\n" +
                            "/history_graph_density - to get density graph for " + archiveDate + "\n" +
                            "/history_graph_all - to get all three graphs for " + archiveDate + "\n";
                }
            }
        }
        return "";
    }

    public void setDbHistoryDate() throws SQLException {
        Config.getDbConnection().setAutoCommit(false);
        final String SQL = "UPDATE sessions SET is_archive=?, archive=? where chat_id=?;";
        PreparedStatement ps = Config.getDbConnection().prepareStatement(SQL);
        try {
            ps.setBoolean(1, true);
            ps.setString(2,archiveDate);
            ps.setLong(3, chatID);
            ps.executeUpdate();
            Config.getDbConnection().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getHistoryData() throws SQLException, ParseException {
        if (isHistoryConfigured) {
            return GetDataFromDB.getHistoryValues(archiveDate);
        }
        else return "Archive is not configured. Please enter required date in <b>yyyy-MM-dd</b> format: \n" +
                "e.g. command for <b>July 01 of 2020</b> will be \n" +
                "/history 2020-07-01";
    }

    private String setNotif (boolean set) throws SQLException {
        if (isNotifConfigured && set) {
            return "Notifications already enabled.";
        }
        else if (!isNotifConfigured && set) {
            setDbNotif(true);
            return "Notifications has been enabled.";
        }
        else if (isNotifConfigured && !set) {
            setDbNotif(false);
            return "Notifications has been disabled.";
        }
        else if (!isNotifConfigured && !set) {
            return "Notifications already disabled.";
        }
        return "";
    }

    private void setDbNotif(boolean param) throws SQLException {
        Config.getDbConnection().setAutoCommit(false);
        final String SQL = "UPDATE sessions SET is_notif=? where chat_id=?;";
        PreparedStatement ps = Config.getDbConnection().prepareStatement(SQL);
        try {
            ps.setBoolean(1, param);
            ps.setLong(2, chatID);
            ps.executeUpdate();
            Config.getDbConnection().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getLinks() {
        return "Here is a lisot of some useful links which can help you:\n" +
                "/weather to get relevant weather links\n" +
                "/skycams to get sky webcam links\n" +
                "/map - to get aurora lights map\n" +
                "/chat - official project chat";
    }

    private String getWeatherLinks() {
        return "Weather links:\n" +
                "https://weather.us/model-charts/deu-hd/leningrad/total-cloud-coverage/2019-0z.html\n" +
                "https://kachelmannwetter.com/de/sat/leningrad/satellit-staub-15min/2019-0z.html\n" +
                "https://www.windy.com";
    }

    private String getCams() {
        return "Tampere (61.6316413,23.5501255)\n" +
                "https://www.ursa.fi/yhd/tampereenursa/Pics/latest-3.jpg\n" +
                "Hankasaalmi(62.38944,26.427378)\n" +
                "http://murtoinen.jklsirius.fi/ccd/skywatch/\n" +
                "Abisko (68.3494106 – 18.8212895)\n" +
                "https://auroraskystation.se/live/\n" +
                "Svalbard (78.6224431,15.572671)\n" +
                "https://aurorainfo.eu/aurora-live-cameras/svalbard-norway-all-sky-aurora-live-camera.jpg";
    }

    private String getAuroraMap() {
        return "Aurora map in north Europe: \n" +
                "http://auroralights.ru/%d0%ba%d0%b0%d1%80%d1%82%d0%b0-%d0%b3%d0%b4%d0%b5-%d1%81%d0%bc%d0%be%d1%82%d1%" +
                "80%d0%b5%d1%82%d1%8c-%d1%81%d0%b5%d0%b2%d0%b5%d1%80%d0%bd%d1%8b%d0%b5-%d1%81%d0%b8%d1%8f%d0%bd%d0%b8%d1%8f/";
    }

    private void sendImage(File image) throws FileNotFoundException, TelegramApiException {
        AuroraBot sendGraph = new AuroraBot();
        SendPhoto graph = new SendPhoto()
                .setPhoto("AuroraHuntersBot_Graph", new FileInputStream(image))
                .setChatId(chatID);
        sendGraph.execute(graph);
    }
}
