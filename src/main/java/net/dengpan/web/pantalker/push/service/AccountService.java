package net.dengpan.web.pantalker.push.service;

import com.google.common.base.Strings;
import net.dengpan.web.pantalker.push.bean.api.account.AccountRspModel;
import net.dengpan.web.pantalker.push.bean.api.account.RegisterModel;
import net.dengpan.web.pantalker.push.bean.api.base.ResponseModel;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 账户相关的服务类
 *
 * @author pan dengpan
 * @create 2018/5/29
 */
@Path("/account")
public class AccountService {
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> register(RegisterModel model){
        if(!RegisterModel.check(model)){
            return ResponseModel.buildParameterError();
        }
        User user = UserFactory.findByPhone(model.getAccount().trim());
        if(user != null){
            return ResponseModel.buildHaveAccountError();
        }
        user = UserFactory.findByName(model.getName());

        if(user != null){
            return ResponseModel.buildHaveNameError();
        }

        //开始注册
        user = UserFactory.register(model.getAccount(),model.getPassword()
        ,model.getName());

        if(user != null){
            //如果携带有pushId
            if(!Strings.isNullOrEmpty(model.getPushId())){
                return bind(user,model.getPushId());
            }
            AccountRspModel rspModel= new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        }else {
            //注册异常
            return ResponseModel.buildRegisterError();
        }

    }

    /**
     * 绑定自己pushId 的操作
     * @param user 自己
     * @param pushId
     * @return
     */
    // 绑定设备Id

    private ResponseModel<AccountRspModel> bind(User self, String pushId) {
        User user = UserFactory.bindPushId(self,pushId);
        if(user == null){
            //绑定失败 user 保存失败是服务器错误
            return ResponseModel.buildServiceError();
        }
        AccountRspModel rspModel = new AccountRspModel(user ,true);
        return ResponseModel.buildOk(rspModel);
    }

    @GET
    @Path("/login")
    public  String get(){
        return "this is start and this is a test";
    }
    @GET
    @Path("/test")
    public ResponseModel Test(){
        ResponseModel model = ResponseModel.buildOk();
        return model;
    }
}
