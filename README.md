QSVideoPlayer
====
  * QSVideoView接口完善,事件的监听,设置视频比例,切换全屏等
  * 提供QSVideoViewHelp辅助类,该类提供了常用控件的逻辑和手势调节支持,可快速自定义ui打造自己的播放器,不用写一行播放逻辑
  * 架构设计优良,模块化可扩展设计,解码模块目前提供了 AndroidMedia(系统自带)、ijkMedia(基于ffmepg)+ijkExoMedia(基于exo)、ExoMedia(2.0.4)解码器
  * 根据系统版本自动选择SurfaceView和TextureView,支持api9+
  * 支持本地视频,在线视频,m3u8直播等...



## 使用说明
下载项目 添加qsvideoplayer文件夹为自己的项目依赖即可 [ **[下载apk]** ](https://raw.githubusercontent.com/tohodog/QSVideoPlayer/master/app-debug-2.0.apk)


源码有4个解码器,根据需求自行选择需要的解码器:<br/>
1.一般简单播放视频AndroidMedia足够(体积最小,无依赖)<br/>
2.有点需求可选择AndroidMedia+ExoMedia(1MB)<br/>
3.需求高的可选AndroidMedia+(ijkMedia+ijkExoMedia)(2MB(单v7a包))<br/>
目前测试解码效果ijkMedia兼容性最好,AndroidMedia个别视频有半途中断BUG,exo无明显缺陷<br/>
ps:<br/>删除ijk解码器: build.gradle注释掉所有依赖,media包里删除IjkBaseMedia IjkExoMedia IjkMedia三个类即可<br/>
删除exo解码器: libs里删除jar,media包里删除ExoMedia即可<br/>

## diy播放器:
0.read source code.<br/>
1.可直接修改DemoQSVideoView改造自己的播放器<br/>
2.继承QSVideoViewHelp参考DemoQSVideoView,源码均有注释,只需子类提供layout布局以及设置各个状态的ui即可完成自己的播放器,播放逻辑一行不用写<br/>
3.直接使用QSVideoView,自己写控制ui和逻辑<br/>
(PS:继承关系:DemoQSVideoView → QSVideoViewHelp → QSVideoView)


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

qsVideoView.enterFullMode=1;//进入全屏的模式 0默认是横屏 1是竖屏,随传感器自动切换横屏

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

![输入图片说明](http://git.oschina.net/uploads/images/2017/0409/201818_d6e50594_530535.jpeg "在这里输入图片标题")
![输入图片说明](http://git.oschina.net/uploads/images/2017/0224/180438_84c8332c_530535.jpeg "在这里输入图片标题")
