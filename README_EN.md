## Protobuf Support for JetBrains IDEs

> FORK FROM https://github.com/protostuff/protobuf-jetbrains-plugin

### ENHANCEMENT

- support generate java file from proto file
- support base code completion for proto2 grammar

### STEP

> 1. Enter setting page, click `PROTO FOLDER` and select proto files folder location.
> 2. Click `JAVA FILE` and select the folder for java file generating.
> 3. Click `PROTOC` and select protoc (or protoc.exe on windows) file location.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/proto%20setting.png)

> Right click on proto file you need to generate and select `Generator Protobuf` option.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/right%20click.png)

> No error.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/ok%20hint.png)

> Error.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/error%20hint.png)

> Base code completion.

![image](https://raw.githubusercontent.com/wiki/LeasonX/protobuf-jetbrains-plugin/code%20completion.png)
