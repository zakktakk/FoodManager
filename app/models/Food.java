package models;

import javax.persistence.Entity;

import play.*;
import play.db.jpa.*;
import sun.util.logging.resources.logging;

@Entity
public class Food extends Model {
	/* FoodModel */
	
	//食材登録者のID
	public long userId;
	
	//食材の分類のID
	public long foodDatabaseId;
	
	//食材名
	public String foodName;
	
	//期限 "yyyy-MM-dd"形式
	public String bestBeforeDate;
	
	//量
	public String amount;
	
	//グループへの表示可否 optional:余裕があれば処理追加。とりあえずtrue
	public boolean toGroup;
	
	//賞味期限or消費期限などの付加情報 optional、とりあえず""
	public String importance;

	//コンストラクタ
	public Food(long userId, long foodDatabaseId, String foodName, String bestBeforeDate, String amount){
		this.userId = userId;
		this.foodDatabaseId = foodDatabaseId;
		this.foodName = foodName;
		this.bestBeforeDate = bestBeforeDate;
		this.amount = amount;
		
		this.toGroup = true;
		this.importance = "";
	}

	//コンストラクタ（toGroup追加）
	public Food(long userId, long foodDatabaseId, String foodName, String bestBeforeDate, String amount, boolean toGroup){
		this.userId = userId;
		this.foodDatabaseId = foodDatabaseId;
		this.foodName = foodName;
		this.bestBeforeDate = bestBeforeDate;
		this.amount = amount;
		this.toGroup = toGroup;
		
		this.importance = "";
	}
	
	// デバック用コンストラクタ
	public Food(long foodDatabaseId, String foodName){
		this.foodDatabaseId = foodDatabaseId;
		this.foodName = foodName;
	}

	//アップデート用メソッド
	public void updateInfo(String bb, String am){
		this.bestBeforeDate = bb;
		this.amount = am;
	}

	//アップデート用メソッド（toGroup追加）
	public void updateInfo(String bb, String am, boolean tg){
		this.bestBeforeDate = bb;
		this.amount = am;
		this.toGroup = tg;
	}
}
