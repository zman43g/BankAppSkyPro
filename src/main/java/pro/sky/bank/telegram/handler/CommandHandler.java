package pro.sky.bank.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandHandler {

    private final RecommendationHandler recommendationHandler;
    private final HelpHandler helpHandler;


    public CommandHandler(RecommendationHandler recommendationHandler,
                          HelpHandler helpHandler) {
        this.recommendationHandler = recommendationHandler;
        this.helpHandler = helpHandler;

    }

    public void handle(Update update, TelegramLongPollingBot bot) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().toLowerCase();
            Long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                case "/help":
                    helpHandler.sendHelpMessage(chatId, bot);
                    break;
                case "/recommendations":
                    recommendationHandler.sendRecommendations(chatId, bot);
                    break;
                default:
                    helpHandler.sendUnknownCommand(chatId, bot);
            }
        }
    }
}
