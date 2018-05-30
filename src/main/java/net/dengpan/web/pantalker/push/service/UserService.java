package net.dengpan.web.pantalker.push.service;

import com.google.common.base.Strings;
import net.dengpan.web.pantalker.push.bean.api.base.ResponseModel;
import net.dengpan.web.pantalker.push.bean.api.user.UpdateInfoModel;
import net.dengpan.web.pantalker.push.bean.card.UserCard;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 与用户相关的服务类
 *
 * @author pan dengpan
 * @create 2018/5/30
 */

@Path("/user")
public class UserService extends BaseService {
    // 用户信息修改接口
    // 返回自己的个人信息
    @PUT
    //@Path("") //127.0.0.1/api/user 不需要写，就是当前目录
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(UpdateInfoModel model){
        if(!UpdateInfoModel.check(model)){
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        self = model.updateToUser(self);
        self = UserFactory.update(self);

        UserCard card = new UserCard(self,true);
        return ResponseModel.buildOk(card);
    }

    //获取所有的联系人
    // 拉取联系人
    @GET
    @Path("/contact")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> contact(){
        User self = getSelf();

        List<User> userList = UserFactory.contacts(self);

        //通过java 8 map 操作 转换成Usercard

        List<UserCard> userCards = userList.stream()
                .map(user -> new UserCard(user,true))
                .collect(Collectors.toList());

        return ResponseModel.buildOk(userCards);
    }

    // 关注 既是 申请加好友
    //后期需要同意后才能添加好友，目前简化 直接成为好友
    @PUT // 修改类使用Put
    @Path("/follow/{followId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> follow(@PathParam("followId") String followId){
        User self = getSelf();
        //不能关注自己
        if(self.getId().equalsIgnoreCase(followId)||
                Strings.isNullOrEmpty(followId)){
            return ResponseModel.buildParameterError();
        }
        //TODO 这里可能有一个bug 可能关注的人，我已经关注过了
        User followUser = UserFactory.findById(followId);
        if(followUser== null){
            return ResponseModel.buildServiceError();
        }
        //通知我要关注的人，我要关注他
        //给他发送一个我的信息过去

        //TODO 发送通知 未实现

        return ResponseModel.buildOk(new UserCard(followUser,true));
    }

    //获取某个人的信息
    @GET
    @Path("{id}") // http://127.0.0.1/api/user/{id}
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> getUser(@PathParam("id") String id){
        if(Strings.isNullOrEmpty(id)){
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        if(self.getId().equalsIgnoreCase(id)){
            return ResponseModel.buildOk(new UserCard(self,true));
        }
        User user = UserFactory.findById(id);
        if(user == null){
            return ResponseModel.buildNotFoundUserError(null);
        }
        // 不是我自己的信息，需要查询两者的是否关注的信息
        boolean isFollow = UserFactory.getUserFollow(self,user)!=null;
        return ResponseModel.buildOk(new UserCard(user,isFollow));
    }
    // 搜索人的接口实现
    // 为了简化分页：只返回20条数据
    @GET // 搜索人，不涉及数据更改，只是查询，则为GET
    // http://127.0.0.1/api/user/search/
    @Path("/search/{name:(.*)?}") // 名字为任意字符，可以为空
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> search(@DefaultValue("") @PathParam("name") String name){
        User self = getSelf();
        List<User> searchUsers = UserFactory.search(name);

        List<User> contacts =UserFactory.contacts(self);
        //通过我的联系人 和我自己的情况的ID 比较  设置是否 关注的信息
        List<UserCard> userCards = searchUsers.stream()
                .map(user -> {
                    boolean isFollow = user.getId().equalsIgnoreCase(self.getId())
                            ||contacts.stream().anyMatch(
                                    contactUser->contactUser.getId().equalsIgnoreCase(user.getId())
                    );
                    return new UserCard(user,isFollow);
                }).collect(Collectors.toList());

        return ResponseModel.buildOk(userCards);
    }
}
