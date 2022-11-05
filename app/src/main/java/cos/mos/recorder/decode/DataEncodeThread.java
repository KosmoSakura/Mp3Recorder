package cos.mos.recorder.decode;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Description:
 * @Author: Kosmos
 * @Date: 2019.05.25 15:25
 * @Email: KosmoSakura@gmail.com
 */
public class DataEncodeThread extends HandlerThread implements AudioRecord.OnRecordPositionUpdateListener {
    private StopHandler handler;
    private static final int PROCESS_STOP = 1;
    private byte[] mp3Buffer;
    private FileOutputStream outputStream;

    private static class StopHandler extends Handler {
        private DataEncodeThread encodeThread;

        public StopHandler(Looper looper, DataEncodeThread encodeThread) {
            super(looper);
            this.encodeThread = encodeThread;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROCESS_STOP) {
                //处理缓冲区中的数据
                while (encodeThread.processData() > 0) ;
                // 清空队列
                removeCallbacksAndMessages(null);
                encodeThread.flushAndRelease();
                getLooper().quit();
            }
        }
    }

    public DataEncodeThread(File file, int bufferSize) throws FileNotFoundException {
        super("DataEncodeThread");
        this.outputStream = new FileOutputStream(file);
        mp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];
    }

    @Override
    protected void onLooperPrepared() {
        handler = new StopHandler(getLooper(), this);
    }

    public void sendStopMessage() {
        handler.sendEmptyMessage(PROCESS_STOP);
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onMarkerReached(AudioRecord recorder) {
        // Do nothing
    }

    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
//        Log.i("yeby", "onPeriodicNotification");
        processData();
    }

    /**
     * 从缓冲区中读取并处理数据，使用lame编码MP3
     *
     * @return 从缓冲区中读取的数据的长度（没有数据时返回0）
     */
    private int processData() {
        if (mTasks.size() > 0) {
            Task task = mTasks.remove(0);
            short[] buffer = task.getData();
            int readSize = task.getReadSize();
            int encodedSize = ULame.encode(buffer, buffer, readSize, mp3Buffer);
            if (encodedSize > 0) {
                try {
                    outputStream.write(mp3Buffer, 0, encodedSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return readSize;
        }
        return 0;
    }

    /**
     * 缓冲区内存不足时，所有数据刷新到文件中
     */
    private void flushAndRelease() {
        //将MP3结尾信息写入buffer中
        final int flushResult = ULame.flush(mp3Buffer);
        if (flushResult > 0) {
            try {
                outputStream.write(mp3Buffer, 0, flushResult);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ULame.close();
            }
        }
    }

    private List<Task> mTasks = Collections.synchronizedList(new ArrayList<Task>());

    public void addTask(short[] rawData, int readSize) {
        mTasks.add(new Task(rawData, readSize));
    }

    private class Task {
        private short[] rawData;
        private int readSize;

        public Task(short[] rawData, int readSize) {
            this.rawData = rawData.clone();
            this.readSize = readSize;
        }

        public short[] getData() {
            return rawData;
        }

        public int getReadSize() {
            return readSize;
        }
    }
}
