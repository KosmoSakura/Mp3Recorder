package cos.mos.recorder.init;

import android.app.Application;

/**
 * @Description:
 * @Author: Kosmos
 * @Date: 2019.05.24 21:19
 * @Email: KosmoSakura@gmail.com
 */
public class KApp extends Application {
    private static Application instance;

    public static Application instance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
