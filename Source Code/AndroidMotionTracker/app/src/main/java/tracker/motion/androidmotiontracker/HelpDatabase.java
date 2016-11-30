package tracker.motion.androidmotiontracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class HelpDatabase extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME="motionTracker";

	public HelpDatabase(Context context)
	{
		super(context, DATABASE_NAME, null,2);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE devices(id TEXT PRIMARY KEY , device_id TEXT , lat REAL, lng REAL,name TEXT,distance TEXT)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS devices");
		onCreate(db);
	}
}


