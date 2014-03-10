package com.mapzen.util;

import com.samsung.android.sdk.accessory.SASocket;

import java.io.IOException;

public class GearServiceSocket extends SASocket {
    public int mConnectionId;

    public GearServiceSocket() {
        super(GearServiceSocket.class.getName());
    }

    @Override
    public void onReceive(int channelId, byte[] data) {
        String strToUpdateUI = new String(data);
        GearServiceSocket uHandler = GearAgentService.mConnection;
        try {
            uHandler.send(GearAgentService.CHANNEL_ID, strToUpdateUI.getBytes());
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
        if (GearAgentService.mConnection != null) {
            GearAgentService.mConnection = null;
        }
    }
}
