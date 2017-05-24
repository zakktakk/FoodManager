/*
 * Masakazu start
 */

 //カテゴリとアイコンの対応配列
var categoryToIcon = {
        "肉" : "meat",
        "魚" : "fish",
        "野菜" : "vegetable",
        "米" : "rice",
        "麺類" : "noodles",
        "パン" : "bread",
        "卵" : "egg",
        "果物" : "fruit",
        "豆" : "beans"
};

// rakutenApiのId
var categoryId = '10-66-50';
var refFood = 'ソーセージ・ウインナー(サンプル)';
// ロード時
$(document).ready(function(){
    // 今のところ使用期限が一番近い食材のレシピ三つ
    var rakutenIds = document.getElementsByClassName("refRakutenId");
    var refFoods = document.getElementsByClassName("refFood");
    // 食材が存在して
    if (rakutenIds.length > 0) {
        // 直接入力等で楽天IDが空でない
        if(rakutenIds[0].value != ''){
            // input hiddenから取得
            categoryId = rakutenIds[0].value;
            // spanから取得
            refFood = refFoods[1].innerHTML;
        }else{
            // 直接入力時カードに表示する食材名
            refFood = 'サンプル';
        }
    }
    displayRakutenApiRecipe(categoryId, refFood);
});

// 食材を選び、その食材のレシピをカードに表示
// リロード時と食材登録直後の更新で使用
function displayRakutenApiRecipe(categoryId, refFood) {
    
    var req = new XMLHttpRequest();
    // 送信先のURLを指定する．
    req.open(
        "POST",
        "https://app.rakuten.co.jp/services/api/Recipe/CategoryRanking/20121121?format=json&applicationId=1001249560279861388&categoryId=" + this.categoryId);
    // 結果が帰ってきた際に実行されるハンドラを指定する．
    req.onreadystatechange = function() {
        // readyState == 4: 修了
        if (req.readyState != 4) {
            return;
        }
        // status == 200: 成功
        if (req.status != 200) {
            console.log("楽天api通信エラー");
            return;
        }

        // body にはサーバから返却された文字列が格納される．
        var body = req.responseText;

        // 戻ってきた JSON 文字列を JavaScript オブジェクトに変換
        var data = eval('(' + body + ')');

        // resultのkeyは三つのレシピ情報（配列）
        var recipes = data.result;

        var debugStr = "";
        for (i in recipes) {
            debugStr += recipes[i].recipeTitle + ", " + recipes[i].recipeUrl + ", " + recipes[i].foodImageUrl + "\n";
        }

        // jQueryでHTML書き換え
        for (i in data.result) {
            $('.card-img' + '.' + i).attr("src", data.result[i].foodImageUrl);
            $('.card-title' + '.' + i).text(data.result[i].recipeTitle);
            $('.card-title' + '.' + i).attr("href", data.result[i].recipeUrl);
            $('.card-text' + '.' + i).text(data.result[i].recipeDescription);
            $('.card-text' + '.card-food-name' + '.' + i).text(refFood);
        }
    }

    // Content-Type の指定
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    // <input id="f"> に入力された文字列をエンコードして送信
    req.send("p=" + enc(document.getElementById("recipe").value));
}

// 入力文字列を urlencode して返す．
function enc(s) {
        return encodeURIComponent(s).replace(/%20/g, '+');
    }
    /*
     * Masakazu end
     */

/**
 * クリアボタンの処理
 */
function clearForm() {
    //フォームの取得
    var form = document.forms.foodstuffAddForm;

    //カテゴリセレクトボックスの初期化
    form.fafSelectCategory.selectedIndex = 0;
    form.fafSelectCategory.disabled = false;

    //食材セレクトボックスの初期化
    var selectBoxes = document.getElementsByClassName("fafSelectFoodstuffBoxes");
    for (var i = 0; i < selectBoxes.length; i++) {
        selectBoxes[i].selectedIndex = 0;
        selectBoxes[i].style.display = "none";
        selectBoxes[i].disabled = false;
    }
    selectBoxes[0].style.display = "inline-block";

    //食材名テキストボックスの初期化
    form.fafFoodstuffName.value = "";
    form.fafFoodstuffName.style.display = "none";
    form.fafFoodstuffName.disabled = false;

    //期限の初期化
    form.fafBestBefore.value = getNDaysAfterDate(1);

    //量の初期化
    form.fafAmount.value = "";
    
    //グループ表示のラジオの初期化
    document.getElementsByName("fafToGroup")[0].checked = true;

    //編集フラグの削除
    form.fafEditFlg.value = "false";
}


/**
 * 追加ボタンの処理
 */
function addFoodstuff() {
    /* ******************* */
    /* 食材などの情報を取得する */
    /* ******************* */

    var form = document.forms.foodstuffAddForm;
    var editFlg = form.fafEditFlg.value;
    if (editFlg != "false") {
        updateFoodstuff();
        return;
    }

    //カテゴリ関連の情報
    var categorySI = form.fafSelectCategory.selectedIndex;
    var categorySVal = form.fafSelectCategory.options[categorySI].value;
    var category = form.fafSelectCategory.options[categorySI].text;

    //選択カテゴリに対応する食材セレクトボックスを取得し、FoodDatabaseIdを取得する
    //直接入力時は-1を送信する
    var fdbid = -1;
    if (categorySVal != -1) {
        var foodstuffSelectBox = document.getElementsByName("fafSelectFoodstuff_" + categorySVal)[0];
        fdbid = foodstuffSelectBox.value;
    }

    //食材名：直接入力でなければ""を送る
    var foodstuffName = form.fafFoodstuffName.value;
    if (categorySVal != -1) {
        foodstuffName = "";
    }

    var bestBefore = form.fafBestBefore.value;
    var amount = form.fafAmount.value;
    var toGroup = form.fafToGroup.value;

    /* **************** */
    /* 入力のチェックと警告 */
    /* **************** */
    //直接入力で、文字入力なし
    if (categorySVal == -1 && foodstuffName == "") {
        alert("直接入力時は食材名の入力が必須です！");
        form.fafFoodstuffName.focus();
        return;
    }

    //日付の入力の判定
    var returnFlg = false;
    if (bestBefore == "") {
        //1:入力不備:空の文字列になるためそれを確認
        alert("期限の入力に不備があります！");
        returnFlg = true;
        //return;
    } else if (bestBefore < getNDaysAfterDate(0)) {
        //2:入力が本日以前の日付
        if (!confirm("本日より前の日付が入力されています。本当によろしいですか？")) {
            returnFlg = true;
            //return;
        }
    } else if (bestBefore > getNDaysAfterDate(365)) {
        //3:入力が1年以上先の場合
        if (!confirm("本日より1年以上先の日付が入力されています。本当によろしいですか？")) {
            returnFlg = true;
            //return;
        }
    }
    if(returnFlg){
        //確認ダイアログが表示されている状態ではフォーカス移動ができなかったための措置
        form.fafBestBefore.focus();
        return;
    }

    //量の入力の有無の判定
    if (amount == "") {
        if (!confirm("数量が未入力です。本当によろしいですか？")) {
            returnFlg = true;
            //return;
        }
        if(returnFlg){
            //確認ダイアログが表示されている状態ではフォーカス移動ができなかったための措置
            form.fafAmount.focus();
            return;
        }
    }

    var req = new XMLHttpRequest();
    // 送信先のURLを指定する．
    req.open("POST", "/maincontroller/addFoodstuff");
    // 結果が帰ってきた際に実行されるハンドラを指定する．
    req.onreadystatechange = function() {
            // readyState == 4: 修了
            if (req.readyState != 4) {
                return;
            }
            // status == 200: 成功
            if (req.status != 200) {
                // 成功しなかったため，失敗であることを表示して抜ける．
                alert("食材の追加に失敗した可能性がございます。一度ページをリロードし、食材が追加されていなければ再度お試しください。失敗が続くようであれば、お手数ですが管理者にご連絡いただけると助かります。");
                return;
            }

            // body にはサーバから返却された文字列が格納される．
            var body = req.responseText;

            // 戻ってきた JSON 文字列を JavaScript オブジェクトに変換
            var data = eval("(" + body + ")");

            if (data.status = "OK") {
                // liの生成
                var newLi = document.createElement("li");
                newLi.classList.add("refFoodstuffs");
                newLi.setAttribute("id", "refFoodstuffs_" + data.foodId);
                
                //spanで各要素の生成
                
                
                var spanCategory = document.createElement("span");
                spanCategory.classList.add("refCategory");
                if(category in categoryToIcon){
                    //画像の生成
                    var img = document.createElement("img");
                    img.classList.add("refCategoryIcon");
                    img.setAttribute("src", "/public/images/categoryIcons/" + categoryToIcon[category] + ".png");
                    img.setAttribute("alt", category);
                    spanCategory.appendChild(img);
                }else{
                    spanCategory.innerHTML = category;
                }

                var spanFood = document.createElement("span");
                spanFood.classList.add("refFood");
                spanFood.innerHTML = data.foodName;

                var spanAmount = document.createElement("span");
                spanAmount.classList.add("refAmount");
                spanAmount.innerHTML = amount;

                var spanBestbefore = document.createElement("span");
                spanBestbefore.classList.add("refBestbefore");
                //期限による危険、警告
                if (bestBefore < getNDaysAfterDate(0)) {
                    spanBestbefore.classList.add("refDanger");
                } else if (bestBefore < getNDaysAfterDate(3)) {
                    spanBestbefore.classList.add("refWarning");
                }
                spanBestbefore.innerHTML = bestBefore;

                var spanButtons = document.createElement("span");
                spanButtons.classList.add("refButtons");

                //ボタンの生成
                var butForm = document.createElement("form");
                butForm.method = "POST";
                var butEdit = document.createElement("button");
                butEdit.type = "button";
                butEdit.setAttribute("onclick", "editFoodstuff('" + data.foodId + "')");
//                butEdit.innerHTML = "編集";
                butEdit.innerHTML = "Edit";
                var butDelete = document.createElement("button");
                butDelete.type = "button";
                butDelete.setAttribute("onclick", "deleteFoodstuff('" + data.foodId + "', '" + data.foodName + "')");
//                butDelete.innerHTML = "削除";
                butDelete.innerHTML = "✕";
                var hiddenId = document.createElement("input");
                hiddenId.type = "hidden";
                hiddenId.name = "refFoodId";
                hiddenId.value = data.foodId;
                var hiddenRakutenId = document.createElement("input");
                hiddenRakutenId.type = "hidden";
                hiddenRakutenId.name = "refRakutenId";
                hiddenRakutenId.value = data.rakutenId;
                hiddenRakutenId.classList.add("refRakutenId");
                var hiddenToGroup = document.createElement("input");
                hiddenToGroup.type = "hidden";
                hiddenToGroup.name = "refToGroup";
                hiddenToGroup.value = toGroup;
                hiddenToGroup.setAttribute("id", "refToGroup_"+data.foodId);
                
                // Masakazu Start
                // 追加した料理でレシピだす　
                categoryId = data.rakutenId;
                refFood = data.foodName;

                if (data.rakutenId == '') {
                    refFood = "サンプル";
                };
                // Masakazu End
                
                butForm.appendChild(hiddenId);
                butForm.appendChild(hiddenRakutenId);
                butForm.appendChild(hiddenToGroup);
                butForm.appendChild(butEdit);
                butForm.appendChild(butDelete);

                spanButtons.appendChild(butForm);

                //appendでリストの１要素に
                newLi.appendChild(spanCategory);
                newLi.appendChild(spanFood);
                newLi.appendChild(spanAmount);
                newLi.appendChild(spanBestbefore);
                newLi.appendChild(spanButtons);

                //ulの取得
                var refUl = document.getElementById("refUl");
                //liの配列取得
                var childs = refUl.children;
                //冷蔵庫空っぽなどの状況への対応フラグ
                var insertFlg = false;
                //期限順の正しい位置に作成したli挿入
                for (var i = 1; i < childs.length; i++) {
                    var li = childs[i];
                    var liChilds = li.children;
                    var liBestBefore = liChilds[3].innerHTML;

                    if (bestBefore > liBestBefore) continue;

                    refUl.insertBefore(newLi, li);
                    insertFlg = true;
                    break;
                }
                if (!insertFlg) refUl.appendChild(newLi);
            }

            //フォームクリア
            clearForm();
            displayRakutenApiRecipe(categoryId, refFood);
        }
        // Content-Type の指定
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    // 食材の情報を送信
    req.send("fdbid=" + fdbid + "&fsn=" + encodeURIComponent(foodstuffName) + "&bb=" + bestBefore + "&am=" + encodeURIComponent(amount) + "&tg=" + toGroup);
}

/**
 * カテゴリ選択時の処理
 */
function changeCategory() {
    //子セレクトボックスと直接入力欄を全て非表示
    var fsSelects = document.getElementsByClassName("fafSelectFoodstuffBoxes");
    for (var i = 0; i < fsSelects.length; i++) {
        fsSelects[i].style.display = "none";
    }
    document.getElementById("fafFoodstuffName").style.display = "none";

    //選択されたカテゴリのインデックス取得
    var selected = document.getElementById("fafSelectCategory").selectedIndex;
    //選択されたカテゴリのvalue取得
    var selectedValue = document.getElementById("fafSelectCategory").options[selected].value;

    //直接入力か否か判定
    if (selectedValue == -1) {
        //直接入力欄を表示
        document.getElementById("fafFoodstuffName").style.display = "inline-block";
        //フォーカス移動
        document.getElementById("fafFoodstuffName").focus();
    } else {
        //選択されたカテゴリに対応するセレクトボックスを表示
        fsSelects[selectedValue - 1].style.display = "inline-block";
        //フォーカス移動
        fsSelects[selectedValue - 1].focus();
    }
}

/**
 * 編集ボタン
 */
function editFoodstuff(foodId) {
    //各種値の取得
    var li = document.getElementById("refFoodstuffs_" + foodId);
    var liChilds = li.children;
    //var category = liChilds[0].innerHTML;
    var category = "";
    var foodName = liChilds[1].innerHTML;
    var amount = liChilds[2].innerHTML;
    var bestBefore = liChilds[3].innerHTML;
    if(liChilds[0].children.length > 0){
        category = liChilds[0].children[0].alt;
    }else{
        category = liChilds[0].innerHTML;
    }
    var toGroupVal = document.getElementById("refToGroup_" + foodId).value;
    var toGroup = 0;
    if(toGroupVal == 0 || toGroupVal == "false") {toGroup = 1;}

    //フォームの取得
    var form = document.forms.foodstuffAddForm;

    //カテゴリセレクトボックスの変更、固定
    opsCategory = form.fafSelectCategory.getElementsByTagName('option');
    opsCategory[opsCategory.length - 1].selected = true;
    for (var i = 0; i < opsCategory.length; i++) {
        if (opsCategory[i].text == category) {
            opsCategory[i].selected = true;
            break;
        }
    }
    form.fafSelectCategory.disabled = true;

    //食材セレクトボックス選択
    changeCategory();

    if (form.fafSelectCategory.value == -1) {
        //直接入力されている場合
        form.fafFoodstuffName.value = foodName;
        form.fafFoodstuffName.disabled = true;
    } else {
        //食材セレクトボックスを変更、固定
        var fsSelectBox = document.getElementsByName("fafSelectFoodstuff_" + form.fafSelectCategory.value)[0];
        var opsFoodstuff = fsSelectBox.getElementsByTagName('option');
        for (var i = 0; i < opsFoodstuff.length; i++) {
            if (opsFoodstuff[i].text == foodName) {
                opsFoodstuff[i].selected = true;
                break;
            }
        }
        fsSelectBox.disabled = true;
    }

    //日付、量、グループ
    form.fafBestBefore.value = bestBefore;
    form.fafAmount.value = amount;
    form.fafToGroup[toGroup].checked = true;

    //EditFlg変更
    form.fafEditFlg.value = foodId;
    
    //最も変更される可能性の高い量にフォーカス
    form.fafAmount.focus();
}

/**
 * 食材情報更新処理
 */
function updateFoodstuff() {
    /* ******************* */
    /* 食材などの情報を取得する */
    /* ******************* */

    var form = document.forms.foodstuffAddForm;
    //編集モードではEditFlgのvalueがIDになる
    var foodId = form.fafEditFlg.value;

    var bestBefore = form.fafBestBefore.value;
    var amount = form.fafAmount.value;
    var toGroup = form.fafToGroup.value;

    /* **************** */
    /* 入力のチェックと警告 */
    /* **************** */

    //日付の入力の判定
    var returnFlg = false;
    if (bestBefore == "") {
        //1:入力不備:空の文字列になるためそれを確認
        alert("期限の入力に不備があります！");
        returnFlg = true;
        //return;
    } else if (bestBefore < getNDaysAfterDate(0)) {
        //2:入力が本日以前の日付
        if (!confirm("本日より前の日付が入力されています。本当によろしいですか？")) {
            returnFlg = true;
            //return;
        }
    } else if (bestBefore > getNDaysAfterDate(365)) {
        //3:入力が1年以上先の場合
        if (!confirm("本日より1年以上先の日付が入力されています。本当によろしいですか？")) {
            returnFlg = true;
            //return;
        }
    }
    if(returnFlg){
        //確認ダイアログが表示されている状態ではフォーカス移動ができなかったための措置
        form.fafBestBefore.focus();
        return;
    }

    //量の入力の有無の判定
    if (amount == "") {
        if (!confirm("数量が未入力です。本当によろしいですか？")) {
            returnFlg = true;
            //return;
        }
        if(returnFlg){
            //確認ダイアログが表示されている状態ではフォーカス移動ができなかったための措置
            form.fafAmount.focus();
            return;
        }
    }

    var req = new XMLHttpRequest();
    // 送信先のURLを指定する．
    req.open("POST", "/maincontroller/updateFoodstuff");
    // 結果が帰ってきた際に実行されるハンドラを指定する．
    req.onreadystatechange = function() {
        // readyState == 4: 修了
        if (req.readyState != 4) {
            return;
        }
        // status == 200: 成功
        if (req.status != 200) {
            // 成功しなかったため，失敗であることを表示して抜ける．
            alert("食材情報の更新に失敗した可能性がございます。一度ページをリロードし、食材の情報が更新されていなければ再度お試しください。失敗が続くようであれば、お手数ですが管理者にご連絡いただけると助かります。");
            return;
        }

        // body にはサーバから返却された文字列が格納される．
        var body = req.responseText;

        // 戻ってきた JSON 文字列を JavaScript オブジェクトに変換
        var data = eval("(" + body + ")");

        if (data.status = "OK") {
            //表示一入れ替えのため、一度以前のliを削除して新たに生成する

            // リストから該当項目を削除
            var li = document.getElementById("refFoodstuffs_" + foodId);
            li.textContent = null;
            li.parentNode.removeChild(li);

            //カテゴリ関連の情報取得
            var categorySI = form.fafSelectCategory.selectedIndex;
            var categorySVal = form.fafSelectCategory.options[categorySI].value;
            var category = form.fafSelectCategory.options[categorySI].text;

            //食材名取得
            var foodstuffName = form.fafFoodstuffName.value;
            if (categorySVal != -1) {
                var foodstuffSelectBox = document.getElementsByName("fafSelectFoodstuff_" + categorySVal)[0];
                var ops = foodstuffSelectBox.getElementsByTagName('option');
                foodstuffName = ops[foodstuffSelectBox.selectedIndex].text;
            }

            // liの生成
            var newLi = document.createElement("li");
            newLi.classList.add("refFoodstuffs");
            newLi.setAttribute("id", "refFoodstuffs_" + foodId);
            
            //spanで各要素の生成
            
            var spanCategory = document.createElement("span");
            spanCategory.classList.add("refCategory");
            if(category in categoryToIcon){
                //画像の生成
                var img = document.createElement("img");
                img.classList.add("refCategoryIcon");
                img.setAttribute("src", "/public/images/categoryIcons/" + categoryToIcon[category] + ".png");
                img.setAttribute("alt", category);
                spanCategory.appendChild(img);
            }else{
                spanCategory.innerHTML = category;
            }
            
            var spanFood = document.createElement("span");
            spanFood.classList.add("refFood");
            spanFood.innerHTML = foodstuffName;

            var spanAmount = document.createElement("span");
            spanAmount.classList.add("refAmount");
            spanAmount.innerHTML = amount;

            var spanBestbefore = document.createElement("span");
            spanBestbefore.classList.add("refBestbefore");
            //期限による危険、警告
            if (bestBefore < getNDaysAfterDate(0)) {
                spanBestbefore.classList.add("refDanger");
            } else if (bestBefore < getNDaysAfterDate(3)) {
                spanBestbefore.classList.add("refWarning");
            }
            spanBestbefore.innerHTML = bestBefore;

            var spanButtons = document.createElement("span");
            spanButtons.classList.add("refButtons");

            //ボタンの生成
            var butForm = document.createElement("form");
            butForm.method = "POST";
            var butEdit = document.createElement("button");
            butEdit.type = "button";
            butEdit.setAttribute("onclick", "editFoodstuff('" + foodId + "')");
            //butEdit.innerHTML = "編集";
            butEdit.innerHTML = "Edit";
            var butDelete = document.createElement("button");
            butDelete.type = "button";
            butDelete.setAttribute("onclick", "deleteFoodstuff('" + foodId + "', '" + foodstuffName + "')");
//            butDelete.innerHTML = "削除";
            butDelete.innerHTML = "✕";
            var hiddenId = document.createElement("input");
            hiddenId.type = "hidden";
            hiddenId.name = "refFoodId";
            hiddenId.value = data.foodId;
            var hiddenRakutenId = document.createElement("input");
            hiddenRakutenId.type = "hidden";
            hiddenRakutenId.name = "refRakutenId";
            hiddenRakutenId.value = data.rakutenId;
            hiddenRakutenId.classList.add("refRakutenId");
            var hiddenToGroup = document.createElement("input");
            hiddenToGroup.type = "hidden";
            hiddenToGroup.name = "refToGroup";
            hiddenToGroup.value = toGroup;
            hiddenToGroup.setAttribute("id", "refToGroup_"+data.foodId);
            
            butForm.appendChild(hiddenId);
            butForm.appendChild(hiddenRakutenId);
            butForm.appendChild(hiddenToGroup);
            butForm.appendChild(butEdit);
            butForm.appendChild(butDelete);

            spanButtons.appendChild(butForm);

            //appendでリストの１要素に
            newLi.appendChild(spanCategory);
            newLi.appendChild(spanFood);
            newLi.appendChild(spanAmount);
            newLi.appendChild(spanBestbefore);
            newLi.appendChild(spanButtons);

            //ulの取得
            var refUL = document.getElementById("refUl");
            //liの配列取得
            var childs = refUl.children;
            //冷蔵庫空っぽなどの状況への対応フラグ
            var insertFlg = false;
            //期限順の正しい位置に作成したli挿入
            for (var i = 1; i < childs.length; i++) {
                var li = childs[i];
                var liChilds = li.children;
                var liBestBefore = liChilds[3].innerHTML;

                if (bestBefore > liBestBefore) continue;

                refUl.insertBefore(newLi, li);
                insertFlg = true;
                break;
            }
            if (!insertFlg) refUl.appendChild(newLi);

            //フォームをクリア
            clearForm();
        }
    }

    // Content-Type の指定
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

    // 食材の情報を送信
    req.send("foodId=" + foodId + "&bb=" + bestBefore + "&am=" + encodeURIComponent(amount) + "&tg=" + toGroup);
}

/**
 * 削除ボタン
 */
function deleteFoodstuff(foodId, foodName) {
    if (!confirm(foodName + "を削除します。一度削除すると戻すことはできません。本当によろしいですか？")) {
        return;
    }

    var req = new XMLHttpRequest();
    // 送信先のURLを指定する．
    req.open("POST", "/maincontroller/deleteFoodstuff");
    // 結果が帰ってきた際に実行されるハンドラを指定する．
    req.onreadystatechange = function() {
        // readyState == 4: 修了
        if (req.readyState != 4) {
            return;
        }
        // status == 200: 成功
        if (req.status != 200) {
            // 成功しなかったため，失敗であることを表示して抜ける．
            alert("食材の削除に失敗した可能性がございます。一度ページをリロードし、食材が削除されていなければ再度お試しください。失敗が続くようであれば、お手数ですが管理者にご連絡いただけると助かります。");
            return;
        }

        // body にはサーバから返却された文字列が格納される．
        var body = req.responseText;

        // 戻ってきた JSON 文字列を JavaScript オブジェクトに変換
        var data = eval("(" + body + ")");

        // リストから該当項目を削除
        var li = document.getElementById("refFoodstuffs_" + foodId);
        li.textContent = null;
        li.parentNode.removeChild(li);

        //編集、削除の順に押された時用にフォームクリア
        clearForm();

    }

    // Content-Type の指定
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

    // 削除する食材のIDを送信
    req.send("foodId=" + foodId);
}

/**
 * plusDate : フォームの日付更新用 1〜31 : n日後の日付をセット 32以上：1ヶ月後
 */
function plusDate(n){
    //フォームの日付取得
    var calForm = document.getElementsByName("fafBestBefore");
    var nowDate = calForm[0].value.replace(/-/g, "/");
    var dt = new Date(nowDate);
    
    if(nowDate.toString() === ""){
        alert("現在の日付が正しく入力されていませんでした。");
        calForm[0].value = getNDaysAfterDate(1);
        return;
    }
    
    if(n <= 31){
        var dayOfMonth = dt.getDate();
        dt.setDate(dayOfMonth + n);
    }else{
        dt.setMonth(dt.getMonth() + 1);
    }
    var format = "yyyy-MM-dd";
    //ここまでで翌日の日付取得、ここからフォーマット
    format = format.replace(/yyyy/g, dt.getFullYear());
    format = format.replace(/MM/g, ('0' + (dt.getMonth() + 1)).slice(-2));
    format = format.replace(/dd/g, ('0' + dt.getDate()).slice(-2));
    
    calForm[0].value = format;
}

/**
 * 内部用関数：n日後の日付を取得
 */
function getNDaysAfterDate(n) {
    var dt = new Date();
    var dayOfMonth = dt.getDate();
    dt.setDate(dayOfMonth + n);
    var format = "yyyy-MM-dd";
    //ここまでで翌日の日付取得、ここからフォーマット
    format = format.replace(/yyyy/g, dt.getFullYear());
    format = format.replace(/MM/g, ('0' + (dt.getMonth() + 1)).slice(-2));
    format = format.replace(/dd/g, ('0' + dt.getDate()).slice(-2));
    return format;
}