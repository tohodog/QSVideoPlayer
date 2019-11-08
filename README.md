![logo][logopng]
<br/>
<br/>
---
<br/>

[![GitHub release][relesesvg]][relesezip] [![api][apisvg]][api]  [![License][licensesvg]][license]

  * QSVideoView接口完善,功能丰富
    * 支持设置视频比例
    * 支持两种悬浮窗
    * 支持扩展解码器
    * 智能切换全屏
    * 支持本地缓存
    * 支持倍速静音等
  * 只需100行java代码即可打造自己的播放器!<br/>提供QSVideoViewHelp辅助类,该类提供了常用控件的逻辑和手势调节支持,可快速自定义ui打造自己的播放器,不用写一行播放逻辑
  * 架构设计优良,模块化可扩展设计,解码模块目前提供了 AndroidMedia(系统自带)、ijkMedia(基于ffmepg)+ijkExoMedia(基于exo)、ExoMedia(2.0.4)解码器
  * 根据系统版本自动选择SurfaceView和TextureView
  * 支持本地视频,在线视频,m3u8直播等
  * 提供DemoQSVideoView成品播放器,支持手势,清晰度
  * 提供list视频列表自动销毁播放框架
  * 一句代码集成弹幕

![qrcode][qrpng]
<br/>
[![apkurl][apkurlsvg]][apkurl]


## Preview
![](https://github.com/tohodog/QSVideoPlayer/raw/master/source/main.png)
![](https://github.com/tohodog/QSVideoPlayer/raw/master/source/full1.png)
![](https://github.com/tohodog/QSVideoPlayer/raw/master/source/lsit.gif)
![](https://github.com/tohodog/QSVideoPlayer/raw/master/source/float.png)
![](https://github.com/tohodog/QSVideoPlayer/raw/master/source/full2.jpg)


## 使用说明

下载项目 添加qsvideoplayer文件夹为自己的项目依赖即可

根据需求自行选择需要的解码器:<br/>
一般简单播放视频AndroidMedia足够(体积最小,无依赖)<br/>
需求高的可选AndroidMedia+(ijkMedia+ijkExoMedia)(2MB单v7a包)<br/>
目前测试解码效果ijkMedia兼容性最好,AndroidMedia个别视频有半途中断BUG,exo无明显缺陷<br/>
ps:<br/>删除ijk解码器: build.gradle注释掉所有依赖,media包里删除IjkBaseMedia IjkExoMedia IjkMedia三个类即可<br/>
删除exo解码器: libs里删除jar,media包里删除ExoMedia即可<br/>

Gradle
```
allprojects {
    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
}

dependencies {
    implementation 'com.github.tohodog:QSVideoPlayer:2.2.8'
}
```


## QSVideoView API接口
```
    void setUp(String url, Object... objects);//设置视频地址

    void play();//播放/初始化(完成自动播放)

    void prePlay();//初始化(完成不会播放)

    void pause();//暂停

    void seekTo(int duration);//进度调节

    void setPlayListener(PlayListener playListener);//播放监听 参数含义参照IVideoPlayer

    void addPlayListener(PlayListener playListener);//多播放监听

    void removePlayListener(PlayListener playListener);//移除播放监听

    void setAspectRatio(int aspectRatio);//设置视频比例 参数见IRenderView

    void setDecodeMedia(Class<? extends BaseMedia> claxx);//设置解码模块
    
    void openCache();//打开缓存

    boolean onBackPressed();//返回键退出全屏

    boolean isPlaying();//是否播放中

    void enterWindowFullscreen();//全屏

    void quitWindowFullscreen();//退出全屏

    boolean enterWindowFloat(FloatParams floatParams);//浮窗 false没权限

    void quitWindowFloat();//退出浮窗

    boolean setMute(boolean isMute);//是否静音 false不支持

    boolean setSpeed(float rate);//设置播放倍速,false不支持

    void release();//销毁

    Bitmap getCurrentFrame();//截图

    int getPosition();//获取播放进度

    int getDuration();//获取视频时长

    int getVideoWidth();//获取视频宽

    int getVideoHeight();//获取视频长

    int getCurrentMode();//获得播放器当前的模式(全屏,普通,浮窗)

    int getCurrentState();//获得播放器当前的状态(播放,暂停,完成...)

```

## Demo使用
### JAVA
```
    //DemoQSVideoView的ui用的jc播放器
    DemoQSVideoView qsVideoView = (DemoQSVideoView) findViewById(R.id.xxx);

    qsVideoView.setUp(url, "这是一一一一一一一一一个标题");

    //设置多个清晰度和ijk配置
    //List<IjkMedia.Option> list = new ArrayList<>();
    //list.add(new IjkMedia.Option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1));
    //demoVideoView.setUp(
    //                QSVideo.Build(url).title("这是标清标题").definition("标清").resolution("标清 720P").build(),
    //                QSVideo.Build(url).title("这是高清标题").definition("高清").resolution("高清 1080P").option(list).build());

    qsVideoView.getCoverImageView().setImageResource(R.mipmap.cover);//封面

    //设置监听
    qsVideoView.setPlayListener(new PlayListener() {
            @Override
            public void onStatus(int status) {//播放器的ui状态
                if (status == IVideoPlayer.STATE_AUTO_COMPLETE)
                    qsVideoView.quitWindowFullscreen();//播放完成退出全屏
            }

            @Override//全屏/普通/浮窗...
            public void onMode(int mode) {

            }

            @Override//播放事件 初始化完成/缓冲/出错/点击事件...
            public void onEvent(int what, Integer... extra) {

            }

        });
    //进入全屏的模式 0横屏 1竖屏 2传感器自动横竖屏 3根据视频比例自动确定横竖屏      -1什么都不做
    qsVideoView.enterFullMode=3;
    qsVideoView.play();
```

### 返回键退出全屏
```
    @Override
    public void onBackPressed() {
        if (qsVideoView.onBackPressed())
            return;
        super.onBackPressed();
    }
```

### XML
```
        <org.song.videoplayer.DemoQSVideoView
                android:id="@+id/xxx"
                android:layout_width="match_parent"
                android:layout_height="400dp" />
```

### AndroidManifest
```
        <activity
            android:name=".VideoActivity"
            android:configChanges="orientation|keyboardHidden|screenSize">
        </activity>
```

### 悬浮窗
```
    FloatParams floatParams = new FloatParams();
    floatParams.x = 0;//浮窗中心坐标x
    floatParams.y = 0;//浮窗中心坐标y
    floatParams.w = 540;//宽
    floatParams.h = 270;//高
    floatParams.round = 30;//浮窗圆角 需SDK_INT >= 21
    floatParams.fade = 0.8f;//透明度 需SDK_INT >= 11
    floatParams.canMove = true;//是否可以拖动
    floatParams.canCross = false;//是否可以越屏幕边界
    floatParams.systemFloat = true;TRUE系统浮窗需要权限　FALSE界面内浮窗

    if (!qsVideoView.enterWindowFloat(floatParams)) {
        Toast.makeText(this,"没有浮窗权限",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
              Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 0);
        }
    }
```


### 生命周期控制
</br>实现后台暂停播放,超过15秒销毁,回来还原播放状态,体验好
```
    private boolean playFlag;//记录退出时播放状态 回来的时候继续播放
    private int position;//记录销毁时的进度 回来继续该进度播放
    private Handler handler = new Handler();

    @Override
    public void onResume() {
        super.onResume();
        if (playFlag)
            demoVideoView.play();
        handler.removeCallbacks(runnable);
        if (position > 0) {
            demoVideoView.seekTo(position);
            position = 0;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (demoVideoView.isSystemFloatMode())
            return;
        //暂停
        playFlag = demoVideoView.isPlaying();
        demoVideoView.pause();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (demoVideoView.isSystemFloatMode())
            return;
        //进入后台不马上销毁,延时15秒
        handler.postDelayed(runnable, 1000 * 15);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();//销毁
        if (demoVideoView.isSystemFloatMode())
            demoVideoView.quitWindowFloat();
        demoVideoView.release();
        handler.removeCallbacks(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (demoVideoView.getCurrentState() != IVideoPlayer.STATE_AUTO_COMPLETE)
                position = demoVideoView.getPosition();
            demoVideoView.release();
        }
    };
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


## Log
### v2.2.8(2019-04-12)
  * 清晰度选择
  * 优化
### v2.2.7(2019-01-05)
  * 倍速播放
  * 优化
### v2.2.5(2018-10-24)
  * 支持视频缓存
### v2.2.4(2018-9-1)
  * 浮窗超出屏幕回弹效果
  * 优化
### v2.2.3(2018-5-12)
  * +Danmaku(一行代码集成弹幕)
  * +getCurrentFrame(增加截图>=4.0)
  * +support systemfloat goback(系统浮窗可返回)
  * +perfect listenner(完善事件监听)
### v2.2.2(2018-2-13)
  * can get the floatparams after moving(可以获取移动后的浮窗参数)
  * 8.0 callback onInfo (804, -1004) problem(8.0断流回调onInfo(804,-1004)问题)
  * Immersion Demo(沉浸式Demo)
### v2.2.1(2018-1-30)
  * add floatwindow in activity(增加界面内悬浮窗功能,无需权限)
  * add event(seekbar) listener(增加seekbar事件监听)
### v2.2.0(2018-1-28)
  * add floatwindow(增加悬浮窗功能)
### v2.1.1(2018-1-8)
  * -add mute(支持静音)
  * -add enterfullmode(增加进入全屏的模式,根据视频自动确定横竖屏)
  * -support content uri(支持uri播放)
  * -fullwindow hide navigation(全屏隐藏虚拟按键)
  * -fix bug(修复bug)

## Other
  * 有问题请Add [issues](https://github.com/tohodog/QsVideoPlayer/issues)
  * 如果项目对你有帮助的话欢迎[![star][starsvg]][star]
  * logo@[mirzazulfan](https://github.com/mirzazulfan)
  
[logopng]: https://raw.githubusercontent.com/tohodog/QSVideoPlayer/master/source/logo.png

[qrpng]: https://raw.githubusercontent.com/tohodog/QSVideoPlayer/master/source/video_qrcode.png

[relesesvg]: https://img.shields.io/github/release/tohodog/QSVideoPlayer.svg
[relesezip]: https://codeload.github.com/tohodog/QSVideoPlayer/zip/2.2.7

[apkurlsvg]: https://img.shields.io/badge/download-demo.apk-brightgreen.svg?style=flat
[apkurl]: https://raw.githubusercontent.com/tohodog/QSVideoPlayer/master/source/qsvideoplayer.apk

[apisvg]: https://img.shields.io/badge/API-9+-brightgreen.svg
[api]: https://android-arsenal.com/api?level=9

[licensesvg]: https://img.shields.io/badge/License-Apache--2.0-red.svg
[license]: https://github.com/tohodog/QSVideoPlayer/blob/master/LICENSE

[starsvg]: https://img.shields.io/github/stars/tohodog/QSVideoPlayer.svg?style=social&label=Stars
[star]: https://github.com/tohodog/QSVideoPlayer
