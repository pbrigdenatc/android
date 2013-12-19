package eu.ourspace.Structures;

import org.json.JSONObject;




public class Forum extends Object {
	public int forumId;
	public String name;
	public String imgUrl;
	
	public Forum() {
		forumId = 0;
		name = "";
		imgUrl = "";
	}
	
	public Forum(int forumId, String name, String imgUrl) {
		this.forumId = forumId;
		this.name = name;
		this.imgUrl = imgUrl;
	}
	
	// parsing when reading in a list
	public Forum(JSONObject obj) {
		this();
		
		if (obj == null)
			return;
		
		forumId = obj.optInt("ForumId", 0);
		name = obj.optString("Name", "");
		imgUrl = obj.optString("ImgUrl", "");
	}
}
