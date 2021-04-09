package com.sdtv.player;

/**
 * 作者：admin016
 * 日期时间: 2021/4/9 14:24
 * 内容描述:
 */
public interface MediaListener {
    public void play();    //播放
    public void pause();   //暂停
    public void moveon();  //继续播放
    public void rePlay();  //重新播放
    public void seekToPosition(int position); //跳转到指定位置
    public long getCurrentPosition(); //获取当前的播放进度
}
