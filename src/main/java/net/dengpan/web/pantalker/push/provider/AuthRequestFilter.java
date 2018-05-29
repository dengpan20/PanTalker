package net.dengpan.web.pantalker.push.provider;

import com.google.common.base.Strings;
import net.dengpan.web.pantalker.push.bean.api.base.ResponseModel;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.factory.UserFactory;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

/**
 * 所有请求的接口的拦截器
 *
 * @author pan dengpan
 * @create 2018/5/29
 */
public class AuthRequestFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String relationPath = ((ContainerRequest)requestContext).getPath(false);
        //如果请求是登陆或者 注册不做拦截 直接返回
        if(relationPath.startsWith("account/login")|| relationPath.startsWith("account/register")){
            return;
        }
        String token = requestContext.getHeaders().getFirst("token");
        if(!Strings.isNullOrEmpty(token)){
            User self = UserFactory.findByToken(token);
            // 给当前请求添加一个上下文
            if(self != null){
                requestContext.setSecurityContext(new SecurityContext() {
                    //设置一个User 的上下文
                    @Override
                    public Principal getUserPrincipal() {
                        return self;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        // 可以在这里写入用户的权限，role 是权限名，
                        // 可以管理管理员权限等等
                        return true;
                    }

                    @Override
                    public boolean isSecure() {
                        // 默认false即可，HTTPS
                        return false;
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return null;
                    }
                });
                //上下文传入User 后就可以返回
                return;
            }

        }

        //需要一个直接返回 需要登陆的Model
        ResponseModel model = ResponseModel.buildAccountError();
        Response response = Response.status(Response.Status.OK)
                .entity(model)
                .build();
        // 拦截，停止一个请求的继续下发，调用该方法后之间返回请求
        // 不会走到Service中去
        requestContext.abortWith(response);
    }
}
