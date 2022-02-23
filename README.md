# AndroidGenshinNoteData

---

**TODO:**

1. Add HoyoLab oversea servers data source.  -> MainActivity.java
2. Fix the problem about widgetservice will be killed by system.
3. Add resin recovery calculator(to 20/40/90/next 40 or so).
4. Test reliability.
5. Retry when network fail (always in frequency request).  -> InfoDetail.java
6. Add settings. ->  layout/settings.xml

---

**Tested Device**

| Model | Android Version | status |
| --- | --- | --- |
| Xiaomi Mi 10 | Android 11 (MIUI 12.5.7) | dev device |

---

This project is an android app, which can get the Genshin in-game data(From Mihoyo bbs api or HoyoLab api) and show it on desktop widget or in-app listview. 

**Features**

* Multi-accounts support
* Multi-widgets support and sync refresh
* Can predict the resin recover time while no internet connection.