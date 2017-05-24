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

public class UserController extends Controller {
   
	public static void checkUserID() {
		// ここでdb照合して分岐
		String userID = params.get("userID");
		String password = getSHA256(params.get("password"));
		List<User> pulldata = User.find("userID = ?", userID).fetch();
		if (pulldata.size() != 0) {
			if (pulldata.get(0).matchPassWord(password)) {
				// ログインに成功
					//sessionにuser.idを追加します tanikawa
				session.put("userLongID", pulldata.get(0).id);
				session.put("userID", userID);
				session.put("login_now", "true");
				MainController.homeFrame("0");
			} else {
				MainController.index("true");
			}
		} else {
			// userIDが間違ってる場合
			MainController.index("true");
		}
	}

	public static void logout() {
		session.put("login_now", "false");
		MainController.index("false");
	}

	public static void createUser(String loginfailed) {
		// ログイン情報を取得
		String login_status = session.get("login_now");
		if ("true".equals(login_status)) {
			String message = "true";
			render(message, loginfailed);
		} else {
			String message = "false";
			render(message, loginfailed);
		}
	}

	public static void postCreateUserID() {
		// ユーザーの新規作成
		String userID = params.get("userID");
		String password = getSHA256(params.get("password"));
		List<User> pulldata = User.find("userID = ?", userID).fetch();
		if (pulldata.size() == 0) {
			// userIDに該当するユーザーがまだ存在しない場合
			session.put("userID", userID);
			session.put("login_now", "true");
			User user_data = new User(userID, password);
			user_data.save();
				//sessionにuser.idを追加します tanikawa
			session.put("userLongID", user_data.id);
			MainController.homeFrame("0");
		} else {
			// 同じ名前のuserが既に存在
			createUser("true");
		}
	}
	//アカウント削除用メソッド
	public static void deleteAccount(String deletefailed) {
		render(deletefailed);
	}
	//アカウント削除用メソッド
	public static void deleteAnAcount() {
		// pass認証
		// あってたら消してindexへ
		// なかったら間違ってますを送信
		String userID = session.get("userID");
		String password = getSHA256(params.get("password"));
		List<User> pulldata = User.find("userID = ?", userID).fetch();
		if (pulldata.size() != 0) {
			if (pulldata.get(0).matchPassWord(password)) {
				pulldata.get(0).delete();
				session.put("login_now", "false");
				MainController.index("false");
			} else {
				deleteAccount("true");
			}
		} else {
			// userIDが間違ってる
			deleteAccount("true");
		}
	}
	
	//パスワード変更用メソッド
	public static void changePassword(String changefailed) {
		render(changefailed);
	}
	//パスワード変更用メソッド
	public static void PostPassword() {
		String userID = session.get("userID");
		String old_password = getSHA256(params.get("old_password"));
		String new_password = getSHA256(params.get("new_password"));
		List<User> pulldata = User.find("userID = ?", userID).fetch();
		if (pulldata.size() != 0) {
			if (pulldata.get(0).matchPassWord(old_password)) {
				pulldata.get(0).setPassWord(new_password);
				pulldata.get(0).save();
				MainController.personalForm(0);
			} else {
				changePassword("true");
			}
		} else {
			// userIDが間違ってる
			changePassword("true");
		}
	}

	public static HashMap<String, String> getIdNameMap(List<UserGroupRelation> UGRList){
		HashMap<String, String> retMap = new HashMap<String, String>();
		for(UserGroupRelation p : UGRList){
			Group temp = (Group)Group.find("id = ?", p.groupID).fetch().get(0);
			retMap.put(String.valueOf(p.groupID), temp.groupName);
		}
		return retMap;
	}
	
	public static List<String> getNameofGroup(List<UserGroupRelation> UGRList){
		List<String> retList = new ArrayList<String>();
		for(UserGroupRelation p : UGRList){
			Group temp = (Group)Group.find("id = ?", p.groupID).fetch().get(0);
			retList.add(temp.groupName);
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
			retList.add(p.userID);
		}
		return retList;
	}

	/**
	 * バイト列を16進の文字列に変換する．
	 */
	public static String bytesToHexString(byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : bytes) {
			final String s = Integer.toHexString(0xff & b);
			sb.append(s.length() == 1 ? "0" + s : s);
		}
		return sb.toString();
	}

	/**
	 * 文字列をバイト列とみなし，そのSHA256値を計算する．
	 */
	public static String getSHA256(String s) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] bytes = s.getBytes();
			digest.update(bytes, 0, bytes.length);

			return bytesToHexString(digest.digest());
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}
}
