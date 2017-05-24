package controllers;

import play.*;
import play.data.validation.Required;
import play.mvc.*;
import models.*;

import javax.persistence.*;

import com.mchange.v2.cfg.PropertiesConfigSource.Parse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Food;
import models.FoodDatabase;
import models.User;
import play.data.validation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Group;
import models.User;
import models.UserGroupRelation;
import play.mvc.Controller;

public class MainController extends Controller {

	//カテゴリアイコンとカテゴリ名のマップ
	public static final HashMap<String, String> categoryToIcon = new HashMap<String, String>(){
		{
			put("肉", "meat");
			put("魚", "fish");
			put("野菜", "vegetable");
			put("米", "rice");
			put("麺類", "noodles");
			put("パン", "bread");
			put("卵", "egg");
			put("果物", "fruit");
			put("豆", "beans");
		}
	};
	
	@Before(unless="index")
    static void makeMenu() {
		String userName = session.get("userID");
		//参加中のグループ情報を渡す
		List<UserGroupRelation> UGRList = new ArrayList<UserGroupRelation>();
		UGRList = UserGroupRelation.find("userID = ? AND joined = ?", userName, true).fetch();
		List<String[]> GroupList = getNameofGroup(UGRList);
		HashMap<String, List<String>> GroupMap = getUserByGroupID(UGRList);
		renderArgs.put("GroupList", GroupList);
		renderArgs.put("GroupMap", GroupMap);
		//未参加のグループ情報を渡す
		List<UserGroupRelation> UGRListunjoin = new ArrayList<UserGroupRelation>();
		UGRListunjoin = UserGroupRelation.find("userID = ? AND joined = ?", userName, false).fetch();
		List<String[]> GroupListunjoin = getNameofGroup(UGRListunjoin);
		renderArgs.put("unjoined", GroupListunjoin);
    }
	
	public static void index(String loginfailed) {
		// 初期アクセス時にFoodDatabaseを作るために遷移する
		if(FoodDatabase.findAll().size() == 0){
			FoodDatabaseController.rakutenApi();
		}
		
		String login_status = session.get("login_now");
		if ("true".equals(login_status)) {
			String message = "true";
			render(message, loginfailed);
		} else {
			String message = "false";
			render(message, loginfailed);
		}	
	}
    
    public static void personalForm(int page){
    	
    	/* ************* */
    	/* 食材追加部の処理 */
    	/* ************* */
    	
    	String userName = session.get("userID");
    	List<FoodDatabase> fdb = FoodDatabase.all().fetch();
    	//カテゴリのリストを作成する
    	List<String> fdbCategories = new ArrayList<String>();
    	for(FoodDatabase f : fdb){
    		if( !fdbCategories.contains(f.category) ){
    			fdbCategories.add(f.category);
    		}
    		
    	}
    	//カテゴリ一覧をHTML側に渡す
    	renderArgs.put("categories", fdbCategories);
    	
    	//カテゴリ毎に食材一覧を作り、html側に渡す
    	List<List<FoodDatabase>> foodstuffList = new ArrayList<List<FoodDatabase>>();
    	for(String cat : fdbCategories){
    		foodstuffList.add(FoodDatabase.find("category = ?", cat).fetch());
    	}
    	renderArgs.put("foodstuffDividedWithCategory", foodstuffList);
    	
    	//当日、翌日、三日後の日付を取得してhtml側へ
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        renderArgs.put("today", sdf.format(c.getTime()));
        c.add(Calendar.DAY_OF_MONTH, 1);
        renderArgs.put("tomollow", sdf.format(c.getTime()));
        c.add(Calendar.DAY_OF_MONTH, 2);
        renderArgs.put("threeDays", sdf.format(c.getTime()));
    	
    	
    	/* ********** */
    	/* 冷蔵庫の処理 */
    	/* ********** */
    	//ユーザID取得
    	long user_id = Long.parseLong(session.get("userLongID"));
    	//食材一覧の取得
    	List<Food> foods = Food.find("userId = ? ORDER BY bestBeforeDate ASC", user_id).fetch();
    	//カテゴリを取得するためのマップ作成
    	HashMap<Long,FoodDatabase> fdbIdToCategory = new HashMap<Long, FoodDatabase>();
    	//直接入力用ダミーカテゴリ
    	fdbIdToCategory.put(-1L, new FoodDatabase("" , "", "直接入力"));
    	for(Food f : foods){
    		if(fdbIdToCategory.containsKey(f.foodDatabaseId)) continue;
    		FoodDatabase fd = FoodDatabase.find("id = ?", f.foodDatabaseId).first();
    		fdbIdToCategory.put(f.foodDatabaseId, fd);
    	}
    	
    	//html側に渡す
    	renderArgs.put("foods",  foods);
    	renderArgs.put("fdbIdToCategory", fdbIdToCategory);
    	renderArgs.put("categoryToIcon", categoryToIcon);

    	// レシピカード生成
    	int cardNum = 3;
		List<Integer> cardList = new ArrayList<Integer>();
		for(int i = 0; i < cardNum; i++){
			cardList.add(i);
		}
		renderArgs.put("cardList", cardList);
		
    	render(page, userName);
    }
    
    /**
     * addFoodstuff : 食材追加処理 jsから呼ばれる
     */
    public static void addFoodstuff(@Required String fdbid, @Required String fsn, @Required String bb, @Required String am, @Required String tg){
    	//食材名は直接入力されていなければセレクトボックスから取得
    	String foodName = fsn;
    	if(!fdbid.equals("-1")){
    		FoodDatabase fdb = FoodDatabase.find("id = ?", Long.parseLong(fdbid) ).first();
    		foodName = fdb.foodName;
    	}
    	//tg:1 or 0 -> true or false
    	boolean tgb = tg.equals("1");
    	//ユーザーIDの取得
    	long uid = Long.parseLong( session.get("userLongID") );
    	//食材の新規追加
    	Food food = new Food(uid, Long.parseLong(fdbid), foodName, bb, am, tgb);
    	food.save();
    	
    	// Map に結果を蓄え，JSON として出力する．
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", "OK");
        //js側に追加した食材のIDを伝達
        map.put("foodId", food.id);
        //食材名もIDで管理しているため表示用にjava側から伝える
        map.put("foodName", food.foodName);
        //楽天ID追加
        if(!fdbid.equals("-1")){
	        FoodDatabase fdb = FoodDatabase.find("id = ?", Long.parseLong(fdbid) ).first();
	        map.put("rakutenId", fdb.rakutenId);
        }else{
        	map.put("rakutenId", "");
        }
        
        renderJSON(map);
    }
    
    /**
     * updateFoodstuff : 食材情報更新処理 jsから呼ばれる
     */
    public static void updateFoodstuff(@Required String foodId, @Required String bb, @Required String am, @Required String tg){
    	//tg:1 or 0 -> true or false
    	boolean tgb = tg.equals("1");
    	
    	//食材の更新
    	Food food = Food.find("id = ?", Long.parseLong(foodId)).first();
    	food.updateInfo(bb, am, tgb);
    	food.save();
    	
    	// Map に結果を蓄え，JSON として出力する．
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", "OK");
        //js側に追加した食材のIDを伝達
        map.put("foodId", food.id);
        //楽天ID追加
        if(! (food.foodDatabaseId == -1)){
	        FoodDatabase fdb = FoodDatabase.find("id = ?", food.foodDatabaseId ).first();
	        map.put("rakutenId", fdb.rakutenId);
        }else{
        	map.put("rakutenId", "");
        }
        renderJSON(map);
    }
    
    /**
     * deleteFoodstuff : 食材削除処理 jsから呼ばれる
     */
    public static void deleteFoodstuff(@Required String foodId){
    	Food f = Food.find("id = ?", Long.parseLong(foodId)).first();
    	f.delete();
    	
    	// Map に結果を蓄え，JSON として出力する．
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", "OK");
        renderJSON(map);
    }
    
    /**
     * groupRefrigerator : グループ用冷蔵庫 : GroupControllerに移動
     */
//    public static void groupRefrigerator(){
//    	/* ********** */
//    	/* 冷蔵庫の処理 */
//    	/* ********** */
//    	//ユーザID取得
//    	long user_id = Long.parseLong(session.get("userLongID"));
//    	//食材一覧の取得
//    	List<Food> foods = Food.find("userId = ? ORDER BY bestBeforeDate ASC", user_id).fetch();
//    	HashMap<Long,FoodDatabase> fdbIdToCategory = new HashMap<Long, FoodDatabase>();
//    	fdbIdToCategory.put(-1L, null);
//    	for(Food f : foods){
//    		if(fdbIdToCategory.containsKey(f.foodDatabaseId)) continue;
//    		FoodDatabase fd = FoodDatabase.find("id = ?", f.foodDatabaseId).first();
//    		fdbIdToCategory.put(f.foodDatabaseId, fd);
//    	}
//    	
//    	//html側に渡す
//    	renderArgs.put("foods",  foods);
//    	renderArgs.put("fdbIdToCategory", fdbIdToCategory);
//    	
//    	// レシピカード生成
//    	int cardNum = 3;
//		List<Integer> cardList = new ArrayList<Integer>();
//		for(int i = 0; i < cardNum; i++){
//			cardList.add(i);
//		}
//		renderArgs.put("cardList", cardList);
//		
//		return;
//    }
    
    /**
     * Tanikawa End
     */

	public static void homeFrame(String page_num) {
		int page = 0;
		if (page_num != null) {
			page = Integer.parseInt(page_num);
		}
		String userName = session.get("userID");
		/*
		//参加中のグループ情報を渡す
		List<UserGroupRelation> UGRList = new ArrayList<UserGroupRelation>();
		UGRList = UserGroupRelation.find("userID = ? AND joined = ?", userName, true).fetch();
		List<String[]> GroupList = getNameofGroup(UGRList);
		HashMap<String, List<String>> GroupMap = getUserByGroupID(UGRList);
		renderArgs.put("GroupList", GroupList);
		renderArgs.put("GroupMap", GroupMap);
		//未参加のグループ情報を渡す
		List<UserGroupRelation> UGRListunjoin = new ArrayList<UserGroupRelation>();
		UGRListunjoin = UserGroupRelation.find("userID = ? AND joined = ?", userName, false).fetch();
		List<String[]> GroupListunjoin = getNameofGroup(UGRListunjoin);
		renderArgs.put("unjoined", GroupListunjoin);
		*/

		//TEST : それぞれのページにリダイレクト
		//render(page, userName);
		if(page == 0){
			personalForm(page);
		}else if(page == -1){
			GroupController.newGroup(false, page, userName);
		}else{
			GroupController.groupEdition(page, userName, null, Long.parseLong(params.get("groupID")), false, true);
			render(page, userName);
		}
	}
		
	public static void homeFrame(String page_num, long groupID) {
		int page = 0;
		if (page_num != null) {
			page = Integer.parseInt(page_num);
		}
		String userName = session.get("userID");

		//TEST : それぞれのページにリダイレクト
		//render(page, userName);
		if(page == 0){
			personalForm(page);
		}else if(page == -1){
			GroupController.newGroup(false, page, userName);
		}else{
			GroupController.groupEdition(page, userName, null, groupID, false, true);
			render(page, userName);
		}
	}
	
	public static void acceptInvite(String id){
		String userName = session.get("userID");
		List<UserGroupRelation> UGR = UserGroupRelation.find("userID = ? AND groupID = ?", userName, Long.parseLong(id)).fetch();
		UserGroupRelation p = UGR.get(0);
		p.joined = true;
		p.save();
		homeFrame("0");
	}
	
	public static void denyInvite(String id){
		String userName = session.get("userID");
		System.out.println("sssssssssssssssss" + id);
		List<UserGroupRelation> UGR = UserGroupRelation.find("userID = ? AND groupID = ?", userName, Long.parseLong(id)).fetch();
		UserGroupRelation p = UGR.get(0);
		p.delete();
		homeFrame("0");
	}
	
	public static HashMap<String, String> getIdNameMap(List<UserGroupRelation> UGRList){
		HashMap<String, String> retMap = new HashMap<String, String>();
		for(UserGroupRelation p : UGRList){
			Group temp = (Group)Group.find("id = ?", p.groupID).fetch().get(0);
			retMap.put(String.valueOf(p.groupID), temp.groupName);
		}
		return retMap;
	}
	
	public static List<String[]> getNameofGroup(List<UserGroupRelation> UGRList){
		List<String[]> retList = new ArrayList<String[]>();
		for(UserGroupRelation p : UGRList){
			Group temp = (Group)Group.find("id = ?", p.groupID).fetch().get(0);
			String[] pair = new String[2];
			pair[0]= String.valueOf(p.groupID);
			pair[1]= temp.groupName;
			retList.add(pair);
		}
		return retList;
	}
	
	public static HashMap<String, List<String>> getUserByGroupID(List<UserGroupRelation> UGRList){
		HashMap<String, List<String>> retMap = new HashMap<String, List<String>>();
		for(UserGroupRelation p : UGRList){
			Group temp = (Group)Group.find("id = ?", p.groupID).fetch().get(0);
			List<UserGroupRelation> tempList = UserGroupRelation.find("groupID = ?", p.groupID).fetch();
			retMap.put(temp.groupName, getuserIDofGroup(tempList));
		}
		return retMap;
	}
	
	public static List<String> getuserIDofGroup(List<UserGroupRelation> UGRList){
		List<String> retList = new ArrayList<String>();
		for(UserGroupRelation p : UGRList){
			if (p.joined == true) {
				retList.add(p.userID);
			}
		}
		return retList;
	}

}
