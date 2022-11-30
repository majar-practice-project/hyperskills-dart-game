package dartsgame.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("ivanhoe@acme.com").password(passwordEncoder().encode("oMoa3VvqnLxW")).roles("GAMER").and()
                .withUser("robinhood@acme.com").password(passwordEncoder().encode("ai0y9bMvyF6G")).roles("GAMER").and()
                .withUser("wilhelmtell@acme.com").password(passwordEncoder().encode("bv0y9bMvyF7E")).roles("GAMER").and()
                .withUser("admin@acme.com").password(passwordEncoder().encode("zy0y3bMvyA6T")).roles("ADMIN", "GAMER").and()
                .withUser("judgedredd@acme.com").password(passwordEncoder().encode("iAmALaw100500")).roles("REFEREE");
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable().authorizeHttpRequests(a -> a
//                        .mvcMatchers("/***").permitAll()
//                        .mvcMatchers("/user").hasAnyRole("ADMIN", "USER")
//                        .mvcMatchers("/", "/public").permitAll()
//                        .mvcMatchers("/**").authenticated() // or .anyRequest().authenticated()
//                )
//                .httpBasic();
//    }
}
