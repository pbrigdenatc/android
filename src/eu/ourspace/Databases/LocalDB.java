package eu.ourspace.Databases;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import eu.ourspace.Structures.Forum;
import eu.ourspace.Structures.RecentActivity;
import eu.ourspace.Structures.Topic;
import eu.ourspace.Structures.TopicPost;
import eu.ourspace.Utils.Utils;

public class LocalDB {

	private static final int DATABASE_VERSION = 4;
	private static final String DATABASE_NAME = "ourspace";
	
	public static final String FORUMS_TABLE = "forums";
	public static final String RECENTACTIVITY_TABLE = "recentactivity";
	public static final String TOPTOPICS_TABLE = "toptopics";
	public static final String PROPOSEDTOPICS_TABLE = "proposedtopics";
	public static final String OPENTOPICS_TABLE = "opentopics";
	public static final String SOLUTIONSTOPICS_TABLE = "solutionstopics";
	public static final String RESULTSTOPICS_TABLE = "resultstopics";
	public static final String POSTS_TABLE = "posts";


	private static final String CREATE_TABLE_FORUMS = "create table " + FORUMS_TABLE +
		" (forum_id integer primary key, name text not null, img_url text);";
	
	private static final String CREATE_TABLE_RECENTACTIVITY = "create table " + RECENTACTIVITY_TABLE +
			" (_id integer primary key autoincrement, topic_id integer, title text not null, lang text not null, name text, " +
			"location text, body text, img_url text);";

	private static final String CREATE_TABLE_TOPTOPCIS = "create table " + TOPTOPICS_TABLE +
			" (topic_id integer primary key, title text not null, lang text not null, posts integer, " +
			"category_id integer, category_name text not null, body text);";

	private static final String CREATE_TABLE_PROPOSEDTOPICS = "create table " + PROPOSEDTOPICS_TABLE +
			" (topic_id integer primary key, title text not null, lang text not null, posts integer, " +
			"category_id integer, category_name text not null, body text);";
	
	private static final String CREATE_TABLE_SOLUTIONSTOPICS = "create table " + SOLUTIONSTOPICS_TABLE +
			" (topic_id integer primary key, title text not null, lang text not null, posts integer, " +
			"category_id integer, category_name text not null, body text);";
	
	private static final String CREATE_TABLE_RESULTSTOPICS = "create table " + RESULTSTOPICS_TABLE +
			" (topic_id integer primary key, title text not null, lang text not null, posts integer, " +
			"category_id integer, category_name text not null, body text);";

	private static final String CREATE_TABLE_OPENTOPICS = "create table " + OPENTOPICS_TABLE +
			" (topic_id integer primary key, title text not null, lang text not null, posts integer, " +
			"category_id integer, category_name text not null, body text);";

	private static final String CREATE_TABLE_POSTS = "create table " + POSTS_TABLE +
		" (_id integer primary key autoincrement, topic_id integer, post_id integer, date text not null, " +
		"user text not null, body text, img_url text);";
	
	
	private SQLiteDatabase db;
	private DatabaseHelper mOpenHelper;

	/**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
    	

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase mydb) {
        	mydb.execSQL(CREATE_TABLE_FORUMS);
        	mydb.execSQL(CREATE_TABLE_RECENTACTIVITY);
			mydb.execSQL(CREATE_TABLE_TOPTOPCIS);
			mydb.execSQL(CREATE_TABLE_PROPOSEDTOPICS);
			mydb.execSQL(CREATE_TABLE_SOLUTIONSTOPICS);
			mydb.execSQL(CREATE_TABLE_RESULTSTOPICS);
			mydb.execSQL(CREATE_TABLE_OPENTOPICS);
			mydb.execSQL(CREATE_TABLE_POSTS);
        }
        
		@Override
		public void onUpgrade(SQLiteDatabase mydb, int oldVersion, int newVersion) {
			if (oldVersion != newVersion) {
				mydb.execSQL("DROP TABLE IF EXISTS " + FORUMS_TABLE);
				mydb.execSQL("DROP TABLE IF EXISTS " + RECENTACTIVITY_TABLE);
	            mydb.execSQL("DROP TABLE IF EXISTS " + TOPTOPICS_TABLE);
	            mydb.execSQL("DROP TABLE IF EXISTS " + PROPOSEDTOPICS_TABLE);
	            mydb.execSQL("DROP TABLE IF EXISTS " + SOLUTIONSTOPICS_TABLE);
	            mydb.execSQL("DROP TABLE IF EXISTS " + RESULTSTOPICS_TABLE);
	            mydb.execSQL("DROP TABLE IF EXISTS " + OPENTOPICS_TABLE);
	            mydb.execSQL("DROP TABLE IF EXISTS " + POSTS_TABLE);
	            onCreate(mydb);
			}
		}
    }
	
	public LocalDB(Context ctx) {
		mOpenHelper = new DatabaseHelper(ctx);
		try {
			db = mOpenHelper.getWritableDatabase();
			db.setLocale(Utils.dbLocale);
			
		} catch (SQLiteException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
	}
	
	
	// insert recent activity
	public long insertRecentActivity(RecentActivity recentActivity) {
		ContentValues values = new ContentValues();
		values.put("topic_id", recentActivity.topicId);
		values.put("title", recentActivity.subject);
		values.put("lang", recentActivity.language);
		values.put("name", recentActivity.name);
		values.put("location", recentActivity.location);
		values.put("body", recentActivity.description);

		return db.insert(RECENTACTIVITY_TABLE, null, values);
	}
	

	public List<RecentActivity> getRecentActivity() {
		List<RecentActivity> recentActivity = new ArrayList<RecentActivity>();
		try {
			Cursor c = db.query(RECENTACTIVITY_TABLE, new String[] { "topic_id", "title",
					"lang", "name", "location", "body" },
					null, null, null, null, null);

			final int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				RecentActivity a = new RecentActivity();
				a.topicId = c.getInt(0);
				a.subject = c.getString(1);
				a.language = c.getString(2);
				a.name = c.getString(3);
				a.location = c.getString(4);
				a.description = c.getString(5);
				recentActivity.add(a);
				c.moveToNext();
			}
			c.close();
		} catch (SQLException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
		return recentActivity;
	}
	
	
	
	// insert topic in relative table
	public long insertTopic(String table, Topic topic) {
		ContentValues values = new ContentValues();
		values.put("topic_id", topic.topicId);
		values.put("title", topic.title);
		values.put("lang", topic.lang);
		values.put("posts", topic.postsCount);
		values.put("category_id", topic.categoryId);
		values.put("category_name", topic.categoryName);
		values.put("body", topic.body);

		return db.insert(table, null, values);
	}
	
	// insert forum
	public long insertForum(Forum forum) {
		ContentValues values = new ContentValues();
		values.put("forum_id", forum.forumId);
		values.put("name", forum.name);
		values.put("img_url", forum.imgUrl);

		return db.insert(FORUMS_TABLE, null, values);
	}
	
	public List<Forum> getForums() {
		List<Forum> forums = new ArrayList<Forum>();
		try {
			Cursor c = db.query(FORUMS_TABLE, new String[] { "forum_id", "name", "img_url" },
					null, null, null, null, null);

			final int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				Forum f = new Forum();
				f.forumId = c.getInt(0);
				f.name = c.getString(1);
				f.imgUrl = c.getString(2);
				forums.add(f);
				c.moveToNext();
			}
			c.close();
		} catch (SQLException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
		return forums;
	}
	

	public boolean deleteAll(String table) {
		return deleteAll(table, null);
	}

	public boolean deleteAll(String table, String whereClause) {
		return (db.delete(table, whereClause, null) > 0);
	}
	
	public boolean deleteAllPosts(int topicId) {
		return (db.delete(POSTS_TABLE, "topic_id=" + topicId, null) > 0);
	}
	

	public List<Topic> getTopics(String table, String whereClause) {
		List<Topic> topics = new ArrayList<Topic>();
		try {
			Cursor c = db.query(table, new String[] { "topic_id", "title",
					"lang", "posts", "category_id", "category_name", "body" },
					whereClause, null, null, null, null);

			final int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				Topic topic = new Topic();
				topic.topicId = c.getInt(0);
				topic.title = c.getString(1);
				topic.lang = c.getString(2);
				topic.postsCount = c.getInt(3);
				topic.categoryId = c.getInt(4);
				topic.categoryName = c.getString(5);
				topic.body = c.getString(6);
				topics.add(topic);
				c.moveToNext();
			}
			c.close();
		} catch (SQLException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
		return topics;
	}

	
	
	
	// insert post
	public long insertPost(int topicId, TopicPost post) {
		ContentValues values = new ContentValues();
		values.put("topic_id", topicId);
		values.put("post_id", post.postId);
		values.put("date", post.date);
		values.put("user", post.user);
		values.put("body", post.body);

		return db.insert(POSTS_TABLE, null, values);
	}
	

	public List<TopicPost> getTopicPosts(int topicId) {
		List<TopicPost> posts = new ArrayList<TopicPost>();
		try {
			Cursor c = db.query(TOPTOPICS_TABLE, new String[] { "post_id", "date",
					"user", "body" },
					"topic_id=" + topicId, null, null, null, null);

			final int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				TopicPost p = new TopicPost();
				p.postId = c.getInt(0);
				p.date = c.getString(1);
				p.user = c.getString(2);
				p.body = c.getString(3);
				posts.add(p);
				c.moveToNext();
			}
			c.close();
		} catch (SQLException e) {
			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
		}
		return posts;
	}
	
	
	
//	public Topic getTopic(Long feedId) {
//		Topic topic = new Topic();
//		try {
//			Cursor c = db.query(TOPTOPICS_TABLE, new String[] { "updated", "have_more" }, "feed_id=" + Long.toString(feedId), null, null, null, null);
//
//			c.moveToFirst();
//			if (c.getCount() > 0) {
//				topic.updated = c.getString(0);
//				topic.haveMore = (c.getInt(1) != 0);
//			}
//			c.close();
//		} catch (SQLException e) {
//			if (Utils.LOG) { Log.e(this.toString() ,e.toString()); }
//		}
//		return topic;
//	}
	
	
	
}
