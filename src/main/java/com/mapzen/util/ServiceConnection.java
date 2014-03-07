package com.mapzen.util;

import com.samsung.android.sdk.accessory.SASocket;

import java.io.IOException;

public class ServiceConnection extends SASocket {
    public int mConnectionId;

    public ServiceConnection() {
        super(ServiceConnection.class.getName());
    }

    @Override
    public void onReceive(int channelId, byte[] data) {
        String strToUpdateUI = new String(data);
        ServiceConnection uHandler = ServiceImpl.mConnection;
        try {
            uHandler.send(ServiceImpl.CHANNEL_ID, strToUpdateUI.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(int channelId, String errorString, int error) {
        Logger.e("Connection is not alive ERROR: " + errorString + "  " + error);
    }

    @Override
    protected void onServiceConnectionLost(int errorCode) {
        Logger.e(
                "onServiceConectionLost  for peer = " + mConnectionId + "error code =" + errorCode);
        if (ServiceImpl.mConnection != null) {
            ServiceImpl.mConnection = null;
        }
    }
}
