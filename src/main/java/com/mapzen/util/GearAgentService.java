package com.mapzen.util;

import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class GearAgentService extends SAAgent {

    public static final int CHANNEL_ID = 104;
    private static final String TAG = "GearAgentService";
    private static final int SERVICE_CONNECTION_RESULT_OK = 0;
    public static GearServiceSocket mConnection = null;

    private final IBinder mBinder = new LocalBinder();

    public GearAgentService() {
        super(TAG, GearServiceSocket.class);
    }

    @Override
    protected void onFindPeerAgentResponse(SAPeerAgent peerAgent, int index) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onServiceConnectionResponse(SASocket socket, int result) {
        if (result == SERVICE_CONNECTION_RESULT_OK) {
            if (socket != null) {
                mConnection = (GearServiceSocket) socket;
                mConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);
                Logger.d("onServiceConnection connectionID = " + mConnection.mConnectionId);
            } else {
                Logger.e("SASocket object is null");
            }
        } else {
            Logger.e("onServiceConnectionResponse result error =" + result);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public GearAgentService getService() {
            return GearAgentService.this;
        }
    }
}
