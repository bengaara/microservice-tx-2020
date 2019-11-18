package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 7/31/2019, Wed
 **/
public abstract class BaseEntity<T>
{
    @JsonIgnore
    public abstract T getId();

    public abstract void setId(T id);
}
