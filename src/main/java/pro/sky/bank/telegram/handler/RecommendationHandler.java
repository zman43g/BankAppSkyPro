package pro.sky.bank.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pro.sky.bank.service.RecommendationService;

import java.util.UUID;

@Component
public class RecommendationHandler {

    private final RecommendationService recommendationService;

    public RecommendationHandler(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public void sendRecommendations(Long chatId, TelegramLongPollingBot bot) {
        try {
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            Object result = recommendationService.getRecommendations(userId);

            // Просто выводим результат как строку
            String text = " **Рекомендации:**\n\n" +
                    (result != null ? result.toString() : "Нет рекомендаций");

            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            message.setParseMode("Markdown");

            bot.execute(message);
        } catch (Exception e) {
            try {
                SendMessage error = new SendMessage();
                error.setChatId(chatId.toString());
                error.setText("❌ Ошибка при получении рекомендаций");
                bot.execute(error);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendErrorMessage(Long chatId, String text, TelegramLongPollingBot bot) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("❌ " + text);
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
