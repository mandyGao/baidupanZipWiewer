baidupanZipWiewer
=================

online explore pics inside of zips in baidupan

# what is it?

it's a online comic viewer which play trick with baidupan online unzip api, so it can view comic or other picture zip files stored in baidupan

# how to use?

it have to be working with http://ddddddd.jd-app.com/comic/howto as the server side and a web application.

* follow the introduction from the above link, add zip files and confirm it work.
* start this app , as this app need cookies to request apis, the app now need a session.properties file to work .
* edit a file,it content should be link this:

        api=replace me with cookie from  http://ddddddd.jd-app.com/comic/
        baidu=replace me with cookie from http://pan.baidu.com/disk/home(need login)
    
* the name must be "session.properties", push it in `/sdcard/Android/data/com.jdapp.ddddddd/files/`

# features

* picture zoom&drag
* Cache
* password protect
* qrcode scan sysnc session from web

# Thanks:

 - [JakeWharton / DiskLruCache](https://github.com/JakeWharton/DiskLruCache "JakeWharton / DiskLruCache")

 - [zxing](https://github.com/zxing/zxing "zxing")

 - ImageZoomView

 - [android-async-http](https://github.com/loopj/android-async-http "android-async-http")



