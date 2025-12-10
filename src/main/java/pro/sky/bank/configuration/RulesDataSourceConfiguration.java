package pro.sky.bank.configuration;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        basePackages = "pro.sky.bank.repository",
        entityManagerFactoryRef = "rulesEntityManagerFactory",
        transactionManagerRef = "rulesTransactionManager"
)
public class RulesDataSourceConfiguration {

    @Primary
    @Bean(name = "rulesDataSource")
    public DataSource rulesDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:h2:file:./rules.mv.db;MODE=PostgreSQL")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("")
                .build();
    }

    @Bean(name = "rulesJdbcTemplate")
    public JdbcTemplate rulesJdbcTemplate() {
        return new JdbcTemplate(rulesDataSource());
    }

    @Bean(name = "rulesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean rulesEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(rulesDataSource());
        em.setPackagesToScan("pro.sky.bank.model.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");

        em.setJpaProperties(properties);
        return em;
    }

    @Bean(name = "rulesTransactionManager")
    public PlatformTransactionManager rulesTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(rulesEntityManagerFactory().getObject());
        return transactionManager;
    }
}