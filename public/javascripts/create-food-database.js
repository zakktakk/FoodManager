/*
 *  楽天から食材データベースの元となるデータを受けり、整形後foodsDataに格納
 *  サーバにfoodsDataを送りサーバはデータベースを完成させる
 */

// 楽天から受け取ったデータを整形して受け渡す
var foodsData = "";
//楽天APIから未完成食材データベースを受け取り表示
$.ajax({
    type: 'GET',
    url: 'https://app.rakuten.co.jp/services/api/Recipe/CategoryList/20121121?format=json&applicationId=1001249560279861388',
    dataType: 'json',
    success: function(json) {
        // JSON分解
        var result = json.result;
        var large = result.large;
        var medium = result.medium;
        var small = result.small;

        // smallだけ分解
        var smallCategoryId = new Array();
        var smallCategoryName = new Array();
        var smallCategoryUrl = new Array();

        // mediuだけ分解
        var mediumCategoryId = new Array();
        var mediumCategoryName = new Array();
        var mediumCategoryUrl = new Array();

        // 全食材名
        var foodName = new Array();
        // 全食材のCategoryUrl
        var foodCategoryUrl = new Array();

        for (i in small) {
            smallCategoryId[i] = small[i].categoryId;
            smallCategoryName[i] = small[i].categoryName;
            smallCategoryUrl[i] = small[i].categoryUrl;

            foodName[i] = small[i].categoryName;
            foodCategoryUrl[i] = small[i].categoryUrl;
        }

        for (i in medium) {
            mediumCategoryId[i] = medium[i].categoryId;
            mediumCategoryName[i] = medium[i].categoryName;
            mediumCategoryUrl[i] = medium[i].categoryUrl;

            foodName[small.length + i] = medium[i].categoryName;
            foodCategoryUrl[small.length + i] = medium[i].categoryUrl;
        }

        // まず各食材に対応する楽天API用のIDを求める

        // それぞれの食材
        var target
            // それぞれの食材のURL
        var targetUrl;
        // targetUrlから抽出される楽天API用のID
        var rakutenApiId;
        // カテゴリー名
        var category;
        // 楽天ApiIdをハイフンでsplitしたもの
        var rakutenApiIds;
        // 自前のカテゴリー
        var categoryIds = ['14', '22', '16', '10', '11', '33', '35', '12', '34'];
        var categories = {
            '14': '米',
            '22': 'パン',
            '16': '麺類',
            '10': '肉',
            '11': '魚',
            '33': '卵',
            '35': '豆',
            '12': '野菜',
            '34': '果物'
        };

        for (h in foodName) {
            target = foodName[h];
            targetUrl = foodCategoryUrl[h].split("/");

            // 一致してURLを分解すると5番目にcategoryIdとハイフンの形表れる
            rakutenApiId = targetUrl[4];

            // 楽天IDからカテゴリーを決定する
            rakutenApiIds = rakutenApiId.split("-");
            for (i in rakutenApiIds) {
                category = "その他";
                for (j in categoryIds) {
                    if (categoryIds[j] == rakutenApiIds[i]) {
                        // console.log(categories[categoryIds[j]]);
                        category = categories[categoryIds[j]];
                        break;
                    }
                }
                if (category != "その他") {
                    break;
                }
            }
            foodsData += target + "," + rakutenApiId + "," + category + "\n";
        }
    },
    complete:
    // javaでデータベースに登録させる
        function sendFoods() {
        var req = new XMLHttpRequest();
        req.open("POST", "/registerFoodDatabase");
        // 結果が帰ってきた際に実行されるハンドラを指定する．
        req.onreadystatechange = function(data) {
            if (req.readyState != 4) {
                return;
            }
            if (req.status != 200) {
                alert("失敗しました．");
                return;
            }
            // body にはサーバから返却された文字列が格納される．
            var body = req.responseText;

            // 戻ってきた JSON 文字列を JavaScript オブジェクトに変換
            var data = eval('(' + body + ')');

            // デバッグ表示
            alert(data.result);
            
            // メインページにリダイレクト
            window.location.href = "http://localhost:9001/";
        }

        req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        req.send("foods=" + enc(foodsData));
    }
});

// 入力文字列を urlencode して返す．
function enc(s) {
    return encodeURIComponent(s).replace(/%20/g, '+');
}