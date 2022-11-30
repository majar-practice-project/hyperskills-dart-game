// very cool task, try my best

//package dartsgame.auth;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.NoOpPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class WebAuthenticationConfiguration {
//
//    @Bean
//    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
//        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
//        authenticationManagerBuilder.inMemoryAuthentication()
//                .withUser("ivanhoe@acme.com").password(passwordEncoder().encode("oMoa3VvqnLxW")).roles("GAMER").and()
//                .withUser("robinhood@acme.com").password(passwordEncoder().encode("ai0y9bMvyF6G")).roles("GAMER").and()
//                .withUser("wilhelmtell@acme.com").password(passwordEncoder().encode("bv0y9bMvyF7E")).roles("GAMER").and()
//                .withUser("admin@acme.com").password(passwordEncoder().encode("zy0y3bMvyA6T")).roles("ADMIN");
//        return authenticationManagerBuilder.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance();
//    }
//
////    @Bean
////    SecurityFilterChain configureAuthorization(HttpSecurity http, AuthenticationManager manager) throws Exception {
////        http.csrf().disable().authorizeHttpRequests(a -> a
////                        .mvcMatchers("/**").permitAll()
//////                        .mvcMatchers("/user").hasAnyRole("ADMIN", "USER")
//////                        .mvcMatchers("/", "/public").permitAll()
//////                        .mvcMatchers("/**").authenticated() // or .anyRequest().authenticated()
////                )
////                .httpBasic()
////                ;
////        return http.build();
////    }
//}
