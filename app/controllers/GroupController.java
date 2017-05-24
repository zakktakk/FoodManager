package controllers;

import play.*;
import play.data.validation.Required;
import play.mvc.*;
import models.*;

import javax.persistence.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

public class GroupController extends Controller {
    
	@Before(unless="index")
    static void makeMenu() {
		String userName = session.get("userID");
		//参加中のグループ情報を渡す
		List<UserGroupRelation> UGRList = new ArrayList<UserGroupRelation>();
		UGRList = UserGroupRelation.find("userID = ? AND joined = ?", userName, true).fetch();
		List<String[]> GroupList = MainController.getNameofGroup(UGRList);
		HashMap<String, List<String>> GroupMap = MainController.getUserByGroupID(UGRList);
		renderArgs.put("GroupList", GroupList);
		renderArgs.put("GroupMap", GroupMap);
		//未参加のグループ情報を渡す
		List<UserGroupRelation> UGRListunjoin = new ArrayList<UserGroupRelation>();
		UGRListunjoin = UserGroupRelation.find("userID = ? AND joined = ?", userName, false).fetch();
		List<String[]> GroupListunjoin = MainController.getNameofGroup(UGRListunjoin);
		renderArgs.put("unjoined", GroupListunjoin);
    }
	
	// 新規グループの作成
	public static void newGroup(boolean existGroup, int page, String userName) {
		renderArgs.put("existGroup", existGroup);
		render(page, userName);
	}

	public static void addGroupName(int page, String userName) {
		long newID = 0;
		String newGroupName = params.get("groupName");
		if (newGroupName == null) {
			newGroup(true, page, userName);
		}
		final List<Group> groups = Group.all().fetch();
		for (final Group g: groups) {
			if (newGroupName.equals(g.groupName)) {
				newGroup(true, page, userName);
			}
		}
		Group group = new Group(newGroupName);
		group.save();
		List<Group> GroupList = new ArrayList<Group>();
		GroupList = Group.find("groupName = ?", newGroupName).fetch();
		if (GroupList.size() == 1) {
			newID = GroupList.get(0).id;
		}
		UserGroupRelation rel = new UserGroupRelation(newID, session.get("userID"), true);
		rel.save();
		inputUser(userName, newGroupName, true, false);
	}

	public static void inputUser(String userName, String groupName, boolean existName, boolean joined) {
		List<Group> GroupList = new ArrayList<Group>();
		GroupList = Group.find("groupName = ?", groupName).fetch();
		Long preId = null;
		if (GroupList.size() != 0) {
			preId = GroupList.get(0).id;	
		}
		List<UserGroupRelation> RelationList, joinedGroup = new ArrayList<UserGroupRelation>();
		RelationList = UserGroupRelation.find("groupId = ? AND userID != ?", preId, userName).fetch();
		joinedGroup = UserGroupRelation.find("userID = ?", userName).fetch();
		renderArgs.put("RelationList", RelationList);
		renderArgs.put("existName", existName);
		renderArgs.put("groupName", groupName);
		renderArgs.put("id", preId);
		renderArgs.put("joined", joined);
		renderArgs.put("existName", existName);
		renderArgs.put("page", -1);
		renderArgs.put("userName", userName);
		renderArgs.put("numUnjoined", RelationList.size());
		renderArgs.put("numGroup", joinedGroup.size());
		System.out.println("------------------------------------" + joinedGroup.size());
		render();
	}

	public static void addUserToGroup(String userName) {
		boolean existName = true;
		boolean joined = false;
		String userID = params.get("userID");
		long id = Long.parseLong(params.get("id"));
		List<User> UserList = new ArrayList<User>();
		UserList = User.find("userID = ?", userID).fetch();
		if (UserList.size() == 1) {
			List<UserGroupRelation> RelationList = new ArrayList<UserGroupRelation>();
			RelationList = UserGroupRelation.find("groupId = ? AND userID = ?", id, userID).fetch();
			if (RelationList.size() == 0) {
				UserGroupRelation rel = new UserGroupRelation(id, userID, false);
				rel.save();
			} else {
				joined = true;
			}
		} else {
			existName = false;
		}
		inputUser(userName, params.get("groupName"), existName, joined);
	}
	
	// 既存グループの編集
	public static void chooseGroup(boolean existGroup) {
		renderArgs.put("existGroup", existGroup);
		render();
	}

	public static void groupEdition(int page, String userName, String groupName, long id, boolean joined, boolean existName) {
		if(groupName == null) {
			List<Group> GroupList = new ArrayList<Group>();
			GroupList = Group.find("id = ?", id).fetch();
			if (GroupList.size() == 1) {
				groupName = GroupList.get(0).groupName;
			}
		}
		List<UserGroupRelation> joinedList, unjoinedList = new ArrayList<UserGroupRelation>();
		joinedList = UserGroupRelation.find("groupID = ? AND joined = ? AND userID != ?", id, true, userName).fetch();
		unjoinedList = UserGroupRelation.find("groupID = ? AND joined = ?", id, false).fetch();
		int numJoined = joinedList.size();
		int numUnjoined = unjoinedList.size();
		renderArgs.put("joinedList", joinedList);
		renderArgs.put("unjoinedList", unjoinedList);
		renderArgs.put("numJoined", numJoined);
		renderArgs.put("numUnjoined", numUnjoined);
		renderArgs.put("groupName", groupName);
		renderArgs.put("id", id);
		renderArgs.put("joined", joined);
		renderArgs.put("existName", existName);
		
		/* ********** */
    	/* 冷蔵庫の処理 */
    	/* ********** */
		//当日の日付の取得
		Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(c.getTime());

    	//ユーザー名とユーザーIDを対応させるマップ
    	HashMap<Long, String> userIdToName = new HashMap<Long, String>();
    	userIdToName.put(Long.parseLong( session.get("userLongID") ), userName);
    	
    	//グループメンバーの食材一覧の取得（期限切れ除く）とユーザーマップ作成
    	List<Food> foods = Food.find("userId = ? AND bestBeforeDate >= ? AND toGroup = ? ORDER BY bestBeforeDate ASC", Long.parseLong(session.get("userLongID")), today, true).fetch();
    	for(UserGroupRelation ugr : joinedList){
    		User u = User.find("userID = ?", ugr.userID).first();
    		foods.addAll(Food.find("userId = ? AND bestBeforeDate >= ? AND toGroup = ? ORDER BY bestBeforeDate ASC", u.id, today, true).fetch());
    		userIdToName.put(u.id, u.userID);
    	}
    	
    	//期限によるソート
    	Collections.sort(foods, new Comparator<Food>(){
    		@Override
    		public int compare(Food f1, Food f2){
    			return (f1.bestBeforeDate.compareTo(f2.bestBeforeDate) >= 0) ? 1 : -1;
    		}
    	});
    	
    	//カテゴリ名とIDを対応させるマップ
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
    	renderArgs.put("userIdToName", userIdToName);
    	renderArgs.put("categoryToIcon", MainController.categoryToIcon);

    	//3日後の日付
        c.add(Calendar.DAY_OF_MONTH, 3);
        renderArgs.put("threeDays", sdf.format(c.getTime()));
		
        /* 冷蔵庫終了 */
        
        // レシピカード生成
    	int cardNum = 3;
		List<Integer> cardList = new ArrayList<Integer>();
		for(int i = 0; i < cardNum; i++){
			cardList.add(i);
		}
		renderArgs.put("cardList", cardList);
        
		render(page, userName);
	}
	
	public static void remove(int page, String userName) {
		long id = Long.parseLong(params.get("id"));
		String groupName = params.get("groupName");
		String userID = params.get("userID");
		UserGroupRelation r = UserGroupRelation.find("groupId = ? AND userID = ?", id, userID).first();
    	r.delete();
    	r = UserGroupRelation.find("groupId = ?", id).first();
    	if (r == null) {
    		Group g = Group.find("groupName = ?", groupName).first();
    		g.delete();
    	}
		if (userID.equals(userName)) {
			MainController.personalForm(0);
		}
		groupEdition(page, userName, groupName, id, false, true);
	}
	
	public static void addNewUser(int page, String userName) {
		long id = Long.parseLong(params.get("id"));
		String userID = params.get("userID");
		boolean joined = false;
		boolean existName = true;
		if (userID != null) {
			List<User> UserList = new ArrayList<User>();
			UserList = User.find("userID = ?", userID).fetch();
			if (UserList.size() == 1) {
				List<UserGroupRelation> RelationList = new ArrayList<UserGroupRelation>();
				RelationList = UserGroupRelation.find("groupId = ? AND userID = ?", id, userID).fetch();
				if (RelationList.size() == 0) {
					UserGroupRelation rel = new UserGroupRelation(id, userID, false);
					rel.save();
				} else {
					joined = true;
				}
			} else {
				existName = false;
			}
		}
		groupEdition(page, userName, params.get("groupName"), id, joined, existName);
	}
}
