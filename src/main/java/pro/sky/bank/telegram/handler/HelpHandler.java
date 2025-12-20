package pro.sky.bank.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class HelpHandler {

    public void sendHelpMessage(Long chatId, TelegramLongPollingBot bot) {
        String help = """
            **Единственная команда:**
                `/recommend username`
           """;

        send(chatId, help, bot);
    }

    public void sendUnknownCommand(Long chatId, TelegramLongPollingBot bot) {
        send(chatId, " Unknown command. USE /help", bot);
    }

    private void send(Long chatId, String text, TelegramLongPollingBot bot) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}