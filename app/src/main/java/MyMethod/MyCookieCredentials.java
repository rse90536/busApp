package MyMethod;

import android.content.Context;
import android.content.SharedPreferences;

import microsoft.aspnet.signalr.client.Credentials;
import microsoft.aspnet.signalr.client.http.Request;

/**
 * Created by AA on 2017/8/24.
 */

public class MyCookieCredentials implements Credentials {
    Context context;
    public MyCookieCredentials(Context context){
        this.context=context;
    }

    @Override
    public void prepareRequest(Request request) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Ticket",Context.MODE_PRIVATE);/*取得ticket */

        String Cookie = sharedPreferences.getString("Ticket","");
      request.addHeader("Cookie", Cookie);/*傳入cookie*/
    }
}
