package net.dengpan.web.pantalker.push.bean.card;

import com.google.gson.annotations.Expose;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.utils.Hib;

import java.time.LocalDateTime;

/**
 * 返回用户信息的简单实体
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
public class UserCard {

    @Expose
    private String id;
    @Expose
    private String name;
    @Expose
    private String phone;
    @Expose
    private String portrait;
    @Expose
    private String desc;
    @Expose
    private int sex = 0;

    // 用户关注人的数量
    @Expose
    private int follows;

    // 用户粉丝的数量
    @Expose
    private int following;

    // 我与当前User的关系状态，是否已经关注了这个人
    @Expose
    private boolean isFollow;

    // 用户信息最后的更新时间
    @Expose
    private LocalDateTime modifyAt;

    public UserCard(final  User user){
        this(user,false);
    }

    public UserCard(final User user,boolean isFollow){
        this.isFollow = isFollow;

        this.id = user.getId();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.portrait = user.getPortrait();
        this.desc = user.getDescription();
        this.sex = user.getSex();
        this.modifyAt = user.getUpdateAt();

        // user.getFollowers().size()
        // 懒加载会报错，因为没有Session

        Hib.queryOnly(session -> {
            session.load(user,user.getId());

            // 这个时候仅仅只是进行了数量查询，并没有查询整个集合
            // 要查询集合，必须在session存在情况下进行遍历
            // 或者使用Hibernate.initialize(user.getFollowers());
            follows = user.getFollowers().size();
            following = user.getFollowing().size();
        });
    }
}
