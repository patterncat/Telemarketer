#Telemarketer

Telemarketer 是一个简单的web服务器,同时也提供了一个简单的Web框架。只是因为有个小需求而不想使用重量级的Web服务器而做。

##启动
`start [address:port]`

##编写自己的服务
继承Service接口,并用 `@ServiceClass(urlPattern = "...")` 标注。

字符串里写入对应路径的正则。会自动扫描并注册服务。