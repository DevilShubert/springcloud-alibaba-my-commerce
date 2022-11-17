package cn.lzr.ecommerce.conf;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

    private final AdminServerProperties adminServer;

    // 用于获取应用上下文
    public SecuritySecureConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 认证成功时的处理器
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

        http.authorizeRequests((authorizeRequests) ->{
            try {
                authorizeRequests
                        // 1. 配置所有的静态资源和登录页可以公开访问
                        .antMatchers(this.adminServer.path("/assets/**")).permitAll()
                        .antMatchers(this.adminServer.path("/actuator/info")).permitAll()
                        .antMatchers(this.adminServer.path("/login")).permitAll()
                        // 2. 其他请求, 必须要经过认证
                        .anyRequest().authenticated()
                        .and()
                        // 3. 配置登录和登出路径
                        .formLogin().loginPage(this.adminServer.path("/login"))
                        .successHandler(successHandler)
                        .and()
                        .logout().logoutUrl(this.adminServer.path("/logout"))
                        .and()
                        // 4. 开启 http basic 支持, 其他的服务模块注册时需要使用
                        .httpBasic()
                        .and()
                        .csrf((csrf) ->
                                // 5. 开启基于 cookie 的 csrf 保护（仅仅是在SBA server）
                            csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).ignoringRequestMatchers(
                                    // 6. 忽略这些路径的 csrf 保护以便其他的（服务）可以实现注册
                                    new AntPathRequestMatcher(this.adminServer.path("/instances"),
                                            HttpMethod.POST.toString()),
                                    new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
                                            HttpMethod.DELETE.toString()),
                                    new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))
                        ));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
