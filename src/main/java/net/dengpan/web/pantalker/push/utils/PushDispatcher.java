package net.dengpan.web.pantalker.push.utils;

import com.gexin.rp.sdk.base.IBatch;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.google.common.base.Strings;
import net.dengpan.web.pantalker.push.bean.api.base.PushModel;
import net.dengpan.web.pantalker.push.bean.db.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 消息推送工具类
 *
 * @author pan dengpan
 * @create 2018/6/9
 */
public class PushDispatcher {
    private static final String appId = "BLaKxAwhUUAcM0KIMu3WK";
    private static final String appKey = "pZa6gOB8Zl9B8w2TF2kyQ6";
    private static final String masterSecret = "vr4DomCgtV9EUQbMQdDru6";
    private static final String host = "http://sdk.open.api.igexin.com/apiex.htm";

    private final IGtPush pusher;
    // 要收到消息的人和内容的列表
    private final List<BatchBean> beans = new ArrayList<>();
    public PushDispatcher() {
        pusher = new IGtPush(host,appKey,masterSecret);
    }

    /**
     * 添加一条消息
     * @param receiver
     * @param model
     * @return
     */
    public boolean add(User receiver,PushModel model){

        if(receiver == null || model ==null||Strings.isNullOrEmpty(receiver.getPushId())){
            return false;
        }
        String pushString = model.getPushString();
        if(Strings.isNullOrEmpty(pushString)){
            return false;
        }
        BatchBean bean = buildMessage(receiver.getPushId(),pushString);
        beans.add(bean);
        return true;
    }

    /**
     * 把消息数据进行格式化
     * @param clientId
     * @param text
     * @return
     */
    private BatchBean buildMessage(String clientId,String text){
        // 透传消息，不是通知栏显示，而是在MessageReceiver收到
        TransmissionTemplate template = new TransmissionTemplate();

        template.setAppId(appId);
        template.setAppkey(appKey);
        template.setTransmissionContent(text);
        template.setTransmissionType(0); //这个Type为int型，填写1则自动启动app

        SingleMessage message= new SingleMessage();
        message.setData(template);//透传设置到单消息模板中
        message.setOffline(true);//是否离线推送
        message.setOfflineExpireTime(24 * 3600 * 1000); // 离线消息时常

        Target target= new Target();
        target.setAppId(appId);
        target.setClientId(clientId);

        return new BatchBean(message,target);
    }

    /**
     * 进行消息的最后推送
     * @return
     */
    public boolean submit(){

        //构建打包的工具类
        IBatch batch = pusher.getBatch();
        //是否有数据进行发送
        boolean havaData = false;

        for (BatchBean bean :beans){
            try {
                batch.add(bean.message,bean.target);
                havaData = true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(!havaData){
            return havaData;
        }
        IPushResult result = null;
        try {
            result = batch.submit();
        }catch (IOException e){
            e.printStackTrace();
            // 失败情况下尝试重复发送一次
            try {
                batch.retry();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if(result != null){
            try {
                Logger.getLogger("PushDispatcher")
                        .log(Level.INFO, (String) result.getResponse().get("result"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Logger.getLogger("PushDispatcher")
                .log(Level.WARNING, "推送服务器响应异常！！！");
        return false;
    }
    /**
     * 给每个人发送消息的封装
     */
    private static class BatchBean{
        SingleMessage message;
        Target target;

        public BatchBean(SingleMessage message, Target target) {
            this.message = message;
            this.target = target;
        }
    }
}
