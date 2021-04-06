# RobotSample
## 1.目录结构
├─app 业务代码
│  ├─com.ainirobot.robotos
│  │  ├─application 应用配置
|  |  |     ├─ ModuleCallback.java 获取CoreService层底层回调接口
│  │  |     ├─ RobotOSApplication.java 当前Application入口，用来连接CoreService和初始化语音
│  │  |     ├─ SpeechCallback.java 获取语音SkillApi回调接口
│  │  ├─fragment 业务场景
│  │  |     ├─ BaseFragment.java 基础业务场景配置，加载公共组件部分
│  │  |     ├─ ChargeFragment.java 充电业务模块
│  │  |     ├─ LeadFragment.java 引领业务模块
│  │  |     ├─ LocationFragment.java 定位相关模块
│  │  |     ├─ MainFragment.java 场景模块主入口
│  │  |     ├─ NavigationFragment.java 导航业务模块
│  │  |     ├─ SpeechFragment.java 语音业务模块
│  │  |     ├─ SportFragment.java 基础运动业务模块
│  │  |     ├─ VisionFragment.java 视觉业务模块
│  │  ├─ view 公共组件
│  │  |     ├─ BackView.java 返回控件
│  │  |     ├─ ResultView.java 结果显示控件
│  │  ├─ LogTools.java 日志收集工具
│  │  ├─ MainActivity.java App主页面入口
│  ├─ res 资源目录
│  ├─AndroidManifest.xml App的清单文件
|
## 2. 环境配置
机器人应用的开发需要依赖Android开发环境,IDE工具，详细配置请参考以下文档。
Android开发环境
### 2.1 Android开发环境
机器人系统是基于Android定制开发，所以我们在开发机器人应用的时候需要配置Android开发环境，具体Android开发环境配置，请参考 : https://developer.android.com/ 。
