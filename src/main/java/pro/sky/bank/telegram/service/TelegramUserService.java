package pro.sky.bank.telegram.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.bank.telegram.model.dto.TelegramUser;
import pro.sky.bank.telegram.model.entity.TelegramUserEntity;
import pro.sky.bank.telegram.model.repository.TelegramUserRepository;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TelegramUserService {

    private final TelegramUserRepository userRepository;

    public TelegramUserService(TelegramUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Регистрация или обновление пользователя
     */
    public TelegramUser registerOrUpdateUser(Long chatId, User telegramUser) {
        Optional<TelegramUserEntity> existingUser = userRepository.findByChatId(chatId);

        if (existingUser.isPresent()) {
            TelegramUserEntity entity = existingUser.get();
            updateUserInfo(entity, telegramUser);
            entity.setLastActive(LocalDateTime.now());
            TelegramUserEntity savedEntity = userRepository.save(entity);
            return TelegramUser.fromEntity(savedEntity);
        } else {
            TelegramUserEntity newEntity = createUserEntity(chatId, telegramUser);
            TelegramUserEntity savedEntity = userRepository.save(newEntity);
            return TelegramUser.fromEntity(savedEntity);
        }
    }

    public Optional<TelegramUser> getUserByChatId(Long chatId) {
        return userRepository.findByChatId(chatId)
                .map(TelegramUser::fromEntity);
    }


    public Optional<TelegramUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(TelegramUser::fromEntity);
    }


    public List<TelegramUser> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue()
                .stream()
                .map(TelegramUser::fromEntity)
                .collect(Collectors.toList());
    }

    public void updateNotificationStatus(Long chatId, boolean enabled) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.setNotificationEnabled(enabled);
            userRepository.save(user);
        });
    }

    public void updateUserRole(Long chatId, TelegramUserEntity.UserRole role) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
    }


    public void deactivateUser(Long chatId) {
        userRepository.updateIsActive(chatId, false);
    }


    public void activateUser(Long chatId) {
        userRepository.updateIsActive(chatId, true);
    }


    public UserStatistics getStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByIsActiveTrue().size();
        long adminUsers = userRepository.findByRole(TelegramUserEntity.UserRole.ADMIN).size();

        return new UserStatistics(totalUsers, activeUsers, adminUsers);
    }


    private TelegramUserEntity createUserEntity(Long chatId, User telegramUser) {
        TelegramUserEntity entity = new TelegramUserEntity();
        entity.setChatId(chatId);
        entity.setUsername(telegramUser.getUserName());
        entity.setFirstName(telegramUser.getFirstName());
        entity.setLastName(telegramUser.getLastName());
        entity.setLanguageCode(telegramUser.getLanguageCode());
        entity.setIsBot(telegramUser.getIsBot());
        entity.setRegisteredAt(LocalDateTime.now());
        entity.setLastActive(LocalDateTime.now());
        entity.setIsActive(true);
        entity.setNotificationEnabled(true);

        return entity;
    }

    private void updateUserInfo(TelegramUserEntity entity, User telegramUser) {
        entity.setUsername(telegramUser.getUserName());
        entity.setFirstName(telegramUser.getFirstName());
        entity.setLastName(telegramUser.getLastName());
        entity.setLanguageCode(telegramUser.getLanguageCode());
    }


    public static class UserStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long adminUsers;

        public UserStatistics(long totalUsers, long activeUsers, long adminUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.adminUsers = adminUsers;
        }

        public long getTotalUsers() {
            return totalUsers;
        }

        public long getActiveUsers() {
            return activeUsers;
        }

        public long getAdminUsers() {
            return adminUsers;
        }

        @Override
        public String toString() {
            return String.format("Total: %d, Active: %d, Admins: %d",
                    totalUsers, activeUsers, adminUsers);
        }
    }
}