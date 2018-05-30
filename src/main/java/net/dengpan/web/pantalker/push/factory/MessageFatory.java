package net.dengpan.web.pantalker.push.factory;

import net.dengpan.web.pantalker.push.bean.api.message.MessageCreateModel;
import net.dengpan.web.pantalker.push.bean.db.Group;
import net.dengpan.web.pantalker.push.bean.db.Message;
import net.dengpan.web.pantalker.push.bean.db.User;

/**
 * 消息类的处理工厂
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
public class MessageFatory {
    public static Message findById(String id) {
        return null;
    }

    public static Message add(User self, User receiver, MessageCreateModel model) {
        return null;
    }
    public static Message add(User self, Group receiver, MessageCreateModel model) {
        return null;
    }
}
