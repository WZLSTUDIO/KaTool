package cn.katool.services.ai.model.entity;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class ErrorMessage<T extends RequestBody> {
    @Expose(serialize = false, deserialize = false)
    T requestBody;
}
