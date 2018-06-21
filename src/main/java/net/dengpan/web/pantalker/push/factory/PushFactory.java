package net.dengpan.web.pantalker.push.factory;

import com.google.common.base.Strings;
import net.dengpan.web.pantalker.push.bean.api.base.PushModel;
import net.dengpan.web.pantalker.push.bean.card.GroupCard;
import net.dengpan.web.pantalker.push.bean.card.GroupMemberCard;
import net.dengpan.web.pantalker.push.bean.card.MessageCard;
import net.dengpan.web.pantalker.push.bean.card.UserCard;
import net.dengpan.web.pantalker.push.bean.db.*;
import net.dengpan.web.pantalker.push.utils.Hib;
import net.dengpan.web.pantalker.push.utils.PushDispatcher;
import net.dengpan.web.pantalker.push.utils.TextUtil;
import sun.rmi.server.UnicastServerRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 推送消息的工厂类
 *
 * @author pan dengpan
 * @create 2018/6/9
 */
public class PushFactory {
    /**
     * 推送一条退出的消息，并且记录在推送记录中
     * @param
     * @param pushId
     */
    public static void pushLogout(User receiver, String pushId) {
        PushHistory history  = new PushHistory();
        history.setEntityType(PushModel.ENTITY_TYPE_LOGOUT);
        history.setEntity("Account logout!!!");
        history.setReceiver(receiver);
        history.setReceiverPushId(pushId);
        //保存到推送记录中
        Hib.queryOnly(session -> session.save(history));
        //发送工具
        PushDispatcher dispatcher= new PushDispatcher();
        //发送内容
        PushModel pushModel = new PushModel();
        pushModel.add(history.getEntityType(),history.getEntity());
        //提交并且推送
        dispatcher.add(receiver,pushModel);
        dispatcher.submit();
    }

    /**
     *  通知成员 你被拉入了某某群
     * @param members
     */
    public static void pushJoinGroup(Set<GroupMember> members) {

        PushDispatcher dispatcher = new PushDispatcher();
        // 历史记录表
        List<PushHistory> histories = new ArrayList<>();

        for (GroupMember member : members){
            User receiver = member.getUser();
            if(receiver == null){
                return;
            }

            GroupCard groupCard= new GroupCard(member);
            String entity = TextUtil.toJson(groupCard);

            PushHistory history = new PushHistory();
            history.setEntityType(PushModel.ENTITY_TYPE_ADD_GROUP);
            history.setEntity(entity);
            history.setReceiver(receiver);
            history.setReceiverPushId(receiver.getPushId());
            histories.add(history);

            PushModel pushModel = new PushModel();
            pushModel.add(history.getEntityType(),history.getEntity());

            dispatcher.add(receiver,pushModel);

        }
        Hib.queryOnly(session -> {
            for (PushHistory history:histories){
                session.saveOrUpdate(history);
            }
        });
        dispatcher.submit();
    }

    /**
     * 通知老群员 有 XXX 新成员 加入群
     * @param oldMembers
     * @param
     */
    public static void pushGroupMemberAdd(Set<GroupMember> oldMembers, List<GroupMemberCard> insertCards) {
        // 发送者
        PushDispatcher dispatcher = new PushDispatcher();

        // 一个历史记录列表
        List<PushHistory> histories = new ArrayList<>();

        // 当前新增的用户的集合的Json字符串
        String entity = TextUtil.toJson(insertCards);
        // 进行循环添加，给oldMembers每一个老的用户构建一个消息，消息的内容为新增的用户的集合
        // 通知的类型是：群成员添加了的类型
        addGroupMembersPushModel(dispatcher,histories,oldMembers,entity,PushModel.ENTITY_TYPE_ADD_GROUP_MEMBERS);

        Hib.queryOnly(session -> {
            for (PushHistory history:histories){
                session.saveOrUpdate(history);
            }
        });
        dispatcher.submit();
    }

    /**
     * 构建多个推送消息
     * @param dispatcher
     * @param histories
     * @param oldMembers
     * @param entity
     * @param entityTypeAddGroupMembers
     */
    private static void addGroupMembersPushModel(PushDispatcher dispatcher, List<PushHistory> histories, Set<GroupMember> oldMembers, String entity, int entityTypeAddGroupMembers) {
        for (GroupMember member:oldMembers){
            User receiver = member.getUser();
            if(receiver == null){
                continue;
            }
            PushHistory history = new PushHistory();
            history.setEntityType(entityTypeAddGroupMembers);
            history.setEntity(entity);
            history.setReceiver(receiver);
            history.setReceiverPushId(receiver.getPushId());
            histories.add(history);

            //构建一个消息model
            PushModel pushModel=new PushModel()
                    .add(history.getEntityType(),history.getEntity());
            dispatcher.add(receiver,pushModel);
        }
    }

    /**
     * 推送一个聊天消息
     * @param sender
     * @param message
     */
    public static void pushNewMessage(User sender, Message message) {

        if (sender == null || message == null)
            return;
        //消息卡片用与发送

        MessageCard card= new MessageCard(message);
        String entity = TextUtil.toJson(card);
        PushDispatcher dispatcher = new PushDispatcher();
        //单聊
        if(message.getGroup() == null
                && Strings.isNullOrEmpty(message.getGroupId())){
            User receiver = UserFactory.findById(message.getReceiverId());
            if(receiver == null)
                return;

            PushHistory history = new PushHistory();
            // 普通消息类型
            history.setEntityType(PushModel.ENTITY_TYPE_MESSAGE);
            history.setEntity(entity);
            history.setReceiver(receiver);
            // 接收者当前的设备推送Id
            history.setReceiverPushId(receiver.getPushId());

            PushModel pushModel = new PushModel();
            pushModel.add(history.getEntityType(),history.getEntity());
            dispatcher.add(receiver,pushModel);

            Hib.queryOnly(session -> session.save(history));

        }else {
            //群聊
            Group group = message.getGroup();

            if(group == null){
                group = GroupFactory.findById(message.getGroupId());
            }
            if(group == null){
                return;
            }
            //给群员发送消息
            Set<GroupMember> members = GroupFactory.getMembers(group);
            //没成员可以推送就返回
            if (members == null|| members.size()==0){
                return;
            }
            //过滤掉自己
            members= members.stream()
                    .filter(groupMember->!groupMember.getUserId().equalsIgnoreCase(sender.getId()))
                    .collect(Collectors.toSet());
            // 一个历史记录列表
            List<PushHistory> histories = new ArrayList<>();

            addGroupMembersPushModel(dispatcher,histories,members,entity,PushModel.ENTITY_TYPE_MESSAGE);

            Hib.queryOnly(session -> {
                for (PushHistory history:histories){
                    session.saveOrUpdate(history);
                }
            });

        }
        dispatcher.submit();
    }

    /**
     * 给要关注的人推送一个我关注他的消息
     * @param
     * @param userCard
     */
    public static void pushFollow(User receiver, UserCard userCard) {
        userCard.setFollow(true);
        String entity = TextUtil.toJson(userCard);

        // 历史记录表字段建立
        PushHistory history = new PushHistory();
        // 你被添加到群的类型
        history.setEntityType(PushModel.ENTITY_TYPE_ADD_FRIEND);
        history.setEntity(entity);
        history.setReceiver(receiver);
        history.setReceiverPushId(receiver.getPushId());
        // 保存到历史记录表
        Hib.queryOnly(session -> session.save(history));

        //推送
        // 推送
        PushDispatcher dispatcher = new PushDispatcher();
        PushModel pushModel = new PushModel()
                .add(history.getEntityType(), history.getEntity());
        dispatcher.add(receiver, pushModel);
        dispatcher.submit();

    }
}
