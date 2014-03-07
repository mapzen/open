package com.mapzen.util;

import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ServiceImpl extends SAAgent {

    public static final int CHANNEL_ID = 104;
    private static final String TAG = "HelloBProviderService";
    private static final int SERVICE_CONNECTION_RESULT_OK = 0;
    public static ServiceConnection mConnection = null;

    private final IBinder mBinder = new LocalBinder();

    public ServiceImpl() {
        super(TAG, ServiceConnection.class);
    }

    @Override
    protected void onFindPeerAgentResponse(SAPeerAgent arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onServiceConnectionResponse(SASocket uThisConnection, int result) {
        if (result == SERVICE_CONNECTION_RESULT_OK) {
            if (uThisConnection != null) {
                mConnection = (ServiceConnection) uThisConnection;
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
        public ServiceImpl getService() {
            return ServiceImpl.this;
        }
    }
}
