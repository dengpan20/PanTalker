package net.dengpan.web.pantalker.push;

import net.dengpan.web.pantalker.push.provider.AuthRequestFilter;
import net.dengpan.web.pantalker.push.provider.GsonProvider;
import net.dengpan.web.pantalker.push.service.AccountService;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

/**
 * 项目应用类
 *
 * @author pan dengpan
 * @create 2018/5/29
 */
public class Application extends ResourceConfig {
    public Application() {
        packages(AccountService.class.getPackage().getName());
        System.out.print(AccountService.class.getPackage().getName());
        register(GsonProvider.class);
        //注册全局得拦截器
        register(AuthRequestFilter.class);

        register(Logger.class);
    }
}
