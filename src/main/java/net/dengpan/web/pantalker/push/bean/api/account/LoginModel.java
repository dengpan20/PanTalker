package net.dengpan.web.pantalker.push.bean.api.account;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;

/**
 * 登陆请求实体
 *
 * @author pan dengpan
 * @create 2018/5/30
 */

public class LoginModel {
    @Expose
    private String account;
    @Expose
    private String password;
    @Expose
    private String pushId;


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public static boolean check(LoginModel model){
        return model!=null && !Strings.isNullOrEmpty(model.getAccount())
                && !Strings.isNullOrEmpty(model.getPassword());
    }
}
