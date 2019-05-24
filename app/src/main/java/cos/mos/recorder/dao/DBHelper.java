package cos.mos.recorder.dao;

import org.litepal.LitePal;

import java.util.List;

/**
 * @Description:
 * @Author: Kosmos
 * @Date: 2019.05.24 18:35
 * @Email: KosmoSakura@gmail.com
 */
public class DBHelper {
    public static void add(String dir) {
        new RecorderDB(dir).save();
    }

    public static void del(String dir) {
        LitePal.deleteAll(RecorderDB.class, "dir = ?", dir);
    }

    public static List<RecorderDB> getAll() {
        return LitePal.findAll(RecorderDB.class);
    }
}
