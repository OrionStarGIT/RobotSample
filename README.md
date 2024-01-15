# RobotSample

## Scroll to view instructions in English

## 1. 目录结构


├─app 业务代码  
│  ├─robotos  
│  │  ├─application 应用配置  
│  │  │	    ├─ ModuleCallback.java 获取CoreService层底层回调接口  
│  │  │     ├─ RobotOSApplication.java 当前Application入口，用来连接CoreService和初始化语音  
│  │  │     ├─ SpeechCallback.java 获取语音SkillApi回调接口  
│  │  ├─fragment 业务场景  
│  │  │     ├─ BaseFragment.java 基础业务场景配置，加载公共组件部分  
│  │  │     ├─ ChargeFragment.java 充电业务模块  
│  │  │     ├─ LeadFragment.java 引领业务模块  
│  │  │     ├─ LocationFragment.java 定位相关模块  
│  │  │     ├─ MainFragment.java 场景模块主入口  
│  │  │     ├─ NavigationFragment.java 导航业务模块  
│  │  │     ├─ SpeechFragment.java 语音业务模块  
│  │  │     ├─ SportFragment.java 基础运动业务模块  
│  │  │     ├─ VisionFragment.java 视觉业务模块
│  │  │     ├─ ElectricDoorControlFragment.java 电动门业务模块
│  │  ├─ view 公共组件  
│  │  │     ├─ BackView.java 返回控件  
│  │  │     ├─ ResultView.java 结果显示控件  
│  │  ├─ LogTools.java 日志收集工具  
│  │  ├─ MainActivity.java App主页面入口  
│  ├─ res 资源目录  
│  ├─AndroidManifest.xml App的清单文件  


## 2. 环境配置

机器人应用的开发需要依赖Android开发环境、IDE工具。详细配置请参考以下文档。
若要直接编译本示例代码运行，需要JDK1.8（Java 8），请手动从机器人屏幕点击启动本App而不是从IDE上的debug按钮，以免无法获得api权限。

### 2.1 Android开发环境

机器人系统是基于Android定制开发，所以我们在开发机器人应用的时候需要配置Android开发环境。具体Android开发环境配置，请参考：[Android开发者文档](https://developer.android.com/)。


---

# English

## 1. Directory Structure

├─app Business code  
│  ├─robotos  
│  │  ├─application Application configuration  
│  │  │	    ├─ ModuleCallback.java Get the underlying callback interface of the CoreService layer  
│  │  │     ├─ RobotOSApplication.java Current Application entry point, used to connect CoreService and initialize voice  
│  │  │     ├─ SpeechCallback.java Get the callback interface of the voice SkillApi  
│  │  ├─fragment Business scenes  
│  │  │     ├─ BaseFragment.java Basic business scene configuration, loading common components  
│  │  │     ├─ ChargeFragment.java Charging business module  
│  │  │     ├─ LeadFragment.java Leading business module  
│  │  │     ├─ LocationFragment.java Location-related module  
│  │  │     ├─ MainFragment.java Scene module main entry  
│  │  │     ├─ NavigationFragment.java Navigation business module  
│  │  │     ├─ SpeechFragment.java Voice business module  
│  │  │     ├─ SportFragment.java Basic sports business module  
│  │  │     ├─ VisionFragment.java Vision business module  
│  │  │     ├─ ElectricDoorControlFragment.java Electric door service module
│  │  ├─ view Common components  
│  │  │     ├─ BackView.java Back control  
│  │  │     ├─ ResultView.java Result display control  
│  │  ├─ LogTools.java Log collection tool  
│  │  ├─ MainActivity.java App main page entry  
│  ├─ res Resource directory  
│  ├─AndroidManifest.xml Manifest file of the App  

## 2. Environment Configuration

Robot application development requires the Android development environment and IDE tools. For detailed configuration, please refer to the following documents.
To compile and run this sample code directly, you will need JDK 1.8 (Java 8). Please manually click on the app launch from the robot's screen instead of using the debug button in the IDE to ensure that you obtain the necessary API permissions.

### 2.1 Android Development Environment

The robot system is based on customized development on Android, so when developing robot applications, we need to configure the Android development environment. For specific Android development environment configuration, please refer to: [Android Developer Documentation](https://developer.android.com/).

