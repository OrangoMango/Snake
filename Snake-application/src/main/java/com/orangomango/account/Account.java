package com.orangomango.account;

import java.net.URLEncoder;

import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.fetch.FetchOptions;
import dev.webfx.platform.fetch.Response;
import dev.webfx.platform.fetch.Headers;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.async.Future;

public class Account{
    public static String HOST = "http://localhost:8000";
    private static String APPLICATION_UID;

    private String uid;
    private String username, password;

    public Account(String username, String password){
        this.username = username;
        this.password = password;
    }

    public Account(String uid){
        this.uid = uid;
        this.username = null;
        this.password = null;
    }

    public void initUsername(Runnable r){
        sendPostRequest("/account/api/auth.php", "uid=" + this.uid).onSuccess(json -> {
            if (json != null && json.getBoolean("success")) {
                this.username = json.getObject("data").getString("username");
                r.run();
            }
        }).onFailure(Throwable::printStackTrace);
    }

    public static void registerApplication(String appuid){
        APPLICATION_UID = appuid;
    }

    public String getUsername(){
        return this.username;
    }

    public String getUID(){
        return this.uid;
    }

    public Future<ReadOnlyAstObject> login(){
		try {
			return sendPostRequest("/account/api/auth.php", "username="+URLEncoder.encode(this.username, "UTF-8")+"&password="+URLEncoder.encode(this.password, "UTF-8"))
				.map(json ->{
					if (json != null && json.getBoolean("success")){
						this.uid = json.getObject("data").getString("uid");
					}
					return json;
				});
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
    }

    public void logout(){
        this.uid = null;
    }

    public Future<ReadOnlyAstArray> listFriends(){
        validateUid();
        return sendPostRequest("/account/api/friends.php", "uid=" + this.uid)
            .map(json -> json != null ? json.getArray("data") : null);
    }

    public Future<ReadOnlyAstObject> getAppData(){
        validateUid();
        return sendPostRequest("/account/api/appdata.php?method=read", "uid=" + this.uid)
            .map(json -> (json != null && json.getBoolean("success")) ? json.getObject("data") : null);
    }

    public Future<Boolean> setAppData(AstObject json){
        validateUid();
        return sendPostRequest("/account/api/appdata.php?method=write", "uid=" + this.uid + "&json_data=" + AST.formatObject(json, "json"))
            .map(dt -> dt != null && dt.getBoolean("success"));
    }

    public Future<ReadOnlyAstObject> getLeaderboard(String leadName){
        return sendPostRequest("/leaderboard/lead.php?lead=" + leadName + "&api=1", "");
    }

    public Future<Boolean> updateLeaderboard(String leadName, int[] score){
        AstObject data = AST.createObject();
        data.set("uid", this.uid);
        data.set("lead", leadName);

        var scores = AST.createArray();
        for (int s : score){
            scores.push(s);
        }
        data.set("score", scores);

        return sendPostRequest("/leaderboard/lead.php", "json_data=" + AST.formatObject(data, "json"))
            .map(dt -> dt != null && dt.getBoolean("success"));
    }

    private Future<ReadOnlyAstObject> sendPostRequest(String urlText, String content){
        Promise<ReadOnlyAstObject> promise = Promise.promise();
        
        FetchOptions fetchOptions = new FetchOptions().setMethod("POST").setHeaders(Headers.create().append("Content-Type", "application/x-www-form-urlencoded")).setBody("appuid=" + APPLICATION_UID + "&" + content);

        return Fetch.fetch(HOST + urlText, fetchOptions)
                    .compose(response -> response.text())
                    .map(text -> AST.parseObject(text, "json"));
    }

    private void validateUid(){
        if (this.uid == null){
            throw new IllegalStateException("User UID is not valid");
        }
    }
}