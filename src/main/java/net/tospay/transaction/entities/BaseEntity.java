package net.tospay.transaction.entities;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

import net.tospay.transaction.util.Utils;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 7/31/2019, Wed
 **/
@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class),
        @TypeDef(name = "int-array", typeClass = IntArrayType.class),
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
        @TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class),
        @TypeDef(name = "json-node", typeClass = JsonNodeStringType.class) })
@MappedSuperclass
public abstract class BaseEntity<T>
{
    @JsonIgnore
    public abstract T getId();

    public abstract void setId(T id);

    public static String toDbField(String s){
        return Arrays.asList(s.split("_")).stream()
                .map(String::toLowerCase)
                .reduce((s1, s2) -> s1 +s2.substring(0,1).toUpperCase()+s2.substring(1).toLowerCase()).get();

    }

    @Override
    public String toString(){
       return Utils.inspect(this);
    }

}
