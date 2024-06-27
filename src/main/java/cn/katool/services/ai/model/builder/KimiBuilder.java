package cn.katool.services.ai.model.builder;

import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.config.ai.kimi.KimiConfig;
import cn.katool.services.ai.constant.KimiBuilderEnum;
import cn.katool.services.ai.model.entity.Kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiBuilder{
        private StringBuffer url;

        private KimiBuilderEnum master;

        private KimiBuilderEnum status;


        public KimiBuilder(StringBuffer url, KimiBuilderEnum status) {
            this.url = url;
            this.status = status;
        }

        public static KimiBuilder create(){
            return new KimiBuilder(new StringBuffer(KimiConfig.KIMI_BASE_URL),KimiBuilderEnum.BASE);
        }

        private void validStatus(KimiBuilderEnum target){
            if (!target.getLastStatus().contains(this.status)){
                throw new KaToolException(ErrorCode.OPER_ERROR,
                        "current status is " + status.getName()+", but target status's father status is" +  target.getLastStatus());
            }
        }

        private KimiBuilder complete(){
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            KimiBuilderEnum target = KimiBuilderEnum.valueOf(methodName.toUpperCase(Locale.ROOT));
            validStatus(target);
            this.url.append("/"+ target.getName());
            this.status = target;
            return this;
        }
        public KimiBuilder chat(){
            return complete();
        }

        public KimiBuilder completions(){
            return complete();
        }

        public KimiBuilder files(){
            return complete();
        }
        public KimiBuilder tokenizers(){
            return  complete();
        }
        public KimiBuilder estimate_token_count(){
            return  complete();
        }
        public  KimiBuilder users(){
            return complete();
        }
        public KimiBuilder me(){
            return  complete();
        }
        public KimiBuilder balance(){
            return  complete();
        }
        public Kimi build(){
            this.master = this.status;
            this.status = KimiBuilderEnum.END;
            return new Kimi(this,KimiConfig.KIMI_API_KEY,"application/json");
        }
        


    }