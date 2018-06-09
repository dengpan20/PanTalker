package net.dengpan.web.pantalker.push.bean.api.base;

import com.google.gson.annotations.Expose;
import net.dengpan.web.pantalker.push.utils.TextUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息推送实体
 * 一个推送的具体Model，内部维持了一个数组，可以添加多个实体
 * 每次推送的详细数据是：把实体数组进行Json操作，然后发送Json字符串
 * 这样做的目的是：减少多次推送，如果有多个消息需要推送可以合并进行
 *
 * @author pan dengpan
 * @create 2018/6/9
 */
public class PushModel {
    //退出的消息推送
    public static final int ENTITY_TYPE_LOGOUT = -1;
    //消息的推送
    public static final int ENTITY_TYPE_MESSAGE = 200;
    //添加好友的推送
    public static final int ENTITY_TYPE_ADD_FRIEND = 1001;
    //添加群组的操作
    public static final int ENTITY_TYPE_ADD_GROUP = 1002;
    //添加群组成员的操作
    public static final int ENTITY_TYPE_ADD_GROUP_MEMBERS = 1003;
    //修改成员的操作
    public static final int ENTITY_TYPE_MODIFY_GROUP_MEMBERS = 2001;
    //推出群组操作
    public static final int ENTITY_TYPE_EXIT_GROUP_MEMBERS = 3001;

    private List<Entity> entities = new ArrayList<>();

    public PushModel add(Entity entity){
        entities.add(entity);
        return this;
    }
    public PushModel add(int type, String content) {
        return add(new Entity(type, content));
    }
    // 拿到一个推送的字符串
    public String getPushString() {
        if (entities.size() == 0)
            return null;
        return TextUtil.toJson(entities);
    }

    /**
     * 具体实体类型  ，包装了类型和内容
     */
    public static class Entity{

        public Entity(int type, String content) {
            this.type = type;
            this.content = content;
        }

        @Expose
        public int type;//消息类型
        @Expose
        public String content;//消息内容 json 格式
        // 消息生成时间
        @Expose
        public LocalDateTime createAt = LocalDateTime.now();

    }
}
