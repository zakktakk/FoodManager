package models;

import javax.persistence.Entity;

import play.*;
import play.db.jpa.*;

@Entity
public class FoodDatabase extends Model {
	/* FoodDatabase */
	
	//食材名
	public String foodName;
	
	//楽天レシピランキングAPIにアクセスするためのID
	public String rakutenId;
	
	//カテゴリーの名前
	public String category;
	
	//コンストラクタ
	public FoodDatabase(String foodName, String rakutenId, String category){
		this.foodName = foodName;
		this.rakutenId = rakutenId;
		this.category = category;
	}
}
