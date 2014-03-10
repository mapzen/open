package com.mapzen.util;

import com.samsung.android.sdk.accessory.SASocket;

import java.io.IOException;

public class GearServiceSocket extends SASocket {
    int mConnectionId;

    public GearServiceSocket() {
        super(GearServiceSocket.class.getName());
    }

    public void setConnectionId(int id) {
       mConnectionId = id;
    }

    public int getConnectionId() {
        return mConnectionId;
    }

    @Override
    public void onReceive(int channelId, byte[] data) {
        String strToUpdateUI = new String(data);
        GearServiceSocket uHandler = GearAgentService.getmConnection();
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
        if (GearAgentService.getmConnection() != null) {
            GearAgentService.setmConnection(null);
        }
    }
}
