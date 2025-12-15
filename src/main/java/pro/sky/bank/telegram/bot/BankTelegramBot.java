package pro.sky.bank.telegram.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import pro.sky.bank.telegram.handler.CommandHandler;

@Component
public class BankTelegramBot extends TelegramLongPollingBot {

    private final CommandHandler commandHandler;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    public BankTelegramBot(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        commandHandler.handle(update, this);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}