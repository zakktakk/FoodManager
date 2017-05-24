package models;
import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class User extends Model{
	/*User model*/
	//ユーザーID
	public String userID;
	//パスワード
	private String Password;
	//コンストラクタ
	public User(String userID, String Password){
		this.userID = userID;
		this.Password = Password;
	}
	//パスワード一致判定用メソッド
	public boolean matchPassWord(String password){
		return Password.equals(password);
	}
	//パスワード変更用メソッド
	public void setPassWord(String pass){
    	Password = pass;
    }
}
