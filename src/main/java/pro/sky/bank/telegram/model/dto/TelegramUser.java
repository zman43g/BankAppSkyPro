package pro.sky.bank.telegram.model.dto;

import pro.sky.bank.telegram.model.entity.TelegramUserEntity;
import java.time.LocalDateTime;

public class TelegramUser {

    private Long id;
    private Long chatId;
    private String username;
    private String firstName;
    private String lastName;
    private String languageCode;
    private Boolean isBot;
    private LocalDateTime registeredAt;
    private LocalDateTime lastActive;
    private Boolean isActive;
    private Boolean notificationEnabled;
    private TelegramUserEntity.UserRole role;


    public TelegramUser() {
    }

    public TelegramUser(Long chatId, String username) {
        this.chatId = chatId;
        this.username = username;
        this.registeredAt = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
        this.isActive = true;
        this.notificationEnabled = true;
        this.role = TelegramUserEntity.UserRole.USER;
    }


    public static TelegramUser fromEntity(TelegramUserEntity entity) {
        TelegramUser dto = new TelegramUser();
        dto.setId(entity.getId());
        dto.setChatId(entity.getChatId());
        dto.setUsername(entity.getUsername());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setLanguageCode(entity.getLanguageCode());
        dto.setIsBot(entity.getIsBot());
        dto.setRegisteredAt(entity.getRegisteredAt());
        dto.setLastActive(entity.getLastActive());
        dto.setIsActive(entity.getIsActive());
        dto.setNotificationEnabled(entity.getNotificationEnabled());
        dto.setRole(entity.getRole());
        return dto;
    }


    public TelegramUserEntity toEntity() {
        TelegramUserEntity entity = new TelegramUserEntity();
        entity.setId(this.id);
        entity.setChatId(this.chatId);
        entity.setUsername(this.username);
        entity.setFirstName(this.firstName);
        entity.setLastName(this.lastName);
        entity.setLanguageCode(this.languageCode);
        entity.setIsBot(this.isBot);
        entity.setRegisteredAt(this.registeredAt);
        entity.setLastActive(this.lastActive);
        entity.setIsActive(this.isActive);
        entity.setNotificationEnabled(this.notificationEnabled);
        entity.setRole(this.role);
        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Boolean getIsBot() {
        return isBot;
    }

    public void setIsBot(Boolean isBot) {
        this.isBot = isBot;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getLastActive() {
        return lastActive;
    }

    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public TelegramUserEntity.UserRole getRole() {
        return role;
    }

    public void setRole(TelegramUserEntity.UserRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "TelegramUser{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", registeredAt=" + registeredAt +
                ", lastActive=" + lastActive +
                ", isActive=" + isActive +
                ", role=" + role +
                '}';
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username != null ? "@" + username : "Unknown";
        }
    }

    public boolean isAdmin() {
        return TelegramUserEntity.UserRole.ADMIN.equals(this.role);
    }

    public boolean isModerator() {
        return TelegramUserEntity.UserRole.MODERATOR.equals(this.role) || isAdmin();
    }
}
