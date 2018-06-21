package net.dengpan.web.pantalker.push.factory;

import com.google.common.base.Strings;
import net.dengpan.web.pantalker.push.bean.api.group.GroupCreateModel;
import net.dengpan.web.pantalker.push.bean.db.Group;
import net.dengpan.web.pantalker.push.bean.db.GroupMember;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.utils.Hib;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 处理群组的工厂类
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
public class GroupFactory {
    public static Group findByName(String name) {
        return Hib.query(session -> (Group) session
                .createQuery("from Group where lower(name)=:name")
                .setParameter("name",name.toLowerCase())
                .uniqueResult());
    }

    public static Group create(User creator, GroupCreateModel model, List<User> groupMembers) {
        return Hib.query(session -> {
            Group group = new Group(creator,model);
            session.save(group);
            GroupMember ownerMemeber = new GroupMember(creator,group);
            //为创建者设置超级权限
            ownerMemeber.setPermissionType(GroupMember.PERMISSION_TYPE_ADMIN_SU);
            //保存并没有提交到数据库
            session.save(ownerMemeber);

            for (User user:groupMembers){
                GroupMember member = new GroupMember(user,group);
                session.save(member);
            }
            return group;
        });

    }
    //获取一个群的成员 单个成员的信息
    public static GroupMember getMember(String userId, String groupId) {
        return  Hib.query(session -> (GroupMember)session
        .createQuery("from GroupMember where userId=:userId and groupId=:groupId")
        .setParameter("userId",userId)
        .setParameter("groupId",groupId)
        .setMaxResults(1)
        .uniqueResult());
    }

    /**
     * 获取要一个群的所有的成员
     * @param group
     * @return
     */
    public static Set<GroupMember> getMembers(Group group) {
        return Hib.query(session -> {
            List<GroupMember> members=session.createQuery("from GroupMember where group=:group")
                    .setParameter("group",group)
                    .list();
            return new HashSet<>(members);
        });
    }

    /**
     * 查询 所有的群
     * @param name
     * @return
     */
    public static List<Group> search(String name) {
        if (Strings.isNullOrEmpty(name))
            name = ""; // 保证不能为null的情况，减少后面的一下判断和额外的错误
        final String searchName = "%" + name + "%"; // 模糊匹配
        return Hib.query(session -> {
            // 查询的条件：name忽略大小写，并且使用like（模糊）查询；
            // 头像和描述必须完善才能查询到
            return (List<Group>) session.createQuery("from Group where lower(name) like :name")
                    .setParameter("name", searchName)
                    .setMaxResults(20) // 至多20条
                    .list();
        });
    }
    //查询一个人加入的所有群

    public static Set<GroupMember> getMembers(User self) {
        return Hib.query(session -> {
            List<GroupMember> members = session.createQuery("from GroupMember where userId=:userId")
                    .setParameter("userId",self.getId())
                    .list();
            return new HashSet<>(members);

        });
    }

    public static Group findById(String groupId) {
        return Hib.query(session -> session.get(Group.class,groupId));
    }

    /**
     * 给群组添加成员
     * @param group
     * @param inserUser
     * @return
     */
    public static Set<GroupMember> addMembers(Group group, List<User> inserUser) {
        return Hib.query(session -> {
            Set<GroupMember> members =new HashSet<>();
            for (User user: inserUser){
                GroupMember member = new GroupMember(user,group);
                session.save(member);
                members.add(member);
            }
            // 进行数据刷新
            /*
            for (GroupMember member : members) {
                // 进行刷新，会进行关联查询；再循环中消耗较高
                session.refresh(member);
            }
            */
            return members;
        });
    }
    //查询一个群，这个人必须是成员
    public static Group findById(User self, String groupId) {

        GroupMember  member = getMember(self.getId(),groupId);
        if(member !=  null){
            return member.getGroup();
        }

        return null;
    }
}
