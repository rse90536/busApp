package MyMethod;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by AA on 2017/8/24.
 */

public class SharedService {
    public static boolean CheckNetWork(Context context){

        ConnectivityManager cManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()){
            //do something
            //能上網
            return true;
        }else{
            //do something
            //不能上網
            return false;
        }
    }
}
