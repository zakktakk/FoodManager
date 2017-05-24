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

public class FoodDatabaseController extends Controller {

	public static void rakutenApi() {
		render();
	}

	public static void registerFoodDatabase(@Required String foods) {
		String foodRows[] = foods.split("\n");
		for (int i = 0; i < foodRows.length; i++) {
			String foodArray[] = foodRows[i].split(",");
			FoodDatabase foodDatabase = new FoodDatabase(foodArray[0],
					foodArray[1], foodArray[2]);
			foodDatabase.save();
		}

		// Map に結果を蓄え，JSON として出力する．
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", "OK");
		map.put("result", "FoodDatabase完成");

		renderJSON(map);
	}
}
