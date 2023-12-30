package net.fileme.config;

import net.fileme.filter.JwtAuthenticationFilter;
import net.fileme.handler.AccessDeniedExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
//    @Value("${server.port}")
//    private int httpsPort;
//    @Value("${http.port}")
//    private int httpPort;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new AccessDeniedExceptionHandler();
    }
    @Bean
    public CorsConfigurationSource configurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        // 允許跨域的domain
        // Arrays.asList("放網址"), for local可設定null, 注意格式, load-balancer domain不可單獨存在
        configuration.setAllowedOrigins(Arrays.asList("https://www.filesme.net","https://filesme.net","https://service.filesme.net"));
        // 允許的請求方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        // 是否可帶憑證
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        // 對哪些url開放
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return urlBasedCorsConfigurationSource;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors().configurationSource(configurationSource()).and() // from gpt
                .csrf().disable()// 關閉csrf
                // 不通過session取得securityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // allow HTTPS
//                .requiresChannel().anyRequest().requiresSecure()
//                .and()
                .authorizeRequests()
                 // 感覺會有一個("/**").hasAuthority("admin")
                .antMatchers("/user/login").anonymous()
                .antMatchers("/support/**").anonymous()
                .antMatchers("/health-check").permitAll() // for AWS checking
                .antMatchers("/pub/**").permitAll()
                .antMatchers("/**/*.html").permitAll()
                .antMatchers("/**/*.css").permitAll()
                .antMatchers("/**/*.js").permitAll()
                .antMatchers("/**/api-docs/**").permitAll()
                .antMatchers("/logo.png").permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                        .loginPage("/index.html") // TODO: change to real login page
                        .failureUrl("/access-denied")
                        .permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler());
//                .accessDeniedPage("/access-denied")

        // allow HTTP redirect to HTTPS
//        http.portMapper().http(httpPort).mapsTo(httpsPort);

        // 加入自定義JWT過濾器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
