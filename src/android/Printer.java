package com.ci24.usbprinter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
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
import java.util.HashMap;
import java.util.Iterator;



public class Printer extends CordovaPlugin {
  //private CallbackContext callbackContext;

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
  public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) {


    if (action.equals("gpsStatus")) {



      // register the broadcast receiver
      mUsbManager = (UsbManager) getActivity().getApplicationContext().getSystemService(Context.USB_SERVICE);
      final String ACTION_USB_PERMISSION = "com.plugin.gpsstatus.USB_PERMISSION";
      mPermissionIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, new Intent(
        ACTION_USB_PERMISSION), 0);
      IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
      getActivity().getApplicationContext().registerReceiver(mUsbReceiver, filter);

      connectUsb();




    }
    else if(action.equals( "openGps")) {



      Toast.makeText(getActivity().getApplicationContext(), "Asking Permission", Toast.LENGTH_SHORT).show();
      UsbDevice device;
      UsbManager manager = (UsbManager) this.getActivity().getSystemService(Context.USB_SERVICE);

      mPermissionIntent = PendingIntent.getBroadcast(this.getActivity(), 0, new Intent(
        ACTION_USB_PERMISSION), 0);

      IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
      getActivity().registerReceiver(mUsbReceiver, filter);



      HashMap<String , UsbDevice> deviceList = manager.getDeviceList();
      Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

      while (deviceIterator.hasNext()) {
        device = deviceIterator.next();
        manager.requestPermission(device, mPermissionIntent);
        Log.d(TAG, "DeviceID: " + device.getDeviceId());
        if(device.getDeviceId()==2002){
          Toast.makeText(getActivity().getApplicationContext(),device.getDeviceName()+" "+device.getDeviceClass()+" "+device.getDeviceSubclass()
            +" "+device.getVendorId()+" "+device.getProductId(), Toast.LENGTH_LONG).show();
        }

        Log.d(TAG,"DeviceName: " + device.getDeviceName());
        Log.d(TAG,"DeviceClass: " + device.getDeviceClass());
        Log.d(TAG, "DeviceSubClass: " + device.getDeviceSubclass());
        Log.d(TAG,"VendorID: " + device.getVendorId() );
        Log.d(TAG,"ProductID: " + device.getProductId());
      }

    }
    else if(action.equals("Subscribe")){







    }





    else {

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




  private void connectUsb() {

    Toast.makeText(getActivity(), "connectUsb ", Toast.LENGTH_LONG)
      .show();


    searchEndPoint();

    if (usbInterfaceFound != null) {
      //
      //
      setupUsbComm();
      Toast.makeText(getActivity(), "Interface is not null", Toast.LENGTH_LONG)
        .show();
    }

  }


  private void searchEndPoint() {


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
      } else {

        Toast.makeText(getActivity(), "UsbInterface found: "
          + usbInterfaceFound.toString() + "\n\n"
          + "Endpoint OUT: " + endpointOut.toString() + "\n\n"
          + "Endpoint IN: " + endpointIn.toString(), Toast.LENGTH_LONG)
          .show();
      }
    }
  }



  private boolean setupUsbComm() {



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
        byte[] message= new byte[dataToPrint.length()+1];
        for(int i=0;i<dataToPrint.length();i++){
          message[i]=(byte) dataToPrint.charAt(i);
        }
        message[dataToPrint.length()]=(byte)10;
        usbResult = usbDeviceConnection.bulkTransfer(endpointOut,
          message, message.length, 500);
        Toast.makeText(getActivity(), "bulkTransfer: " + usbResult,
          Toast.LENGTH_LONG).show();






      }

    } else {
      manager.requestPermission(deviceFound, mPermissionIntent);
      Toast.makeText(getActivity(), "Permission: " + permitToRead,
        Toast.LENGTH_LONG).show();

    }

    return success;
  }





}
