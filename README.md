## Protobuf Support for JetBrains IDEs

> English Version: 

> FORK FROM https://github.com/protostuff/protobuf-jetbrains-plugin

### 增强功能(proto2)

- 支持.proto文件生成.java文件
- 支持proto语法基本的代码提示

### STEP

> 1. 进入设置页面, 点击 `PROTO FOLDER` 选择proto所在的目录
    (如果是windows,且目录下有protoc.exe会自动补全protoc路径).
    （如果protobuf目录位于resource目录下,会尝试自动匹配java src目录）
> 2. 点击 `JAVA FILE` 设置java src文件夹来存放生成的java文件.
> 3. 点击 `PROTOC` 设置 protoc (windows上是protoc.exe)文件位置.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/proto%20setting.png)

> 右击你需要生成的proto文件(或在proto文件编辑器内)选择`Generator Protobuf`来生成对应的java文件.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/right%20click.png)

> 成功.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/ok%20hint.png)

> 错误信息.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/error%20hint.png)

> 基本的代码补全
>
>message需要提示来自其他proto文件的enum或message时, 需要先写全import;
>
>import后先补全双引号, 再写文件名会自动提示proto文件, 否则不会

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/code%20completion.png)
