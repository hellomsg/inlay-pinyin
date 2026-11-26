# Inlay Pinyin

适合 Android 掌机的手柄输入法，支持离线简体中文全拼。方向键选键，按键确认，也可以直接触屏输入。基于 Emre（dakingeman）的 [Inlay](https://github.com/dakingeman/inlay) v1.3.0。

**Inlay Pinyin** is a controller-first Android keyboard with offline Simplified Chinese Pinyin. It is an independent fork of [Inlay](https://github.com/dakingeman/inlay).

## 下载与安装

- [GitHub Releases](https://github.com/hellomsg/inlay-pinyin/releases) · 项目签名的发布版 APK。没有公开条目时，说明发布版尚未推出。
- [GitHub Actions](https://github.com/hellomsg/inlay-pinyin/actions/workflows/build-preview.yml) · 测试预览。登录 GitHub，在成功的运行记录中下载 APK 构建产物并解压。

需要 Android 10 或更新版本。桌面名称为 **Inlay Pinyin**，Android 包名为 `io.github.dakingeman.inlay.pinyin`。打开应用，在 Android 输入法设置中启用并选择 **Inlay Pinyin 键盘**。

在 **Typing → Languages** 将 **Language** 或 **Second language** 设为 **简体中文 · 全拼**。配置了两种语言时，按 **RT + LT** 切换。用十字键或左摇杆移动高亮，按 **A**（或自定义确认键）输入。打出 `nihao` 后，向上进入候选栏，左右选词并确认，也可以直接点候选。右摇杆或肩键移动文本光标。详见[上游手柄操作](https://github.com/dakingeman/inlay#controls)。

先装过 CI 预览包的用户，若安装项目签名版时遇到签名不一致，可先在 **About → Back up your settings** 导出设置，再卸载预览包、安装发布版，并在 **About → Restore from a backup** 恢复。Android 会独立识别原版 Inlay 的包名。

## 兼容性与反馈

Retroid Pocket Duo 已有用户试用反馈。云端 Android 14 模拟器验证了预览包的包名、应用及输入法名称，以及 `nihao`、`youxi`、`zhongwen` 的离线简体候选。设备与应用是否把手柄按键交给系统输入法，仍以实际使用为准。

请在 [Issues](https://github.com/hellomsg/inlay-pinyin/issues) 附上设备型号、Android 版本、Inlay Pinyin 版本和复现步骤。报告输入问题时，建议使用可公开的示例文字。

## 源码与许可

Inlay Pinyin 采用 [GNU GPL v3.0](LICENSE)。感谢原版作者 Emre（dakingeman）和 Inlay 贡献者。[原版说明与署名](https://github.com/dakingeman/inlay#brand-and-credits) · [第三方拼音组件许可](third_party/rime/NOTICES.txt) · [字体、词表与图形署名](licenses/)

[更新记录](CHANGELOG.md) · [云端构建与发布说明](docs/RELEASING.md) · [拼音词库与构建来源](third_party/rime/README.md)
