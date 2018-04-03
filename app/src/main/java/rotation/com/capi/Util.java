package rotation.com.capi;

import android.content.Context;
import android.content.SharedPreferences;

public class Util {

	public static String email;

	//Email Validation pattern
	public static final String regEx = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";

	//Fragments Tags
	public static final String Login_Fragment = "Login_Fragment";
	public static final String SignUp_Fragment = "SignUp_Fragment";
	public static final String ForgotPassword_Fragment = "ForgotPassword_Fragment";

	public static int flag=0;



	public static void setFlag(int flag,Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences("lll1", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("flag",flag);
		editor.apply();
		editor.commit();
	}

	public static int getFlag(Context context) {

		SharedPreferences sharedPreferences = context.getSharedPreferences("lll1", Context.MODE_PRIVATE);
		return sharedPreferences.getInt("flag",0);
	}

	public static void setLogin(boolean login, Context context)
	{

		SharedPreferences sharedPreferences = context.getSharedPreferences("lll", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("first",login);
		editor.clear();
		editor.apply();
		editor.commit();


	}

	public static boolean getLogin(Context context)
	{

		SharedPreferences sharedPreferences = context.getSharedPreferences("lll", Context.MODE_PRIVATE);
		return  sharedPreferences.getBoolean("first",false);
	}


}
