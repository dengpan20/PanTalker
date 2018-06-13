package net.dengpan.web.pantalker.push.service;

import net.dengpan.web.pantalker.push.bean.api.account.RegisterModel;
import net.dengpan.web.pantalker.push.bean.api.base.ResponseModel;
import net.dengpan.web.pantalker.push.bean.api.message.MessageCreateModel;
import net.dengpan.web.pantalker.push.bean.card.MessageCard;
import net.dengpan.web.pantalker.push.bean.db.Group;
import net.dengpan.web.pantalker.push.bean.db.Message;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.factory.GroupFactory;
import net.dengpan.web.pantalker.push.factory.MessageFatory;
import net.dengpan.web.pantalker.push.factory.PushFactory;
import net.dengpan.web.pantalker.push.factory.UserFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 消息类接口服务类
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
@Path("/msg")
public class MessageService extends BaseService {
    // 发送一条消息到服务器
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<MessageCard> pushMessage(MessageCreateModel model){
        if(!MessageCreateModel.check(model)){
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        //查询是否有相同消息
        Message message = MessageFatory.findById(model.getId());
        if(message != null){
            return ResponseModel.buildOk(new MessageCard(message));
        }
        if(model.getReceiverType() == Message.RECEIVER_TYPE_GROUP){
            return pushToGroup(self,model);
        }else {
            return pushToUser(self,model);
        }
    }
    //发送消息到群组
    private ResponseModel<MessageCard> pushToGroup(User self, MessageCreateModel model) {
        // 找群是有权限性质的找
        Group group = GroupFactory.findById(self, model.getReceiverId());

        if(group ==null){
            // 没有找到接收者群，有可能是你不是群的成员
            return ResponseModel.buildNotFoundUserError("Can`t find receiver group");
        }

        //储存到数据库中
        Message message = MessageFatory.add(self,group,model);
        return buildAndPushResponse(self,message);

    }
    //发送消息到人
    private ResponseModel<MessageCard> pushToUser(User self, MessageCreateModel model) {
        User receiver =UserFactory.findById(model.getReceiverId());
        if(receiver ==null){
            return ResponseModel.buildNotFoundUserError("Can`t find receiver user");
        }
        //可以考虑可以给自己发送消息
        if (receiver.getId().equalsIgnoreCase(self.getId())) {
            // 发送者接收者是同一个人就返回创建消息失败
            return ResponseModel.buildCreateError(ResponseModel.ERROR_CREATE_MESSAGE);
        }
        //储存到数据库中
        Message message = MessageFatory.add(self,receiver,model);
        return buildAndPushResponse(self,message);
    }

    //构建一个消息并返回 和通知
    private ResponseModel<MessageCard> buildAndPushResponse(User sender, Message message) {
        if(message == null){
            //储存失败
            return ResponseModel.buildCreateError(ResponseModel.ERROR_CREATE_MESSAGE);
        }
        //进行推送消息
        PushFactory.pushNewMessage(sender,message);
        return ResponseModel.buildOk(new MessageCard(message));
    }
}
