轻笔记大改说明

网络框架不适用，需要修改

1forResult的RequestID都需要修改，bug

2.新框架使用说明：
新增包1：_constructer，具体的mvp的操作，
新增包2：_interface,mvp的回调
新增包3：bean,各种接口返回数据的封装
新增包4：http，里头是Okhttp3+retrofit2+Rxjava的封装，最新网络框架

3.旧框架说明：
不用包1：Action
不用包2：NetWork
不用包3:OAuth2 网络框架太老，不可用



