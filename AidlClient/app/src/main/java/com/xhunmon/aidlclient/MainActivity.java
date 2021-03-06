package com.xhunmon.aidlclient;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.aidl.player.IPlayer;
import com.xhunmon.aidl.pay.IPay;

import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{

    private EditText mInput;
    private TextView mSpare,mResult;

    private IPay mIPay;
    private IPlayer mIPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInput = (EditText) findViewById(R.id.input);
         findViewById(R.id.pay).setOnClickListener(this);
        mSpare = (TextView) findViewById(R.id.spare);

         findViewById(R.id.player).setOnClickListener(this);
         findViewById(R.id.stop).setOnClickListener(this);
        mResult = (TextView) findViewById(R.id.result);

        //第一种绑定方式
        final Intent intentPay = new Intent();
        intentPay.setAction("com.xhunmon.remote.PayService");
        final Intent eintent = new Intent(createExplicitFromImplicitIntent(this,intentPay));
        bindService(eintent,new Conn(), Service.BIND_AUTO_CREATE);

        //第二种绑定方式
        Intent intentPlayer = new Intent();
        intentPlayer.setClassName("com.xhunmon.aidlserver","com.xhunmon.aidlserver.PlayerService");
        bindService(intentPlayer,mServiceConnection,Service.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPlayer = IPlayer.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private class Conn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPay = IPay.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.pay){
            try {
                String pay = mIPay.pay(Integer.parseInt(mInput.getText().toString()));
                mSpare.setText(pay);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else if(v.getId() == R.id.player){
            try {
                String play = mIPlayer.play();
                mResult.setText(play);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else if (v.getId()==R.id.stop){
            try {
                String stop = mIPlayer.stop();
                mResult.setText(stop);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Android5.0中service的intent一定要显性声明
     * 来自-- http://blog.csdn.net/shenzhonglaoxu/article/details/42675287
     * @param context
     * @param implicitIntent
     * @return
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
