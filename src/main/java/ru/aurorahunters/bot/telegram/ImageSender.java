package ru.aurorahunters.bot.telegram;

import java.io.File;
import java.io.FileNotFoundException;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

class ImageSender {

    private final String chatId;

    ImageSender(Long chatId) {
        this.chatId = chatId.toString();
    }

    /**
     * Method which sends generated graph to user.
     *
     * @param image is a generated and retrieved as a .png file TimeGraph image chart.
     */
    void sendImage(File image) throws FileNotFoundException, TelegramApiException {
        AuroraBot sendGraph = new AuroraBot();
        SendPhoto graph = new SendPhoto();
        InputFile inputFile = new InputFile(image);

        graph.setCaption("AuroraHuntersBot_Graph");
        graph.setPhoto(inputFile);
        graph.setChatId(chatId);
        sendGraph.execute(graph);
    }
}
