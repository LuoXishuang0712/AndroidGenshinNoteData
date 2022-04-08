# AndroidGenshinNoteData

[简体中文](./README_ZHCN.md)

---

**TODO:**

1. Add HoyoLab oversea servers data source.  -> MainActivity.java
4. Test reliability.
6. Add settings. ->  layout/settings.xml

---

**Known issues**

1. Resin calculator is likely to display negative time while no Internet connection.
1. Some kinds of OS (or UI) will has trouble in opening WebView.

---

**Tested Device**

| Model | Android Version | status |
| --- | --- | --- |
| Xiaomi Mi 10 | Android 11 (MIUI 12.5.7) | dev device |

---

**description**

This project is an android app, which can get the Genshin in-game data(From Mihoyo bbs api or HoyoLab api) and show it on desktop widget or in-app listview.
Also, it provides an easy-to-use resin recovery time calculator.

**Features**

* Multi-accounts support
* Multi-widgets support and sync refresh
* Can predict the resin recover time while no internet connection.

---

**Reference project**

1. [Genshin-Dailynote-Helper](https://github.com/Xm798/Genshin-Dailynote-Helper) For api and its usage.
