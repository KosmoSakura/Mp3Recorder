package cos.mos.recorder.decode;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * @Description: 录音工具
 * @Author: Kosmos
 * @Date: 2019.05.25 15:16
 * @Email: KosmoSakura@gmail.com
 */
public class MP3Recorder {
    //Recorder
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;//音源：麦克风
    private static final int DEFAULT_SAMPLING_RATE = 44100;//采样率：模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;//单声道
    private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;///PCM编码,16Bit
    //Lame
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;//音质
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;//mono=>1
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;//编码比特率
    //采样配置
    private static final int FRAME_COUNT = 160;//每160帧作为一个数列周期，通知编码
    private AudioRecord record;
    private int bufferSize;
    private short[] bufferPCM;
    private DataEncodeThread encodeThread;
    private boolean isRecording = false;

    /**
     * @apiNote 采样率1通道，16位pcm
     */
    public MP3Recorder() {
    }

    public void start(File targetFile) throws IOException {
        if (isRecording) {
            return;
        }
        isRecording = true; //提早，防止init或startRecording被多次调用
        initAudioRecorder(targetFile);
        record.startRecording();
        new Thread() {
            @Override
            public void run() {
                //设置线程权限
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                while (isRecording) {
                    int readSize = record.read(bufferPCM, 0, bufferSize);
                    if (readSize > 0) {
                        encodeThread.addTask(bufferPCM, readSize);
                        calculateRealVolume(bufferPCM, readSize);
                    }
                }
                //释放
                record.stop();
                record.release();
                record = null;
                // 等待完成转码
                encodeThread.sendStopMessage();
            }

            /**
             * @apiNote 此计算方法来自samsung开发范例
             * @param buffer buffer
             * @param readSize readSize
             */
            private void calculateRealVolume(short[] buffer, int readSize) {
                double sum = 0;
                for (int i = 0; i < readSize; i++) {
                    // 这里没有做运算的优化，为了更加清晰的展示代码
                    sum += buffer[i] * buffer[i];
                }
                if (readSize > 0) {
                    double amplitude = sum / readSize;
                    mVolume = (int) Math.sqrt(amplitude);
                }
            }
        }.start();
    }

    private int mVolume;

    /**
     * @return 真实音量
     * @apiNote 获取真实的音量。 [算法来自三星]
     */
    public int getRealVolume() {
        return mVolume;
    }

    /**
     * @return 音量
     * @apiNote 获取相对音量。 超过最大值时取最大值。
     */
    public int getVolume() {
        if (mVolume >= MAX_VOLUME) {
            return MAX_VOLUME;
        }
        return mVolume;
    }

    private static final int MAX_VOLUME = 2000;

    /**
     * @return 最大音量值。
     * @apiNote 根据资料假定的最大值。 实测时有时超过此值。
     */
    public int getMaxVolume() {
        return MAX_VOLUME;
    }

    public void stop() {
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    /**
     * @param targetFile 目标文件
     * @apiNote 初始化
     */
    private void initAudioRecorder(File targetFile) throws IOException {
        bufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
            DEFAULT_AUDIO_FORMAT.getAudioFormat());
        int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();//得到样本个数。计算缓冲区大小
        //四舍五入到给定帧大小，方便整除，以通知
        int frameSize = bufferSize / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            bufferSize = frameSize * bytesPerFrame;
        }
        record = new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLING_RATE,
            DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat(), bufferSize);
        bufferPCM = new short[bufferSize];
        //初始化lame缓冲区mp3采样速率与所记录的pcm采样速率相同，比特率为32kbps
        ULame.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE,
            DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
        encodeThread = new DataEncodeThread(targetFile, bufferSize);
        encodeThread.start();
        record.setRecordPositionUpdateListener(encodeThread, encodeThread.getHandler());
        record.setPositionNotificationPeriod(FRAME_COUNT);
    }
}
