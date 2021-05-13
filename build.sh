#!/bin/bash

export ANDBIN=/usr/lib/android-sdk/build-tools/debian/
export ANDJAR=`find /usr/lib/android-sdk/platforms -name "*.jar"`

echo "android platform: "$ANDJAR

# ========================== INIT

if [ -e maclef.key ]; then
  echo "clef déjà générée"
else
  echo "génération de la clef..."
  keytool -genkey -noprompt -keyalg RSA -sigalg SHA256withRSA -keysize 2048 -storepass tototo -keypass tototo -keystore maclef.key -alias toto -dname "CN=example.com, OU=ID, O=example, L=Toto, S=tutu, C=FR" -validity 10000
  # echo "génération icône..."
  # mkdir res/drawable-mdpi res/drawable-hdpi res/drawable-xhdpi res/drawable-xxhdpi 
  # convert -size 124x124  plasma:fractal -blur 0x2  -swirl 180  -shave 10x10  res/drawable-mdpi/dlicon.png
  # cp res/drawable-mdpi/dlicon.png res/drawable-hdpi/dlicon.png
  # cp res/drawable-mdpi/dlicon.png res/drawable-xhdpi/dlicon.png
  # cp res/drawable-mdpi/dlicon.png res/drawable-xxhdpi/dlicon.png
fi

# ========================== BUILD

rm -rf gen out
mkdir gen out

$ANDBIN/aapt package -f \
    -M AndroidManifest.xml \
    -I $ANDJAR \
    -S res/ \
    -J gen/ \
    -m

# version avec jack/jill
#libs=$(ls libs/*.jar)
#lidx=0
#imports=""
#clp=""
#for lib in $libs; do
#    java -jar $ANDBIN/jill.jar --output libs/xx$lidx --verbose $lib
#    imports=$imports" --import libs/xx"$lidx
#    clp=$clp":"$lib
#    lidx=$(($lidx+1))
#done

GENFILES=$(find gen -name "*.java" | awk '{a=a" "$1}END{print a}')
SRCFILES=$(find src -name "*.java" | awk '{a=a" "$1}END{print a}')
LIBS=$(ls libs/*.jar | awk '{a=a":"$1}END{print a}')

mkdir out
javac -bootclasspath $ANDJAR -source 1.7 -target 1.7 -cp "$ANDJAR""$LIBS" -d out $SRCFILES $GENFILES

JARS=$(ls $PWD/libs/*.jar | awk '{a=a" "$1}END{print a}')
echo $JARS
cd out
$ANDBIN/dx --dex --output classes.dex $JARS .
cd ..

# java -jar $ANDBIN/jack.jar --classpath "$ANDJAR"$clp --output-dex out $imports src/ gen/

$ANDBIN/aapt package -f -M AndroidManifest.xml -I $ANDJAR -S res/ -F out/app.apk

find assets -type f -exec $ANDBIN/aapt add -v out/app.apk {} \;
cd out
$ANDBIN/aapt add app.apk classes.dex

apksigner sign --ks ../maclef.key --ks-key-alias toto --ks-pass pass:tototo -key-pass pass:tototo app.apk
# jarsigner -verbose -keystore ../maclef.key -storepass tototo -keypass tototo app.apk toto


