package com.yourapp.expensetracker.expense_api.config;

import com.yourapp.expensetracker.expense_api.security.CustomUserDetailsService;
import com.yourapp.expensetracker.expense_api.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // FRONTEND FILES
                .requestMatchers(mvcMatcherBuilder.pattern("/")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/index.html")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/frontend/**")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/login.html")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/register.html")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/dashboard.html")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/style.css")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/auth.js")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/login.js")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/register-script.js")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/script.js")).permitAll()

                // AUTH API
                .requestMatchers(mvcMatcherBuilder.pattern("/api/auth/**")).permitAll()

                // PROTECTED APIS
                .requestMatchers(mvcMatcherBuilder.pattern("/api/expenses/**")).authenticated()
                .requestMatchers(mvcMatcherBuilder.pattern("/api/budgets/**")).authenticated()
                .requestMatchers(mvcMatcherBuilder.pattern("/api/reports/**")).authenticated()
                .requestMatchers(mvcMatcherBuilder.pattern("/api/budget-alerts/**")).authenticated()
                .requestMatchers(mvcMatcherBuilder.pattern("/api/categories/**")).authenticated()

                .anyRequest().permitAll()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
