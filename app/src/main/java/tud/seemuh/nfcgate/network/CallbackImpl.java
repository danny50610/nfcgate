package tud.seemuh.nfcgate.network;

import android.nfc.Tag;
import android.util.Log;
import android.widget.TextView;

import tud.seemuh.nfcgate.reader.IsoDepReaderImpl;
import tud.seemuh.nfcgate.reader.NFCTagReader;
import tud.seemuh.nfcgate.reader.NfcAReaderImpl;
import tud.seemuh.nfcgate.util.Utils;


public class CallbackImpl implements SimpleNetworkConnectionClientImpl.Callback {

    private NFCTagReader mReader = null;
    private TextView debugView;

    public void setUpdateButton(TextView ldebugView) {
        debugView = ldebugView;
    }

    /**
     * Implementation of SimpleNetworkConnectionClientImpl.Callback
     * @param data: received bytes
     */
    @Override
    public void onDataReceived(byte[] data) {
        if(mReader.isConnected()) {
            byte[] bytesFromCard = mReader.sendCmd(data);
            SimpleNetworkConnectionClientImpl.getInstance().sendBytes(bytesFromCard);
            //Ugly way to send data to the GUI from an external thread
            new UpdateUI(debugView).execute(Utils.bytesToHex(bytesFromCard)+"\n");
        }
    }

    /**
     * Called on nfc tag intend
     * @param tag nfc tag
     * @return true if a supported tag is found
     */
    public boolean setTag(Tag tag) {

        boolean found_supported_tag = false;

        //identify tag type
        for(String type: tag.getTechList()) {
            // TODO: Refactor this into something much nicer to avoid redundant work betw.
            //       this code and the worker thread, which also does this check.
            Log.i("NFCGATE_DEBUG", "Tag TechList: " + type);
            if("android.nfc.tech.IsoDep".equals(type)) {
                found_supported_tag = true;

                mReader = new IsoDepReaderImpl(tag);
                Log.d("NFCGATE_DEBUG", "Chose IsoDep technology.");
                break;
            } else if("android.nfc.tech.NfcA".equals(type)) {
                found_supported_tag = true;

                mReader = new NfcAReaderImpl(tag);
                Log.d("NFCGATE_DEBUG", "Chose NfcA technology.");
                break;
            }
        }

        //set callback when data is received
        if(found_supported_tag){
            SimpleNetworkConnectionClientImpl.getInstance().setCallback(this);
        }

        return found_supported_tag;
    }
}