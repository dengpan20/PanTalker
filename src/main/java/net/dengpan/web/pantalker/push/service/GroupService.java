package net.dengpan.web.pantalker.push.service;

import com.google.common.base.Strings;
import net.dengpan.web.pantalker.push.bean.api.base.ResponseModel;
import net.dengpan.web.pantalker.push.bean.api.group.GroupCreateModel;
import net.dengpan.web.pantalker.push.bean.api.group.GroupMemberAddModel;
import net.dengpan.web.pantalker.push.bean.api.group.GroupMemberUpdateModel;
import net.dengpan.web.pantalker.push.bean.card.ApplyCard;
import net.dengpan.web.pantalker.push.bean.card.GroupCard;
import net.dengpan.web.pantalker.push.bean.card.GroupMemberCard;
import net.dengpan.web.pantalker.push.bean.card.UserCard;
import net.dengpan.web.pantalker.push.bean.db.Group;
import net.dengpan.web.pantalker.push.bean.db.GroupMember;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.factory.GroupFactory;
import net.dengpan.web.pantalker.push.factory.UserFactory;
import net.dengpan.web.pantalker.push.provider.LocalDateTimeConverter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 处理群组的服务类
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
@Path("/group")
public class GroupService extends BaseService{
    /**
     * 创建群
     * @param model
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<GroupCard> create(GroupCreateModel model){
        if(!GroupCreateModel.check(model)){
            return ResponseModel.buildParameterError();
        }

        User creator = getSelf();
        //创建者并不在其中
        model.getUsers().remove(creator.getId());

        if(model.getUsers().size()==0){
            //必须要有多人才可以创建群组
            return ResponseModel.buildParameterError();
        }
        if(GroupFactory.findByName(model.getName())!= null){
            return ResponseModel.buildHaveNameError();
        }
        List<User> groupMember = new ArrayList<>();
        for(String id : model.getUsers()){
            User user = UserFactory.findById(id);
            if(user!= null){
                groupMember.add(user);
            }
        }
        //没有成员
        if(groupMember.size() == 0){
            return ResponseModel.buildParameterError();
        }
        Group group = GroupFactory.create(creator,model,groupMember);

        if(group == null){
            return ResponseModel.buildServiceError();
        }

        //拿到自己的信息 管理员
        GroupMember creatorMember = GroupFactory.getMember(creator.getId(),group.getId());

        if(creatorMember == null){
            return ResponseModel.buildServiceError();
        }
        Set<GroupMember> members = GroupFactory.getMembers(group);
        if( members == null){
            return ResponseModel.buildServiceError();
        }
        //排除创建者自己
        members = members.stream()
                .filter(groupMember1 -> !groupMember1.getId().equalsIgnoreCase(creatorMember.getId()))
                .collect(Collectors.toSet());
        //TODO 推送给各个成员 未实现

        return ResponseModel.buildOk(new GroupCard(creatorMember));
    }
    /**
     * 查找群，没有传递参数就是搜索最近所有的群
     *
     * @param name 搜索的参数
     * @return 群信息列表
     */
    @GET
    @Path("/search/{name:(.*)?}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<GroupCard>>  search(@PathParam("name") String name){
        User self = getSelf();
        List<Group> groups = GroupFactory.search(name);
        if(groups != null && groups.size()>0){
            List<GroupCard> groupCards = groups.stream()
                    .map(group -> {
                        GroupMember member = GroupFactory.getMember(self.getId(),group.getId());
                        return new GroupCard(group,member);
                    }).collect(Collectors.toList());
            return ResponseModel.buildOk(groupCards);
        }
        return ResponseModel.buildOk();
    }
    /**
     * 拉取自己当前的群的列表
     *
     * @param dateStr 时间字段，不传递，则返回全部当前的群列表；有时间，则返回这个时间之后的加入的群
     * @return 群信息列表
     */
    @GET
    @Path("/list/{date:(.*)?}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<GroupCard>> getMyGroupList(@DefaultValue("") @PathParam("date")String dateStr){
        User self = getSelf();
        LocalDateTime dateTime = null;
        if(!Strings.isNullOrEmpty(dateStr)){
            try{
                dateTime = LocalDateTime.parse(dateStr,LocalDateTimeConverter.FORMATTER);

            }catch (Exception e){
                dateTime = null;
            }

        }
        Set<GroupMember> members = GroupFactory.getMembers(self);

        if(members == null || members.size() == 0){
            return ResponseModel.buildOk();
        }
        final LocalDateTime finalDateTime = dateTime;
        List<GroupCard> groupCards = members.stream()
                .filter(groupMember->
                finalDateTime == null || groupMember.getUpdateAt().isAfter(finalDateTime) )
                .map(GroupCard::new)
                .collect(Collectors.toList());
        return ResponseModel.buildOk(groupCards);
    }
    /**
     * 获取一个群的信息, 你必须是群的成员
     *
     * @param
     * @return 群的信息
     */
    @GET
    @Path("/{groupId}")
    //http:.../api/group/0000-0000-0000-0000
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<GroupCard> getGroup(@PathParam("groupId") String groupId){
        if(Strings.isNullOrEmpty(groupId)){
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        GroupMember member = GroupFactory.getMember(self.getId(),groupId);
        if(member == null){
            return ResponseModel.buildNotFoundGroupError(null);
        }
        return ResponseModel.buildOk(new GroupCard(member));
    }
    /**
     * 拉取一个群的所有成员，你必须是成员之一
     *
     * @param groupId 群id
     * @return 成员列表
     */
    @GET
    @Path("/{groupId}/member")
    //http:.../api/group/0000-0000-0000-0000/member
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<GroupMemberCard>> members(@PathParam("groupId") String groupId){
        User self = getSelf();
        Group group = GroupFactory.findById(groupId);

        if( group == null){
            return ResponseModel.buildNotFoundGroupError(null);
        }
        //check the permission
        GroupMember selfMember = GroupFactory.getMember(self.getId(),groupId);

        if( selfMember  == null){
            return ResponseModel.buildNoPermissionError();
        }
        Set<GroupMember> members = GroupFactory.getMembers(group);
        if(members == null){
            return ResponseModel.buildServiceError();
        }

        List<GroupMemberCard> groupMemberCards = members.stream()
                .map(GroupMemberCard::new)
                .collect(Collectors.toList());
        return ResponseModel.buildOk(groupMemberCards);

    }
    /**
     * 给群添加成员的接口
     *
     * @param groupId 群Id，你必须是这个群的管理者之一
     * @param model   添加成员的Model
     * @return 添加成员列表
     */
    @POST
    @Path("/{groupId}/member")
    //http:.../api/group/0000-0000-0000-0000/member
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<GroupMemberCard>>memberAdd(@PathParam("groupId") String groupId,GroupMemberAddModel model){
        if (Strings.isNullOrEmpty(groupId) || !GroupMemberAddModel.check(model)) {

            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        model.getUsers().remove(self.getId());
        if(model.getUsers().size() == 0){
            return ResponseModel.buildParameterError();
        }
        // this is no group
        Group group = GroupFactory.findById(groupId);
        if(group == null){
            return ResponseModel.buildNotFoundGroupError(null);
        }
        //我必须是成员，并且权限在管理员以上级别
        GroupMember selfMember = GroupFactory.getMember(self.getId(),groupId);
        if (selfMember == null || selfMember.getPermissionType() == GroupMember.PERMISSION_TYPE_NONE)
            return ResponseModel.buildNoPermissionError();

        Set<GroupMember> oldMembers = GroupFactory.getMembers(group);

        Set<String> oldMembersIds =oldMembers.stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toSet());


        List<User> inserUser = new ArrayList<>();
        for (String id : model.getUsers()){
            User user = UserFactory.findById(id);
            if(user == null){
                continue;
            }
            //已经在群里了
            if(oldMembers.contains(user.getId())){
                continue;
            }
            inserUser.add(user);
        }
        if(inserUser.size()==0){
            return ResponseModel.buildParameterError();
        }
        //添加操作
        Set<GroupMember> insertMembers= GroupFactory.addMembers(group,inserUser);
        if(insertMembers == null){
            return ResponseModel.buildServiceError();
        }


        //转换
        List<GroupMemberCard> groupMemberCards = insertMembers.stream()
                .map(GroupMemberCard::new)
                .collect(Collectors.toList());

        //TODO 通知
        // 1 通知新增群员，你被加入了某群
        // 2 通知老成员，有某某加入了群

        return ResponseModel.buildOk(groupMemberCards);
    }

    /**
     * 更改成员信息，请求的人要么是管理员，要么就是成员本人
     *
     * @param memberId 成员Id，可以查询对应的群，和人
     * @param model    修改的Model
     * @return 当前成员的信息
     */
    @PUT
    @Path("/member/{memberId}")
    //http:.../api/group/member/0000-0000-0000-0000
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<GroupMemberCard> modifyMember(@PathParam("memberId") String memberId, GroupMemberUpdateModel model) {
        return null;
    }

    /**
     * 申请加入一个群，
     * 此时会创建一个加入的申请，并写入表；然后会给管理员发送消息
     * 管理员同意，其实就是调用添加成员的接口把对应的用户添加进去
     *
     * @param groupId 群Id
     * @return 申请的信息
     */
    @POST
    @Path("/applyJoin/{groupId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<ApplyCard> join(@PathParam("groupId") String groupId) {
        return null;
    }

}
