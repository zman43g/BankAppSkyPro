package pro.sky.bank.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import pro.sky.bank.model.dto.DynamicRuleResponse;
import pro.sky.bank.service.DynamicRuleService;

import java.util.List;

@Component
public class RuleHandler {

    private final DynamicRuleService dynamicRuleService;

    public RuleHandler(DynamicRuleService dynamicRuleService) {
        this.dynamicRuleService = dynamicRuleService;
    }

    public void showRules(Long chatId, TelegramLongPollingBot bot) {
        try {
            List<DynamicRuleResponse> rules = dynamicRuleService.getAllRules();

            if (rules.isEmpty()) {
                send(chatId, "📋 Нет правил", bot);
                return;
            }

            StringBuilder message = new StringBuilder("📋 **Правила:**\n\n");

            for (DynamicRuleResponse rule : rules) {
                message.append("• ").append(rule.getProductName()).append("\n");
                message.append("  ID: ").append(rule.getProductId()).append("\n\n");
            }

            send(chatId, message.toString(), bot);

        } catch (Exception e) {
            send(chatId, "❌ Ошибка: " + e.getMessage(), bot);
        }
    }


    public void showAddForm(Long chatId, TelegramLongPollingBot bot) {
        String form = "Чтобы добавить правило, отправьте JSON в чат";
        send(chatId, form, bot);
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