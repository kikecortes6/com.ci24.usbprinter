package com.ci24.usbprinter;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import android.app.Activity;
import android.widget.Toast;
import com.jolimark.UsbPrinter;
//Plugin ci24 para imprimir en dispositivo movil im78
public class Printer extends CordovaPlugin {

  public Activity getActivity() {    return this.cordova.getActivity();  }
  @Override
  public boolean execute(String action, final JSONArray args, CallbackContext callbackContext)  {
     //función que recibe el texto desde el dispositivo y lo imprime, maneja protocolo ESC/POS
     if (action.equals("printData")) {
        UsbPrinter myprinter = new UsbPrinter();

        boolean ret = myprinter.Open();
       String printData=null;
       try{
        printData = args.get(0).toString();
      }catch (Exception e){
        Toast.makeText(getActivity().getApplicationContext(), "Error: Code 2:Json error, no text or bad format", Toast.LENGTH_LONG).show();
        callbackContext.error("Error: Code 2:Json error, no text or bad format");
      }
      if (ret) {
         ret = myprinter.WriteString(printData);
          if (!ret) {
            Toast.makeText(getActivity().getApplicationContext(), "Error: Code 1:There is no connection with the printer", Toast.LENGTH_LONG).show();
            callbackContext.error("Error: Code 1:There is no connection with the printer");
          }
          else{
            myprinter.Close();
            Toast.makeText(getActivity().getApplicationContext(), "Impresion exitosa ", Toast.LENGTH_LONG).show();
            callbackContext.success("Impresión exitosa");
          }
        }
        else{
          Toast.makeText(getActivity().getApplicationContext(), "Error: Code 1:There is no connection with the printer", Toast.LENGTH_LONG).show();
          callbackContext.error("Error: Code 1:There is no connection with the printer");
        }
    }
    //Función para abrir la impresora del dispositivo im78
    else  if (action.equals("openPrinter")) {
       UsbPrinter tmpUsbDev = new UsbPrinter();
       boolean retnVale = tmpUsbDev.UnLock();
      if(retnVale){
        callbackContext.success("Open Succesfull +retnVale.toString");
      }
      else {
        callbackContext.error("Error Opening Printer");
      }
    }
    //Función para alimentar papel del dispositivo im78
    else  if (action.equals("feedPaper")) {
      UsbPrinter myprinter = new UsbPrinter();
      boolean ret = myprinter.Open();
      String printData="\n";
     if (ret) {
        ret = myprinter.WriteString(printData);
        if (!ret) {
          Toast.makeText(getActivity().getApplicationContext(), "Error: Code 1:There is no connection with the printer", Toast.LENGTH_LONG).show();
          callbackContext.error("Error: Code 1:There is no connection with the printer");
        }
        else{
          myprinter.Close();
          Toast.makeText(getActivity().getApplicationContext(), "Impresion exitosa ", Toast.LENGTH_LONG).show();
          callbackContext.success("Impresión exitosa");
        }
      }
      else{
        Toast.makeText(getActivity().getApplicationContext(), "Error: Code 1:There is no connection with the printer", Toast.LENGTH_LONG).show();
        callbackContext.error("Error: Code 1:There is no connection with the printer");
      }
    }
    return true;
  }
}
