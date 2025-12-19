package pro.sky.bank.telegram.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.bank.telegram.model.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface TelegramUserRepository extends JpaRepository<TelegramUserEntity, Long> {

    boolean existsByChatId(Long chatId);

    Optional<TelegramUserEntity> findByChatId(Long chatId);

    Optional<TelegramUserEntity> findByUsername(String username);

    List<TelegramUserEntity> findByIsActiveTrue();

    List<TelegramUserEntity> findByNotificationEnabledTrueAndIsActiveTrue();


    List<TelegramUserEntity> findByRole(TelegramUserEntity.UserRole role);

    @Modifying
    @Query("UPDATE TelegramUserEntity u SET u.lastActive = :lastActive WHERE u.chatId = :chatId")
    void updateLastActive(@Param("chatId") Long chatId, @Param("lastActive") LocalDateTime lastActive);

    @Modifying
    @Query("UPDATE TelegramUserEntity u SET u.isActive = :isActive WHERE u.chatId = :chatId")
    void updateIsActive(@Param("chatId") Long chatId, @Param("isActive") Boolean isActive);


}