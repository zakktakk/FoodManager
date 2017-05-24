package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="exp4mainGroup")
public class Group extends Model {

    public String groupName;
    
    /**
     * コンストラクタ．
     * @param groupName グループの名前(String)
     */
    public Group(String groupName) {
    	this.groupName = groupName;
    }

}
