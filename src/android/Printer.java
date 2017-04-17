package com.ci24.usbprinter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import com.jolimark.*;




public class Printer extends CordovaPlugin {
  private CallbackContext Callback;
  UsbPrinter ci24printer = new UsbPrinter();
  private static final String TAG = "printer";
  private static final String ACTION_USB_PERMISSION ="com.ci24.usbprinter.USB_PERMISSION";
  private static final int targetVendorID = 7072;  //1008
  private static final int targetProductID = 8706;//1322
  private PendingIntent mPermissionIntent;
  private UsbInterface usbInterfaceFound = null;
  private UsbEndpoint endpointOut = null;
  private UsbDevice deviceFound = null;
  private UsbManager mUsbManager;
  public Activity getActivity() {    return this.cordova.getActivity();  }
  @Override
  public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException, UnsupportedEncodingException {


    if (action.equals("printData1")) {

      String print=null;
      try {
        print = args.optJSONObject(0).getString("print");
      } catch (JSONException e) {
        e.printStackTrace();
        callbackContext.error("Error: Code 1:No Print Text Send In Json Argument");
      }
      JSONObject opts=null;
      try {
         opts = args.optJSONObject(0).has("opts")? args.optJSONObject(0).getJSONObject("opts") : new JSONObject();
      } catch (JSONException e) {
        e.printStackTrace();
      }
      // register the broadcast receiver
      try{
        mUsbManager = (UsbManager) getActivity().getApplicationContext().getSystemService(Context.USB_SERVICE);
        final String ACTION_USB_PERMISSION = "com.plugin.gpsstatus.USB_PERMISSION";
        mPermissionIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, new Intent(
          ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().getApplicationContext().registerReceiver(mUsbReceiver, filter);
      }catch (Exception e){
        callbackContext.error("Error: code 2: "+e.toString());
      }


      connectUsb(print, opts,callbackContext);




    }






    else  if (action.equals("printData")) {

    UsbPrinter myprinter =new UsbPrinter();//创建类实例
      boolean ret = false;
       byte[] SData = null;
       ret = myprinter.Open();
       if (ret) {
          String printData = args.optJSONObject(0).getString("print");
         SData = printData.getBytes("GB2312");
     ret = myprinter.WriteBuf(SData, SData.length);
         if (!ret) {

           Toast.makeText(getActivity().getApplicationContext(),myprinter.GetLastPrintErr() , Toast.LENGTH_LONG).show();
   }
      myprinter.Close();//关闭打印机 
           }
           myprinter = null;
      Toast.makeText(getActivity().getApplicationContext(),"termino " , Toast.LENGTH_LONG).show();
                callbackContext.success("Printing Success");

    }



    return true;

  }





  private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          deviceFound = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
            if (deviceFound != null) {





            }
            else {
              Log.d(TAG, "permission denied for device " + deviceFound);
            }
        }
      }
    }
  };




  private void connectUsb(String print, JSONObject opts, CallbackContext callbackContext) {

    Toast.makeText(getActivity(), "connectUsb ", Toast.LENGTH_LONG)
      .show();


    searchEndPoint(callbackContext);

    if (usbInterfaceFound != null) {
      //
      //
      setupUsbComm( print,  opts,callbackContext);
      Toast.makeText(getActivity(), "Interface is not null", Toast.LENGTH_LONG)
        .show();
    }

  }


  private void searchEndPoint(CallbackContext callbackContext) {


    usbInterfaceFound = null;
    endpointOut = null;
    UsbEndpoint endpointIn = null;

    // Search device for targetVendorID and targetProductID
    if (deviceFound == null) {
      UsbManager manager = (UsbManager) getActivity().getApplicationContext().getSystemService(Context.USB_SERVICE);
      HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
      Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

      while (deviceIterator.hasNext()) {
        UsbDevice device = deviceIterator.next();


        if (device.getVendorId() == targetVendorID) {
          if (device.getProductId() == targetProductID) {
            deviceFound = device;
            Toast.makeText(getActivity().getApplicationContext(),device.getDeviceName()+" "+device.getDeviceClass()+" "+device.getDeviceSubclass()
              +" "+device.getVendorId()+" "+device.getProductId(), Toast.LENGTH_LONG).show();
            Toast.makeText(getActivity().getApplicationContext(),device.getVendorId()+" = "+targetVendorID, Toast.LENGTH_LONG).show();
            Toast.makeText(getActivity().getApplicationContext(),device.getProductId()+" = "+targetProductID , Toast.LENGTH_LONG).show();
          }
        }
      }
    }
    if (deviceFound == null) {
      Toast.makeText(getActivity(), "device not found",
        Toast.LENGTH_LONG).show();
      callbackContext.error("Error: Code 03: Device Not found");

    } else {

      for (int i = 0; i < deviceFound.getInterfaceCount(); i++) {
        UsbInterface usbif = deviceFound.getInterface(i);

        UsbEndpoint tOut = null;
        UsbEndpoint tIn = null;

        int tEndpointCnt = usbif.getEndpointCount();
        if (tEndpointCnt >= 2) {
          for (int j = 0; j < tEndpointCnt; j++) {
            if (usbif.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
              if (usbif.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT) {
                tOut = usbif.getEndpoint(j);
              } else if (usbif.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN) {
                tIn = usbif.getEndpoint(j);
              }
            }
          }

          if (tOut != null && tIn != null) {
            // This interface have both USB_DIR_OUT
            // and USB_DIR_IN of USB_ENDPOINT_XFER_BULK
            usbInterfaceFound = usbif;
            endpointOut = tOut;
            endpointIn = tIn;
          }
        }

      }

      if (usbInterfaceFound == null) {

        Toast.makeText(getActivity(), "No suitable interface found!", Toast.LENGTH_LONG)
          .show();
        callbackContext.error("Error: Code 04:No suitable interface found!");
      } else {

        Toast.makeText(getActivity(), "UsbInterface found: "
          + usbInterfaceFound.toString() + "\n\n"
          + "Endpoint OUT: " + endpointOut.toString() + "\n\n"
          + "Endpoint IN: " + endpointIn.toString(), Toast.LENGTH_LONG)
          .show();
      }
    }
  }



  private boolean setupUsbComm(String print, JSONObject opts, CallbackContext callbackContext) {



    boolean success = false;

    UsbManager manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
    Boolean permitToRead = mUsbManager.hasPermission(deviceFound);


    if (permitToRead) {
      Toast.makeText(getActivity(), "Permission: " + permitToRead,Toast.LENGTH_LONG).show();
      UsbDeviceConnection usbDeviceConnection = mUsbManager.openDevice(deviceFound);
      if (usbDeviceConnection != null) {
        usbDeviceConnection.claimInterface(usbInterfaceFound, true);

        int usbResult;
        String dataToPrint="This is a printer test $intro$ controles inteligentes Oscar Enrique Cortes ";
        byte[] message= new byte[print.length()+5];
        int ver=2;
        int data=print.length();
        String dat=null;
        for(int i=0;i<data-3;i++){
          if(ver==2) {

                  dat=print.substring(i, i + 3);
            if (dat.equals("$i$")) {
              message[i] = (byte) 10;
              ver=0;
            }else if(dat.equals("$h$")){
              message[i] = (byte) 9;
              ver=0;
            } else if(dat.equals("$b$")){
              message[i] = (byte) 27;
              message[i+1] = (byte) 14;
              ver=0;
            }else{
              message[i] = (byte) print.charAt(i);
            }

          }else{
            ver=ver+1;

          }

        }
        message[print.length()+1]=(byte)10;
        usbResult = usbDeviceConnection.bulkTransfer(endpointOut,
          message, message.length, 500);
        Toast.makeText(getActivity(), "bulkTransfer: " + usbResult,
          Toast.LENGTH_LONG).show();
          callbackContext.success("Printing Success");





      }

    } else {
      manager.requestPermission(deviceFound, mPermissionIntent);
      Toast.makeText(getActivity(), "Permission: " + permitToRead,
        Toast.LENGTH_LONG).show();
      callbackContext.error("Error: code 05: Permission to read Denied");

    }

    return success;
  }





}
