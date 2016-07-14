package com.parse.starter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HaoWu on 1/11/2016.
 */
public class MyPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public static final String PARSE_DATA_KEY = "com.parse.Data";
    private static final String REQUEST_TYPE = "REQUEST";
    private static final String MESSAGE_TYPE = "MESSAGE";
    private static final String RATING_TYPE = "RATING";

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // deactivate standard notification
        return null;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        // I am only using the default open behaviour at this moment
        super.onPushOpen(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject data = getDataFromIntent(intent);
        // Do something with the data. To create a notification do:
        if (data == null) {
            Log.d("GET PUSH DATA", "FAILED");
        }

        boolean ignore = false;
        String type = null;
        String description = null;

        try {
            type = data.getString("TYPE");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Push Receive Exception", "failed to retrieve type");
            return;
        }

        if ( !StarterApplication.isActivityVisible() || (StarterApplication.isActivityVisible() && StarterApplication.isInMessage()) ) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            Intent openIntent = new Intent(context, ACTRequest.class);
            PendingIntent pIntent = null;
            if(type.equals(REQUEST_TYPE)){
                /*Go to the main page where requests are displayed*/
                openIntent.putExtra("enter", 1);
                RequestObject requestObject = new RequestObject(data);
                openIntent.putExtra("valueList", requestObject.spitValueList());
                openIntent.putExtra("userPic", requestObject.getUserPic());
                builder.setContentTitle("New Favourama Request");
            }
            else if(type.equals(MESSAGE_TYPE)){
                openIntent.putExtra("enter", 2);
                String fname = new String();
                try{
                    fname = data.getString("username");
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                if(fname.equals(StarterApplication.getToWhom())){
                    ignore = true;
                }
                if(!ignore) {
                    openIntent.putExtra("username", fname);
                    openIntent.putExtra("CONTENT", data.toString());
                /*Hao to Jeremy: Enter the message page, should we try directly open the conversation or just the threads?*/
                    builder.setContentTitle("New Favourama Message");
                }
            }

            if(!ignore) {
                openIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                pIntent = PendingIntent.getActivity(context, 0, openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentText(description);
                builder.setSmallIcon(R.drawable.logo);
                builder.setContentIntent(pIntent);
                builder.setAutoCancel(true);
                notificationManager.notify("MyTag", 0, builder.build());
                // OPTIONAL create soundUri and set sound:
            /*builder.setSound(soundUri);*/
                return;
            }
        }

        if (type.equals(REQUEST_TYPE)) {
            try {
                description = data.getString("note");
            } catch (JSONException e) {
                e.printStackTrace();
                description = "EMPTY";
            }
            Intent i = new Intent();
            i.putExtra("CONTENT", data.toString());
            i.setAction("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS");
            context.sendBroadcast(i);
        } else if (type.equals(MESSAGE_TYPE)) {
            /* HAO
            * MessageObject messageObject = new MessageObject(data);
            * description = messageObject.getNote();
            * */
            Intent i = new Intent();
            i.putExtra("CONTENT", data.toString());
            i.setAction("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES");
            context.sendBroadcast(i);
        } else if (type.equals(RATING_TYPE)) {
            //do nothing now
        }

    }

    private JSONObject getDataFromIntent(Intent intent) {
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));
            Log.d("RECDATA", intent.getExtras().get(PARSE_DATA_KEY).toString());
        } catch (JSONException e) {
            // Json was not readable...
            Log.d("PUSH RECEIVE FAILURE", ">>>COULD NOT PROCESS JSON DATA");
        }
        return data;
    }
}
