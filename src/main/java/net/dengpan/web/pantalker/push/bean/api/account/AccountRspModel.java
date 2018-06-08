package net.dengpan.web.pantalker.push.bean.api.account;

import com.google.gson.annotations.Expose;
import net.dengpan.web.pantalker.push.bean.card.UserCard;
import net.dengpan.web.pantalker.push.bean.db.User;
import sun.dc.pr.PRError;

/**
 * 注册账户返回实体的model
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
public class AccountRspModel {
    @Expose
    private UserCard user;
    @Expose
    private String account;
    @Expose
    private String token;
    @Expose
    private boolean isBind;

    public AccountRspModel(User user) {
        // 默认无绑定
        this(user, false);
    }

    public AccountRspModel(User user, boolean isBind) {
        this.user = new UserCard(user);
        this.account = user.getPhone();
        this.token = user.getToken();
        this.isBind = isBind;
    }

    public UserCard getUser() {
        return user;
    }

    public void setUser(UserCard user) {
        this.user = user;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }

    @Override
    public String toString() {
        return "AccountRspModel{" +
                ", account='" + account + '\'' +
                ", token='" + token + '\'' +
                ", isBind=" + isBind +
                '}';
    }
}
