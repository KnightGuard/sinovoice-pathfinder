package com.sinovoice.pathfinder.hcicloud.sys;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.AuthExpireTime;
import com.sinovoice.hcicloudsdk.common.CapabilityItem;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.InitParam;

public class HciCloudSysHelper {
    private static final String TAG = HciCloudSysHelper.class.getSimpleName();
    
    public static final int ERRORCODE_NONE = 0;
    public static final int ERRORCODE_AUTH_FILE_INVALID = 1;
    public static final int ERRORCODE_AUTH_FILE_WILL_EXPIRED = 2;
    public static final int ERRORCODE_AUTH_FILE_HAS_EXPIRED = 3;
    
    private static HciCloudSysHelper mInstance;

    private HciCloudSysHelper() {
    	
    }

    public static HciCloudSysHelper getInstance() {
        if (mInstance == null) {
            mInstance = new HciCloudSysHelper();
        }
        return mInstance;
    }

    /**
     * HciCloudϵͳ��ʼ��
     * @param context
     * @return
     * 
     */
    public int init(Context context) {
        String initConfig = getInitConfig(context);
        Log.i(TAG, "initConfig: " + initConfig);

        // ��ʼ��
        int initErrorCode = HciCloudSys.hciInit(initConfig, context);
        if (initErrorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "hciInit error. initErrorCode: " + initErrorCode);
        } else {
            Log.i(TAG, "hciInit success.");
        }
        
        return initErrorCode;
    }

    /**
     * �����Ȩ����ʱ��
     * 
     * @return
     */
    public int checkExpireTime() {
    	int result = ERRORCODE_NONE;
    	
        AuthExpireTime expireTime = new AuthExpireTime();
        int errorCode = HciCloudSys.hciGetAuthExpireTime(expireTime);
        Log.d(TAG, "hciGetAuthExpireTime(), errorCode: " + errorCode);
        
        if (errorCode == HciErrorCode.HCI_ERR_SYS_AUTHFILE_INVALID) {
            // ��Ȩ�ļ������ڻ�Ƿ�
        	result = ERRORCODE_AUTH_FILE_INVALID;
        } else if (errorCode == HciErrorCode.HCI_ERR_NONE) {
            // ��ȡ�ɹ����жϹ���ʱ��
            long expireTimeValue = expireTime.getExpireTime();
            long currTime = System.currentTimeMillis();

            // �������ʱ��ms�����˴�Ϊ1��
            final long TIME_DIFFERENCE_MAX = 1 * 24 * 3600 * 1000L;
            long timeDifference = expireTimeValue * 1000 - currTime;

            if(timeDifference < 0){
            	result = ERRORCODE_AUTH_FILE_HAS_EXPIRED;
            }else if (timeDifference < TIME_DIFFERENCE_MAX) {
                // ʱ����С���趨��ֵ
            	result = ERRORCODE_AUTH_FILE_WILL_EXPIRED;
            }else{
            	Log.v(TAG, "authfile expireTime is valid.");
            }
        } else {
            //
        	Log.e(TAG, "δ��������������");
        }
        
        return result;
    }
    

    /**
     * ������ȡ��Ȩ
     * 
     * @return 
     */
    public int checkAuthByNet() {
        int errorCode = HciCloudSys.hciCheckAuth();
        Log.v(TAG, "hciCheckAuth(), errorCode: " + errorCode);
        
        if(errorCode == HciErrorCode.HCI_ERR_NONE){
        	Log.v(TAG, "hciCheckAuth success.");
        }else{
        	Log.e(TAG, "hciCheckAuth fail.");
        }
        return errorCode;
    }

    /**
     * ���ȫ��capkey�Ƿ����
     * 
     * @return
     */
    public int checkCapkeysEnable() {
    	int errorCode = HciErrorCode.HCI_ERR_NONE;
        for (String capKey : SysConfig.ALL_CAPKEY_ARRAY) {
            CapabilityItem item = new CapabilityItem();
            errorCode = HciCloudSys.hciGetCapability(capKey, item);
            item = null;
            if (errorCode != HciErrorCode.HCI_ERR_NONE) {
                Log.e(TAG, "hciGetCapability() fail, code: " + errorCode + ", capKey: " + capKey);
                break;
            }
        }
        return errorCode;
    }

    /**
     * ϵͳ����ʼ��
     */
    public void release() {
        int errorCode = HciCloudSys.hciRelease();
        Log.i(TAG, "hciRelease(), errorCode: " + errorCode);
    }

    /**
     * ���س�ʼ����Ϣ
     * 
     * @param context
     *            �������ﾳ
     * @return ϵͳ��ʼ������
     */
    private String getInitConfig(Context context) {
        String authDirPath = context.getFilesDir().getAbsolutePath();

        // ǰ����������
        InitParam initparam = new InitParam();

        // ��Ȩ�ļ�����·�����������
        initparam.addParam(InitParam.PARAM_KEY_AUTH_PATH, authDirPath);

        // �Ƿ��Զ���������Ȩ,��� ��ȡ��Ȩ/������Ȩ�ļ���ע��
        initparam.addParam(InitParam.PARAM_KEY_AUTO_CLOUD_AUTH, "no");

        // �����Ʒ���Ľӿڵ�ַ���������
        initparam.addParam(InitParam.PARAM_KEY_CLOUD_URL, SysConfig.CLOUDURL);

        // ������Key���������ɽ�ͨ�����ṩ
        initparam.addParam(InitParam.PARAM_KEY_DEVELOPER_KEY, SysConfig.DEVELOPERKEY);

        // Ӧ��Key���������ɽ�ͨ�����ṩ
        initparam.addParam(InitParam.PARAM_KEY_APP_KEY, SysConfig.APPKEY);

        // ������־����
        String sdcardState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String packageName = context.getPackageName();
            
            String logPath = sdPath + File.separator + "sinovoice"
                    + File.separator + packageName + File.separator + "log"
                    + File.separator;

            // ��־�ļ���ַ
            File fileDir = new File(logPath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            // ��־��·������ѡ�������������Ϊ����������־
            initparam.addParam(InitParam.PARAM_KEY_LOG_FILE_PATH, logPath);

            // ��־��Ŀ��Ĭ�ϱ������ٸ���־�ļ��������򸲸���ɵ���־
            initparam.addParam(InitParam.PARAM_KEY_LOG_FILE_COUNT, "5");

            // ��־��С��Ĭ��һ����־�ļ�д��󣬵�λΪK
            initparam.addParam(InitParam.PARAM_KEY_LOG_FILE_SIZE, "1024");

            // ��־�ȼ���0=�ޣ�1=����2=���棬3=��Ϣ��4=ϸ�ڣ�5=���ԣ�SDK�����С�ڵ���logLevel����־��Ϣ
            initparam.addParam(InitParam.PARAM_KEY_LOG_LEVEL, "5");
        }

        return initparam.getStringConfig();
    }

}
