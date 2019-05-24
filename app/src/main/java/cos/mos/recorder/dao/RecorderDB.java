package cos.mos.recorder.dao;

import org.litepal.crud.LitePalSupport;

/**
 * @Description:
 * @Author: Kosmos
 * @Date: 2019.05.24 18:35
 * @Email: KosmoSakura@gmail.com
 */
public class RecorderDB extends LitePalSupport {
    private String dirs;

    public RecorderDB(String dirs) {
        this.dirs = dirs;
    }

    public String getDirs() {
        return dirs;
    }

    public void setDirs(String dirs) {
        this.dirs = dirs;
    }
}
