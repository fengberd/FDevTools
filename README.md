# FDevTools
The first devtools plugin for Nukkit!

# Download
You can get compiled jar file from [ZXDA's Jenkins](https://jenkins.zxda.net/view/Nukkit/job/FDevTools/)

# Feathres
* You can load plugin in source(*.java).
* If you don't have JDK,just download tools.jar from [Here](https://www.dropbox.com/s/vjvcebljpk6qlmj/tools.jar?dl=0) and put it into FDevTools folder then it works well too.
* You can pack the plugin into jar and relese it easyily.

## Attention
Your source plugin should follow this format:

1. Make a dir in plugins folder(e.g. MyPlugin_src).
2. Put your plugins yml into it.
3. Make a "src" dir into it.
4. Put your source into "src" dir.

Now your plugin should looks like this:
>MyPlugin_src
├plugin.yml
└src
&nbsp;&nbsp;&nbsp;└moe/berd/Test
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└Main.java

# Commands
* `makeplugin <PluginName>` - Compress the plugin into jar format(It must loaded by FDevTools).
