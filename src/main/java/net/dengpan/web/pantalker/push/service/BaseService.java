package net.dengpan.web.pantalker.push.service;

import net.dengpan.web.pantalker.push.bean.db.User;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 * 服务类基类
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
public class BaseService {
    @Context
    protected SecurityContext securityContext;


    /**
     * 从上下文中取出 User
     * @return
     */
    protected User getSelf(){
        return (User) securityContext.getUserPrincipal();
    }
}
