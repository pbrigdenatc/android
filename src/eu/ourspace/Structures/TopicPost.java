package eu.ourspace.Structures;

import org.json.JSONObject;

import android.text.Html;



public class TopicPost extends Object {
	public int postId;
	public String date;
	public String user;
	public String body;
	public int thumbsUp;
	public int thumbsDown;
	public String imgUrl;
	public boolean hasVoted;
	public boolean isSolution;
	
	public TopicPost() {
		postId = 0;
		date = "";
		user = "";
		body = "";
		thumbsUp = 0;
		thumbsDown = 0;
		imgUrl = "";
		hasVoted = false;
		isSolution = false;
	}
	
	public TopicPost(int postId, String date, String user, String body, int thumbsUp, int thumbsDown,
			String imgUrl, boolean hasVoted, boolean isSolution) {
		this.postId = postId;
		this.date = date;
		this.user = user;
		this.body = body;
		this.thumbsUp = thumbsUp;
		this.thumbsDown = thumbsDown;
		this.imgUrl = imgUrl;
		this.hasVoted = hasVoted;
		this.isSolution = isSolution;
	}
	
	
	// parsing when reading in a list
	public TopicPost(JSONObject obj) {
		this();
		
		if (obj == null)
			return;
		
		postId = obj.optInt("PostId", 0);
		date = obj.optString("UpadedDate", "");
		body = obj.optString("Body", "");
		thumbsUp = obj.optInt("ThumbsUp", 0);
		thumbsDown = obj.optInt("ThumbsDown", 0);
		imgUrl = obj.optString("ImgUrl", "");
		hasVoted = obj.optBoolean("HasVoted", false);
		isSolution = obj.optBoolean("IsSolution", false);
		
		// html special characters problems, so make it clear text
		body = Html.fromHtml(body).toString();
		body = body.replaceAll("<br/>","\n");
		body = body.replaceAll("\\<.*?>","");

		
		String firstName = obj.optString("FirstName", "");
		String lastName = obj.optString("LastName", "");
		try {
			user = firstName + " " + lastName.substring(0, 1) + ".";
		} catch (IndexOutOfBoundsException e) {}
		
	}
	
}
