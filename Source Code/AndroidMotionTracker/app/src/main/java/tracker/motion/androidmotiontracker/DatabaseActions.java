package tracker.motion.androidmotiontracker;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


	public class DatabaseActions
	{
		private SQLiteDatabase db;
		private Context context;

        /**
         * Constructor
         * @param context context of activity calls the DatabaseActions
         * @param dbParam the object which will be responsible for open SQLite database
         */
		public DatabaseActions(Context context,SQLiteDatabase dbParam)
		{
			this.context = context;
            db=dbParam;
		}



        /**
         * Send a query for retrieve all stored points-devices in database
         *
         */
        public Cursor retrievePoints()
        {
            try
            {
                String sql=new String();
                sql="SELECT * FROM devices where 1>0 order by distance DESC";
                Cursor cursor=db.rawQuery(sql,null);
                return cursor;
            }
            catch (SQLException e)
            {
                return null;
            }

        }


        /**
         * Send a query for retrieve all stored points-devices in database
         *
         */
        public String[] retrievePointsAsStringArray()
        {
            try
            {
                String sql=new String();
                sql="SELECT * FROM devices where 1>0 order by distance DESC";
                Cursor cursor=db.rawQuery(sql,null);
                String[] deviceArr = new String[cursor.getCount()];
                cursor.moveToFirst();
                int counter=0;
                while(!cursor.isAfterLast()) {

                    deviceArr[counter] = "device name: "+ cursor.getString(cursor.getColumnIndex("name"))+" / distance: "+ cursor.getString(cursor.getColumnIndex("distance"))+ " km";
                    counter++;
                    cursor.moveToNext();
                }
                return deviceArr;
            }
            catch (SQLException e)
            {
                return null;
            }

        }
}
