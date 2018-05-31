package net.dengpan.web.pantalker.push.factory;

import net.dengpan.web.pantalker.push.bean.api.message.MessageCreateModel;
import net.dengpan.web.pantalker.push.bean.db.Group;
import net.dengpan.web.pantalker.push.bean.db.Message;
import net.dengpan.web.pantalker.push.bean.db.User;
import net.dengpan.web.pantalker.push.utils.Hib;

/**
 * 消息类的处理工厂
 *
 * @author pan dengpan
 * @create 2018/5/30
 */
public class MessageFatory {
    public static Message findById(String id) {
        return Hib.query(session -> session.get(Message.class,id));
    }

    /**
     * 添加一条消息
     * @param
     * @param receiver
     * @param model
     * @return
     */
    public static Message add(User sender, User receiver, MessageCreateModel model) {
        Message message = new Message(sender,receiver,model);
        return save(message);
    }
    private static Message save(Message message) {
        return Hib.query(session -> {
            session.save(message);
            session.flush();//写入数据库
            session.refresh(message);//从数据库中查询出来
            return message;
        });
    }

    public static Message add(User sender, Group receiver, MessageCreateModel model) {
        Message message = new Message(sender,receiver,model);
        return save(message);
    }
}
