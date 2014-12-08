package com.sinovoice.pathfinder.hcicloud.hwr;

import android.content.Context;
import android.util.Log;

import com.sinovoice.hcicloudsdk.api.hwr.HciCloudHwr;
import com.sinovoice.hcicloudsdk.common.hwr.HwrInitParam;
import com.sinovoice.pathfinder.hcicloud.sys.SysConfig;

public class HciCloudHwrHelper {
    private static final String TAG = HciCloudHwrHelper.class.getSimpleName();

    private static HciCloudHwrHelper mInstance;

    private HciCloudHwrHelper() {
    }

    public static HciCloudHwrHelper getInstance() {
        if (mInstance == null) {
            mInstance = new HciCloudHwrHelper();
        }
        return mInstance;
    }

    /**
     * HWR��дʶ��������ʼ�������صĴ����������API��HciErrorCode�鿴
     * 
     * @param context
     * @return ������, return 0 ��ʾ�ɹ�
     */
    public int init(Context context) {
        int initResult = 0;

        // ����Hwr��ʼ���Ĳ������ʵ��
        HwrInitParam hwrInitParam = new HwrInitParam();

        // ��ȡAppӦ���е�lib��·��,���ʹ��/data/data/pkgName/lib�µ���Դ�ļ�,��Ҫ���android_so�ı��
        String hwrDirPath = context.getFilesDir().getAbsolutePath()
                .replace("files", "lib");
        hwrInitParam.addParam(HwrInitParam.PARAM_KEY_DATA_PATH, hwrDirPath);
        hwrInitParam.addParam(HwrInitParam.PARAM_KEY_FILE_FLAG, "android_so");
        hwrInitParam.addParam(HwrInitParam.PARAM_KEY_INIT_CAP_KEYS,
                SysConfig.CAPKEY_HWR);

        Log.d(TAG, "hwr init config: " + hwrInitParam.getStringConfig());

        // HWR ��ʼ��
        initResult = HciCloudHwr.hciHwrInit(hwrInitParam.getStringConfig());
        return initResult;
    }

    /**
     * HWR��дʶ����������ʼ�������صĴ����������API��HciErrorCode�鿴
     * 
     * @return ������, return 0 ��ʾ�ɹ�
     */
    public int release() {
        int result = HciCloudHwr.hciHwrRelease();
        return result;
    }
}
