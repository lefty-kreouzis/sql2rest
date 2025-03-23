package gr.rtfm.sql2rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

import gr.rtfm.sql2rest.utils.AESUtils;
import gr.rtfm.sql2rest.utils.KeystoreUtils;

@Configuration
@ComponentScan("gr.rtfm.slm")
@PropertySource("classpath:application.properties")
@PropertySource(ignoreResourceNotFound = true, value= "classpath:git.properties")
@EnableTransactionManagement
@EnableScheduling
public class SQL2RestConfiguration {
    private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    
    @Autowired
	Environment env;

    @Autowired
    AESUtils aesUtils;

    @Autowired
    KeystoreUtils keystoreUtils;

    @Value("${password.safe:keystore}")
    String passwordSafe;

    @Bean
	@Primary
	public javax.sql.DataSource dataSource() {

		HikariDataSource driverManagerDataSource = new HikariDataSource();
		

		driverManagerDataSource.setDriverClassName(env.getProperty("spring.datasource.driverClassName"));
		String url = env.getProperty("spring.datasource.url");
        driverManagerDataSource.setJdbcUrl(url);
		String username = env.getProperty("spring.datasource.username");
        driverManagerDataSource.setUsername(username);

		String password = env.getProperty("spring.datasource.password");
		if ( StringUtils.isEmpty(password) )
		{
			log.error("password is empty");
		}
        switch (passwordSafe) {
            case "keystore":
                password = keystoreUtils.getPassword(password);
                break;
            case "aes":
                password = aesUtils.decrypt(password);
                break;
            default:
                log.warn("Unknown password safe: {} assume plaintext password",passwordSafe);
                break;
        }
		driverManagerDataSource.setPassword(password);

        log.info("dataSource(): url:"+url+" username:"+username);

		return driverManagerDataSource;
	}

}
