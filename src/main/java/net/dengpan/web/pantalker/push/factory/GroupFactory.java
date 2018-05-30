package net.dengpan.web.pantalker.push.factory;

import net.dengpan.web.pantalker.push.bean.api.group.GroupCreateModel;
import net.dengpan.web.pantalker.push.bean.db.Group;
import net.dengpan.web.pantalker.push.bean.db.GroupMember;
import net.dengpan.web.pantalker.push.bean.db.User;

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
        return null;
    }

    public static Group create(User creator, GroupCreateModel model, List<User> groupMember) {
        return null;
    }

    public static GroupMember getMember(String id, String id1) {
        return  null;
    }

    public static Set<GroupMember> getMembers(Group group) {
        return null;
    }

    public static List<Group> search(String name) {
        return  null;
    }

    public static Set<GroupMember> getMembers(User self) {
        return null;
    }

    public static Group findById(String groupId) {
        return null;
    }

    public static Set<GroupMember> addMembers(Group group, List<User> inserUser) {
        return null;
    }
}
