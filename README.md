![CI](https://github.com/Raival-e/File-Explorer/actions/workflows/android.yml/badge.svg)
[![License](https://img.shields.io/github/license/Raival-e/File-Explorer)](https://github.com/Raival-e/File-Explorer/blob/master/LICENSE)
![Commit Activity](https://img.shields.io/github/commit-activity/m/Raival-e/File-Explorer)
[![Total downloads](https://img.shields.io/github/downloads/Raival-e/File-Explorer/total)](https://github.com/Raival-e/File-Explorer/releases)
![Repository Size](https://img.shields.io/github/repo-size/Raival-e/File-Explorer)

# File Explorer

A full-featured file manager with Material 3 Dynamic colors and Java/Kotlin compiler!

# Screenshots

<div style="overflow: hidden">
<img src="/assets/screenshot1.png" width="32%" /> <img src="/assets/screenshot2.png" width="32%" /> <img src="/assets/screenshot3.png" width="32%" />
</div>

# Features

- Open source and simple.
- All basic file management functionality (e.g. copy, paste,.. etc) are supported.
- Support for multiple tabs, and Tasks which make managing files much easier.
- Powerful Code Editor ([Sora Editor](https://github.com/Rosemoe/sora-editor)).
- Deep search that allows you to search in files contents.
- Additional features:
  - Java/Kotlin compiler: you can run java/kotlin projects. see the compiler details bellow.

# Compiler

To use this feature, all you need to do is to create Main.java/.kt file (it is recommended to create
it in an empty folder), then open it, a dialog will show asking you if you want to use a sample
code, click Yes. Now you can run you code by clicking run button in the toolbar. The main method
gives you the context and the folder where the Main.java/.kt is located.

You can add additional java files to the same directory, and if you want to use a local library, 
create a folder and name it `libs`, then put all libraries there (note that **only** `.dex` and `.jar` are allowed.
If the library comes with jar file only, long click onthe jar file and use `Jar2Dex` option to create a dex file.
You **must** have both `.dex` and `.jar` files of the library in `/libs` folder to allow the app compiling and running it properly).

After running the code, an output folder will be created. In this folder, you can see the output `.extension` file(s),
you can run these files directly without the need to compile your code again by clicking on `classes.extension`.

Using this features, the possibilities are endless!, and you can create any extension you want and use it as part of the app!.

# Download

[<img alt="Get it on IzzyOnDroid" height="80" src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png">](https://apt.izzysoft.de/fdroid/index/apk/com.raival.fileexplorer)

- Latest release from [here](https://github.com/Raival-e/File-Explorer/releases/tag/v1.0.0).
- Latest debug build from [Github Actions](https://github.com/Raival-e/File-Explorer/actions).

# Special thanks

- [Mike Anderson](https://github.com/MikeAndrson).

- [Tyron](https://github.com/tyron12233).

- [Sketchware Pro](https://github.com/Sketchware-Pro/Sketchware-Pro).

- [PranavPurwar](https://github.com/PranavPurwar)
