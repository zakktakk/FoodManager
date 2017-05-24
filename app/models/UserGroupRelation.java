package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class UserGroupRelation extends Model {

    public long groupID;
    public String userID;
    public boolean joined;
    
    /**
     * コンストラクタ．
     * @param groupID グループID(long)
     * @param userID ユーザーの名前(String)
     * @param join ユーザーがグループに参加しているか招待中か(boolean)
     */
    public UserGroupRelation(long groupID, String userID, boolean joined) {
    	this.groupID = groupID;
        this.userID = userID;
        this.joined = joined;
    }
    
}