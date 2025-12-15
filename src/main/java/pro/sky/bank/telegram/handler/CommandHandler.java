package pro.sky.bank.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandHandler {

    private final RecommendationHandler recommendationHandler;
    private final RuleHandler ruleHandler;
    private final HelpHandler helpHandler;
    private final StatsHandler statsHandler;

    public CommandHandler(RecommendationHandler recommendationHandler,
                          RuleHandler ruleHandler,
                          HelpHandler helpHandler,
                          StatsHandler statsHandler) {
        this.recommendationHandler = recommendationHandler;
        this.ruleHandler = ruleHandler;
        this.helpHandler = helpHandler;
        this.statsHandler = statsHandler;
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
                case "/rules":
                    ruleHandler.showRules(chatId, bot);
                    break;
                case "/stats":
                case "/статистика":
                    statsHandler.sendRuleStats(chatId, bot);
                    break;
                case "/system":
                case "/система":
                    statsHandler.sendSystemInfo(chatId, bot);
                    break;
                default:
                    helpHandler.sendUnknownCommand(chatId, bot);
            }
        }
    }
}
