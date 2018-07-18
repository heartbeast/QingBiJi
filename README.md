轻笔记大改说明

网络框架不适用，需要修改

1forResult的RequestID都需要修改，bug

2.新框架使用说明：
新增包1：_constructer，具体的mvp的操作，
新增包2：_interface,mvp的回调
新增包3：bean,各种接口返回数据的封装
新增包4：http，里头是Okhttp3+retrofit2+Rxjava的封装，最新网络框架

新框架MVP的使用说明：
每一个界面（eg：TNMainAct），都有对应的m v p的类，_interface包封装的是main的抽象接口（调用传递-->p,调用接口-->m，接口回调-->v），用于具体实现
在_constructer包和Activity包（act是V层的具体实现）中,具体显示更新ui在v的Activity中实现，具体接口http的封装使用在m的module类中。

3.旧框架说明：
不用包1：Action
不用包2：NetWork
不用包3:OAuth2 网络框架太老，不可用
4.若完全放弃就框架，要删除的jar包：
org.apache.http.legacy.jar

5 Uri.fromFile(new File(temp))获取Uri的样式在android7.0+后，改成FileProvider.getUriForFile()样式，具体参考本app

6数据库比较乱，本次未修改，但是需要优化成一个数据库---sjy 0713

7.百度地图开元账号：行客记事 Qunbiji2015 (目前没有定位正确)

8.android4.2以后，任何为JS暴露的接口，都需要加@JavascriptInterface