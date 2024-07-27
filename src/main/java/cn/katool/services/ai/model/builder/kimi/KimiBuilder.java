package cn.katool.services.ai.model.builder.kimi;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.common.CopyOnTransmittableThreadLocal;
import cn.katool.config.ai.kimi.KimiConfig;
import cn.katool.services.ai.CommonAIService;
import cn.katool.services.ai.constant.kimi.KimiBuilderEnum;
import cn.katool.services.ai.model.entity.ErrorMessage;
import cn.katool.services.ai.model.entity.kimi.Kimi;
import cn.katool.util.AiServiceHttpUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import java.util.Locale;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiBuilder extends KimiCommonBuilder{
        private String url;
        private KimiBuilderEnum master;
        private KimiBuilderEnum status;
        public KimiBuilder(String url, KimiBuilderEnum status) {
            this.url = url;
            this.status = status;
        }
        public static KimiBuilder create(){
            return new KimiBuilder(KimiConfig.KIMI_BASE_URL,KimiBuilderEnum.BASE);
        }
        private void validStatus(KimiBuilderEnum target){
            if (!target.getLastStatus().contains(this.status)){
                throw new KaToolException(ErrorCode.OPER_ERROR,
                        "current status is " + status.getName()+", but target status's father status is" +  target.getLastStatus());
            }
        }
        private KimiBuilder solveStepWithMethodName(){
            KimiBuilder kimiBuilder = KimiBuilder.create();
            BeanUtils.copyProperties(this, kimiBuilder);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            KimiBuilderEnum target = KimiBuilderEnum.valueOf(methodName.toUpperCase(Locale.ROOT));
            validStatus(target);
            kimiBuilder.url+=("/"+ target.getName());
            kimiBuilder.status = target;
            return kimiBuilder;
        }
        public KimiChatBuilder chat(){
            return new KimiChatBuilder(solveStepWithMethodName());
        }
        public KimiCacheBuilder caching(){
            return new KimiCacheBuilder(solveStepWithMethodName());
        }
        private KimiCacheRefsBuilder refs(){
            return new KimiCacheRefsBuilder(solveStepWithMethodName());
        }
        private KimiCacheRefsTagsBuilder tags(){
            return new KimiCacheRefsTagsBuilder(solveStepWithMethodName());
        }
        private KimiBuilder completions(){
            return solveStepWithMethodName();
        }
        private KimiBuilder content(){
            return solveStepWithMethodName();
        }
        public KimiFilesBuilder files(){
            return new KimiFilesBuilder(solveStepWithMethodName());
        }
        public KimiTokenizersBuilder tokenizers(){
            return  new KimiTokenizersBuilder(solveStepWithMethodName());
        }
        private KimiBuilder estimate_token_count(){
            return  solveStepWithMethodName();
        }
        public  KimiUsersBuilder users(){
            return new KimiUsersBuilder(solveStepWithMethodName());
        }
        private KimiBuilder me(){
            return  solveStepWithMethodName();
        }
        private KimiBuilder balance(){
            return  solveStepWithMethodName();
        }
        public Kimi build(AiServiceHttpUtil httpUtil){
            return build(httpUtil,null);
        }

        public Kimi build(AiServiceHttpUtil httpUtil,Map<String, String> cacheHeaders){
            return build(KimiConfig.getKimiKey(),httpUtil,cacheHeaders);
        }
        public Kimi build(String kimiApiKey,AiServiceHttpUtil httpUtil,Map<String, String> cacheHeaders){
            this.master = this.status;
            this.status = KimiBuilderEnum.END;
            return new Kimi(this,kimiApiKey,new CopyOnTransmittableThreadLocal<String>(){
                @Override
                protected String initialValue() {
                    return "application/json";
                }
            },httpUtil,cacheHeaders);
        }
    }