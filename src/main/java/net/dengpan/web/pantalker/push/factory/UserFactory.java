package net.dengpan.web.pantalker.push.factory;

import net.dengpan.web.pantalker.push.bean.db.User;

/**
 * 操作用户数据的工厂类
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
public class UserFactory {

    // 通过Token字段查询用户信息
    // 只能自己使用，查询的信息是个人信息，非他人信息
    public static User findByToken(String token) {

        return null;
    }

    public static User findByPhone(String trim) {
        return  null;
    }

    public static User findByName(String name) {
        return  null;
    }

    public static User register(String account, String password, String name) {
        return  null;
    }

    public static User bindPushId(User self, String pushId) {
        return null;
    }
}
