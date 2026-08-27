# CloudMusic-Mod 26.2 [Fabric]

在 Minecraft 客户端内播放网易云音乐，支持歌曲、歌单、每日推荐、私人 FM、歌词、封面、播放队列、分享和快捷键。

本仓库是 [FengLiuFeseliud/CloudMusic-Mod](https://github.com/FengLiuFeseliud/CloudMusic-Mod) 的 Minecraft 26.2 适配 fork。

![CloudMusic 图标](src/main/resources/assets/cloudmusic/icon.png)

## 运行要求

| 组件 | 版本 |
| --- | --- |
| Minecraft | 26.2 |
| Java | 25 或更高 |
| Fabric Loader | 0.19.3 或更高 |
| Fabric API | 0.158.0+26.2 |
| MaLiLib | 0.29.3 或更高 |

这是纯客户端模组，服务端无需安装。与 [CloudMusicRoom-Mod](https://github.com/FengLiuFeseliud/CloudMusicRoom-Mod) 配合时可以使用联机听歌功能。

## 安装

1. 安装 Minecraft 26.2、Fabric Loader、Fabric API 和 MaLiLib。
2. 将 `cloudmusic-mod-26.2-0.5.1.jar` 放入实例的 `mods` 文件夹。
3. 启动游戏，在聊天框输入 `/cloudmusic` 查看完整命令帮助。

## 登录

支持五种登录方式：

- 二维码：`/cloudmusic login qr`
- 手机验证码：先执行 `/cloudmusic login captcha <手机号>`，再执行 `/cloudmusic login captcha <手机号> <验证码>`
- 手机密码：`/cloudmusic login phone <手机号> <密码>`
- 邮箱密码：`/cloudmusic login email <邮箱> <密码>`
- `MUSIC_U`：`/cloudmusic login music_u <MUSIC_U>`

`MUSIC_U` 也可以在 MaLiLib 配置界面的“登录”标签中填写。命令参数既支持纯令牌，也支持 `MUSIC_U=...` 或包含 `MUSIC_U` 的完整 Cookie。

> [!CAUTION]
> `MUSIC_U` 和 Cookie 等同于账号登录凭证。不要截图、提交到仓库或发送给其他人。通过命令输入时，令牌可能保留在本地命令历史中；更推荐使用配置界面。

二维码登录会直接在游戏 HUD 中显示二维码，无需切出游戏。登录状态保存在本地配置中，重启后会自动恢复。

## 26.2 适配内容

- 迁移至 Minecraft 26.2、Java 25、Fabric Loader 0.19.3 和 Fabric API 0.158.0+26.2。
- MaLiLib 最低版本调整为 0.29.3。
- 修复异步命令反馈和封面纹理在错误线程调用渲染系统的问题。
- 修复 26.2 ARGB 字体颜色导致歌名、进度和歌词透明的问题，并兼容旧配置。
- 重写歌词时间轴，支持 `[分:秒.百分秒]`、`[分:秒:百分秒]`、1～3 位小数、多时间标签和 `offset`。
- 改进二维码登录会话与 `chainId` 处理。
- 增加独立 `MUSIC_U` 登录参数，并保留 Cookie 中的其他字段。

## 使用与配置

- `/cloudmusic`：查看命令帮助。
- `/cloudmusic my`：查看当前登录账户。
- `/cloudmusic my like`：播放“我喜欢的音乐”。
- `/cloudmusic playing`：查看当前歌曲。
- `/cloudmusic playing all`：查看播放队列。
- `/cloudmusic stop`、`/cloudmusic continue`、`/cloudmusic prev`、`/cloudmusic next`：控制播放。

通过 Mod Menu 或默认快捷键 `左 Ctrl + C + M` 打开配置界面，可以调整音量、音质、歌词位置与缩放、歌曲信息面板、缓存、登录和代理设置。

## 从源码构建

Windows：

```powershell
./gradlew.bat clean build
```

Linux/macOS：

```bash
./gradlew clean build
```

构建产物位于 `build/libs/cloudmusic-mod-26.2-0.5.1.jar`。

## 截图

![分享](img/share.png)

![登录](img/login_1.png)

![配置](img/con1.png)

## 参考与致谢

- 原项目：[FengLiuFeseliud/CloudMusic-Mod](https://github.com/FengLiuFeseliud/CloudMusic-Mod)
- API：[NeteaseCloudMusicApiEnhanced/api-enhanced](https://github.com/NeteaseCloudMusicApiEnhanced/api-enhanced)
- 原 API：[Binaryify/NeteaseCloudMusicApi](https://github.com/Binaryify/NeteaseCloudMusicApi)

项目使用 MIT 许可证，详见 [LICENSE](LICENSE)。
