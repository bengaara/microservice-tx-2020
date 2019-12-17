package net.tospay.transaction.models;

import java.io.Serializable;

import net.tospay.transaction.util.Utils;

public class BaseModel  implements Serializable
{


    @Override
    public String toString(){
        return Utils.inspect(this);
    }
}
