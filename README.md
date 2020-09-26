# License
GPL
# YGOMobile
Android compilation environment
---------------------
    Download Android Studio and Android sdk
    
ndk compilation environment (please use Thunder, Cyclone, or vpn download)
---------------------
    Stable ndk
    https://dl.google.com/android/repository/android-ndk-r15b-windows-x86_64.zip
    https://dl.google.com/android/repository/android-ndk-r15b-darwin-x86_64.zip
    https://dl.google.com/android/repository/android-ndk-r15b-linux-x86_64.zip
    
important
---------------------------------------------
    NDKR15 compilation has been passed, newer NDK may not pass
    Download the above compressed package according to your own system, decompress and configure environment variables, cmd window, enter ndk-build --version, see a bunch of numbers and English copyright is ok
    Don't understand environment variables? It doesn't matter, you automatically put the following ndk-build before the NDK decompressed folder such as D:\NDK\ndk-build -j4

1. Prepare the data
--------------------------
    Extract from the apk published on the official website, or provide it yourself, the script folder is inside scripts.zip
    mobile\assets\data\cards.cdb
    mobile\assets\data\pics.zip
    mobile\assets\data\scripts.zip
    mobile\assets\data\fonts\ygo.ttf
    mobile\libs\libWindbot.aar
    Get the latest Libwindbot.aar:
    https://github.com/mercury233/libWindbot/releases

2. Compile ygo's so
-------------------------
    Run the command line window in the project root directory
    cd libcore
    ndk-build -j4
    Success: libcore\libs\armeabi-v7a\libYGOMobile.so
    Greater than 8M

3. Package name and signature
---------------------
    Only one app with the same package name can exist on a mobile phone at the same time, and the lower version cannot cover the higher version
    Apps with the same package name and different signatures cannot be overwritten and installed.
    (Because you don’t have the original version’s signature, if you don’t want to uninstall the original version, you need to change the package name and prepare your own signature, otherwise your app cannot overwrite the original version or cannot exist at the same time)

4. How to change the package name
----------------------------
    If you need to change the package name
    Edit: mobile\build.gradle
    applicationId "cn.garymb.ygomobile"
    Change cn.garymb.ygomobile to your package name. If you don’t understand, please add .xxxx at the end. For example, I am Cai Cai, change to cn.garymb.ygomobile.caicai

5. Modify PreferencesProvider
---------------------------
    Provider and package names are similar and will conflict, if you need to coexist with the original version, you must modify
    Rename the cn\garymb\ygomobile\ui\preference\YGOPreferencesProvider class
    Then modify the provider section in AndroidManifest.xml accordingly
    
6. Make a signature (only for the first time or you have no signature file)
--------------------------
    On the project tab on the left, select mobile, click on the top menu Build->Generate Signed Apk->Create New
    key store path: Click on the first line... to select the save location or enter manually, for example, D:\ygo.jks
    Password: Signature password, if you don’t understand, it is recommended to directly 123456
    Alias: Signature key, any name, suggest ygo directly
    Password: key password, if you don’t understand, it is recommended to directly 123456
    First and Last name: any name

7. Generate apk file
-------------------------
    If it’s your own computer, it’s best to check Remember passwords
    key store password signature password
    key alias signature key
    key password key password
    Click Next, the first line is the save folder of the apk, the following V1 and V2, if you don’t understand, please don’t tick V2, and then click Finish.
