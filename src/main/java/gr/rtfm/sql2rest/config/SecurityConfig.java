package gr.rtfm.sql2rest.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import gr.rtfm.sql2rest.utils.KeystoreUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KeystoreUtils keystoreUtils;

    @Value("${password.safe:keystore}")
    private String passwordSafe;

    @Value("${spring.datasource.password}")
    private String passwordAlias;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic();
        
        return http.build();
    }

    Map<String, String> getUsers() {
        Map<String, String> users = new HashMap<>();
        List<String> aliases = keystoreUtils.getAliases();
        log.info("Retrieved {} aliases from keystore", aliases.size());
        if ("keystore".equals(passwordSafe)) {
            aliases.removeIf(alias -> alias == null || alias.trim().isEmpty() || alias.equals(passwordAlias) || keystoreUtils.getPassword(alias).isEmpty());
        }
        for (String alias : aliases) 
        {
            if ("keystore".equals(passwordSafe)) {
                if (alias == null || alias.trim().isEmpty() || alias.equals(passwordAlias) )
                    continue;
                String password = keystoreUtils.getPassword(alias);
                if (password == null || password.trim().isEmpty())
                    continue;
            }
            log.info("Found alias '{}' with password", alias);
            users.put(alias, keystoreUtils.getPassword(alias));
        }
        return users;
    }

    @Bean
    public UserDetailsService userDetailsService() {

        Map<String, String> users = getUsers();

        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        users.forEach((username, password) -> {
            UserDetails userDetails = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles("API_USER")
                .build();
            userDetailsManager.createUser(userDetails);
        });

        return userDetailsManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
