package pro.sky.bank.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import pro.sky.bank.controller.SimpleStatsController;

import java.util.Map;

@Component
public class StatsHandler {

    private final SimpleStatsController statsController;

    public StatsHandler(SimpleStatsController statsController) {
        this.statsController = statsController;
    }

    public void sendRuleStats(Long chatId, TelegramLongPollingBot bot) {
        try {
            Map<String, Object> stats = statsController.getRuleStats();

            StringBuilder message = new StringBuilder();
            message.append("📊 **Статистика правил:**\n\n");

            message.append("• Всего правил сработало: ").append(stats.get("totalRulesTriggered")).append("\n");
            message.append("• Всего срабатываний: ").append(stats.get("totalTriggerCount")).append("\n");

            // Топ 3 правила
            @SuppressWarnings("unchecked")
            Map<String, Integer> topRules = (Map<String, Integer>) stats.get("topRules");

            if (topRules != null && !topRules.isEmpty()) {
                message.append("\n**Топ правил:**\n");
                int counter = 1;
                for (Map.Entry<String, Integer> entry : topRules.entrySet()) {
                    message.append(counter).append(". ").append(entry.getKey())
                            .append(": ").append(entry.getValue()).append(" раз\n");
                    counter++;
                }
            }

            sendMessage(chatId, message.toString(), bot);

        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка получения статистики", bot);
        }
    }

    public void sendSystemInfo(Long chatId, TelegramLongPollingBot bot) {
        try {
            Map<String, String> info = statsController.getSystemInfo();

            String message = String.format("""
                    🖥️ **Информация о системе:**
                    
                    • **Название:** %s
                    • **Версия:** %s
                    • **Окружение:** %s
                    • **Время работы:** %s
                    • **Описание:** %s
                    """,
                    info.get("name"),
                    info.get("version"),
                    info.get("environment"),
                    info.get("uptime"),
                    info.get("description")
            );

            sendMessage(chatId, message, bot);

        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка получения информации о системе", bot);
        }
    }

    private void sendMessage(Long chatId, String text, TelegramLongPollingBot bot) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            message.setParseMode("Markdown");
            bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
