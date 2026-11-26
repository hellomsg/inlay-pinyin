# 云端构建与发布

## 分支和预览

`main` 是项目默认分支。通过 Pull Request 合入文档、构建和发布变更。上游仓库为 `dakingeman/inlay`，同步上游时保留 Inlay Pinyin 的独立版本和包名。旧分支 `codex/inlay-cloud-build` 保留此前的预览构建记录。

[预览工作流](https://github.com/hellomsg/inlay-pinyin/actions/workflows/build-preview.yml) 使用仓库内的 Gradle Wrapper、JDK 17、Android SDK 37、NDK 27.2.12479018 和 CMake 3.22.1。在拉取请求、默认分支推送或手动触发时构建并运行模拟器测试。整理分支 `codex/project-setup` 的推送同样触发验证。预览包使用调试签名。

## 第一次配置签名

为项目准备专用 Android 签名密钥和离线备份。适用于长期分发的 APK 应始终使用同一密钥签名。通过 GitHub 的 **Settings → Secrets and variables → Actions** 为仓库配置以下 secrets：

| Secret | 内容 |
| --- | --- |
| `ANDROID_RELEASE_KEYSTORE_BASE64` | 发布用 keystore 的 Base64 内容 |
| `ANDROID_RELEASE_KEYSTORE_PASSWORD` | keystore 密码 |
| `ANDROID_RELEASE_KEY_ALIAS` | 发布密钥别名 |
| `ANDROID_RELEASE_KEY_PASSWORD` | 发布密钥密码 |

密钥文件和密码由项目维护者保管。Base64 是编码，keystore 原件和编码结果都须保密。[Android 应用签名](https://developer.android.com/studio/publish/app-signing.html)

## 创建发布草稿

1. 在 `main` 上核对 `app/build.gradle.kts` 的 `versionName`、递增的 `versionCode` 和 `CHANGELOG.md`。首版对应标签 `v0.1.0`，版本号 `0.1.0`，`versionCode` 为 `7`。
2. 等待同一提交的预览工作流通过。打开 [Draft Inlay Pinyin Release](https://github.com/hellomsg/inlay-pinyin/actions/workflows/draft-release.yml)，选 `main` 并填写匹配的标签。
3. 工作流生成项目签名的 APK，验证包名、版本和签名，并创建 GitHub Release 草稿。草稿附 APK、SHA-256、签名证书指纹、GPL 许可证和对应源码提交地址。
4. 在目标设备验证安装、启用输入法、手柄与触屏选词。复核发布说明、验证范围和预发行标识，再发布草稿。发布下一个版本时沿用同一包名和签名。

## 构建来源

APK 对应源码和构建脚本由 Release 说明链接到同一提交。`python3 tools/prepare_rime.py` 从固定版本和哈希准备 Rime 数据与原生依赖。复现构建时使用 Python 3.12 或更新版本，再运行 `bash ./gradlew :app:assembleRelease`。第三方源码和词库的来源见 [Rime 依赖说明](../third_party/rime/README.md)。
