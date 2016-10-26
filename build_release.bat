set versionnumber=4
copy extension.xml build\release
copy plugin.xml build\release\project-argon
del build\release\project-argon\libs\argon*.jar
xcopy build\libs build\release\project-argon\libs\ /Y/S/I
xcopy build\resources\main build\release\project-argon\resources /Y/S/I
cd build\release
jar cvf Argon-0.0.%versionnumber%.jar project-argon
cd ..\..