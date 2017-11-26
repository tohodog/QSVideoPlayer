QSVideoPlayer
====
  * 只需100行java代码即可打造自己的播放器!<br/>提供QSVideoViewHelp辅助类,该类提供了常用控件的逻辑和手势调节支持,可快速自定义ui打造自己的播放器,不用写一行播放逻辑
  * QSVideoView接口完善,事件的监听,设置视频比例,切换全屏等
  * 架构设计优良,模块化可扩展设计,解码模块目前提供了 AndroidMedia(系统自带)、ijkMedia(基于ffmepg)+ijkExoMedia(基于exo)、ExoMedia(2.0.4)解码器
  * 根据系统版本自动选择SurfaceView和TextureView,支持api9+
  * 提供list视频列表自动销毁播放代码
  * 支持本地视频,在线视频,m3u8直播等...

![下载demo.apk](https://raw.githubusercontent.com/tohodog/QSVideoPlayer/master/source/video_qrcode.png)
<br/>
扫码 [demo.apk](https://raw.githubusercontent.com/tohodog/QSVideoPlayer/master/source/qsvideoplayer.apk) 下载

## 使用说明
下载项目 添加qsvideoplayer文件夹为自己的项目依赖即可 


源码有4个解码器,根据需求自行选择需要的解码器:<br/>
1.一般简单播放视频AndroidMedia足够(体积最小,无依赖)<br/>
2.有点需求可选择AndroidMedia+ExoMedia(1MB)<br/>
3.需求高的可选AndroidMedia+(ijkMedia+ijkExoMedia)(2MB单v7a包)<br/>
目前测试解码效果ijkMedia兼容性最好,AndroidMedia个别视频有半途中断BUG,exo无明显缺陷<br/>
ps:<br/>删除ijk解码器: build.gradle注释掉所有依赖,media包里删除IjkBaseMedia IjkExoMedia IjkMedia三个类即可<br/>
删除exo解码器: libs里删除jar,media包里删除ExoMedia即可<br/>

混淆
```
-dontwarn org.song.videoplayer.**
-keep class org.song.videoplayer.** { *; }
```

## Demo使用
```
//DemoQSVideoView的ui用的jc播放器
DemoQSVideoView qsVideoView = (DemoQSVideoView) findViewById(R.id.xxx);

qsVideoView.setUp(url, "这是一一一一一一一一一个标题");

qsVideoView.getCoverImageView().setImageResource(R.mipmap.cover);//封面

//设置监听
qsVideoView.setPlayListener(new PlayListener() {
            @Override
            public void onStatus(int status) {//播放器的ui状态
                if (status == IVideoPlayer.STATE_AUTO_COMPLETE)
                    qsVideoView.quitWindowFullscreen();//播放完成退出全屏
            }

            @Override//全屏/普通...
            public void onMode(int mode) {

            }

            @Override//播放事件 初始化完成/缓冲/出错/...
            public void onEvent(int what, Integer... extra) {

            }

        });
//进入全屏的模式 0横屏 1竖屏 2传感器自动横竖屏 3根据视频比例自动确定横竖屏      -1什么都不做
qsVideoView.enterFullMode=1;

qsVideoView.play();//


```

## 返回键退出全屏
```
    @Override
    public void onBackPressed() {
        if (qsVideoView.onBackPressed())
            return;
        super.onBackPressed();
    }
```

## DIY播放器:
0.read source code.<br/><br/>
1.可直接修改DemoQSVideoView改造自己的播放器<br/><br/>
2.继承QSVideoViewHelp参考DemoQSVideoView,源码均有注释,不用写一行播放逻辑<br/>
    (1) 子类提供layout布局,布局里需要help类实现逻辑的控件,设置id为以下特定id即可
 ```
    <!--ImageView播放按钮1 2-->
    <item name="help_start" type="id" />
    <item name="help_start2" type="id" />
    <!--TextView播放时间  视频时长-->
    <item name="help_total" type="id" />
    <item name="help_current" type="id" />
    <!--ProgressBar进度条  SeekBar拖动条-->
    <item name="help_progress" type="id" />
    <item name="help_seekbar" type="id" />
    <!--ImageView全屏按钮  View返回按钮-->
    <item name="help_fullscreen" type="id" />
    <item name="help_back" type="id" />
    
    //如播放按钮定义,注意: @id 没有加号,这样定义父类会自动完成该按钮逻辑
    <ImageView
            android:id="@id/help_start"
            android:layout_width="60dp"
            android:layout_height="60dp"
            android:layout_centerInParent="true"/>
 ```
<br/>
    (2) java代码里设置各个状态的ui即可完成自己的播放器,具体参考DemoQSVideoView<br/><br/>
3.直接使用QSVideoView,自己写控制ui和逻辑<br/>
(继承关系:DemoQSVideoView → QSVideoViewHelp → QSVideoView)


## QSVideoView API接口
```
    void setUp(String url, Object... objects);//设置视频地址

    void play();//播放

    void pause();//暂停

    void seekTo(int duration);//进度调节

    void setPlayListener(PlayListener playListener);//播放监听

    void setAspectRatio(int aspectRatio);//设置视频比例

    void setiMediaControl(int i);//设置解码模块

    boolean onBackPressed();//返回键退出全屏

    boolean isPlaying();//是否播放中

    int getPosition();//获取播放进度

    int getDuration();//获取视频时长

    int getCurrentMode();//获得播放器当前的模式(全屏,普通...)

    int getCurrentState();//获得播放器当前的状态(播放,暂停,完成...)

    void enterWindowFullscreen();//全屏

    void quitWindowFullscreen();//退出全屏

    void release();//销毁

```

![输入图片说明](http://git.oschina.net/uploads/images/2017/0614/173143_a73430c9_530535.jpeg "在这里输入图片标题")
![](https://github.com//tohodog/QSVideoPlayer/raw/master/source/lsit.gif)

![输入图片说明](http://git.oschina.net/uploads/images/2017/0224/180438_84c8332c_530535.jpeg "在这里输入图片标题")
