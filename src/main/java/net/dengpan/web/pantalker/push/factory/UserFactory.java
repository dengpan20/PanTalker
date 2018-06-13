package net.dengpan.web.pantalker.push.factory;

import com.google.common.base.Strings;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.bean.db.UserFollow;
import net.dengpan.web.pantalker.push.utils.Hib;
import net.dengpan.web.pantalker.push.utils.TextUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return Hib.query(session -> (User)session
        .createQuery("from User where token=:token")
        .setParameter("token",token)
        .uniqueResult());
    }

    public static User findByPhone(String phone) {
        return  Hib.query(session -> (User)session
        .createQuery("from User where phone=:inPhone")
        .setParameter("inPhone",phone)
        .uniqueResult());
    }

    public static User findByName(String name) {
        return  Hib.query(session -> (User)session
                .createQuery("from User where name=:name")
                .setParameter("name",name)
                .uniqueResult());
    }

    public static User register(String account, String password, String name) {
        account = account.trim();
        password = encodePassword(password);
        User user =createUser(account,password,name);

        if(user != null){
            user = login(user);
        }
        return  user;
    }

    /**
     * 注册保存新建用户
     * @param account
     * @param password
     * @param name
     * @return
     */
    private static User createUser(String account, String password, String name) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setPhone(account);

        return Hib.query(session -> {
            session.save(user);
            return user;
        });
    }

    public static User bindPushId(User user, String pushId) {
        if(Strings.isNullOrEmpty(pushId)){
            return null;
        }
        // 第一步，查询是否有其他账户绑定了这个设备
        // 取消绑定，避免推送混乱
        // 查询的列表不能包括自己
        Hib.queryOnly(session -> {
            List<User> userList=(List<User>) session
                    .createQuery("from User where lower(pushId)=:pushId and id!=:userId")
                    .setParameter("pushId",pushId.toLowerCase())
                    .setParameter("userId",user.getId())
                    .list();
            for(User u: userList){
                u.setPushId(null);
                session.saveOrUpdate(u);
            }
        });
        if(pushId.equalsIgnoreCase(user.getPushId())){
            // 如果当前需要绑定的设备Id，之前已经绑定过了
            // 那么不需要额外绑定

            return user;
        }else {
            // 如果当前账户之前的设备Id，和需要绑定的不同
            // 那么需要单点登录，让之前的设备退出账户，
            // 给之前的设备推送一条退出消息
            if (Strings.isNullOrEmpty(user.getPushId())) {
                // 推送一个退出消息
                PushFactory.pushLogout(user, user.getPushId());
            }
            user.setPushId(pushId);
            return update(user);
        }
    }

    public static User login(String account, String password) {
        final String accountStr = account.trim();
        final String encodePassword = encodePassword(password);

        User user = Hib.query(session -> (User) session
                .createQuery("from User where phone=:phone and password=:password")
                .setParameter("phone", accountStr)
                .setParameter("password", encodePassword)
                .uniqueResult());

        if(user != null){
            user = login(user);
        }
        return user;
    }
    /**
     * 把一个User进行登录操作
     * 本质上是对Token进行操作
     *
     * @param user User
     * @return User
     */
    private static User login(User user) {
        // 使用一个随机的UUID值充当Token
        String newToken = UUID.randomUUID().toString();
        // 进行一次Base64格式化
        newToken = TextUtil.encodeBase64(newToken);
        user.setToken(newToken);
        update(user);
        return  user;
    }

    private static String encodePassword(String password) {
        // 密码去除首位空格
        password = password.trim();
        // 进行MD5非对称加密，加盐会更安全，盐也需要存储
        password = TextUtil.getMD5(password);
        // 再进行一次对称的Base64加密，当然可以采取加盐的方案
        return TextUtil.encodeBase64(password);
    }

    public static User update(User user) {
        return Hib.query(session -> {
            session.saveOrUpdate(user);
            return user;
        });
    }

    /**
     * 获取我的联系人列表
     * @param self
     * @return
     */
    public static List<User> contacts(User self) {
        return Hib.query(session -> {
            //重新加载用户信息到self 中 绑定session
            session.load(self,self.getId());
            Set<UserFollow> follows = self.getFollowing();
            return follows.stream()
                    .map(UserFollow::getTarget)
                    .collect(Collectors.toList());
        });
    }

    public static User findById(String id) {
        return Hib.query(session -> session.get(User.class,id));
    }
    //查询2个人是否相互关注
    public static UserFollow getUserFollow(User origin, User target) {
        return Hib.query(session -> (UserFollow)session
        .createQuery("from UserFollow where originId =:originId")
        .setParameter("originId",origin.getId())
        .setParameter("targetId",target.getId())
        .setMaxResults(1)
        .uniqueResult());
    }

    /**
     * 搜索联系人
     * @param name
     * @return
     */
    public static List<User> search(String name) {
        if(Strings.isNullOrEmpty(name))
            name="";//保证不能为null的情况，减少后面的一下判断和额外的错误
        final String searchName = "%"+name+"%";
        return Hib.query(session -> {
            // 查询的条件：name忽略大小写，并且使用like（模糊）查询；
            // 头像和描述必须完善才能查询到
            return (List<User>)session.createQuery("from User where lower(name) like :name and " +
                    "portrait is not null and description is not null")
                    .setParameter("name",searchName)
                    .setMaxResults(20)
                   .list();
        });
    }

    /**
     * 一个用户关注另外一个用户
     * @param self
     * @param followUser
     * @param alias 备注 用于后期扩展
     */
    public static User follow(User self, User followUser, String alias) {
        return Hib.query(session -> {
           session.load(self,self.getId());
           session.load(followUser,followUser.getId());

           //我关注的人，关注他，同时，他也关注我 需要保存两条信息
            UserFollow originFollow = new UserFollow();
            originFollow.setOrigin(self);
            originFollow.setTarget(followUser);
            originFollow.setAlias(alias);

            UserFollow targetFollow = new UserFollow();
            targetFollow.setOrigin(followUser);
            targetFollow.setTarget(self);

            session.save(originFollow);
            session.save(targetFollow);
            return followUser;
        });
    }
}
