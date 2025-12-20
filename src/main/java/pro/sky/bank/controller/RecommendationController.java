package pro.sky.bank.controller;

import pro.sky.bank.model.RecommendationResponse;
import pro.sky.bank.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Предоставляет персонализированные рекомендации для указанного пользователя.
     * <p>
     * Рекомендации формируются на основе оценки как статических, так и динамических бизнес-правил,
     * связанных с профилем и действиями пользователя в системе.
     * </p>
     *
     * @param userId Строковый идентификатор пользователя, который должен быть корректным UUID.
     * @return {@link ResponseEntity} с телом {@link RecommendationResponse}, содержащим список рекомендаций.
     *         Возвращает статус 200 (OK) при успешном выполнении.
     * @throws IllegalArgumentException если параметр {@code userId} не является валидной строкой UUID.
     *         В этом случае возвращается статус 400 (Bad Request).
     * @see RecommendationService#getRecommendations(UUID)
     */

    @GetMapping("/{userId}")
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable String userId) {
        try {
            UUID uuid = UUID.fromString(userId);
            RecommendationResponse response = recommendationService.getRecommendations(uuid);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
