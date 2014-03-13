package com.mapzen.util;

import com.samsung.android.sdk.accessory.SASocket;

import java.io.IOException;

public class GearServiceSocket extends SASocket {
    int mConnectionId;
    private static final String TAG = "GearServiceSocket";

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
        Logger.d(TAG + "onReceive: data for channel: " + String.valueOf(channelId));
        String jsonRaw = new String(data);
        Logger.d(TAG + "onReceive: data to send: " + jsonRaw);
        GearServiceSocket connection = GearAgentService.getConnection();
        if (connection != null) {
            try {
                Logger.d(TAG + "onReceive: trying to send");
                connection.send(GearAgentService.CHANNEL_ID, jsonRaw.getBytes());
            } catch (IOException e) {
                Logger.d(TAG + "onReceive: sending failed: error: " + e.toString());
                e.printStackTrace();
            }
        } else {
            Logger.e(TAG + "onReceive sending failed connection is null");
        }
    }

    @Override
    public void onError(int channelId, String errorString, int error) {
        Logger.e(TAG + "onError: Connection is not alive ERROR: " + errorString + "  " + error);
    }

    @Override
    protected void onServiceConnectionLost(int errorCode) {
        Logger.e(TAG + "onServiceConectionLost  for peer = " +
                mConnectionId + "error code =" + errorCode);
        if (GearAgentService.getConnection() != null) {
            GearAgentService.setConnection(null);
        }
    }
}
