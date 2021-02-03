package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Transient;
import lombok.Data;
import net.tospay.transaction.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class BaseModel {



    @JsonIgnore
    boolean hideData = false;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public boolean isHideData() {
        return hideData;
    }

    public BaseModel setHideData(boolean hideData) {
        this.hideData = hideData;
        LoggerFactory.getLogger(this.getClass()).debug("setHideData {} {}  ", hideData, Utils.getCallerCallerClassName());
        return this;
    }

    public BaseModel withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
//
//    @Override
//    public String toString() {
//        String s = "";
//        if (!hideData) {
//            s = Utils.inspect(this);
//        } else {
//            s = super.toString();
//        }
//        hideData = !hideData;//toggle show once only
//        logger.debug("toString toggling hideData to {} {}", hideData, Utils.getCallerCallerClassName());
//        return s;
//    }
}
